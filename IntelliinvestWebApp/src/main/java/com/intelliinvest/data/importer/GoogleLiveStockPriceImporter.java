package com.intelliinvest.data.importer;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.HttpUtil;
import com.intelliinvest.util.ScheduledThreadPoolHelper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@ManagedResource(objectName = "bean:name=GoogleLiveStockPriceImporter", description = "GoogleLiveStockPriceImporter")
public class GoogleLiveStockPriceImporter {
	private static Logger logger = Logger.getLogger(GoogleLiveStockPriceImporter.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private IntelliInvestStore intelliinvestStore;
	private final static String GOOGLE_QUOTE_URL = "https://www.google.com/finance/info?q=#CODE#";
	private static boolean REFRESH_PERIODICALLY = false;
	private ZoneId zoneId = DateUtil.ZONE_ID;

	@PostConstruct
	public void init() {
		if (enablePeriodicRefresh()) {
			REFRESH_PERIODICALLY = true;
			logger.info("Setting REFRESH_PERIODICALLY to true inside init()");
		}
		initializeScheduledTasks();
	}

	public boolean enablePeriodicRefresh() {
		boolean enable = false;
		ZonedDateTime zonedNow = ZonedDateTime.now(zoneId);
		int hour = zonedNow.getHour();
		int periodicRefreshStartHour = new Integer(IntelliInvestStore.properties.getProperty("periodic.refresh.start.hr"));
		int periodicRefreshEndHour = new Integer(IntelliInvestStore.properties.getProperty("periodic.refresh.end.hr"));
		
		if (!DateUtil.isBankHoliday(DateUtil.getCurrentDate()) && hour >= periodicRefreshStartHour && hour <= periodicRefreshEndHour) {
			enable = true;
		}
		return enable;
	}

	private void initializeScheduledTasks() {
		Runnable refreshCurrentPricesTask = new Runnable() {
			public void run() {
				if (REFRESH_PERIODICALLY) {
					logger.info("refreshing current price data for stocks and world indexes");
					try {
						updateCurrentPrices();
					} catch (Exception e) {
						logger.error(
								"Error refreshing current price data for stocks and world indexes " + e.getMessage());
					}
				} else {
					logger.info("refreshing of live price data for IntelliInvest is disabled");
				}
			}
		};

		Runnable enablePeriodicRefreshTask = new Runnable() {
			public void run() {
				if (enablePeriodicRefresh()) {
					REFRESH_PERIODICALLY = true;
					logger.info("Setting REFRESH_PERIODICALLY to true inside enablePeriodicRefreshTask");
				}
			}
		};
		Runnable disablePeriodicRefreshTask = new Runnable() {
			public void run() {
				if (!enablePeriodicRefresh()) {
					REFRESH_PERIODICALLY = false;
					logger.info("Setting REFRESH_PERIODICALLY to false inside disablePeriodicRefreshTask");
				}
			}
		};

		int priceRefreshInitialDelay = new Integer(
				IntelliInvestStore.properties.getProperty("price.refresh.initial.delay"));
		int priceRefreshPeriod = new Integer(IntelliInvestStore.properties.getProperty("price.refresh.period"));

		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(refreshCurrentPricesTask,
				priceRefreshInitialDelay, priceRefreshPeriod, TimeUnit.SECONDS);
		logger.info("Scheduled refreshCurrentPricesTask for periodic price refresh");

		int periodicRefreshStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("periodic.refresh.start.hr"));
		int periodicRefreshStartMin = new Integer(
				IntelliInvestStore.properties.getProperty("periodic.refresh.start.min"));

		ZonedDateTime zonedNow = ZonedDateTime.now(zoneId);
		ZonedDateTime zonedNext9 = zonedNow.withHour(periodicRefreshStartHour).withMinute(periodicRefreshStartMin)
				.withSecond(0);

		if (zonedNow.compareTo(zonedNext9) > 0) {
			zonedNext9 = zonedNext9.plusDays(1);
		}

		Duration duration9 = Duration.between(zonedNow, zonedNext9);
		long initialDelay9 = duration9.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(enablePeriodicRefreshTask,
				initialDelay9, 24 * 60 * 60, TimeUnit.SECONDS);
		logger.info("Scheduled enablePeriodicRefreshTask for periodic price refresh");
		int periodicRefreshEndHour = new Integer(IntelliInvestStore.properties.getProperty("periodic.refresh.end.hr"));
		int periodicRefreshEndMin = new Integer(IntelliInvestStore.properties.getProperty("periodic.refresh.end.min"));
		ZonedDateTime zonedNext16 = zonedNow.withHour(periodicRefreshEndHour).withMinute(periodicRefreshEndMin)
				.withSecond(0);
		if (zonedNow.compareTo(zonedNext16) > 0) {
			zonedNext16 = zonedNext16.plusDays(1);
		}
		Duration duration16 = Duration.between(zonedNow, zonedNext16);
		long initialDelay16 = duration16.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(disablePeriodicRefreshTask,
				initialDelay16, 24 * 60 * 60, TimeUnit.SECONDS);
		logger.info("Scheduled disablePeriodicRefreshTask for periodic price refresh");
	}

	public void updateCurrentPrices() {
		List<Stock> stockDetails = stockRepository.getStocks();
		List<Stock> nonWorldStocks = new ArrayList<Stock>();
		List<Stock> worldStocks = new ArrayList<Stock>();
		for (Stock stock : stockDetails) {
			if (stock.isWorldStock()) {
				worldStocks.add(stock);
			} else {
				nonWorldStocks.add(stock);
			}
		}
		List<StockPrice> nonWorldPrices = getCurrentNonWorldStockPrices(nonWorldStocks);
		if (Helper.isNotNullAndNonEmpty(nonWorldPrices)) {
			nonWorldPrices = stockRepository.updateCurrentStockPrices(nonWorldPrices);
		}
		List<StockPrice> worldPrices = getCurrentWorldStockPrices(worldStocks);
		if (Helper.isNotNullAndNonEmpty(worldPrices)) {
			worldPrices = stockRepository.updateCurrentStockPrices(worldPrices);
		}
	}

	public List<StockPrice> getCurrentNonWorldStockPrices(List<Stock> nonWorldStocks) {
		ArrayList<StockPrice> stockCurrentPriceList = new ArrayList<StockPrice>();
		int start = -10;
		int end = 0;
		while (end < nonWorldStocks.size()) {
			start = start + 10;
			end = end + 10;
			if (end > nonWorldStocks.size()) {
				end = nonWorldStocks.size();
			}
			stockCurrentPriceList.addAll(getCurrentNonWorldStockPricesForSubList(nonWorldStocks.subList(start, end)));
		}
		return stockCurrentPriceList;
	}

	public List<StockPrice> getCurrentNonWorldStockPricesForSubList(List<Stock> stockDetailDatas) {
		List<StockPrice> stockCurrentPriceList = new ArrayList<StockPrice>();
		String codes = "";
		try {
			for (Stock stockDetailData : stockDetailDatas) {
				String code = stockDetailData.getCode();
				code = "NSE:" + stockDetailData.getCode();
				codes = codes + code + ",";
			}
			if (!codes.isEmpty()) {
				codes = codes.substring(0, codes.lastIndexOf(","));
			}

			String response = HttpUtil
					.getFromHttpUrlAsString(GOOGLE_QUOTE_URL.replace("#CODE#", codes.replace("&", "%26")));
			stockCurrentPriceList.addAll(getPriceFromJSON("NSE", codes, response));
		} catch (Exception e) {
			logger.error("Error fetching stock price in getStockPrice " + codes);
			logger.error(e.getMessage());
		}
		return stockCurrentPriceList;
	}

	private List<StockPrice> getPriceFromJSON(String exchange, String codes, String response) {
		List<StockPrice> stockCurrentPriceList = new ArrayList<StockPrice>();
		SimpleDateFormat format = new SimpleDateFormat("MMM dd, hh:mmaa z");
		JSONArray jsonArray = JSONArray.fromObject(response.replaceFirst("//", "").trim());
		try {
			Calendar currentCal = Calendar.getInstance();
			currentCal.setTime(format.parse(format.format(DateUtil.getCurrentDate())));
			currentCal.add(Calendar.MONTH, -1);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject stockObject = (JSONObject) jsonArray.get(i);
				String code = stockObject.getString("t").replace("\\x26", "&");
				try {
					Double price = new Double(stockObject.getString("l_fix").replaceAll(",", ""));
					Double cp = new Double(stockObject.getString("cp").replaceAll(",", ""));
					String lt = stockObject.getString("lt");
					Date ltDate = format.parse(lt);
					if (currentCal.getTime().compareTo(ltDate) > 0) {
						throw new RuntimeException("Stale details for " + code);
					}
					if ("BOM".equals(exchange)) {
						String bseCode = code;
						code = intelliinvestStore.getNSECode(bseCode);
						// logger.info("getNSECode: from bseCode:" + bseCode + " to nseCode:" + code);
					}
					// logger.info("Adding stock price for code:" + code + " and exchange:" + exchange);
					stockCurrentPriceList.add(new StockPrice(code, cp, price, 0, null, ltDate));
				} catch (Exception e1) {
					if (exchange.equals("NSE")) {
						// logger.error("Error fetching stock price from " +
						// exchange + " for " + code + ". Trying from BOM now");
						String bseCode = intelliinvestStore.getBSECode(code);
						if (bseCode != null) {
							response = HttpUtil.getFromHttpUrlAsString(
									GOOGLE_QUOTE_URL.replace("#CODE#", "BOM:" + bseCode.replace("&", "%26")));
							stockCurrentPriceList.addAll(getPriceFromJSON("BOM", code, response));
						} else {
							// logger.error("bseToNSse mapping not found for " + code);
						}
					} else {
						// logger.error("Error fetching stock price from " + exchange + " for code " + code);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error fetching stock price for " + codes);
		}

		return stockCurrentPriceList;
	}

	public List<StockPrice> getCurrentWorldStockPrices(List<Stock> worldStocks) {
		List<StockPrice> stockCurrentPriceList = new ArrayList<StockPrice>();
		try {
			for (Stock stock : worldStocks) {
				String stockCode = stock.getCode();
				String response = HttpUtil
						.getFromHttpUrlAsString(GOOGLE_QUOTE_URL.replace("#CODE#", stockCode.replace("&", "%26")));
				JSONArray jsonArray = JSONArray.fromObject(response.replaceFirst("//", "").trim());
				for (int j = 0; j < jsonArray.size(); j++) {
					try {
						JSONObject stockObject = (JSONObject) jsonArray.get(j);
						Double price = new Double(stockObject.getString("l_fix").replaceAll(",", ""));
						Double cp = new Double(stockObject.getString("cp").replaceAll(",", ""));
						stockCurrentPriceList
								.add(new StockPrice(stockCode, cp, price, 0, null, DateUtil.getCurrentDate()));
					} catch (Exception e) {
						logger.error("Error fetching stock price for " + stockCode);
						logger.error(e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error fetching World stock prices");
		}
		return stockCurrentPriceList;
	}

	@ManagedOperation(description = "backLoadLivePrices")
	public String backLoadLivePrices() {
		logger.info("Inside backLoadLivePrices...");
		try {
			List<Stock> stockDetails = stockRepository.getStocks();
			List<Stock> nonWorldStocks = new ArrayList<Stock>();
			List<Stock> worldStocks = new ArrayList<Stock>();
			for (Stock stock : stockDetails) {
				if (stock.isWorldStock()) {
					worldStocks.add(stock);
				} else {
					nonWorldStocks.add(stock);
				}
			}
			List<StockPrice> nonWorldPrices = getCurrentNonWorldStockPrices(nonWorldStocks);
			if (Helper.isNotNullAndNonEmpty(nonWorldPrices)) {
				nonWorldPrices = stockRepository.updateCurrentStockPrices(nonWorldPrices);
			}
			List<StockPrice> worldPrices = getCurrentWorldStockPrices(worldStocks);
			if (Helper.isNotNullAndNonEmpty(worldPrices)) {
				worldPrices = stockRepository.updateCurrentStockPrices(worldPrices);
			}
		} catch (Exception e) {
			return e.getMessage();
		}

		return "Success";
	}
}