package com.intelliinvest.data.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.HttpUtil;
import com.intelliinvest.util.ScheduledThreadPoolHelper;

@ManagedResource(objectName = "bean:name=QuandlEODStockPriceImporter", description = "QuandlEODStockPriceImporter")
public class QuandlEODStockPriceImporter {
	private static Logger logger = Logger.getLogger(QuandlEODStockPriceImporter.class);
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private StockRepository stockRepository;

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
	}
	
	private void initializeScheduledTasks() {
		Runnable refreshEODPricesTask = new Runnable() {
			public void run() {
				if (!DateUtil.isBankHoliday(DateUtil.getCurrentDate())) {
					try {
						updateLatestEODPricesFromNSE();
					} catch (Exception e) {
						logger.error("Error refreshing EOD price data for NSE stocks " + e.getMessage());
					}
				}
			}
		};
		ZonedDateTime zonedNow = DateUtil.getZonedDateTime();
		int dailyEODPriceRefreshStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("daily.eod.price.refresh.start.hr"));
		int dailyEODPriceRefreshStartMin = new Integer(
				IntelliInvestStore.properties.getProperty("daily.eod.price.refresh.start.min"));
		ZonedDateTime zonedNext = zonedNow.withHour(dailyEODPriceRefreshStartHour)
				.withMinute(dailyEODPriceRefreshStartMin).withSecond(0);
		if (zonedNow.compareTo(zonedNext) > 0) {
			zonedNext = zonedNext.plusDays(1);
		}
		Duration duration = Duration.between(zonedNow, zonedNext);
		long initialDelay = duration.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(refreshEODPricesTask, initialDelay,
				24 * 60 * 60, TimeUnit.SECONDS);

		logger.info("Scheduled refreshEODPricesTask for periodic eod price refresh");
	}

	public void updateLatestEODPricesFromNSE() throws Exception {
		logger.info("Inside updateLatestEODPricesFromNSE");
		Date currentDate = DateUtil.getCurrentDateWithNoTime();
		List<StockPrice> stockPriceList = new ArrayList<StockPrice>();
		List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
		
		fetchEODPricesFromNSE(currentDate,stockPriceList,quandlStockPriceList);

		// if prices not available for T, try T-1
		if (!(Helper.isNotNullAndNonEmpty(stockPriceList) && Helper.isNotNullAndNonEmpty(quandlStockPriceList))) {
			Date lastBusinessDate = DateUtil.getLastBusinessDate();
			fetchEODPricesFromNSE(lastBusinessDate,stockPriceList,quandlStockPriceList);
		}

		if (Helper.isNotNullAndNonEmpty(stockPriceList) && Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
			stockRepository.updateEODStockPrices(stockPriceList);
			quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
			quandlEODStockPriceRepository.updateCache(quandlStockPriceList);
		} else {
			logger.error("updateLatestEODPricesFromNSE did not retrieved any prices");
		}
	}
	
	private void fetchEODPricesFromNSE(Date date, List<StockPrice> stockPriceList, List<QuandlStockPrice> quandlStockPriceList){
		logger.info("Inside fetchEODPricesFromNSE for date " + date);
		List<Stock> stockDetails = stockRepository.getStocks();
		List<Stock> nonWorldStocks = new ArrayList<Stock>();
		for (Stock stock : stockDetails) {
			if (!stock.isWorldStock()) {
				nonWorldStocks.add(stock);
			}
		}
		
		for (Stock stock : nonWorldStocks) {		
			try {				
				List<StockPrice> stockPriceListTemp = null;
				List<QuandlStockPrice> quandlStockPriceListTemp = new ArrayList<QuandlStockPrice>();
				
				if(stockPriceList!=null){
					stockPriceListTemp = new ArrayList<StockPrice>();
				}
				fetchEODPricesFromNSE(stock.getCode(), date, date, stockPriceListTemp, quandlStockPriceListTemp);	
				
				if (stockPriceList!=null && Helper.isNotNullAndNonEmpty(stockPriceListTemp)){ 
					stockPriceList.addAll(stockPriceListTemp);
				}
				if (Helper.isNotNullAndNonEmpty(quandlStockPriceListTemp)) {
					quandlStockPriceList.addAll(quandlStockPriceListTemp);
				} else {
					logger.debug("No prices were retrieved for stock: " + stock.getCode() + " and startDate=" + date + " and endDate=" + date);
				}
			}catch (Exception e){
				// log error and move on to next stock 
				logger.error("Exception while retrieving EOD prices for tock: " + stock.getCode() + " and startDate=" + date + " and endDate=" + date);
			}			
		}
		
		logger.info("Inside fetchEODPricesFromNSE, # of stockPriceList downloaded for date " + date + " is " + stockPriceList.size());
		logger.info("Inside fetchEODPricesFromNSE, # of quandlStockPriceList downloaded for date " + date + " is " + quandlStockPriceList.size());
	}

	private void fetchEODPricesFromNSE(String stockCode, Date startDate, Date endDate, List<StockPrice> stockPriceList, List<QuandlStockPrice> quandlStockPriceList)
			throws Exception {
//		logger.info("Inside fetchEODPricesFromNSE for stockCode:" + stockCode + " from  " + startDate + " to " + endDate);
		try {
			String eodPricesAsString = getDataFromQuandl(stockCode, startDate, endDate);
			// logger.info("eodPricesAsString for stock "+ stock.getCode() + ":"+ eodPricesAsString);
			populateEODStockPrices(stockCode, eodPricesAsString,stockPriceList,quandlStockPriceList);			
		} catch (Exception e) {
			throw new IntelliinvestException("Error refreshing EOD price data for stock " + stockCode + " " + e.getMessage());
		}
	}
	
	private void populateEODStockPrices(String stockCode, String eodPricesAsString, List<StockPrice> stockPriceList, List<QuandlStockPrice> quandlStockPriceList) throws Exception {
		if (!Helper.isNotNullAndNonEmpty(eodPricesAsString)) {
			throw new IntelliinvestException("No prices were retrieved for : " + stockCode);
		}
		boolean populateStockPrice = true;
		if (stockPriceList == null) {
			populateStockPrice = false;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String[] eodPricesAsArray = eodPricesAsString.split("\n");

		for (int i = 1; i < eodPricesAsArray.length; i++) {
			String eodPriceAsString = eodPricesAsArray[i];
			String[] eodPriceAsArray = eodPriceAsString.split(",");
			if (eodPriceAsArray.length >= 8) {
				Date eodDate = format.parse(eodPriceAsArray[0]);
				double open = new Double(eodPriceAsArray[1]);
				double high = new Double(eodPriceAsArray[2]);
				double low = new Double(eodPriceAsArray[3]);
				double last = new Double(eodPriceAsArray[4]);
				double close = new Double(eodPriceAsArray[5]);
				int totTrdQty = new Double(eodPriceAsArray[6]).intValue();
				double totTrdVal = new Double(eodPriceAsArray[7]);

				if (populateStockPrice) {
					StockPrice stockPrice = new StockPrice();
					stockPrice.setCode(stockCode);
					stockPrice.setEodDate(eodDate);
					stockPrice.setEodPrice(close);
					stockPriceList.add(stockPrice);
				}
				QuandlStockPrice quandlStockPrice = new QuandlStockPrice();
				quandlStockPrice.setExchange("NSE");
				quandlStockPrice.setSymbol(stockCode);
				quandlStockPrice.setSeries("EQ");
				quandlStockPrice.setOpen(open);
				quandlStockPrice.setHigh(high);
				quandlStockPrice.setLow(low);
				quandlStockPrice.setLast(last);
				quandlStockPrice.setClose(close);
				quandlStockPrice.setTottrdqty(totTrdQty);
				quandlStockPrice.setTottrdval(totTrdVal);
				quandlStockPrice.setEodDate(eodDate);
				quandlStockPriceList.add(quandlStockPrice);
			} else {
				throw new IntelliinvestException(
						"Error while fetching EOD prices for date: " + eodPriceAsArray[0] + " for stock: " + stockCode);
			}
		}
	}

	private String getDataFromQuandl(String stockCode, Date startDate, Date endDate) throws IntelliinvestException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		stockCode = getQuandlStockCode(stockCode);
		String retVal;
		String link = "https://www.quandl.com/api/v3/datasets/NSE/" + stockCode
				+ ".csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=" + format.format(startDate) + "&end_date="
				+ format.format(endDate);
		try {
			// logger.debug("Sending Quandl Request:" + link);
			byte b[] = HttpUtil.getFromUrlAsBytes(link);
			retVal = new String(b);
			if (!Helper.isNotNullAndNonEmpty(retVal)) {
				throw new IntelliinvestException("Error while fetching EOD prices from date: " + startDate + " to date "
						+ endDate + " for Stock: " + stockCode);
			}
		} catch (Exception e) {
			throw new IntelliinvestException("Error while fetching Quandl data from Start Date: " + startDate
					+ "to End date: " + endDate + " for Stock: " + stockCode + " Error " + e.getMessage());
		}
		return retVal;
	}

	private String getQuandlStockCode(String stock_code) {
		Iterator<String> itr = IntelliInvestStore.QUANDL_STOCK_CODES_MAPPING.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value = IntelliInvestStore.QUANDL_STOCK_CODES_MAPPING.get(key);
			stock_code = stock_code.replaceAll(key, value);
		}
		return stock_code;
	}

	@ManagedOperation(description = "uploadLatestEODPricesFromNSE")
	public String uploadLatestEODPricesFromNSE() {
		try {
			updateLatestEODPricesFromNSE();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Success";
	}

	@ManagedOperation(description = "backloadEODPricesFromNSE")
	@ManagedOperationParameters({
			@ManagedOperationParameter(name = "Start Date", description = "Start Date (yyyy-MM-dd)"),
			@ManagedOperationParameter(name = "End Date", description = "End Date (yyyy-MM-dd)") })
	public String backloadEODPricesFromNSE(String startDate, String endDate) {
		StringBuilder buf = new StringBuilder();
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date start = format.parse(startDate);
			Date end = format.parse(endDate);			
			List<Stock> stockDetails = stockRepository.getStocks();
			List<Stock> nonWorldStocks = new ArrayList<Stock>();
			for (Stock stock : stockDetails) {
				if (!stock.isWorldStock()) {
					nonWorldStocks.add(stock);
				}
			}
			for (Stock stock : nonWorldStocks) {
				try {
					List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();

					fetchEODPricesFromNSE(stock.getCode(),start,end, null,quandlStockPriceList);
					if (Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
						quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
					}
				} catch (Exception e) {
					logger.error("Error while loading stock " + stock.getCode() + " " + e.getMessage());
					buf.append("Error while loading stock " + stock.getCode());
					buf.append("\n");
				}
			}
			quandlEODStockPriceRepository.initialiseCacheFromDB();
		} catch (Exception e) {
			return "Exception: " + buf.toString();
		}
		return "Success";
	}

	@ManagedOperation(description = "backloadEODPricesFromNSEForStock")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "Stock Code", description = "Stock Code"),
			@ManagedOperationParameter(name = "Start Date", description = "Start Date (yyyy-MM-dd)"),
			@ManagedOperationParameter(name = "End Date", description = "End Date (yyyy-MM-dd)") })
	public String backloadEODPricesFromNSEForStock(String stockCode, String startDate, String endDate) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
			Date start = format.parse(startDate);
			Date end = format.parse(endDate);
			fetchEODPricesFromNSE(stockCode,start,end, null, quandlStockPriceList);
			if (Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
				quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
			}
			quandlEODStockPriceRepository.initialiseCacheFromDB();
		} catch (Exception e) {
			logger.error("Error while loading stock " + stockCode + " " + e.getMessage());
			return e.getMessage();
		}
		return "Success";
	}

	@ManagedOperation(description = "backloadEODPricesFromNSEForStockFromFile")
	public String backloadEODPricesFromNSEForStockFromFile(String stockCode, String filePath) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
			SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] eodPriceAsArray = line.split(",");
				if (eodPriceAsArray.length >= 8) {
					Date eodDate = format.parse(eodPriceAsArray[0].trim());
					double open = new Double(eodPriceAsArray[1]);
					double high = new Double(eodPriceAsArray[2]);
					double low = new Double(eodPriceAsArray[3]);
					double last = new Double(eodPriceAsArray[4]);
					double close = new Double(eodPriceAsArray[5]);
					int totTrdQty = new Double(eodPriceAsArray[6]).intValue();
					double totTrdVal = new Double(eodPriceAsArray[7]);

					QuandlStockPrice quandlStockPrice = new QuandlStockPrice();
					quandlStockPrice.setExchange("NSE");
					quandlStockPrice.setSymbol(stockCode);
					quandlStockPrice.setSeries("EQ");
					quandlStockPrice.setOpen(open);
					quandlStockPrice.setHigh(high);
					quandlStockPrice.setLow(low);
					quandlStockPrice.setLast(last);
					quandlStockPrice.setClose(close);
					quandlStockPrice.setTottrdqty(totTrdQty);
					quandlStockPrice.setTottrdval(totTrdVal);
					quandlStockPrice.setEodDate(eodDate);
					quandlStockPriceList.add(quandlStockPrice);
				} else {
					throw new IntelliinvestException(
							"Error while fetching EOD prices for date: " + eodPriceAsArray[0] + " for stock");
				}
			}
			if (Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
				quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
			}
			quandlEODStockPriceRepository.initialiseCacheFromDB();
		} catch (Exception e) {
			logger.error("Error while loading stock " + e.getMessage());
			return e.getMessage();
		} finally {
			reader.close();
		}
		return "Success";
	}
}