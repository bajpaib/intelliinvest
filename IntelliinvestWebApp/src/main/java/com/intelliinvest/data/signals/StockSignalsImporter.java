//package com.intelliinvest.data.signals;
//
//import java.time.Duration;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import javax.annotation.PostConstruct;
//
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jmx.export.annotation.ManagedResource;
//
//import com.intelliinvest.common.IntelliInvestStore;
//import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
//import com.intelliinvest.data.dao.StockRepository;
//import com.intelliinvest.data.dao.StockSignalsRepository;
//import com.intelliinvest.data.dao.WatchListRepository;
//import com.intelliinvest.data.model.QuandlStockPrice;
//import com.intelliinvest.data.model.Stock;
//import com.intelliinvest.data.model.StockSignalsDTO;
//import com.intelliinvest.util.DateUtil;
//import com.intelliinvest.util.ScheduledThreadPoolHelper;
//
//@ManagedResource(objectName = "bean:name=StockSignalsImporter", description = "StockSignalsImporter")
//public class StockSignalsImporter {
//	private Logger logger = Logger.getLogger(StockSignalsImporter.class);
//	@Autowired
//	private StockRepository stockRepository;
//	@Autowired
//	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
//	@Autowired
//	private StockSignalsRepository stockSignalsRepository;
//
//	@Autowired
//	private DateUtil dateUtil;
//
//	@Autowired
//	WatchListRepository watchListRepository;
//
//	@PostConstruct
//	public void init() {
//		initializeScheduledTasks();
//	}
//
//	private void initializeScheduledTasks() {
//		Runnable dailyStockSignalsGeneratorTask = new Runnable() {
//			public void run() {
//				if (!dateUtil.isBankHoliday(dateUtil.getLocalDate())) {
//					try {
//						// We need to generate forecast report for today
//						boolean signalGeneratorResponse = generateSignalsForToday();
//						watchListRepository.sendDailyTradingAccountUpdateMail();
//
//					} catch (Exception e) {
//						logger.error("Error while running StockSignalsImporter for all stocks "
//								+ e.getMessage());
//					}
//				}
//			}
//		};
//		LocalDateTime timeNow = dateUtil.getLocalDateTime();
//		int dailyStockSignalsGeneratorStartHour = new Integer(
//				IntelliInvestStore.properties
//						.getProperty("daily.stock.signals.generator.start.hr"));
//		int dailyStockSignalsGeneratorStartMin = new Integer(
//				IntelliInvestStore.properties
//						.getProperty("daily.stock.signals.generator.start.min"));
//		LocalDateTime timeNext = timeNow
//				.withHour(dailyStockSignalsGeneratorStartHour)
//				.withMinute(dailyStockSignalsGeneratorStartMin).withSecond(0);
//		if (timeNow.compareTo(timeNext) > 0) {
//			timeNext = timeNext.plusDays(1);
//		}
//		Duration duration = Duration.between(timeNow, timeNext);
//		long initialDelay = duration.getSeconds();
//		ScheduledThreadPoolHelper.getScheduledExecutorService()
//				.scheduleAtFixedRate(dailyStockSignalsGeneratorTask,
//						initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);
//		logger.info("Scheduled dailyStockSignalsGenratorTask . Next run at "
//				+ timeNext);
//	}
//
//	private boolean generateSignalsForToday() {
//		return generateTodaySignals("ALL");
//	}
//
//	public boolean generateSignals(String symbol) {
//		Integer ma = new Integer(IntelliInvestStore.properties.get("ma")
//				.toString());
//		return generateSignals(symbol, ma);
//	}
//
//	public boolean generateSignals(String symbol, int ma) {
//		int f = 1;
//		try {
//			if ("ALL".equals(symbol)) {
//				Map<String, List<QuandlStockPrice>> eodPrices = quandlEODStockPriceRepository
//						.getEODStockPrices();
//				for (Stock stockDetailData : stockRepository.getStocks()) {
//					try {
//						logger.info("Calculating signals for symbol "
//								+ stockDetailData.getSecurityId());
//						if (eodPrices.get(stockDetailData.getSecurityId()) != null
//								&& eodPrices.get(
//										stockDetailData.getSecurityId()).size() > 0) {
//							List<StockSignalsDTO> stockSignalsDTOs = getSignals(
//									stockDetailData.getSecurityId(), ma,
//									eodPrices.get(stockDetailData
//											.getSecurityId()));
//							// stockSignalsRepository.deleteStockSignals(ma,
//							// stockDetailData.getSecurityId());
//							if (stockSignalsDTOs != null)
//								stockSignalsRepository.updateStockSignals(
//										ma, stockSignalsDTOs);
//							else
//								f = 0;
//						}
//					} catch (Exception e) {
//						logger.info("Error generating signals for "
//								+ stockDetailData.getSecurityId()
//								+ " with error " + e.getMessage(), e);
//						return false;
//					}
//				}
//				stockSignalsRepository.refreshCache();
//				// watchListRepository.refreshCache();
//				// ChartDao.getInstance().insertSignals(ma, "ALL", null);
//			} else {
//				List<QuandlStockPrice> eodPrices = quandlEODStockPriceRepository
//						.getStockPricesFromDB(symbol);
//				logger.info("Calculating signals for symbol " + symbol);
//				if (eodPrices != null && eodPrices.size() > 0) {
//					List<StockSignalsDTO> stockSignalsDTOs = getSignals(symbol,
//							ma, eodPrices);
//					logger.info("signals generated for " + symbol);
//					// stockSignalsRepository.deleteStockSignals(ma, symbol);
//					if (stockSignalsDTOs != null) {
//						stockSignalsRepository.updateStockSignals(ma,
//								stockSignalsDTOs);
//						stockSignalsRepository.refreshCache();
//						// watchListRepository.refreshCache();
//					} else
//						f = 0;
//				}
//
//			}
//			logger.debug("generate signal method is going to end...");
//		} catch (Exception e) {
//			logger.info("Error generating signal for " + symbol
//					+ " with error " + e.getMessage(), e);
//			return false;
//		}
//		if (f == 1)
//			return true;
//		else
//			return false;
//		// IntelliInvestStore.refresh();TODO
//	}
//
//	private List<StockSignalsDTO> getSignals(String stockCode, Integer ma,
//			List<QuandlStockPrice> quandlStockPrices) {
//		Integer magicNumber = 45;
//		// String magicNumberStr = IntelliInvestStore.getMagicNumber(ma,
//		// stockCode);TODO
//		// if(null!=magicNumberStr){
//		// magicNumber = new Integer(magicNumberStr);
//		// }
//		// List<QuandlStockPrice> quandlStockPrices =
//		// quandlEODStockPriceRepository
//		// .getStockPricesFromDB(stockCode);
//		// logger.debug("Quandl Stock Price list size is:" +
//		// quandlStockPrices.size());
//
//		QuandlStockPrice quandlStockPrice_1 = new QuandlStockPrice();
//		int count = 0, movingAverageCounter = 0;
//		List<StockSignalsDTO> signals = new ArrayList<StockSignalsDTO>();
//		StockSignalsDTO prevSignalComponents = null;
//		SignalComponentsEnhancer signalComponentsEnhancer = new SignalComponentsEnhancer(
//				ma);
//		for (QuandlStockPrice quandlStockPrice : quandlStockPrices) {
//			// logger.debug("in generating signal for :" + stockCode
//			// + " quandl eod date:" + quandlStockPrice.getEodDate());
//			StockSignalsDTO signalComponents = null;
//			if (count != 0) {
//
//				if (count <= ma) {
//					signalComponents = signalComponentsEnhancer.init9(
//							quandlStockPrice, quandlStockPrice_1);
//				} else if (count == (ma + 1)) {
//					signalComponents = signalComponentsEnhancer.init10(
//							magicNumber, quandlStockPrices.subList(count - ma
//									+ 1, count + 1), signals);
//				} else {
//					signalComponents = signalComponentsEnhancer.init(
//							magicNumber, quandlStockPrices.subList(count - ma
//									+ 1, count + 1), prevSignalComponents,
//							signals);
//				}
//				if (null != signalComponents) {
//					signals.add(signalComponents);
//					prevSignalComponents = signalComponents;
//
//				}
//				try {
//					if (movingAverageCounter <= 50)
//						signalComponentsEnhancer.generateMovingAverageSignals(
//								quandlStockPrices.subList(0,
//										movingAverageCounter + 1),
//								signalComponents, prevSignalComponents);
//					else {
//
//						signalComponentsEnhancer.generateMovingAverageSignals(
//								quandlStockPrices.subList(
//										movingAverageCounter - 50 + 1,
//										movingAverageCounter + 1),
//								signalComponents, prevSignalComponents);
//					}
//					signalComponentsEnhancer.generateAggregateSignals(
//							signalComponents, prevSignalComponents);
//				} catch (Exception e) {
//					logger.error("unable to generate moving average signals...for stock: "
//							+ stockCode);
//				}
//
//			}
//
//			count++;
//			movingAverageCounter++;
//			quandlStockPrice_1 = quandlStockPrice;
//		}
//
//		return signals;
//	}
//
//	public boolean generateTodaySignals(String symbol) {
//		Integer ma = new Integer(IntelliInvestStore.properties.get("ma")
//				.toString());
//		return generateTodaySignals(symbol, ma);
//	}
//
//	public boolean generateTodaySignals(String symbol, int ma) {
//		int f = 1;
//		try {
//			logger.debug("in generateTodaySignals method...");
//			LocalDate startDate = dateUtil.substractBusinessDays(
//					dateUtil.getLocalDate(), 20);
//			LocalDate businessDate = dateUtil.getLastBusinessDate();
//			// IntelliInvestDataDao.getInstance().getQuandlStockPriceMaxDate();
//			// TODO
//			List<StockSignalsDTO> stockSignalsDTOsList = new ArrayList<StockSignalsDTO>();
//			if ("ALL".equals(symbol)) {
//				Map<String, List<QuandlStockPrice>> quandlStockPrices = quandlEODStockPriceRepository
//						.getEODStockPricesFromStartDate(startDate);
//				Map<String, List<StockSignalsDTO>> signalComponents = stockSignalsRepository
//						.getStockSignalsFromStartDate(startDate, ma);
//				logger.debug("Quandl stock price list size is"
//						+ quandlStockPrices.size());
//				logger.debug("stock signal list size is"
//						+ signalComponents.size());
//				for (Stock stockDetailData : stockRepository.getStocks()) {
//					try {
//						if (quandlStockPrices.containsKey(stockDetailData
//								.getSecurityId())
//								&& signalComponents.containsKey(stockDetailData
//										.getSecurityId())) {
//							List<QuandlStockPrice> stockPrices = quandlStockPrices
//									.get(stockDetailData.getSecurityId());
//							// logger.info("Calculating signals for today for
//							// symbol "
//							// + stockDetailData.getSecurityId());
//							List<StockSignalsDTO> stockSignalsDTOs = signalComponents
//									.get(stockDetailData.getSecurityId());
//							if (stockSignalsDTOs != null
//									&& stockSignalsDTOs.size() > 0
//									&& !stockSignalsDTOs
//											.get(stockSignalsDTOs.size() - 1)
//											.getSignalDate()
//											.equals(businessDate))
//								stockSignalsDTOsList.add(getTodaysSignals(
//										stockDetailData.getSecurityId(), ma,
//										stockPrices, stockSignalsDTOs));
//							else {
//								if (stockSignalsDTOs == null
//										|| stockSignalsDTOs.size() == 0) {
//									logger.debug("Invalid data for stock:"
//											+ stockDetailData.getSecurityId());
//								} else
//									logger.debug("signal for today date for stock: "
//											+ stockDetailData.getSecurityId()
//											+ " already present...");
//							}
//							// stockSignalsRepository.deleteStockSignals(ma,
//							// stockDetailData.getSecurityId());
//						} else {
//							logger.info("not able to generate today signals for:"
//									+ stockDetailData.getSecurityId());
//							f = 0;
//						}
//					} catch (Exception e) {
//						logger.error("Error generating signal for "
//								+ stockDetailData.getSecurityId()
//								+ " with error " + e.getMessage(), e);
//						e.printStackTrace();
//						// return false;
//					}
//				}
//				stockSignalsRepository.refreshCache();
//			} else {
//				List<StockSignalsDTO> stockSignalsDTOs = stockSignalsRepository
//						.getStockSignalsFromStartDate(startDate, symbol, ma);
//				if (stockSignalsDTOs != null
//						&& stockSignalsDTOs.size() > 0
//						&& !stockSignalsDTOs.get(stockSignalsDTOs.size() - 1)
//								.getSignalDate().equals(businessDate)) {
//					logger.info("Calculating signals for today for symbol "
//							+ symbol);
//					List<QuandlStockPrice> quandlStockPrice = quandlEODStockPriceRepository
//							.getStockPricesFromDB(symbol);
//					// logger.debug("stock signals list size is" +
//					// stockSignalsDTO);
//					logger.debug("Quandl stock price list size is"
//							+ quandlStockPrice.size());
//					StockSignalsDTO signalComponentsList = getTodaysSignals(
//							symbol, ma, quandlStockPrice, stockSignalsDTOs);
//					// stockSignalsRepository.deleteStockSignals(ma, symbol);
//
//					stockSignalsRepository.refreshCache();
//					// watchListRepository.refreshCache();
//				} else {
//					if (stockSignalsDTOs == null
//							|| stockSignalsDTOs.size() == 0)
//						logger.info("not able to generate today signals for:"
//								+ symbol);
//					else {
//						logger.debug("signal for today date for stock: "
//								+ symbol + " already present...");
//					}
//					f = 0;
//				}
//			}
//
//			stockSignalsRepository.updateStockSignals(ma,
//					stockSignalsDTOsList);
//			logger.debug("generate signal today method is going to end...");
//		} catch (Exception e) {
//			logger.info("Error generating signal for " + symbol
//					+ " with error " + e.getMessage(), e);
//			e.printStackTrace();
//			return false;
//		}
//		if (f == 1)
//			return true;
//		else
//			return false;
//	}
//
//	private StockSignalsDTO getTodaysSignals(String symbol, Integer ma,
//			List<QuandlStockPrice> quandlStockPrices,
//			List<StockSignalsDTO> stockSignalsDTOs) {
//		// List<StockSignalsDTO> stockSignalsDTOs = stockSignalsRepository
//		// .getStockSignalsFromStartDate(dateUtil
//		// .substractBusinessDays(dateUtil.getLocalDate(), 20),
//		// symbol, ma);
//		ArrayList<StockSignalsDTO> signalComponentsList = new ArrayList<StockSignalsDTO>();
//		Integer magicNumber = 45;
//		String magicNumberStr = null;
//		// magicNumberStr= IntelliInvestStore.getMagicNumber(ma, symbol);TODO
//		if (null != magicNumberStr) {
//			magicNumber = new Integer(magicNumberStr);
//		}
//		SignalComponentsEnhancer signalComponentsEnhancer = new SignalComponentsEnhancer(
//				ma);
//		logger.debug("Quandl Stock Price list size is:: "
//				+ quandlStockPrices.size());
//		StockSignalsDTO prevSignalComponents = stockSignalsDTOs
//				.get(stockSignalsDTOs.size() - 1);
//		logger.debug("Previous Stock Signal date:"
//				+ prevSignalComponents.getSignalDate() + " and list size is :"
//				+ stockSignalsDTOs.size());
//		StockSignalsDTO signalComponents = signalComponentsEnhancer.init(
//				magicNumber, quandlStockPrices, prevSignalComponents,
//				stockSignalsDTOs);
//		if (quandlStockPrices.size() > 50) {
//			int size = quandlStockPrices.size();
//			signalComponentsEnhancer.generateMovingAverageSignals(
//					quandlStockPrices.subList(size - 50, size),
//					signalComponents, prevSignalComponents);
//		} else {
//			signalComponentsEnhancer.generateMovingAverageSignals(
//					quandlStockPrices, signalComponents, prevSignalComponents);
//
//		}
//
//		signalComponentsEnhancer.generateAggregateSignals(signalComponents,
//				prevSignalComponents);
//		return signalComponents;
//	}
//
//}
