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
import java.util.Iterator;
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

@ManagedResource(objectName = "bean:name=DailyClosePriceForecaster", description = "DailyClosePriceForecaster")
public class DailyClosePriceForecaster {

	private static Logger logger = Logger.getLogger(DailyClosePriceForecaster.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private ForecastedStockPriceRepository forecastedStockPriceRepository;

	private ExecutorService executorService = null;
	private static final String LEARNING_DATA_FILE_NAME = "learningData.csv";
	private static final String NEURAL_NETWORK_MODEL_FILE_NAME = "stockPredictor.nnet";
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
				if (!DateUtil.isBankHoliday(DateUtil.getLocalDate())) {
					try {
						// We need to forecast tomorrow's close using values for
						// today's close
						forecastTomorrowClose(DateUtil.getLocalDate());
					} catch (Exception e) {
						logger.error("Error refreshing EOD price data for NSE stocks " + e.getMessage());
					}
				}
			}
		};
		LocalDateTime zonedNow = DateUtil.getLocalDateTime();
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
			futuresMap.put(stock.getCode(), future);
		}
		List<ForecastedStockPrice> forecastPrices = new ArrayList<ForecastedStockPrice>();
		for (Map.Entry<String, Future<Double>> entry : futuresMap.entrySet()) {
			String code = null;
			try {
				code = entry.getKey();
				Future<Double> future = entry.getValue();
				Double result = future.get();				
				// We need to update STOCK_PRICE_DAILY_FORECAST table
				LocalDate nextBusinessDate = DateUtil.addBusinessDays(today, 1);
				forecastPrices.add(new ForecastedStockPrice(code, result, nextBusinessDate, DateUtil.getLocalDateTime()));
//				logger.debug("Stock:" + code + ". ForecastPrice:" + result.doubleValue() + ". ForecastDate:"+ nextBusinessDate);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("InterruptedException in forecastCloseTask for stock:" + code + ". Exception:" + e.getMessage());
			} catch (ExecutionException e) {
				logger.error("ExecutionException in forecastCloseTask for stock:" + code + ". Exception:" + e.getMessage());
			}
		}
		forecastedStockPriceRepository.updateDailyForecastStockPrices(forecastPrices);
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
			QuandlStockPrice price = quandlEODStockPriceRepository.getStockPriceFromDB(stock.getCode(), today);
			if (price == null) {
				throw new Exception("Can't forecast tomorrow close. Today's price is not available for stock:"
						+ stock.getCode() + " and date:" + today.toString());
			}
			if (!validateEODPrice(price)) {
				throw new Exception("Can't forecast tomorrow close. Invalid today's closing price:" + price.toString());
			}
			// prepare data
			double[] minMaxData = prepareData(stock, today);
			// train network
			trainNetwork(stock);
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
		if (price.getTottrdqty() == 0) {
			return false;
		}
		if (MathUtil.isNearZero(price.getOpen()) || MathUtil.isNearZero(price.getHigh())
				|| MathUtil.isNearZero(price.getLow()) || MathUtil.isNearZero(price.getLast())
				|| MathUtil.isNearZero(price.getClose())) {
			return false;
		}
		return true;
	}

	private double[] prepareData(Stock stock, LocalDate today) throws Exception {
		logger.debug("Inside prepareData() for stock:" + stock.getCode() + " and date:" + today.toString());
		int years = new Integer(IntelliInvestStore.properties.getProperty("daily.close.price.forecast.history.years"))
				.intValue();
		int months = new Integer(IntelliInvestStore.properties.getProperty("daily.close.price.forecast.history.months"))
				.intValue();
		LocalDate startDate = today.minusYears(years).minusMonths(months);
		List<QuandlStockPrice> stockPricesFromDB = quandlEODStockPriceRepository.getStockPricesFromDB(stock.getCode(), startDate, today);
		stockPricesFromDB.sort(new Comparator<QuandlStockPrice>() {
			public int compare(QuandlStockPrice price1, QuandlStockPrice price2) {
				return price1.getEodDate().compareTo(price2.getEodDate());

			}
		});
		// Find the minimum and maximum values for price - needed for normalization
		double maxPrice = 0;
		double minPrice = Double.MAX_VALUE;
		ArrayList<QuandlStockPrice> stockPrices = validateEODPrices(stockPricesFromDB);

		// Find the minimum and maximum values for eod date - needed for normalization
		int maxDate = 0;
		int minDate = Integer.MAX_VALUE;

		// Find the minimum and maximum values for tradeVol - needed for normalization
		int maxTrdVol = 0;
		int minTrdVol = Integer.MAX_VALUE;

		// ignore today's inputs for min/max calc
		for (int i = 0; i < stockPrices.size() - 1; ++i) {
			// find max/min price
			double tempMaxPrice = getMaxPrice(stockPrices.get(i));
			double tempMinPrice = getMinPrice(stockPrices.get(i));
			if (tempMaxPrice > maxPrice) {
				maxPrice = tempMaxPrice;
			}
			if (tempMinPrice < minPrice) {
				minPrice = tempMinPrice;
			}
			// find max/min date
			int tempDate = DateUtil.convertToJulian(stockPrices.get(i).getEodDate());
			if (tempDate > maxDate) {
				maxDate = tempDate;
			}
			if (tempDate < minDate) {
				minDate = tempDate;
			}

			// find max/min trdVol
			int tempTradeVol = stockPrices.get(i).getTottrdqty();
			if (tempTradeVol > maxTrdVol) {
				maxTrdVol = tempTradeVol;
			}
			if (tempTradeVol < minTrdVol) {
				minTrdVol = tempTradeVol;
			}
		}

		BufferedWriter writer = new BufferedWriter(
				new FileWriter(learningDataFileDir + "/" + stock.getCode() + "_" + LEARNING_DATA_FILE_NAME));
		LinkedList<Double> valuesQueue1 = new LinkedList<Double>();
		LinkedList<Double> valuesQueue2 = new LinkedList<Double>();
		try {
			Iterator<QuandlStockPrice> iter = stockPrices.iterator();
			while (iter.hasNext()) {
				QuandlStockPrice price1 = iter.next();
				valuesQueue1.add(normalizeValue(DateUtil.convertToJulian(price1.getEodDate()), maxDate, minDate));
				valuesQueue1.add(normalizeValue(price1.getOpen(), maxPrice, minPrice));
				valuesQueue1.add(normalizeValue(price1.getHigh(), maxPrice, minPrice));
				valuesQueue1.add(normalizeValue(price1.getLow(), maxPrice, minPrice));
				valuesQueue1.add(normalizeValue(price1.getLast(), maxPrice, minPrice));
				valuesQueue1.add(normalizeValue(price1.getClose(), maxPrice, minPrice));
				valuesQueue1.add(normalizeValue(price1.getTottrdqty(), maxTrdVol, minTrdVol));
				if (!valuesQueue2.isEmpty()) {
					valuesQueue2.add(normalizeValue(price1.getClose(), maxPrice, minPrice));
					String valueLine = valuesQueue2.toString().replaceAll("\\[|\\]", "");
					writer.write(valueLine);
					writer.newLine();
					valuesQueue2.clear();
				}
				if (iter.hasNext()) {
					QuandlStockPrice price2 = iter.next();
					valuesQueue2.add(normalizeValue(DateUtil.convertToJulian(price2.getEodDate()), maxDate, minDate));
					valuesQueue2.add(normalizeValue(price2.getOpen(), maxPrice, minPrice));
					valuesQueue2.add(normalizeValue(price2.getHigh(), maxPrice, minPrice));
					valuesQueue2.add(normalizeValue(price2.getLow(), maxPrice, minPrice));
					valuesQueue2.add(normalizeValue(price2.getLast(), maxPrice, minPrice));
					valuesQueue2.add(normalizeValue(price2.getClose(), maxPrice, minPrice));
					valuesQueue2.add(normalizeValue(price2.getTottrdqty(), maxTrdVol, minTrdVol));
					if (!valuesQueue1.isEmpty()) {
						valuesQueue1.add(normalizeValue(price2.getClose(), maxPrice, minPrice));
						String valueLine = valuesQueue1.toString().replaceAll("\\[|\\]", "");
						writer.write(valueLine);
						writer.newLine();
						valuesQueue1.clear();
					}
				}
			}
		} finally {
			writer.flush();
		    writer.close();
		}
		return new double[] { maxPrice, minPrice, maxDate, minDate, maxTrdVol, minTrdVol };
	}

	// train network
	private void trainNetwork(Stock stock) throws IOException {
		logger.debug("Inside trainNetwork() for stock:" + stock.getCode());
		NeuralNetwork<BackPropagation> neuralNetwork = new MultiLayerPerceptron(TransferFunctionType.SIGMOID,
				NUM_INPUTS, 2 * NUM_INPUTS + 1, 1);
		SupervisedLearning learningRule = neuralNetwork.getLearningRule();
		learningRule.setMaxError(maxError);
		learningRule.setLearningRate(learningRate);
		learningRule.setMaxIterations(maxIterations);
		learningRule.addListener(new LearningEventListener() {
			public void handleLearningEvent(LearningEvent learningEvent) {
				SupervisedLearning rule = (SupervisedLearning) learningEvent.getSource();
				// logger.debug("Network error for interation " + rule.getCurrentIteration() + ": " + rule.getTotalNetworkError());
			}
		});
		DataSet trainingSet = loadTraininigData(
				learningDataFileDir + "/" + stock.getCode() + "_" + LEARNING_DATA_FILE_NAME);
		neuralNetwork.learn(trainingSet);
		neuralNetwork.save(learningDataFileDir + "/" + stock.getCode() + "_" + NEURAL_NETWORK_MODEL_FILE_NAME);
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

	// forecast close from network
	private double forecastTomorrowClose(QuandlStockPrice price, double maxPrice, double minPrice, double maxDate,
			double minDate, double maxTrdVol, double minTrdVol) {
		double retVal = 0;
		logger.debug("Inside forecastTomorrowClose() for stock:" + price.getSymbol());
		NeuralNetwork neuralNetwork = NeuralNetwork
				.createFromFile(learningDataFileDir + "/" + price.getSymbol() + "_" + NEURAL_NETWORK_MODEL_FILE_NAME);
		neuralNetwork.setInput(normalizeValue(DateUtil.convertToJulian(price.getEodDate()), maxDate, minDate),
				normalizeValue(price.getOpen(), maxPrice, minPrice),
				normalizeValue(price.getHigh(), maxPrice, minPrice), normalizeValue(price.getLow(), maxPrice, minPrice),
				normalizeValue(price.getLast(), maxPrice, minPrice),
				normalizeValue(price.getClose(), maxPrice, minPrice),
				normalizeValue(price.getTottrdqty(), maxTrdVol, minTrdVol));
		neuralNetwork.calculate();
		double[] networkOutput = neuralNetwork.getOutput();
		retVal = deNormalizeValue(networkOutput[0], maxPrice, minPrice);
		return retVal;
	}

	private double getMaxPrice(QuandlStockPrice price) {
		double[] temp = { price.getClose(), price.getHigh(), price.getLast(), price.getLow(), price.getOpen() };
		double max = 0;
		for (int i = 0; i < temp.length; i++) {
			if (temp[i] > max) {
				max = temp[i];
			}
		}
		return max;
	}

	private double getMinPrice(QuandlStockPrice price) {
		double[] temp = { price.getClose(), price.getHigh(), price.getLast(), price.getLow(), price.getOpen() };
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
		int count = new Integer(IntelliInvestStore.properties.getProperty("daily.close.price.forecast.thread.pool.count")).intValue();
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
			// We need to forecast tomorrow's close using values for today's
			// close
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(today, dateFormat);
			forecastTomorrowClose(date);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Success";
	}
}
