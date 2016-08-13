package com.intelliinvest.data.importer;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
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

import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.web.common.IntelliInvestStore;
import com.intelliinvest.web.common.IntelliinvestException;
import com.intelliinvest.web.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.web.dao.StockRepository;
import com.intelliinvest.web.util.DateUtil;
import com.intelliinvest.web.util.Helper;
import com.intelliinvest.web.util.HttpUtil;
import com.intelliinvest.web.util.ScheduledThreadPoolHelper;

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
				DayOfWeek dayOfWeek = DateUtil.getDayOfWeek();
				if (!dayOfWeek.equals(DayOfWeek.SATURDAY) && !dayOfWeek.equals(DayOfWeek.SUNDAY)) {
					try {
						updateLatestEODPricesFromNSE();
					} catch (Exception e) {
						logger.error("Error refreshing EOD price data for NSE stocks " + e.getMessage());
					}
				}
			}
		};
		ZonedDateTime zonedNow = DateUtil.getZonedDateTime();
		int periodicEODPriceRefreshStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("periodic.eod.price.refresh.start.hr"));
		int periodicEODPriceRefreshStartMin = new Integer(
				IntelliInvestStore.properties.getProperty("periodic.eod.price.refresh.start.min"));
		ZonedDateTime zonedNext21 = zonedNow.withHour(periodicEODPriceRefreshStartHour)
				.withMinute(periodicEODPriceRefreshStartMin).withSecond(0);
		if (zonedNow.compareTo(zonedNext21) > 0) {
			zonedNext21 = zonedNext21.plusDays(1);
		}
		Duration duration21 = Duration.between(zonedNow, zonedNext21);
		long initialDelay21 = duration21.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(refreshEODPricesTask,
				initialDelay21, 24 * 60 * 60, TimeUnit.SECONDS);

		logger.info("Scheduled refreshEODPricesTask for periodic eod price refresh");
	}

	private void updateLatestEODPricesFromNSE() throws Exception {
		Date currentDate = DateUtil.getCurrentDate();
		List<StockPrice> stockPriceList = new ArrayList<StockPrice>();
		List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();

		getEODPricesFromNSE(currentDate, currentDate, stockPriceList, quandlStockPriceList);
		logger.info("Inside getEODPricesFromNSE, # of stockPriceList downloaded for date "+ currentDate + " is " + stockPriceList.size());
		logger.info("Inside getEODPricesFromNSE, # of quandlStockPriceList downloaded for date "+ currentDate + " is " + quandlStockPriceList.size());
		
		// if prices not available for T, try T-1
		if (!(Helper.isNotNullAndNonEmpty(stockPriceList) && Helper.isNotNullAndNonEmpty(quandlStockPriceList))) {
			Date lastBusinessDate = DateUtil.getLastBusinessDate();
			getEODPricesFromNSE(lastBusinessDate, lastBusinessDate, stockPriceList, quandlStockPriceList);
			logger.info("Inside getEODPricesFromNSE, # of stockPriceList downloaded for date "+ lastBusinessDate + " is " + stockPriceList.size());
			logger.info("Inside getEODPricesFromNSE, # of quandlStockPriceList downloaded for date "+ lastBusinessDate + " is " + quandlStockPriceList.size());
		}

		if (Helper.isNotNullAndNonEmpty(stockPriceList) && Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
			stockRepository.updateEODStockPrices(stockPriceList);
			quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
			quandlEODStockPriceRepository.updateCache(quandlStockPriceList);
		} else {
			logger.error("updateLatestEODPricesFromNSE did not retrieved any prices");
		}
	}

	private void getEODPricesFromNSE(Date startDate, Date endDate, List<StockPrice> stockPriceList,
			List<QuandlStockPrice> quandlStockPriceList) throws Exception {
		logger.info("Inside getEODPricesFromNSE from " + startDate + " to " + endDate);
		List<Stock> stockDetails = stockRepository.getStocks();
		List<Stock> nseStocks = new ArrayList<Stock>();
		for (Stock stock : stockDetails) {
			if (!stock.isWorldStock()) {
				nseStocks.add(stock);
			}
		}
		for (Stock stock : nseStocks) {
			try {
				String eodPricesAsString = getDataFromQuandl(stock.getCode(), startDate, endDate);
//				logger.info("eodPricesAsString for stock "+ stock.getCode() + ":"+ eodPricesAsString);
				List<StockPrice> stockPriceListTemp = new ArrayList<StockPrice>();
				List<QuandlStockPrice> quandlStockPriceListTemp = new ArrayList<QuandlStockPrice>();
				populateEODStockPrices(stock.getCode(), eodPricesAsString, stockPriceListTemp,
						quandlStockPriceListTemp);
				if (Helper.isNotNullAndNonEmpty(stockPriceListTemp) && Helper.isNotNullAndNonEmpty(quandlStockPriceListTemp)) {
					stockPriceList.addAll(stockPriceListTemp);
					quandlStockPriceList.addAll(quandlStockPriceListTemp);
				}			
			} catch (Exception e) {
				// log error and move on to next stock
				logger.error("Error refreshing EOD price data for stock " + stock.getCode() + " " + e.getMessage());
			}
		}
	}

	private void populateEODStockPrices(String stockCode, String eodPricesAsString, List<StockPrice> stockPriceList,
			List<QuandlStockPrice> quandlStockPriceList) throws Exception {
		if (!Helper.isNotNullAndNonEmpty(eodPricesAsString)) {
			throw new IntelliinvestException("Error while fetching EOD prices for stock: " + stockCode);
		}
		boolean populateStockPrice = true;
		if (stockPriceList==null) {
			populateStockPrice = false;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String[] eodPricesAsArray = eodPricesAsString.split("\n");
		for (int i = 1; i < eodPricesAsArray.length; i++) {
			String eodPriceAsString = eodPricesAsArray[1];
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
//			logger.debug("Sending Quandl Request:" + link);
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
		try {
			List<Stock> stockDetails = stockRepository.getStocks();
			List<Stock> nseStocks = new ArrayList<Stock>();
			for (Stock stock : stockDetails) {
				if (!stock.isWorldStock()) {
					nseStocks.add(stock);
				}
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			for (Stock stock : nseStocks) {
				List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
				startDate = startDate.trim();
				endDate = endDate.trim();
				Date start = format.parse(startDate);
				Date end = format.parse(endDate);
				String eodPricesAsString = getDataFromQuandl(stock.getCode(), start, end);
				populateEODStockPrices(stock.getCode(), eodPricesAsString, null, quandlStockPriceList);
				if (Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
					quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
				}
			}
			quandlEODStockPriceRepository.initialiseCacheFromDB();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Success";
	}
}