package com.intelliinvest.data.signals;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.dao.StockSignalsRepository;
import com.intelliinvest.data.dao.WatchListRepository;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.ScheduledThreadPoolHelper;
import com.intelliinvest.web.dto.StockSignalsDTO;

@ManagedResource(objectName = "bean:name=StockSignalsImporter", description = "StockSignalsImporter")
public class StockSignalsImporter {
	private Logger logger = Logger.getLogger(StockSignalsImporter.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private StockSignalsRepository stockSignalsRepository;

	@Autowired
	private DateUtil dateUtil;

	@Autowired
	WatchListRepository watchListRepository;

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
						boolean signalGeneratorResponse = generateSignalsForToday();
						if (signalGeneratorResponse) {
							watchListRepository.sendDailyTradingAccountUpdateMail();
						}
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

	private boolean generateSignalsForToday() {
		return generateTodaySignals("ALL");
	}

	public boolean generateSignals(String symbol) {
		Integer ma = new Integer(IntelliInvestStore.properties.get("ma").toString());
		return generateSignals(symbol, ma);
	}

	public boolean generateSignals(String symbol, int ma) {
		int f = 1;
		try {
			if ("ALL".equals(symbol)) {

				for (Stock stockDetailData : stockRepository.getStocks()) {
					try {
						logger.info("Calculating signals for symbol " + stockDetailData.getSecurityId());
						List<StockSignalsDTO> stockSignalsDTOs = getSignals(stockDetailData.getSecurityId(), ma);
						// stockSignalsRepository.deleteStockSignals(ma,
						// stockDetailData.getSecurityId());
						if (stockSignalsDTOs != null)
							stockSignalsRepository.updateStockSignals(ma, stockSignalsDTOs);
						else
							f = 0;
					} catch (Exception e) {
						logger.info("Error generating signals for " + stockDetailData.getSecurityId() + " with error "
								+ e.getMessage(), e);
						return false;
					}
				}
				// ChartDao.getInstance().insertSignals(ma, "ALL", null);
			} else {
				logger.info("Calculating signals for symbol " + symbol);
				List<StockSignalsDTO> stockSignalsDTOs = getSignals(symbol, ma);
				logger.info("signals generated for " + symbol);
				// stockSignalsRepository.deleteStockSignals(ma, symbol);
				if (stockSignalsDTOs != null)
					stockSignalsRepository.updateStockSignals(ma, stockSignalsDTOs);
				else
					f = 0;
				// ChartDao.getInstance().insertSignals(ma, symbol, null);
			}
		} catch (Exception e) {
			logger.info("Error generating signal for " + symbol + " with error " + e.getMessage(), e);
			return false;
		}
		if (f == 1)
			return true;
		else
			return false;
		// IntelliInvestStore.refresh();TODO
	}

	private List<StockSignalsDTO> getSignals(String stockCode, Integer ma) {
		Integer magicNumber = 45;
		// String magicNumberStr = IntelliInvestStore.getMagicNumber(ma,
		// stockCode);TODO
		// if(null!=magicNumberStr){
		// magicNumber = new Integer(magicNumberStr);
		// }
		List<QuandlStockPrice> quandlStockPrices = quandlEODStockPriceRepository.getStockPricesFromDB(stockCode);
		logger.debug("Quandl Stock Price list size is:" + quandlStockPrices.size());

		if (quandlStockPrices == null || quandlStockPrices.size() == 0) {
			return null;
		}
		QuandlStockPrice quandlStockPrice_1 = new QuandlStockPrice();
		int count = 0;
		List<StockSignalsDTO> signals = new ArrayList<StockSignalsDTO>();
		StockSignalsDTO prevSignalComponents = null;
		SignalComponentsEnhancer signalComponentsEnhancer = new SignalComponentsEnhancer(ma);
		for (QuandlStockPrice quandlStockPrice : quandlStockPrices) {
			// logger.debug("in generating signal for :" + stockCode
			// + " quandl eod date:" + quandlStockPrice.getEodDate());
			if (count != 0) {
				StockSignalsDTO signalComponents = null;
				if (count <= ma) {
					signalComponents = signalComponentsEnhancer.init9(quandlStockPrice, quandlStockPrice_1);
				} else if (count == (ma + 1)) {
					signalComponents = signalComponentsEnhancer.init10(magicNumber,
							quandlStockPrices.subList(count - ma + 1, count), signals);
				} else {
					signalComponents = signalComponentsEnhancer.init(magicNumber,
							quandlStockPrices.subList(count - ma + 1, count), prevSignalComponents, signals);
				}
				if (null != signalComponents) {
					signals.add(signalComponents);
					prevSignalComponents = signalComponents;
				}
			}
			count++;
			quandlStockPrice_1 = quandlStockPrice;
		}

		return signals;
	}

	public boolean generateTodaySignals(String symbol) {
		Integer ma = new Integer(IntelliInvestStore.properties.get("ma").toString());
		return generateTodaySignals(symbol, ma);
	}

	public boolean generateTodaySignals(String symbol, int ma) {
		int f = 1;
		try {
			logger.debug("in generateTodaySignals method...");
			LocalDate startDate = dateUtil.addBusinessDays(-20);
			LocalDate businessDate = dateUtil.getLastBusinessDate();
			// IntelliInvestDataDao.getInstance().getQuandlStockPriceMaxDate();
			// TODO
			if ("ALL".equals(symbol)) {
				Map<String, List<QuandlStockPrice>> quandlStockPrices = quandlEODStockPriceRepository
						.getEODStockPricesFromStartDate(startDate);
				Map<String, StockSignalsDTO> signalComponents = stockSignalsRepository
						.getStockSignalsComplete(businessDate, ma);
				logger.debug("Quandl stock price list size is" + quandlStockPrices.size());
				logger.debug("stock signal list size is" + signalComponents.size());
				for (Stock stockDetailData : stockRepository.getStocks()) {
					try {
						if (quandlStockPrices.containsKey(stockDetailData.getSecurityId())
								&& signalComponents.containsKey(stockDetailData.getSecurityId())) {
							List<QuandlStockPrice> stockPrices = quandlStockPrices.get(stockDetailData.getSecurityId());
							// logger.info("Calculating signals for today for
							// symbol "
							// + stockDetailData.getSecurityId());
							List<StockSignalsDTO> signalComponentsList = getTodaysSignals(
									stockDetailData.getSecurityId(), ma, stockPrices,
									signalComponents.get(stockDetailData.getSecurityId()));
							// stockSignalsRepository.deleteStockSignals(ma,
							// stockDetailData.getSecurityId());
							stockSignalsRepository.updateStockSignals(ma, signalComponentsList);
						} else {
							logger.info("not able to generate today signals for:" + symbol);
							f = 0;
						}
					} catch (Exception e) {
						logger.info("Error generating signal for " + stockDetailData.getSecurityId() + " with error "
								+ e.getMessage(), e);
						return false;
					}
				}
				// ChartDao.getInstance().insertSignals(ma, "ALL",
				// businessDate);
			} else {
				/*
				 * List<StockSignalsDTO> prevSignalComponents =
				 * stockSignalsRepository .getStockSignalsComplete(symbol, ma);
				 */
				StockSignalsDTO stockSignalsDTO = stockSignalsRepository.getStockSignalsComplete(businessDate, symbol,
						ma);
				if (stockSignalsDTO != null) {
					logger.info("Calculating signals for today for symbol " + symbol);
					List<QuandlStockPrice> quandlStockPrice = quandlEODStockPriceRepository
							.getStockPricesFromDB(symbol);
					logger.debug("stock signals list size is" + stockSignalsDTO);
					logger.debug("Quandl stock price list size is" + quandlStockPrice.size());
					List<StockSignalsDTO> signalComponentsList = getTodaysSignals(symbol, ma, quandlStockPrice,
							stockSignalsDTO);
					// stockSignalsRepository.deleteStockSignals(ma, symbol);
					stockSignalsRepository.updateStockSignals(ma, signalComponentsList);
				} else {
					logger.info("not able to generate today signals for:" + symbol);
					f = 0;
				}
				// ChartDao.getInstance().insertSignals(ma, symbol,
				// businessDate);
			}
		} catch (Exception e) {
			logger.info("Error generating signal for " + symbol + " with error " + e.getMessage(), e);
			return false;
		}
		if (f == 1)
			return true;
		else
			return false;
	}

	private List<StockSignalsDTO> getTodaysSignals(String symbol, Integer ma,
			List<QuandlStockPrice> quandlStockPricesTmp, StockSignalsDTO prevSignalComponents) {
		// List<QuandlStockPrice> QuandlStockPricesTmp =
		// IntelliInvestDataDao.getInstance().getQuandlStockPrice(symbol, ma-1);
		// List<QuandlStockPrice> quandlStockPrices = new
		// ArrayList<QuandlStockPrice>();
		// for (int i = (quandlStockPricesTmp.size() - 1); i >= 0; i--) {
		// quandlStockPrices.add(quandlStockPricesTmp.get(i));
		// }
		// List<StockSignalsDTO> prevSignalComponents =
		// stockSignalsRepository.getStockSignalsFromStartDate();
		List<StockSignalsDTO> stockSignalsDTOs = stockSignalsRepository
				.getEODStockPriceddFromStartDate(dateUtil.addBusinessDays(-20), symbol, ma);
		ArrayList<StockSignalsDTO> signalComponentsList = new ArrayList<StockSignalsDTO>();
		Integer magicNumber = 45;
		String magicNumberStr = null;
		// magicNumberStr= IntelliInvestStore.getMagicNumber(ma, symbol);TODO
		if (null != magicNumberStr) {
			magicNumber = new Integer(magicNumberStr);
		}
		// logger.debug("StockSignalsDTO list size is:" +
		// stockSignalsDTOs.size());
		// logger.debug("in gettoday Signal: "
		// + quandlStockPricesTmp.get(quandlStockPricesTmp.size() - 2)
		// .getEodDate());
		// logger.debug("in gettoday Signal: "
		// + quandlStockPricesTmp.get(quandlStockPricesTmp.size() - 1)
		// .getEodDate());
		SignalComponentsEnhancer signalComponentsEnhancer = new SignalComponentsEnhancer(ma);
		StockSignalsDTO signalComponents = signalComponentsEnhancer.init(magicNumber, quandlStockPricesTmp,
				prevSignalComponents, stockSignalsDTOs);
		// logger.info("Todays signal for " + signalComponents.getSymbol()
		// + " Signal type " + signalComponents.getSignalType()
		// + " signal present " + signalComponents.getSignalPresent());
		signalComponentsList.add(signalComponents);
		return signalComponentsList;
	}

}
