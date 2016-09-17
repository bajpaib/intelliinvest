package com.intelliinvest.data.forecast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.CommonConstParams.ForecastType;
import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.ForecastedStockPriceRepository;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.ForecastedStockPrice;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.util.ScheduledThreadPoolHelper;

/**
 * Daily Forecast 
 *
 * Load data for daily.close.price.forecast.history.years/months
 *
 * TRAINING: INPUT: select date, open, high,low,last,close and totTrdQty
 * for Day1. OUTPUT: close as output for Day2
 *
 * Continue above process by choosing Day 2 as input and Day 3 as output
 * Continue above process by choosing Day 3 as input and Day 4 as output 
 * ....
 * .... 
 * Last set of input and output used for training INPUT: select date, open,
 * high,low,last,close and totTrdQty for (T-1) OUTPUT: today's (T) closing price
 *
 * PREDICTION: INPUT: select date, open, high,low,last,close and
 * totTrdQty for T OUTPUT: tomorrow's (T+1) closing price
 *
 */

@ManagedResource(objectName = "bean:name=DailyClosePriceForecaster", description = "DailyClosePriceForecaster")
public class DailyClosePriceForecaster {
	private static Logger logger = Logger.getLogger(DailyClosePriceForecaster.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private ForecastedStockPriceRepository forecastedStockPriceRepository;
	@Autowired
	private DateUtil dateUtil;

	private ExecutorService executorService = null;
	private static final String LEARNING_DATA_FILE_NAME = "dailyLearningData";
	private static final String NEURAL_NETWORK_MODEL_FILE_NAME = "dailyStockPredictor";
	private static final int NUM_INPUTS = 7;
	private String learningDataFileDir;
	private int maxIterations;
	private double learningRate;
	private double maxError;

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
		learningDataFileDir = IntelliInvestStore.properties.getProperty("daily.close.price.forecast.data.dir");
		maxIterations = new Integer(
				IntelliInvestStore.properties.getProperty("daily.close.price.forecast.max.iterations", "10000"));
		learningRate = new Double(
				IntelliInvestStore.properties.getProperty("daily.close.price.forecast.learning.rate", "0.5"));
		maxError = new Double(
				IntelliInvestStore.properties.getProperty("daily.close.price.forecast.max.error", "0.00001"));
	}

	private void initializeScheduledTasks() {
		Runnable dailyClosePricePredictorTask = new Runnable() {
			public void run() {
				if (!dateUtil.isBankHoliday(dateUtil.getLocalDate())) {
					try {
						// We need to forecast tomorrow's close using values for
						// today's close
						forecastTomorrowClose(dateUtil.getLocalDate());
					} catch (Exception e) {
						logger.error("Error while running dailyClosePricePredictorTask " + e.getMessage());
					}
				}
			}
		};
		LocalDateTime zonedNow = dateUtil.getLocalDateTime();
		int dailyClosePricePredictStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("daily.close.price.forecast.start.hr"));
		int dailyClosePricePredictStartMin = new Integer(
				IntelliInvestStore.properties.getProperty("daily.close.price.forecast.start.min"));
		LocalDateTime zonedNext = zonedNow.withHour(dailyClosePricePredictStartHour)
				.withMinute(dailyClosePricePredictStartMin).withSecond(0);
		if (zonedNow.compareTo(zonedNext) > 0) {
			zonedNext = zonedNext.plusDays(1);
		}
		Duration duration = Duration.between(zonedNow, zonedNext);
		long initialDelay = duration.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(dailyClosePricePredictorTask,
				initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);

		logger.info("Scheduled dailyClosePricePredictorTask for tomorrow close price forecast");
	}

	private void forecastTomorrowClose(LocalDate today) throws Exception {
		createExcutorService();
		List<Stock> stockDetails = stockRepository.getStocks();
		List<Stock> nonWorldStocks = new ArrayList<Stock>();
		for (Stock stock : stockDetails) {
			if (!stock.isWorldStock()) {
				nonWorldStocks.add(stock);
			}
		}
		Map<String, Future<Double>> futuresMap = new HashMap<String, Future<Double>>();
		// for each stock
		for (Stock stock : nonWorldStocks) {
			ForecastCloseTask forecastCloseTask = new ForecastCloseTask(stock, today);
			Future<Double> future = executorService.submit(forecastCloseTask);
			futuresMap.put(stock.getSecurityId(), future);
		}
		List<ForecastedStockPrice> forecastPrices = new ArrayList<ForecastedStockPrice>();
		for (Map.Entry<String, Future<Double>> entry : futuresMap.entrySet()) {
			String code = null;
			try {
				code = entry.getKey();
				Future<Double> future = entry.getValue();
				Double result = future.get();
				// We need to update STOCK_PRICE_FORECAST table
				LocalDate nextBusinessDate = dateUtil.addBusinessDays(today, 1);
				forecastPrices
						.add(new ForecastedStockPrice(code, result, 0d, 0d, today, nextBusinessDate, null, null, null));
				logger.debug("Stock:" + code + ". ForecastPrice:" + result.doubleValue() + ". ForecastDate:" + nextBusinessDate);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("InterruptedException in forecastCloseTask for stock:" + code + ". Exception:"
						+ e.getMessage());
			} catch (ExecutionException e) {
				logger.error(
						"ExecutionException in forecastCloseTask for stock:" + code + ". Exception:" + e.getMessage());
			}
		}
		forecastedStockPriceRepository.updateForecastStockPrices(forecastPrices, ForecastType.DAILY);
		shutdownExecutorService();
	}

	class ForecastCloseTask implements Callable<Double> {
		private Stock stock;
		private LocalDate today;

		public ForecastCloseTask(Stock stock, LocalDate today) {
			super();
			this.stock = stock;
			this.today = today;
		}

		@Override
		public Double call() throws Exception {
			// We need to forecast tomorrow's close using value for today's close
			QuandlStockPrice price = quandlEODStockPriceRepository.getStockPriceFromDB(stock.getSecurityId(), today);
			if (price == null) {
				throw new Exception("Can't forecast tomorrow close. Today's closing price is not available for stock:"
						+ stock.getSecurityId() + " and date:" + today.toString());
			}
			if (!validateEODPrice(price)) {
				throw new Exception("Can't forecast tomorrow close. Invalid today's closing price:" + price.toString());
			}
			// fetch data from DB
			ArrayList<QuandlStockPrice> stockPrices = fetchDataFromDB(stock.getSecurityId(), today);
			// prepare training data
			double[] minMaxData = prepareTrainingData(stock.getSecurityId(), stockPrices);
			// train network
			trainNetwork(stock.getSecurityId());
			// forecast close from network
			return forecastTomorrowClose(price, minMaxData[0], minMaxData[1], minMaxData[2], minMaxData[3],
					minMaxData[4], minMaxData[5]);
		}
	}

	private ArrayList<QuandlStockPrice> validateEODPrices(List<QuandlStockPrice> prices) {
		ArrayList<QuandlStockPrice> retVal = new ArrayList<QuandlStockPrice>();
		for (QuandlStockPrice temp : prices) {
			if (validateEODPrice(temp)) {
				retVal.add(temp);
			}
		}
		return retVal;
	}

	private boolean validateEODPrice(QuandlStockPrice price) {
		if (price.getEodDate() == null) {
			return false;
		}
		if (MathUtil.isNearZero(price.getTradedQty())) {
			return false;
		}
		if (MathUtil.isNearZero(price.getOpen()) || MathUtil.isNearZero(price.getHigh())
				|| MathUtil.isNearZero(price.getLow()) || MathUtil.isNearZero(price.getLast())
				|| MathUtil.isNearZero(price.getClose())) {
			return false;
		}
		return true;
	}

	private ArrayList<QuandlStockPrice> fetchDataFromDB(String stockCode, LocalDate today) {
//		logger.debug("Inside fetchDataFromDB() for stock:" + stockCode + " and date:" + today.toString());
		int years = new Integer(IntelliInvestStore.properties.getProperty("daily.close.price.forecast.history.years"))
				.intValue();
		int months = new Integer(IntelliInvestStore.properties.getProperty("daily.close.price.forecast.history.months"))
				.intValue();
		LocalDate startDate = today.minusYears(years).minusMonths(months);
		List<QuandlStockPrice> stockPricesFromDB = quandlEODStockPriceRepository.getStockPricesFromDB(stockCode,
				startDate, today);
		stockPricesFromDB.sort(new Comparator<QuandlStockPrice>() {
			public int compare(QuandlStockPrice price1, QuandlStockPrice price2) {
				return price1.getEodDate().compareTo(price2.getEodDate());

			}
		});
		return validateEODPrices(stockPricesFromDB);
	}

	private double[] prepareTrainingData(String stockCode, ArrayList<QuandlStockPrice> stockPrices) throws Exception {
//		logger.debug("Inside prepareTrainingData() for stock:" + stockCode);
		List<TrainingData> trainingData = new ArrayList<TrainingData>();
		for (int i = 0; i < stockPrices.size() - 1; ++i) {
			// Fetch first and second record
			QuandlStockPrice price1 = stockPrices.get(i);
			QuandlStockPrice price2 = stockPrices.get(i + 1);
			trainingData.add(new TrainingData(dateUtil.convertToJulian(price1.getEodDate()), price1.getOpen(),
					price1.getHigh(), price1.getLow(), price1.getLast(), price1.getClose(), price1.getTradedQty(),
					price2.getClose()));
		}
		// Find the minimum and maximum values for price - needed for
		// normalization
		double maxPrice = 0;
		double minPrice = Double.MAX_VALUE;
		// Find the minimum and maximum values for eod date - needed for
		// normalization
		double maxDate = 0d;
		double minDate = Double.MAX_VALUE;
		// Find the minimum and maximum values for tradeVol - needed for
		// normalization
		int maxTrdVol = 0;
		int minTrdVol = Integer.MAX_VALUE;

		for (int i = 0; i < trainingData.size(); ++i) {
			// find max/min price
			double tempMaxPrice = getMaxPrice(trainingData.get(i));
			double tempMinPrice = getMinPrice(trainingData.get(i));
			if (tempMaxPrice > maxPrice) {
				maxPrice = tempMaxPrice;
			}
			if (tempMinPrice < minPrice) {
				minPrice = tempMinPrice;
			}
			// find max/min date
			double tempDate = trainingData.get(i).getDate();
			if (tempDate > maxDate) {
				maxDate = tempDate;
			}
			if (tempDate < minDate) {
				minDate = tempDate;
			}
			// find max/min totTrdQty
			int tempTradeVol = trainingData.get(i).getTottrdqty();
			if (tempTradeVol > maxTrdVol) {
				maxTrdVol = tempTradeVol;
			}
			if (tempTradeVol < minTrdVol) {
				minTrdVol = tempTradeVol;
			}
		}

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String date = dateFormat.format(dateUtil.getLocalDate());
		
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(learningDataFileDir + "/" + stockCode + "_" + LEARNING_DATA_FILE_NAME + date + ".csv"));
		try {
			LinkedList<Double> valuesQueue = new LinkedList<Double>();
			for (TrainingData data : trainingData) {
				valuesQueue.add(normalizeValue(data.getDate(), maxDate, minDate));
				valuesQueue.add(normalizeValue(data.getOpen(), maxPrice, minPrice));
				valuesQueue.add(normalizeValue(data.getHigh(), maxPrice, minPrice));
				valuesQueue.add(normalizeValue(data.getLow(), maxPrice, minPrice));
				valuesQueue.add(normalizeValue(data.getLast(), maxPrice, minPrice));
				valuesQueue.add(normalizeValue(data.getClose(), maxPrice, minPrice));
				valuesQueue.add(normalizeValue(data.getTottrdqty(), maxTrdVol, minTrdVol));
				valuesQueue.add(normalizeValue(data.getOutputClose(), maxPrice, minPrice));
				String valueLine = valuesQueue.toString().replaceAll("\\[|\\]", "");
				writer.write(valueLine);
				writer.newLine();
				valuesQueue.clear();
			}
		} finally {
			writer.flush();
			writer.close();
		}
		return new double[] { maxPrice, minPrice, maxDate, minDate, maxTrdVol, minTrdVol };
	}

	// train network
	private void trainNetwork(String stockCode) throws IOException {
//		logger.debug("Inside trainNetwork() for stock:" + stockCode);
		NeuralNetwork<BackPropagation> neuralNetwork = new MultiLayerPerceptron(TransferFunctionType.SIGMOID,
				NUM_INPUTS, 2 * NUM_INPUTS + 1, 1);
		SupervisedLearning learningRule = neuralNetwork.getLearningRule();
		learningRule.setMaxError(maxError);
		learningRule.setLearningRate(learningRate);
		learningRule.setMaxIterations(maxIterations);
		learningRule.addListener(new LearningEventListener() {
			public void handleLearningEvent(LearningEvent learningEvent) {
				SupervisedLearning rule = (SupervisedLearning) learningEvent.getSource();
//				logger.debug("Network error for interation " + rule.getCurrentIteration() + ": " + rule.getTotalNetworkError());
			}
		});
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String date = dateFormat.format(dateUtil.getLocalDate());
		DataSet trainingSet = loadTraininigData(learningDataFileDir + "/" + stockCode + "_" + LEARNING_DATA_FILE_NAME+ date + ".csv");
		neuralNetwork.learn(trainingSet);
		
		neuralNetwork.save(learningDataFileDir + "/" + stockCode + "_" + NEURAL_NETWORK_MODEL_FILE_NAME + date + ".nnet");
	}

	private DataSet loadTraininigData(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		DataSet trainingSet = new DataSet(NUM_INPUTS, 1);
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(",");
				double trainValues[] = new double[NUM_INPUTS];
				for (int i = 0; i < NUM_INPUTS; i++) {
					trainValues[i] = Double.valueOf(tokens[i]);
				}
				double expectedValue[] = new double[] { Double.valueOf(tokens[NUM_INPUTS]) };
				trainingSet.addRow(new DataSetRow(trainValues, expectedValue));
			}
		} finally {
			reader.close();
		}
		return trainingSet;
	}

	// forecast T+1 close from network
	private double forecastTomorrowClose(QuandlStockPrice price, double maxPrice, double minPrice, double maxDate,
			double minDate, double maxTrdVol, double minTrdVol) {
		double retVal = 0;
//		logger.debug("Inside forecastTomorrowClose() for stock:" + price.getSecurityId());
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String date = dateFormat.format(dateUtil.getLocalDate());
		NeuralNetwork neuralNetwork = NeuralNetwork
				.createFromFile(learningDataFileDir + "/" + price.getSecurityId() + "_" + NEURAL_NETWORK_MODEL_FILE_NAME + date + ".nnet");
		neuralNetwork.setInput(normalizeValue(dateUtil.convertToJulian(price.getEodDate()), maxDate, minDate),
				normalizeValue(price.getOpen(), maxPrice, minPrice),
				normalizeValue(price.getHigh(), maxPrice, minPrice), normalizeValue(price.getLow(), maxPrice, minPrice),
				normalizeValue(price.getLast(), maxPrice, minPrice),
				normalizeValue(price.getClose(), maxPrice, minPrice),
				normalizeValue(price.getTradedQty(), maxTrdVol, minTrdVol));
		neuralNetwork.calculate();
		double[] networkOutput = neuralNetwork.getOutput();
		retVal = deNormalizeValue(networkOutput[0], maxPrice, minPrice);
		return retVal;
	}

	private double getMaxPrice(TrainingData data) {
		double[] temp = { data.getClose(), data.getHigh(), data.getLast(), data.getLow(), data.getOpen() };
		double max = 0;
		for (int i = 0; i < temp.length; i++) {
			if (temp[i] > max) {
				max = temp[i];
			}
		}
		return max;
	}

	private double getMinPrice(TrainingData data) {
		double[] temp = { data.getClose(), data.getHigh(), data.getLast(), data.getLow(), data.getOpen() };
		double min = Double.MAX_VALUE;
		for (int i = 0; i < temp.length; i++) {
			if (temp[i] < min) {
				min = temp[i];
			}
		}
		return min;
	}

	double normalizeValue(double input, double max, double min) {
		return (input - min) / (max - min) * 0.8 + 0.1;
	}

	double deNormalizeValue(double input, double max, double min) {
		return min + (input - 0.1) * (max - min) / 0.8;
	}

	private void createExcutorService() {
		int count = new Integer(
				IntelliInvestStore.properties.getProperty("daily.close.price.forecast.thread.pool.count")).intValue();
		executorService = Executors.newFixedThreadPool(count);
	}

	private void shutdownExecutorService() {
		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@ManagedOperation(description = "forecastAndUpdateTomorrowClose")
	@ManagedOperationParameters({
			@ManagedOperationParameter(name = "Today's Date (yyyy-MM-dd)", description = "Today's Date") })
	public String forecastAndUpdateTomorrowClose(String today) {
		try {
			// We need to forecast T+1 close using values for T close
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(today, dateFormat);
			forecastTomorrowClose(date);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Success";
	}
}
