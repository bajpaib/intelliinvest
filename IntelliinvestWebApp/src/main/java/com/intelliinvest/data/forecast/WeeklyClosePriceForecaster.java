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

import com.intelliinvest.common.IntelliinvestConstants.ForecastType;
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
 * Weekly Forecast
 * ---------------
 *
 * Load data for weekly.close.price.forecast.history.years/months
 *
 * TRAINING
 * --------
 * INPUT: Select 1-5 business days from start and select the following
 * date: Avg(Day1-Day5)
 * open: Day1
 * high: Max(Day1 - Day5)
 * low: Min(Day1 - Day5)
 * last: Day5
 * close: Day5
 * totTrdQty: Sum(Day1-Day5)
 *
 * OUTPUT: Select 10th business day from start and choose close as output
 *
 * Continue above process by choosing 2-6 business days as inputs and 11th business day as output
 * Continue above process by choosing 3-7 business days as inputs and 12th business day as output
 * ....
 * ....
 * Last set of inputs and output used for training
 * INPUT: select date, open, high,low,last,close and totTrdQty for (T-10) to (T-5) business days
 * OUTPUT: today's (T) closing price
 *
 * PREDICTION
 * ----------
 * INPUT: select date, open, high,low,last,close and totTrdQty for (T-4) to (T) business days
 * Predict OUTPUT: next week's (T+5) closing price
 *
 *
 */
@ManagedResource(objectName = "bean:name=WeeklyClosePriceForecaster", description = "WeeklyClosePriceForecaster")
public class WeeklyClosePriceForecaster {

	private static Logger logger = Logger.getLogger(WeeklyClosePriceForecaster.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private ForecastedStockPriceRepository forecastedStockPriceRepository;
	@Autowired
	private DateUtil dateUtil;

	private ExecutorService executorService = null;
	private static final String LEARNING_DATA_FILE_NAME = "weeklyLearningData.csv";
	private static final String NEURAL_NETWORK_MODEL_FILE_NAME = "weeklyStockPredictor.nnet";
	private static final int NUM_INPUTS = 7;
	private String learningDataFileDir;
	private int maxIterations;
	private double learningRate;
	private double maxError;

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
		learningDataFileDir = IntelliInvestStore.properties.getProperty("weekly.close.price.forecast.data.dir");
		maxIterations = new Integer(
				IntelliInvestStore.properties.getProperty("weekly.close.price.forecast.max.iterations", "10000"));
		learningRate = new Double(
				IntelliInvestStore.properties.getProperty("weekly.close.price.forecast.learning.rate", "0.5"));
		maxError = new Double(
				IntelliInvestStore.properties.getProperty("weekly.close.price.forecast.max.error", "0.00001"));
	}

	private void initializeScheduledTasks() {
		Runnable weeklyClosePricePredictorTask = new Runnable() {
			public void run() {
				if (!dateUtil.isBankHoliday(dateUtil.getLocalDate())) {
					try {
						// We need to forecast tomorrow's close using values for
						// today's close
						forecastWeeklyClose(dateUtil.getLocalDate());
					} catch (Exception e) {
						logger.error("Error while running weeklyClosePricePredictorTask " + e.getMessage());
					}
				}
			}
		};
		LocalDateTime timeNow = dateUtil.getLocalDateTime();
		int weeklyClosePricePredictStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("weekly.close.price.forecast.start.hr"));
		int weeklyClosePricePredictStartMin = new Integer(
				IntelliInvestStore.properties.getProperty("weekly.close.price.forecast.start.min"));
		LocalDateTime timeNext = timeNow.withHour(weeklyClosePricePredictStartHour)
				.withMinute(weeklyClosePricePredictStartMin).withSecond(0);
		if (timeNow.compareTo(timeNext) > 0) {
			timeNext = timeNext.plusDays(1);
		}
		Duration duration = Duration.between(timeNow, timeNext);
		long initialDelay = duration.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(weeklyClosePricePredictorTask,
				initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);

		logger.info("Scheduled weeklyClosePricePredictorTask for weekly close price forecast. Next run at " + timeNext);
	}

	private void forecastWeeklyClose(LocalDate today) throws Exception {
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
				LocalDate nextWeeklyBusinessDate = dateUtil.addBusinessDays(today, 5);
				forecastPrices.add(new ForecastedStockPrice(code, 0d, result, 0d, today, null, nextWeeklyBusinessDate,
						null, null));
				logger.debug("Stock:" + code + ". ForecastPrice:" + result.doubleValue() + ". ForecastDate:"
						+ nextWeeklyBusinessDate);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("InterruptedException in forecastCloseTask for stock:" + code + ". Exception:"
						+ e.getMessage());
			} catch (ExecutionException e) {
				logger.error(
						"ExecutionException in forecastCloseTask for stock:" + code + ". Exception:" + e.getMessage());
			}
		}
		forecastedStockPriceRepository.updateForecastStockPrices(forecastPrices, ForecastType.WEEKLY);
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
			// We need to forecast T+5 close using (T-4) to (T) as inputs
			QuandlStockPrice price = quandlEODStockPriceRepository.getStockPriceFromDB(stock.getSecurityId(), today);
			if (price == null) {
				throw new Exception("Can't forecast T+5 close. Today's closing price is not available for stock:"
						+ stock.getSecurityId() + " and date:" + today.toString());
			}
			if (!validateEODPrice(price)) {
				throw new Exception("Can't forecast T+5 close. Invalid today's closing price:" + price.toString());
			}

			// fetch data from DB
			ArrayList<QuandlStockPrice> stockPrices = fetchDataFromDB(stock.getSecurityId(), today);
			if (stockPrices.size() <= 10) {
				throw new Exception("Can't forecast T+5 close. Insufficient number of input prices");
			}
			// prepare training data
			double[] minMaxData = prepareTrainingData(stock.getSecurityId(), stockPrices);
			// train network
			trainNetwork(stock.getSecurityId());
			// forecast close from network
			return forecastWeeklyClose(stock.getSecurityId(), stockPrices, minMaxData[0], minMaxData[1], minMaxData[2],
					minMaxData[3], minMaxData[4], minMaxData[5]);
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
		int years = new Integer(IntelliInvestStore.properties.getProperty("weekly.close.price.forecast.history.years"))
				.intValue();
		int months = new Integer(
				IntelliInvestStore.properties.getProperty("weekly.close.price.forecast.history.months")).intValue();
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
		for (int i = 0; i < stockPrices.size() - 9; ++i) {
			// Fetch first 5 and 10th record
			List<QuandlStockPrice> prices = new ArrayList<QuandlStockPrice>();
			for (int j = i; j < i + 5; ++j) {
				prices.add(stockPrices.get(j));
			}
			QuandlStockPrice price10 = stockPrices.get(i + 9);

			// Inputs
			double date = getAverageDate(prices);
			double open = prices.get(0).getOpen();
			double high = getMaxHigh(prices);
			double low = getMinLow(prices);
			double last = prices.get(4).getLast();
			double close = prices.get(4).getClose();
			int totTrdQty = getSumTotTrdQty(prices);
			// Output
			double outputClose = price10.getClose();
			trainingData.add(new TrainingData(date, open, high, low, last, close, totTrdQty, outputClose));
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
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(learningDataFileDir + "/" + stockCode + "_" + LEARNING_DATA_FILE_NAME));
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
		DataSet trainingSet = loadTraininigData(learningDataFileDir + "/" + stockCode + "_" + LEARNING_DATA_FILE_NAME);
		neuralNetwork.learn(trainingSet);
		neuralNetwork.save(learningDataFileDir + "/" + stockCode + "_" + NEURAL_NETWORK_MODEL_FILE_NAME);
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

	// forecast T+5 close from network
	private double forecastWeeklyClose(String stockCode, ArrayList<QuandlStockPrice> stockPrices, double maxPrice,
			double minPrice, double maxDate, double minDate, double maxTrdVol, double minTrdVol) {
		double retVal = 0;
//		logger.debug("Inside forecastWeeklyClose() for stock:" + stockCode);
		NeuralNetwork neuralNetwork = NeuralNetwork
				.createFromFile(learningDataFileDir + "/" + stockCode + "_" + NEURAL_NETWORK_MODEL_FILE_NAME);

		// Input select date, open, high,low,last,close and totTrdQty for (T-4)
		// to (T) business days
		// Output is (T+5) close
		int size = stockPrices.size();
		List<QuandlStockPrice> prices = new ArrayList<QuandlStockPrice>();
		for (int i = size - 1; i > stockPrices.size() - 6; --i) {
			// Fetch last 5
			prices.add(stockPrices.get(i));
		}

		prices.sort(new Comparator<QuandlStockPrice>() {
			public int compare(QuandlStockPrice price1, QuandlStockPrice price2) {
				return price1.getEodDate().compareTo(price2.getEodDate());

			}
		});

		// Inputs
		double date = getAverageDate(prices);
		double open = prices.get(0).getOpen();
		double high = getMaxHigh(prices);
		double low = getMinLow(prices);
		double last = prices.get(4).getLast();
		double close = prices.get(4).getClose();
		int totTrdQty = getSumTotTrdQty(prices);

		neuralNetwork.setInput(normalizeValue(date, maxDate, minDate), normalizeValue(open, maxPrice, minPrice),
				normalizeValue(high, maxPrice, minPrice), normalizeValue(low, maxPrice, minPrice),
				normalizeValue(last, maxPrice, minPrice), normalizeValue(close, maxPrice, minPrice),
				normalizeValue(totTrdQty, maxTrdVol, minTrdVol));

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

	private double getAverageDate(List<QuandlStockPrice> prices) {
		double sum = 0;
		for (QuandlStockPrice price : prices) {
			sum = sum + dateUtil.convertToJulian(price.getEodDate());
		}
		return sum / prices.size();
	}

	private int getSumTotTrdQty(List<QuandlStockPrice> prices) {
		int sum = 0;
		for (QuandlStockPrice price : prices) {
			sum = sum + price.getTradedQty();
		}
		return sum;
	}

	private double getMaxHigh(List<QuandlStockPrice> prices) {
		double max = 0;
		for (QuandlStockPrice price : prices) {
			if (price.getHigh() > max) {
				max = price.getHigh();
			}
		}
		return max;
	}

	private double getMinLow(List<QuandlStockPrice> prices) {
		double min = Double.MAX_VALUE;
		for (QuandlStockPrice price : prices) {
			if (price.getLow() < min) {
				min = price.getLow();
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
				IntelliInvestStore.properties.getProperty("weekly.close.price.forecast.thread.pool.count")).intValue();
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

	@ManagedOperation(description = "forecastAndUpdateWeeklyClose")
	@ManagedOperationParameters({
			@ManagedOperationParameter(name = "Today's Date (yyyy-MM-dd)", description = "Today's Date") })
	public String forecastAndUpdateWeeklyClose(String today) {
		try {
			// We need to forecast T+5 close using values T-4 to T closes
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(today, dateFormat);
			forecastWeeklyClose(date);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Success";
	}
}