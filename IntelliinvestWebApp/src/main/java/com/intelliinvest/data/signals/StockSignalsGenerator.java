package com.intelliinvest.data.signals;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.MagicNumberRepository;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.dao.StockSignalsRepository;
import com.intelliinvest.data.dao.WatchListRepository;
import com.intelliinvest.data.model.MagicNumberData;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.HttpUtil;
import com.intelliinvest.util.ScheduledThreadPoolHelper;
import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

@ManagedResource(objectName = "bean:name=StockSignalsGenerator", description = "StockSignalsGenerator")
public class StockSignalsGenerator {
	private Logger logger = Logger.getLogger(StockSignalsGenerator.class);

	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;

	@Autowired
	private StockSignalsRepository stockSignalsRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private DateUtil dateUtil;

	@Autowired
	private MagicNumberRepository magicNumberRepository;

	@Autowired
	WatchListRepository watchListRepository;

	private int MOVING_AVERGAE_STOCK_PRICE_LIMIT = new Integer(
			IntelliInvestStore.properties.get("movingAverage").toString());
	private static Integer MOVING_AVERAGE = new Integer(IntelliInvestStore.properties.get("ma").toString());;

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
	}

	private void initializeScheduledTasks() {
		Runnable dailyStockSignalsGeneratorTask = new Runnable() {
			public void run() {
				if (!dateUtil.isBankHoliday(dateUtil.getLocalDate())) {
					try {
						// We need to generate forecast report for today
						generateSignalsForToday();
						watchListRepository.sendDailyTradingAccountUpdateMail();

					} catch (Exception e) {
						logger.error("Error while running StockSignalsImporter for all stocks " + e.getMessage());
					}
				}
			}
		};
		LocalDateTime timeNow = dateUtil.getLocalDateTime();
		int dailyStockSignalsGeneratorStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("daily.stock.signals.generator.start.hr"));
		int dailyStockSignalsGeneratorStartMin = new Integer(
				IntelliInvestStore.properties.getProperty("daily.stock.signals.generator.start.min"));
		LocalDateTime timeNext = timeNow.withHour(dailyStockSignalsGeneratorStartHour)
				.withMinute(dailyStockSignalsGeneratorStartMin).withSecond(0);
		if (timeNow.compareTo(timeNext) > 0) {
			timeNext = timeNext.plusDays(1);
		}
		Duration duration = Duration.between(timeNow, timeNext);
		long initialDelay = duration.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(dailyStockSignalsGeneratorTask,
				initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);
		logger.info("Scheduled dailyStockSignalsGenratorTask . Next run at " + timeNext);
	}

	private void generateSignalsForToday() {
		generateTodaysSignal(MOVING_AVERAGE);
	}

	public static void main(String[] args) throws Exception {
		StockSignalsGenerator signalComponentGenerator = new StockSignalsGenerator();
		String url = "https://www.quandl.com/api/v3/datasets/NSE/INFY.csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=2013-01-01&end_date=2016-09-30";
		String eodPricesAsString = HttpUtil.getFromUrlAsString(url);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String[] eodPricesAsArray = eodPricesAsString.split("\n");
		LinkedList<QuandlStockPrice> quandlStockPrices = new LinkedList<QuandlStockPrice>();
		for (int i = 1; i < eodPricesAsArray.length; i++) {
			String eodPriceAsString = eodPricesAsArray[i];
			String[] eodPriceAsArray = eodPriceAsString.split(",");
			LocalDate eodDate = LocalDate.parse(eodPriceAsArray[0], dateFormat);
			double open = new Double(eodPriceAsArray[1]);
			double high = new Double(eodPriceAsArray[2]);
			double low = new Double(eodPriceAsArray[3]);
			double last = new Double(eodPriceAsArray[4]);
			double wap = 0d;
			double close = new Double(eodPriceAsArray[5]);
			int tradedQty = new Double(eodPriceAsArray[6]).intValue();
			double turnover = new Double(eodPriceAsArray[7]);

			QuandlStockPrice quandlStockPrice = new QuandlStockPrice("INFY", "NSE", "EQ", open, high, low, close, last,
					wap, tradedQty, turnover, eodDate, LocalDateTime.now());
			quandlStockPrices.push(quandlStockPrice);

		}
		SignalComponentHolder signalComponentHolder = new SignalComponentHolder(10, 3);
		Integer magicNumberADX = 45;
		Double magicNumberBolliger = .17;
		Integer magicNumberOscillator = 15;
		signalComponentHolder.setMagicNumberADX(magicNumberADX);
		signalComponentHolder.setMagicNumberBolliger(magicNumberBolliger);
		signalComponentHolder.setMagicNumberOscillator(magicNumberOscillator);
		signalComponentGenerator.generateSignalsInternal(signalComponentHolder, quandlStockPrices);
	}

	public Boolean generateSignals(Integer ma) {
		Map<String, List<QuandlStockPrice>> quandalStockPricesMap = quandlEODStockPriceRepository.getEODStockPrices();

		for (Stock stockDetailData : stockRepository.getStocks()) {
			try {
				String securityId = stockDetailData.getSecurityId();
				List<QuandlStockPrice> quandlStockPrices = null;
				if (securityId != null) {
					if (quandalStockPricesMap != null && quandalStockPricesMap.get(securityId) != null) {
						quandlStockPrices = quandalStockPricesMap.get(securityId);
					} else
						quandlStockPrices = quandlEODStockPriceRepository.getStockPricesFromDB(securityId);

					if (quandlStockPrices != null && quandlStockPrices.size() > 0)
						generateSignals(ma, stockDetailData.getSecurityId(), quandlStockPrices);
					else {
						logger.info("Not able to generate signals for stock: " + securityId);
					}
				}
			} catch (Exception e) {
				logger.info("Error generating signal for " + stockDetailData.getSecurityId() + " with error "
						+ e.getMessage(), e);
				return false;
			}
		}
		stockSignalsRepository.refreshCache();
		return true;
	}

	public List<StockSignalsDTO> generateSignals(Integer ma, String stockCode) {
		List<QuandlStockPrice> quandlStockPrices = quandlEODStockPriceRepository.getStockPricesFromDB(stockCode);

		List<StockSignalsDTO> stockSignalsDTOs = generateSignals(ma, stockCode, quandlStockPrices);
		if (stockSignalsDTOs != null && stockSignalsDTOs.size() > 0)
			stockSignalsRepository.refreshCache();
		return stockSignalsDTOs;
	}

	private List<StockSignalsDTO> generateSignals(Integer ma, String stockCode,
			List<QuandlStockPrice> quandlStockPrices) {
		MagicNumberData magicNumberData = magicNumberRepository.getMagicNumber(stockCode);
		if (null == magicNumberData) {
			logger.info("Setting default magic number for stock " + stockCode);
			magicNumberData = new MagicNumberData(stockCode, ma);
		}
		// else
		// logger.info("Magic Number Object: " + magicNumberData.toString());

		SignalComponentHolder signalComponentHolder = new SignalComponentHolder(ma, 3);

		signalComponentHolder.setMagicNumberADX(magicNumberData.getMagicNumberADX());
		signalComponentHolder.setMagicNumberBolliger(magicNumberData.getMagicNumberBollinger());
		signalComponentHolder.setMagicNumberOscillator(magicNumberData.getMagicNumberOscillator());
		return generateSignalsInternal(signalComponentHolder, quandlStockPrices);
	}

	private List<StockSignalsDTO> generateSignalsInternal(SignalComponentHolder signalComponentHolder,
			List<QuandlStockPrice> quandlStockPrices) {
		List<StockSignalsDTO> stockSignalsDTOs = generateSignals(signalComponentHolder, quandlStockPrices, "ALL");
		try {
			stockSignalsRepository.updateStockSignals(signalComponentHolder.getMa(), stockSignalsDTOs, false);

		} catch (Exception e) {
			logger.info("Error updating signals for today with error " + e.getMessage(), e);
		}
		return stockSignalsDTOs;
	}

	protected List<StockSignalsDTO> generateSignals(SignalComponentHolder signalComponentHolder,
			List<QuandlStockPrice> quandlStockPrices, String type) {
		List<StockSignalsDTO> stockSignalsDTOs = new ArrayList<StockSignalsDTO>();
		List<SignalComponentBuilder> signalComponentBuilders = new ArrayList<SignalComponentBuilder>();
		if ("ADX".equals(type) || "BOL".equals(type) || "ALL".equals(type)) {
			signalComponentBuilders.add(new BaseSignalComponentBuilder());
		}
		if ("ADX".equals(type) || "ALL".equals(type)) {
			signalComponentBuilders.add(new ADXSignalComponentBuilder());
		}
		if ("BOL".equals(type) || "ALL".equals(type)) {
			signalComponentBuilders.add(new BollingerSignalComponentBuilder());
		}
		if ("OSC".equals(type) || "ALL".equals(type)) {
			signalComponentBuilders.add(new OscillatorSignalComponentBuilder());
		}

		int movingAverageCounter = 0;
		SignalComponentInitilaizer signalComponentInitilaizer = new SignalComponentInitilaizer();
		// logger.info("Quandl stock price list size is:"
		// + quandlStockPrices.size());
		for (QuandlStockPrice quandlStockPrice : quandlStockPrices) {
			signalComponentInitilaizer.init(signalComponentHolder, quandlStockPrice);
			for (SignalComponentBuilder signalComponentBuilder : signalComponentBuilders) {
				signalComponentBuilder.generateSignal(signalComponentHolder);
			}

			if ("ALL".equals(type)) {
				generateMovingAverageSignal(quandlStockPrices, signalComponentHolder, movingAverageCounter);

				movingAverageCounter++;

				generateAggregateSignals(signalComponentHolder);
			}
			stockSignalsDTOs.add(signalComponentHolder.getStockSignalsDTOs().getLast());
		}
		return stockSignalsDTOs;
	}

	private void generateAggregateSignals(SignalComponentHolder signalComponentHolder) {
		AggregateSignalsComponentBuilder aggregateSignalsComponentBuilder = new AggregateSignalsComponentBuilder();
		aggregateSignalsComponentBuilder.generateSignal(signalComponentHolder);

	}

	private void generateMovingAverageSignal(List<QuandlStockPrice> quandlStockPrices,
			SignalComponentHolder signalComponentHolder, int movingAverageCounter) {
		List<QuandlStockPrice> quandalStockPriceList = null;
		if (movingAverageCounter <= MOVING_AVERGAE_STOCK_PRICE_LIMIT) {

			quandalStockPriceList = quandlStockPrices.subList(0, movingAverageCounter + 1);
		} else {
			quandalStockPriceList = quandlStockPrices
					.subList(movingAverageCounter - MOVING_AVERGAE_STOCK_PRICE_LIMIT + 1, movingAverageCounter + 1);
		}
		SignalComponentHolder movingAverageSignalComponentHolder = getMovingAverageSignalComponentHolder(
				signalComponentHolder);
		movingAverageSignalComponentHolder.addQuandlStockPrices(quandalStockPriceList);
		MovingAverageComponentBuilder movingAverageComponentBuilder = new MovingAverageComponentBuilder();
		movingAverageComponentBuilder.generateSignal(movingAverageSignalComponentHolder);
	}

	private SignalComponentHolder getMovingAverageSignalComponentHolder(SignalComponentHolder signalComponentHolder) {
		SignalComponentHolder retSignalComponentHolder = new SignalComponentHolder(MOVING_AVERGAE_STOCK_PRICE_LIMIT,
				signalComponentHolder.getStockSignalsDTOSize());
		retSignalComponentHolder.addStockSignalsDTOs(signalComponentHolder.getStockSignalsDTOs());
		return retSignalComponentHolder;
	}

	public List<StockSignalsDTO> generateTodaysSignal(Integer ma) {
		List<StockSignalsDTO> todaysStockSignalsDTOs = new ArrayList<StockSignalsDTO>();
		LocalDate startDate = dateUtil.substractBusinessDays(dateUtil.getLocalDate(), 50);
		Map<String, List<StockSignalsDTO>> stockSignalsDTOsMap = stockSignalsRepository
				.getStockSignalsFromStartDate(startDate, ma);
		Map<String, List<QuandlStockPrice>> quandlStockPricesMap = quandlEODStockPriceRepository
				.getEODStockPricesFromStartDate(startDate);

		for (Stock stockDetailData : stockRepository.getStocks()) {
			try {
				String securityId = stockDetailData.getSecurityId();
				if (stockSignalsDTOsMap != null && stockSignalsDTOsMap.size() > 0) {
					if (securityId != null && quandlStockPricesMap.get(securityId) != null
							&& quandlStockPricesMap.get(securityId).size() > 0
							&& stockSignalsDTOsMap.get(securityId) != null
							&& stockSignalsDTOsMap.get(securityId).size() > 0) {
						StockSignalsDTO stockSignalsDTO = generateTodaysSignalInternal(ma,
								stockDetailData.getSecurityId(), quandlStockPricesMap.get(securityId),
								stockSignalsDTOsMap.get(securityId));
						todaysStockSignalsDTOs.add(stockSignalsDTO);
					} else {
						logger.info("Not able to generate signals for stock: " + securityId);

					}

				} else {
					if (securityId != null && quandlStockPricesMap.get(securityId) != null
							&& quandlStockPricesMap.get(securityId).size() > 0) {
						List<StockSignalsDTO> stockSignalsDTOs = stockSignalsRepository
								.getStockSignalsFromStartDate(startDate, securityId, ma);
						StockSignalsDTO stockSignalsDTO = generateTodaysSignalInternal(ma,
								stockDetailData.getSecurityId(), quandlStockPricesMap.get(securityId),
								stockSignalsDTOs);
						todaysStockSignalsDTOs.add(stockSignalsDTO);
					} else {
						logger.info("Not able to generate signals for stock: " + securityId);

					}

				}
			} catch (Exception e) {
				logger.info("Error generating signal for " + stockDetailData.getSecurityId() + " with error "
						+ e.getMessage(), e);
			}
		}
		try {
			stockSignalsRepository.updateStockSignals(ma, todaysStockSignalsDTOs, true);

		} catch (Exception e) {
			logger.info("Error updating signals for today with error " + e.getMessage(), e);
		}
		return todaysStockSignalsDTOs;
	}

	public StockSignalsDTO generateTodaysSignal(Integer ma, String stockCode) {
		StockSignalsDTO stockSignalsDTO = generateTodaysSignalInternal(ma, stockCode);
		try {
			stockSignalsRepository.updateStockSignals(ma, Collections.singletonList(stockSignalsDTO), true);
		} catch (Exception e) {
			logger.info("Error updating signals for today with error " + e.getMessage(), e);
		}
		return stockSignalsDTO;
	}

	private StockSignalsDTO generateTodaysSignalInternal(Integer ma, String stockCode) {
		// LocalDate businessDate = dateUtil.getLastBusinessDate();
		LocalDate startDate = dateUtil.substractBusinessDays(dateUtil.getLocalDate(), 60);
		List<StockSignalsDTO> stockSignalsDTOs = stockSignalsRepository.getStockSignalsFromStartDate(startDate,
				stockCode, ma);
		List<QuandlStockPrice> quandlStockPrices = quandlEODStockPriceRepository.getStockPricesFromStartDate(stockCode,
				startDate);
		return generateTodaysSignalInternal(ma, stockCode, quandlStockPrices, stockSignalsDTOs);
	}

	private StockSignalsDTO generateTodaysSignalInternal(Integer ma, String stockCode,
			List<QuandlStockPrice> quandlStockPrices, List<StockSignalsDTO> stockSignalsDTOs) {
		if (stockSignalsDTOs != null && quandlStockPrices != null && !quandlStockPrices.isEmpty()
				&& !stockSignalsDTOs.isEmpty()) {
			// logger.info("Stock Signal list size is:" +
			// stockSignalsDTOs.size() + " quandl price list size is : "
			// + quandlStockPrices.size());
		} else {
			throw new RuntimeException("EOD Prices not available");
		}

		SignalComponentHolder signalComponentHolder = new SignalComponentHolder(ma, 3);
		MagicNumberData magicNumberData = magicNumberRepository.getMagicNumber(stockCode);

		if (null == magicNumberData) {
			logger.info("Setting default magic number for stock " + stockCode);
			magicNumberData = new MagicNumberData(stockCode, ma);
		}
		// logger.info("Magic Number Object: " + magicNumberData.toString());

		signalComponentHolder.setMagicNumberADX(magicNumberData.getMagicNumberADX());
		signalComponentHolder.setMagicNumberBolliger(magicNumberData.getMagicNumberBollinger());
		signalComponentHolder.setMagicNumberOscillator(magicNumberData.getMagicNumberOscillator());

		signalComponentHolder.addStockSignalsDTOs(stockSignalsDTOs);
		return generateTodaysSignal(signalComponentHolder, quandlStockPrices);
	}

	private StockSignalsDTO generateTodaysSignal(SignalComponentHolder signalComponentHolder,
			List<QuandlStockPrice> quandlStockPrices) {
		QuandlStockPrice quandlStockPrice = quandlStockPrices.get(quandlStockPrices.size() - 1);
		quandlStockPrices = quandlStockPrices.subList(0, quandlStockPrices.size() - 1);
		signalComponentHolder.addQuandlStockPrices(quandlStockPrices);

		SignalComponentInitilaizer signalComponentInitilaizer = new SignalComponentInitilaizer();
		BaseSignalComponentBuilder baseSignalComponentBuilder = new BaseSignalComponentBuilder();
		ADXSignalComponentBuilder adxSignalComponentBuilder = new ADXSignalComponentBuilder();
		BollingerSignalComponentBuilder bollingerSignalComponentBuilder = new BollingerSignalComponentBuilder();
		OscillatorSignalComponentBuilder oscillatorSignalComponentBuilder = new OscillatorSignalComponentBuilder();
		signalComponentInitilaizer.init(signalComponentHolder, quandlStockPrice);
		baseSignalComponentBuilder.generateSignal(signalComponentHolder);
		adxSignalComponentBuilder.generateSignal(signalComponentHolder);
		bollingerSignalComponentBuilder.generateSignal(signalComponentHolder);
		oscillatorSignalComponentBuilder.generateSignal(signalComponentHolder);

		generateMovingAverageSignal(quandlStockPrices, signalComponentHolder, quandlStockPrices.size() - 1);
		generateAggregateSignals(signalComponentHolder);
		return signalComponentHolder.getStockSignalsDTOs().getLast();
	}
}
