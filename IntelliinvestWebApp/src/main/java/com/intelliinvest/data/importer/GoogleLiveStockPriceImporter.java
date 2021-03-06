package com.intelliinvest.data.importer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.PriceVolumeData;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.HttpUtil;
import com.intelliinvest.util.ScheduledThreadPoolHelper;
import com.intelliinvest.web.bo.response.StockPriceTimeSeriesResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@ManagedResource(objectName = "bean:name=GoogleLiveStockPriceImporter", description = "GoogleLiveStockPriceImporter")
public class GoogleLiveStockPriceImporter {
	private static Logger logger = Logger.getLogger(GoogleLiveStockPriceImporter.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private DateUtil dateUtil;
	private final static String GOOGLE_QUOTE_URL = "https://www.google.com/finance/info?q=#CODE#";
	private final static String GOOGLE_REALTIME_QUOTE_URL = "http://www.google.com/finance/getprices?q=#CODE#&x=#EXCHANGE#&i=#INTERVAL#&p=#PERIOD#&f=d,c,v&df=cpct";
	private final static String GOOGLE_REALTIME_FULL_QUOTE_URL = "http://www.google.com/finance/getprices?q=#CODE#&x=#EXCHANGE#&i=#INTERVAL#&p=#PERIOD#&f=d,c,v,o,h,l&df=cpct";
	
	private static boolean REFRESH_PERIODICALLY = false;

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
		LocalDateTime localDateTime = dateUtil.getLocalDateTime();		
		int hour = localDateTime.getHour();
		int periodicRefreshStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("periodic.refresh.start.hr"));
		int periodicRefreshEndHour = new Integer(IntelliInvestStore.properties.getProperty("periodic.refresh.end.hr"));

		logger.info("Inside enablePeriodicRefresh():localDateTime= "+localDateTime);
		logger.info("Inside enablePeriodicRefresh():localDateTime.getHour()= "+hour);
		logger.info("Inside enablePeriodicRefresh():periodicRefreshStartHour= "+periodicRefreshStartHour);
		logger.info("Inside enablePeriodicRefresh():periodicRefreshEndHour= "+periodicRefreshEndHour);
		if (!dateUtil.isBankHoliday(dateUtil.getLocalDate()) && hour >= periodicRefreshStartHour
				&& hour <= periodicRefreshEndHour) {
			enable = true;
		}
		logger.info("Inside enablePeriodicRefresh():enable= "+enable);
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

		LocalDateTime timeNow = dateUtil.getLocalDateTime();
		LocalDateTime timeStart = timeNow.withHour(periodicRefreshStartHour).withMinute(periodicRefreshStartMin)
				.withSecond(0);

		if (timeNow.compareTo(timeStart) > 0) {
			timeStart = timeStart.plusDays(1);
		}

		Duration durationStart = Duration.between(timeNow, timeStart);
		long initialDelayStart = durationStart.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(enablePeriodicRefreshTask,
				initialDelayStart, 24 * 60 * 60, TimeUnit.SECONDS);
		logger.info("Scheduled enablePeriodicRefreshTask for periodic price refresh at " + timeStart);
		int periodicRefreshEndHour = new Integer(IntelliInvestStore.properties.getProperty("periodic.refresh.end.hr"));
		int periodicRefreshEndMin = new Integer(IntelliInvestStore.properties.getProperty("periodic.refresh.end.min"));
		LocalDateTime timeEnd = timeNow.withHour(periodicRefreshEndHour).withMinute(periodicRefreshEndMin)
				.withSecond(0);
		if (timeNow.compareTo(timeEnd) > 0) {
			timeEnd = timeEnd.plusDays(1);
		}
		Duration durationEnd = Duration.between(timeNow, timeEnd);
		long initialDelayEnd = durationEnd.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(disablePeriodicRefreshTask,
				initialDelayEnd, 24 * 60 * 60, TimeUnit.SECONDS);
		logger.info("Scheduled disablePeriodicRefreshTask for periodic price refresh at " + timeEnd);
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
			stockRepository.updateLiveStockPrices(nonWorldPrices);
		}
		List<StockPrice> worldPrices = getCurrentWorldStockPrices(worldStocks);
		if (Helper.isNotNullAndNonEmpty(worldPrices)) {
			stockRepository.updateLiveStockPrices(worldPrices);
		}
	}

	public List<StockPrice> getCurrentNonWorldStockPrices(List<Stock> nonWorldStocks) {		
		List<Stock> nseStocks = new ArrayList<Stock>();
		List<Stock> nonNseStocks = new ArrayList<Stock>();
		
		for(Stock stock: nonWorldStocks){
			if(stock.isNseStock()){
				nseStocks.add(stock);
			}else {
				nonNseStocks.add(stock);
			}
		}
		
		ArrayList<StockPrice> stockCurrentPriceList = new ArrayList<StockPrice>();
		
		//first fetch prices for NSE stocks
		int start = -10;
		int end = 0;
		while (end < nseStocks.size()) {
			start = start + 10;
			end = end + 10;
			if (end > nseStocks.size()) {
				end = nseStocks.size();
			}
			stockCurrentPriceList.addAll(getCurrentNonWorldStockPricesForSubList(nseStocks.subList(start, end),IntelliinvestConstants.EXCHANGE_NSE));
		}
		
		//then fetch prices for BSE stocks
		start = -10;
		end = 0;
		while (end < nonNseStocks.size()) {
			start = start + 10;
			end = end + 10;
			if (end > nonNseStocks.size()) {
				end = nonNseStocks.size();
			}
			stockCurrentPriceList.addAll(getCurrentNonWorldStockPricesForSubList(nonNseStocks.subList(start, end),IntelliinvestConstants.EXCHANGE_BSE));
		}
		return stockCurrentPriceList;
	}

	public List<StockPrice> getCurrentNonWorldStockPricesForSubList(List<Stock> stocks, String exchange) {
		List<StockPrice> stockCurrentPriceList = new ArrayList<StockPrice>();
		String codes = "";
		
		try {
			for (Stock stock : stocks) {
				String code = exchange.equals(IntelliinvestConstants.EXCHANGE_NSE) ? IntelliinvestConstants.EXCHANGE_NSE +":" + stock.getNseCode() : IntelliinvestConstants.EXCHANGE_BOM+":" + stock.getBseCode();
				codes = codes + code + ",";
			}
			if (!codes.isEmpty()) {
				codes = codes.substring(0, codes.lastIndexOf(","));
				String response = HttpUtil.getFromHttpUrlAsString(GOOGLE_QUOTE_URL.replace("#CODE#", codes.replace("&", "%26")));
				// System.out.println("Response:" + response);
				List <StockPrice> prices = getPriceFromJSON(exchange, codes, response);				
				stockCurrentPriceList.addAll(prices);
			}
		} catch (Exception e) {
			logger.error("Error fetching stock price for " + codes);
			logger.error(e.getMessage());
		}
		return stockCurrentPriceList;
	}

	private List<StockPrice> getPriceFromJSON(String exchange, String codes, String response) {
		// System.out.println("Response:" + response);
//		logger.info("getPriceFromJSON:" + codes + " and exchange:" + exchange);
		List<StockPrice> stockCurrentPriceList = new ArrayList<StockPrice>();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		JSONArray jsonArray = JSONArray.fromObject(response.replaceFirst("//", "").trim());
//		logger.info("jsonArray.size():" + jsonArray.size() + " and exchange:" + exchange);
		try {
			LocalDateTime localDateTime = dateUtil.getLocalDateTime();
			LocalDateTime oneMonthBefore = localDateTime.minusMonths(1);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject stockObject = (JSONObject) jsonArray.get(i);
				String code = stockObject.getString("t").replace("\\x26", "&");
				try {
					Double price = new Double(stockObject.getString("l_fix").replaceAll(",", ""));
					Double cp = new Double(stockObject.getString("cp").replaceAll(",", ""));
					String lt = stockObject.getString("lt_dts");
					LocalDateTime ltDate = LocalDateTime.parse(lt, dateFormat);
					if (oneMonthBefore.isAfter(ltDate)) {
						throw new RuntimeException("Stale details for " + code);
					}

					String securityId = IntelliinvestConstants.EXCHANGE_NSE.equals(exchange) ? stockRepository.getSecurityIdFromNSECode(code)
							: stockRepository.getSecurityIdFromBSECode(code);
					if (Helper.isNotNullAndNonEmpty(securityId)) {
						stockCurrentPriceList.add(new StockPrice(securityId, exchange, cp, price, ltDate));
					} else {
						logger.error("Error updating price. SecurityId mapping not found for code:" + code
								+ " and exchange:" + exchange);
					}
				} catch (Exception e1) {
					if (exchange.equals(IntelliinvestConstants.EXCHANGE_NSE)) {
						logger.error("Error fetching stock price from " + exchange + " for " + code
								+ ". Trying from BSE now");
						String bseCode = stockRepository.getBSECodeFromNSECode(code);
						if (bseCode != null) {
							response = HttpUtil.getFromHttpUrlAsString(
									GOOGLE_QUOTE_URL.replace("#CODE#", IntelliinvestConstants.EXCHANGE_BOM +":" + bseCode.replace("&", "%26")));
							stockCurrentPriceList.addAll(getPriceFromJSON(IntelliinvestConstants.EXCHANGE_BSE, code, response));
						} else {
							logger.error("Can't fetch price from BSE. nseToBSE mapping not found for " + code);
						}
					} else {
						logger.error("Error fetching stock price from " + exchange + " for code " + code);
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
				String securityId = stock.getSecurityId();
				try {
					String response = HttpUtil
							.getFromHttpUrlAsString(GOOGLE_QUOTE_URL.replace("#CODE#", securityId.replace("&", "%26")));
					JSONArray jsonArray = JSONArray.fromObject(response.replaceFirst("//", "").trim());
					for (int j = 0; j < jsonArray.size(); j++) {
							JSONObject stockObject = (JSONObject) jsonArray.get(j);
							Double price = new Double(stockObject.getString("l_fix").replaceAll(",", ""));
							Double cp = new Double(stockObject.getString("cp").replaceAll(",", ""));
							stockCurrentPriceList.add(
									new StockPrice(stock.getSecurityId(), "", cp, price, dateUtil.getLocalDateTime()));
					} 
				}catch (Exception e) {
					logger.error("Error fetching stock price for " + securityId);
					logger.error(e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error("Error fetching World stock prices", e);
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
				stockRepository.updateLiveStockPrices(nonWorldPrices);
			}
			List<StockPrice> worldPrices = getCurrentWorldStockPrices(worldStocks);
			if (Helper.isNotNullAndNonEmpty(worldPrices)) {
				stockRepository.updateLiveStockPrices(worldPrices);
			}
		} catch (Exception e) {
			return e.getMessage();
		}

		return "Success";
	}
	
	public List<StockPriceTimeSeriesResponse> getStockPriceTimeSeriesResponse(String exchange, String securityId, Integer requestInterval, String period) {
		List<StockPriceTimeSeriesResponse> volumeChartDataList = new ArrayList<StockPriceTimeSeriesResponse>(); 
		try{
			String response = HttpUtil.getFromHttpUrlAsString(
					GOOGLE_REALTIME_FULL_QUOTE_URL.replace("#CODE#", securityId.replace("&", "%26"))
						.replace("#EXCHANGE#", exchange)
						.replace("#INTERVAL#", requestInterval.toString())
						.replace("#PERIOD#", period));
			String[] values = response.split("\n");
			Date baseDate = null;
			Integer interval = requestInterval;
			for(String value : values){
				Date date = null;
				if(value.startsWith("COLUMNS")){
					continue;
				}else if(value.split(",").length==6){
					String[] datas = value.split(",");
					if(datas[0].startsWith("a")){
						baseDate = new Date(new Long(datas[0].replace("a", ""))*1000L);
						date = baseDate;
					}else{
						date = new Date(baseDate.getTime() + (new Long(datas[0])*interval*1000L));
					}
					Double close = new Double(datas[1]);
					Double high = new Double(datas[2]);
					Double low = new Double(datas[3]);
					Double open = new Double(datas[4]);
					Double volume = new Double(datas[5]);
					volumeChartDataList.add(new StockPriceTimeSeriesResponse(securityId, open, high, low, close, volume, dateUtil.getLocalDateFromDate(date)));
				}else if(value.startsWith("INTERVAL")){
					interval = new Integer(value.replace("INTERVAL=", ""));
				}
			}
			return volumeChartDataList;
		}catch(Exception e){
			logger.info("Error fetching StockPrice TimeSeries Response", e);
			return new ArrayList<StockPriceTimeSeriesResponse>();
		}
	}
	
	public List<PriceVolumeData> getPriceVolumeData(String exchange, String securityId, Integer requestInterval, String period) {
		List<PriceVolumeData> volumeChartDataList = new ArrayList<PriceVolumeData>(); 
		try{
			String response = HttpUtil.getFromHttpUrlAsString(
						GOOGLE_REALTIME_QUOTE_URL.replace("#CODE#", securityId.replace("&", "%26"))
						.replace("#EXCHANGE#", exchange)
						.replace("#INTERVAL#", requestInterval.toString())
						.replace("#PERIOD#", period));
			String[] values = response.split("\n");
			Date baseDate = null;
			Integer interval = requestInterval;
			for(String value : values){
				Date date = null;
				if(value.startsWith("COLUMNS")){
					continue;
				}else if(value.split(",").length==3){
					String[] datas = value.split(",");
					if(datas[0].startsWith("a")){
						baseDate = new Date(new Long(datas[0].replace("a", ""))*1000L);
						date = baseDate;
					}else{
						date = new Date(baseDate.getTime() + (new Long(datas[0])*interval*1000L));
					}
					Double price = new Double(datas[1]);
					Long volume = new Long(datas[2]);
					volumeChartDataList.add(new PriceVolumeData(dateUtil.getLocalDateTimeFromDate(date), price, volume));
				}else if(value.startsWith("INTERVAL")){
					interval = new Integer(value.replace("INTERVAL=", ""));
				}
			}
			return volumeChartDataList;
		}catch(Exception e){
			logger.info("Error fetching Intra Day Price Volume Data");
			return new ArrayList<PriceVolumeData>();
		}
	}

}