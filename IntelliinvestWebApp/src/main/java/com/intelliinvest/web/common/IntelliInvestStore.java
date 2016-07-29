package com.intelliinvest.web.common;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.intelliinvest.data.model.NSEtoBSEMap;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockDetailStaticHolder;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.web.dao.StockRepository;
import com.intelliinvest.web.util.DateUtil;
import com.intelliinvest.web.util.Helper;
import com.intelliinvest.web.util.HttpUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class IntelliInvestStore {
	private static Logger logger = Logger.getLogger(IntelliInvestStore.class);
	@Autowired
	private StockRepository stockRepository;
	final static String GOOGLE_QUOTE_URL = "https://www.google.com/finance/info?q=#CODE#";
//	final static String GOOGLE_REALTIME_QUOTE_URL = "http://www.google.com/finance/getprices?q=#CODE#&x=#EXCHANGE#&i=120&p=1d&f=d,c,v&df=cpct";
	private static boolean REFRESH_PERIODICALLY = false;
	public static Properties properties = null;
	private ScheduledExecutorService scheduledPool = null;

	@PostConstruct
	public void init() {
		loadProperties();
		if (enablePeriodicRefresh()) {
			REFRESH_PERIODICALLY = true;
			logger.info("Setting REFRESH_PERIODICALLY to true inside init()");
		}
		scheduledPool = Executors.newScheduledThreadPool(5);
		initializeScheduledTasks();
		initialiseCacheFromDB();
	}

	@PreDestroy
	public void destroy() {
		scheduledPool.shutdown();
		try {
			scheduledPool.awaitTermination(1, TimeUnit.MINUTES);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public boolean enablePeriodicRefresh() {
		boolean enable = false;
		ZoneId zoneId = DateUtil.ZONE_ID;
		ZonedDateTime zonedNow = ZonedDateTime.now(zoneId);	
		DayOfWeek dayOfWeek = zonedNow.getDayOfWeek();
		int hour = zonedNow.getHour();
		int periodicRefreshStartHour = new Integer(properties.getProperty("periodic.refresh.start.hr"));
		int periodicRefreshEndHour = new Integer(properties.getProperty("periodic.refresh.end.hr"));
		if (!dayOfWeek.equals(DayOfWeek.SATURDAY) && !dayOfWeek.equals(DayOfWeek.SUNDAY) && hour >= periodicRefreshStartHour
				&& hour <= periodicRefreshEndHour) {
			enable = true;
		}
		return enable;
	}

	private void initializeScheduledTasks() {
		Runnable refreshCurrentPricesTask = new Runnable() {
			@Override
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
					logger.info("refreshing of all price data for IntelliInvest is disabled");
				}
			}
		};

		Runnable refreshCacheFromDBTask = new Runnable() {
			@Override
			public void run() {
				if (REFRESH_PERIODICALLY) {
					logger.info("refreshing IntelliInvest data from DB");
					try {
						refreshCacheFromDB();
					} catch (Exception e) {
						logger.error("Error refreshing IntelliInvest data from DB " + e.getMessage());
					}
				} else {
					logger.info("refreshing of all data for IntelliInvest is disabled");
				}
			}
		};

		Runnable enablePeriodicRefreshTask = new Runnable() {
			@Override
			public void run() {
				if (enablePeriodicRefresh()) {
					REFRESH_PERIODICALLY = true;
					logger.info("Setting REFRESH_PERIODICALLY to true inside enablePeriodicRefreshTask");
				}
			}
		};

		Runnable disablePeriodicRefreshTask = new Runnable() {
			@Override
			public void run() {
				if (!enablePeriodicRefresh()) {
					REFRESH_PERIODICALLY = false;
					logger.info("Setting REFRESH_PERIODICALLY to false inside disablePeriodicRefreshTask");
				}
			}
		};

		Runnable refreshEODPricesTask = new Runnable() {
			@Override
			public void run() {
				ZoneId zoneId = DateUtil.ZONE_ID;
				ZonedDateTime zonedNow = ZonedDateTime.now(zoneId);	
				DayOfWeek dayOfWeek = zonedNow.getDayOfWeek();
				if (!dayOfWeek.equals(DayOfWeek.SATURDAY) && !dayOfWeek.equals(DayOfWeek.SUNDAY)) {
					try {
						updateEODNonWorldPrices();
					} catch (Exception e) {
						logger.error("Error refreshing EOD price data for stocks and world indexes " + e.getMessage());
					}
				}
			}
		};

		int priceRefreshInitialDelay = new Integer(properties.getProperty("price.refresh.initial.delay"));
		int priceRefreshPeriod = new Integer(properties.getProperty("price.refresh.period"));

		scheduledPool.scheduleAtFixedRate(refreshCurrentPricesTask, priceRefreshInitialDelay, priceRefreshPeriod,
				TimeUnit.SECONDS);
		logger.info("Scheduled refreshCurrentPricesTask for periodic price refresh");

		int cacheRefreshInitialDelay = new Integer(properties.getProperty("cache.refresh.initial.delay"));
		int cacheRefreshPeriod = new Integer(properties.getProperty("cache.refresh.period"));

		scheduledPool.scheduleAtFixedRate(refreshCacheFromDBTask, cacheRefreshInitialDelay, cacheRefreshPeriod,
				TimeUnit.SECONDS);
		logger.info("Scheduled refreshCacheFromDBTask for periodic price refresh");

		int periodicRefreshStartHour = new Integer(properties.getProperty("periodic.refresh.start.hr"));
		int periodicRefreshStartMin = new Integer(properties.getProperty("periodic.refresh.start.min"));

		ZoneId zoneId = DateUtil.ZONE_ID;
		ZonedDateTime zonedNow = ZonedDateTime.now(zoneId);

		ZonedDateTime zonedNext9 = zonedNow.withHour(periodicRefreshStartHour).withMinute(periodicRefreshStartMin)
				.withSecond(0);
		
		if (zonedNow.compareTo(zonedNext9) > 0) {
			zonedNext9 = zonedNext9.plusDays(1);
		}

		Duration duration9 = Duration.between(zonedNow, zonedNext9);
		long initialDelay9 = duration9.getSeconds();
		scheduledPool.scheduleAtFixedRate(enablePeriodicRefreshTask, initialDelay9, 24 * 60 * 60, TimeUnit.SECONDS);
		logger.info("Scheduled enablePeriodicRefreshTask for periodic price refresh");

		int periodicRefreshEndHour = new Integer(properties.getProperty("periodic.refresh.end.hr"));
		int periodicRefreshEndMin = new Integer(properties.getProperty("periodic.refresh.end.min"));

		ZonedDateTime zonedNext16 = zonedNow.withHour(periodicRefreshEndHour).withMinute(periodicRefreshEndMin)
				.withSecond(0);

		if (zonedNow.compareTo(zonedNext16) > 0) {
			zonedNext16 = zonedNext16.plusDays(1);
		}

		Duration duration16 = Duration.between(zonedNow, zonedNext16);
		long initialDelay16 = duration16.getSeconds();
		scheduledPool.scheduleAtFixedRate(disablePeriodicRefreshTask, initialDelay16, 24 * 60 * 60, TimeUnit.SECONDS);
		logger.info("Scheduled disablePeriodicRefreshTask for periodic price refresh");

		int periodicEODPriceRefreshStartHour = new Integer(
				properties.getProperty("periodic.eod.price.refresh.start.hr"));
		int periodicEODPriceRefreshStartMin = new Integer(
				properties.getProperty("periodic.eod.price.refresh.start.min"));

		ZonedDateTime zonedNext21 = zonedNow.withHour(periodicEODPriceRefreshStartHour)
				.withMinute(periodicEODPriceRefreshStartMin).withSecond(0);

		if (zonedNow.compareTo(zonedNext21) > 0) {
			zonedNext21 = zonedNext21.plusDays(1);
		}

		Duration duration21 = Duration.between(zonedNow, zonedNext21);
		long initialDelay21 = duration21.getSeconds();
		scheduledPool.scheduleAtFixedRate(refreshEODPricesTask, initialDelay21, 24 * 60 * 60, TimeUnit.SECONDS);
		
//		scheduledPool.scheduleAtFixedRate(refreshEODPricesTask, 20, 300, TimeUnit.SECONDS);
		logger.info("Scheduled refreshEODPricesTask for periodic eod price refresh");
	}

	public void initialiseCacheFromDB() {
		refreshCacheFromDB();
		logger.info("Initialised IntelliInvestStore");
	}

	private static void loadProperties() {
		try {
			logger.info("Loading property file for intelliinvest");
			properties = new Properties();
			properties.load(IntelliInvestStore.class.getResourceAsStream("/intelliinvest.properties"));
			logger.info("Loaded property file " + properties);
			if (StockDetailStaticHolder.QUANDL_STOCK_CODES_MAPPING.size() == 0) {
				String mapping_str = properties.getProperty("quandl_special_chracteres_mapping");
				String mappings[] = mapping_str.split("#");
				for (String mapping : mappings) {
					String[] key_value = mapping.split(":");
					if (key_value.length > 1)
						StockDetailStaticHolder.QUANDL_STOCK_CODES_MAPPING.put(key_value[0], key_value[1]);
					else
						StockDetailStaticHolder.QUANDL_STOCK_CODES_MAPPING.put(key_value[0], "");
				}
				logger.info(StockDetailStaticHolder.QUANDL_STOCK_CODES_MAPPING.toString());
			}
		} catch (Exception e) {
			logger.error("Error loading properties  " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public void refreshCacheFromDB() {
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
		
		List<StockPrice> allStockPrices = stockRepository.getStockPrices();
		List<NSEtoBSEMap> nseToBseMap = stockRepository.getNSEtoBSEMap();

		StockDetailStaticHolder.nonWorldStockDetailsMap = getStockDetailMapFromList(nonWorldStocks);
		StockDetailStaticHolder.worldStockDetailsMap = getStockDetailMapFromList(worldStocks);

		if (Helper.isNotNullAndNonEmpty(allStockPrices)) {
			StockDetailStaticHolder.nonWorldStockPriceMap = getNonWorldStockPriceMapFromList(allStockPrices);
			StockDetailStaticHolder.worldStockPriceMap = getWorldStockPriceMapFromList(allStockPrices);
			StockDetailStaticHolder.NIFTYStockPriceMap = getNiftyStockPriceMapFromList(allStockPrices);
		}

		if (nseToBseMap != null && !nseToBseMap.isEmpty()) {
			StockDetailStaticHolder.NSEToBSEMap = getMapFromList(nseToBseMap);
			StockDetailStaticHolder.BSEToNSEMap = getMapFromListReverse(nseToBseMap);
		}
		logger.info("refreshed IntelliInvestStore data from DB");
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
		logger.debug("From updateCurrentNonWorldPrices()");
		for(StockPrice price: nonWorldPrices){		
			logger.debug(price.toString());
		}
		
		if (Helper.isNotNullAndNonEmpty(nonWorldPrices)) {
			nonWorldPrices = stockRepository.updateCurrentStockPrices(nonWorldPrices);
			StockDetailStaticHolder.nonWorldStockPriceMap = getStockPriceMapFromList(nonWorldPrices);
		}
		List<StockPrice> worldPrices = getCurrentWorldStockPrices(worldStocks);
		logger.debug("From updateCurrentWorldPrices()");
		for(StockPrice price: worldPrices){		
			logger.debug(price.toString());
		}
		if (Helper.isNotNullAndNonEmpty(worldPrices)) {
			worldPrices = stockRepository.updateCurrentStockPrices(worldPrices);
			StockDetailStaticHolder.worldStockPriceMap = getStockPriceMapFromList(worldPrices);
		}
	}

	public void updateEODNonWorldPrices() throws Exception {
		List<Stock> stockDetails = stockRepository.getStocks();
		List<Stock> nonWorldStocks = new ArrayList<Stock>();
		for (Stock stock : stockDetails) {
			if (!stock.isWorldStock()) {
				nonWorldStocks.add(stock);
			}
		}
		List<StockPrice> nonWorldEODPrices = getEODNonWorldStockPrices(nonWorldStocks);
		logger.debug("From updateEODNonWorldPrices()");
		for(StockPrice price: nonWorldEODPrices){		
			logger.debug(price.toString());
		}
		if (Helper.isNotNullAndNonEmpty(nonWorldEODPrices)) {
			nonWorldEODPrices = stockRepository.updateEODStockPrices(nonWorldEODPrices);
			StockDetailStaticHolder.nonWorldStockPriceMap = getStockPriceMapFromList(nonWorldEODPrices);
		}
	}

	public List<StockPrice> getCurrentNonWorldStockPrices(List<Stock> nonWorldStocks) {
		StockDetailStaticHolder.BOMStocksSetTemp = new HashSet<String>();
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
		StockDetailStaticHolder.BOMStocksSet = StockDetailStaticHolder.BOMStocksSetTemp;
		logger.info("Stocks added for BOMStocksSet " + StockDetailStaticHolder.BOMStocksSet + " ");
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
			if(!codes.isEmpty()){
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
						stockCurrentPriceList.add(new StockPrice(stockCode, cp, price, 0, null, new Date()));
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

	private List<StockPrice> getPriceFromJSON(String exchange, String codes, String response) {
		List<StockPrice> stockCurrentPriceList = new ArrayList<StockPrice>();
		SimpleDateFormat format = new SimpleDateFormat("MMM dd, hh:mmaa z");
		JSONArray jsonArray = JSONArray.fromObject(response.replaceFirst("//", "").trim());
		try {
			Calendar currentCal = Calendar.getInstance();
			currentCal.setTime(format.parse(format.format(new Date())));
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
					if (!exchange.equals("NSE")) {
						code = StockDetailStaticHolder.BSEToNSEMap.get(code);
						StockDetailStaticHolder.BOMStocksSetTemp.add(code);
					}
					stockCurrentPriceList.add(new StockPrice(code, cp, price, 0, null, ltDate));
				} catch (Exception e1) {
					if (exchange.equals("NSE")) {
						if (StockDetailStaticHolder.NSEToBSEMap.containsKey(code)) {
							String bseCode = StockDetailStaticHolder.NSEToBSEMap.get(code);
							// logger.info("Error fetching stock price from NSE.
							// Trying to get from BOM for code " + code + " -> "
							// + bseCode );
							response = HttpUtil.getFromHttpUrlAsString(
									GOOGLE_QUOTE_URL.replace("#CODE#", "BOM:" + bseCode.replace("&", "%26")));
							stockCurrentPriceList.addAll(getPriceFromJSON("BOM", code, response));
						}
					} else {
						logger.info("Error fetching data from " + exchange + " for code " + code);
					}
				}
			}
		} catch (Exception e) {
			logger.info("Error fetching stock price for " + codes);
		}
		return stockCurrentPriceList;
	}

	private List<StockPrice> getEODNonWorldStockPrices(List<Stock> nonWorldStocks) throws Exception {
		List<StockPrice> stockEODPriceList = new ArrayList<StockPrice>();
		Date currentDate = DateUtil.getCurrentDate();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		for (Stock stock : nonWorldStocks) {
			String eodPricesAsString = getDataFromQuandl(stock.getCode(), currentDate, currentDate);
			if (!Helper.isNotNullAndNonEmpty(eodPricesAsString)) {
				throw new IntelliinvestException("Error while fetching EOD prices for date: " + currentDate
						+ " for Stock: " + stock.getCode());
			}
			String[] eodPricesAsArray = eodPricesAsString.split("\n");
			String eodPriceAsString = eodPricesAsArray[1];
			String[] eodPriceAsArray = eodPriceAsString.split(",");
			if (eodPriceAsArray.length >= 8) {
				StockPrice stockPrice = new StockPrice();
				stockPrice.setCode(stock.getCode());
				stockPrice.setEodDate(format.parse(eodPriceAsArray[0]));
				stockPrice.setEodPrice(new Double(eodPriceAsArray[5]));
				stockEODPriceList.add(stockPrice);
			} else {
				throw new IntelliinvestException("Error while fetching EOD prices for date: " + currentDate
						+ " for Stock: " + stock.getCode());
			}
		}
		return stockEODPriceList;
	}

	private Map<String, Stock> getStockDetailMapFromList(List<Stock> datas) {
		Map<String, Stock> dataMap = new HashMap<String, Stock>();
		for (Stock data : datas) {
			dataMap.put(data.getCode(), data);
		}
		return dataMap;
	}

	private Map<String, StockPrice> getStockPriceMapFromList(List<StockPrice> datas) {
		Map<String, StockPrice> dataMap = new HashMap<String, StockPrice>();
		for (StockPrice data : datas) {
			dataMap.put(data.getCode(), data);
		}
		return dataMap;
	}

	private Map<String, StockPrice> getNonWorldStockPriceMapFromList(List<StockPrice> datas) {
		Map<String, StockPrice> dataMap = new HashMap<String, StockPrice>();
		for (StockPrice data : datas) {
			if (!isWorldStock(data.getCode())) {
				dataMap.put(data.getCode(), data);
			}
		}
		return dataMap;
	}

	private Map<String, StockPrice> getWorldStockPriceMapFromList(List<StockPrice> datas) {
		Map<String, StockPrice> dataMap = new HashMap<String, StockPrice>();
		for (StockPrice data : datas) {
			if (isWorldStock(data.getCode())) {
				dataMap.put(data.getCode(), data);
			}
		}
		return dataMap;
	}

	private Map<String, StockPrice> getNiftyStockPriceMapFromList(List<StockPrice> datas) {
		Map<String, StockPrice> dataMap = new HashMap<String, StockPrice>();
		for (StockPrice data : datas) {
			if (isNiftyStock(data.getCode())) {
				dataMap.put(data.getCode(), data);
			}
		}
		return dataMap;
	}

	private boolean isWorldStock(String code) {
		boolean retVal = false;
		Stock stock = StockDetailStaticHolder.worldStockDetailsMap.get(code);
		if (stock != null && stock.isWorldStock()) {
			retVal = true;
		}
		return retVal;
	}

	private boolean isNiftyStock(String code) {
		boolean retVal = false;
		Stock stock = StockDetailStaticHolder.nonWorldStockDetailsMap.get(code);
		if (stock != null && stock.isNiftyStock()) {
			retVal = true;
		}
		return retVal;
	}

	private Map<String, String> getMapFromList(List<NSEtoBSEMap> datas) {
		Map<String, String> map = new HashMap<String, String>();
		for (NSEtoBSEMap data : datas) {
			map.put(data.getNseCode(), data.getBseCode());
		}
		return map;
	}

	private Map<String, String> getMapFromListReverse(List<NSEtoBSEMap> datas) {
		Map<String, String> map = new HashMap<String, String>();
		for (NSEtoBSEMap data : datas) {
			map.put(data.getBseCode(), data.getNseCode());
		}
		return map;
	}

	private String getDataFromQuandl(String stock_code, Date startDate, Date endDate) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		stock_code = getQuandlStockCode(stock_code);
		// link="https://www.quandl.com/api/v3/datasets/NSE/SUVEN.csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=2016-05-01&end_date=2016-05-24";
		String link = "https://www.quandl.com/api/v3/datasets/NSE/" + stock_code
				+ ".csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=" + format.format(startDate) + "&end_date="
				+ format.format(endDate);
		/*String link = "https://www.quandl.com/api/v3/datasets/NSE/" + stock_code
				+ ".csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=" + "2016-07-28" + "&end_date="
				+ "2016-07-28";*/
		try {
			logger.debug("Sending Quandl Request:"+ link);	
			byte b[] = HttpUtil.getFromUrlAsBytes(link);
			return new String(b);
		} catch (Exception e) {
			logger.info("Error while fetching Quandl data from Start Date: " + startDate + "to End date: "
					+ endDate + " for Stock: " + stock_code + " Error " + e.getMessage());
		}
		return null;
	}

	private String getQuandlStockCode(String stock_code) {
		Iterator<String> itr = StockDetailStaticHolder.QUANDL_STOCK_CODES_MAPPING.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value = StockDetailStaticHolder.QUANDL_STOCK_CODES_MAPPING.get(key);
			stock_code = stock_code.replaceAll(key, value);
		}
		return stock_code;
	}
}
