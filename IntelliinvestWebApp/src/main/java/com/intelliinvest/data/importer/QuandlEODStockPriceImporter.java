package com.intelliinvest.data.importer;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.web.common.IntelliInvestStore;
import com.intelliinvest.web.common.IntelliinvestException;
import com.intelliinvest.web.dao.QuandlStockPriceRepository;
import com.intelliinvest.web.dao.StockRepository;
import com.intelliinvest.web.util.DateUtil;
import com.intelliinvest.web.util.Helper;
import com.intelliinvest.web.util.HttpUtil;
import com.intelliinvest.web.util.ScheduledThreadPoolHelper;

public class QuandlEODStockPriceImporter {
	private static Logger logger = Logger.getLogger(QuandlEODStockPriceImporter.class);
	@Autowired
	private QuandlStockPriceRepository quandlStockPriceRepository;
	@Autowired
	private StockRepository stockRepository;

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
	}

	private void initializeScheduledTasks() {
		Runnable refreshEODPricesTask = new Runnable() {
			public void run() {
				ZoneId zoneId = DateUtil.ZONE_ID;
				ZonedDateTime zonedNow = ZonedDateTime.now(zoneId);
				DayOfWeek dayOfWeek = zonedNow.getDayOfWeek();
				if (!dayOfWeek.equals(DayOfWeek.SATURDAY) && !dayOfWeek.equals(DayOfWeek.SUNDAY)) {
					try {
						updateEODPricesFromNSE();
					} catch (Exception e) {
						logger.error("Error refreshing EOD price data for stocks and world indexes " + e.getMessage());
					}
				}
			}
		};
		ZoneId zoneId = DateUtil.ZONE_ID;
		ZonedDateTime zonedNow = ZonedDateTime.now(zoneId);
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
		// ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(refreshEODPricesTask,
		// initialDelay21, 24 *
		// 60 * 60, TimeUnit.SECONDS);
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(refreshEODPricesTask, 20, 300,
				TimeUnit.SECONDS);
		logger.info("Scheduled refreshEODPricesTask for periodic eod price refresh");
	}

	private void updateEODPricesFromNSE() throws Exception {
		List<Stock> stockDetails = stockRepository.getStocks();
		List<Stock> nseStocks = new ArrayList<Stock>();
		for (Stock stock : stockDetails) {
			if (!stock.isWorldStock()) {
				nseStocks.add(stock);
			}
		}
		List<StockPrice> stockEODPriceList = new ArrayList<StockPrice>();
		List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
		Date currentDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		for (Stock stock : nseStocks) {
			String eodPricesAsString = getDataFromQuandl(stock.getCode(), currentDate, currentDate);
//			logger.debug("Response: " + eodPricesAsString);
			if (!Helper.isNotNullAndNonEmpty(eodPricesAsString)) {
				throw new IntelliinvestException(
						"Error while fetching EOD prices for date: " + currentDate + " for Stock: " + stock.getCode());
			}
			String[] eodPricesAsArray = eodPricesAsString.split("\n");
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
				StockPrice stockPrice = new StockPrice();
				stockPrice.setCode(stock.getCode());
				stockPrice.setEodDate(eodDate);
				stockPrice.setEodPrice(close);
				stockEODPriceList.add(stockPrice);
				
				QuandlStockPrice quandlStockPrice = new QuandlStockPrice();
				quandlStockPrice.setExchange("NSE");
				quandlStockPrice.setSymbol(stock.getCode());
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
						"Error while fetching EOD prices for date: " + currentDate + " for Stock: " + stock.getCode());
			}
		}
		if (Helper.isNotNullAndNonEmpty(stockEODPriceList)) {
			stockRepository.updateEODStockPrices(stockEODPriceList);
		}
		if (Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
			quandlStockPriceRepository.updateQuandlStockPrices(quandlStockPriceList);
		}
	}

	private String getDataFromQuandl(String stock_code, Date startDate, Date endDate) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		stock_code = getQuandlStockCode(stock_code);
		String link = "https://www.quandl.com/api/v3/datasets/NSE/" + stock_code
				+ ".csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=" + format.format(startDate) + "&end_date="
				+ format.format(endDate);
		/*
		 * String link = "https://www.quandl.com/api/v3/datasets/NSE/" +
		 * stock_code + ".csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=" +
		 * "2016-08-02" + "&end_date=" + "2016-08-02";
		 */

		try {
//			logger.debug("Sending Quandl Request:" + link);
			byte b[] = HttpUtil.getFromUrlAsBytes(link);
			return new String(b);
		} catch (Exception e) {
			logger.info("Error while fetching Quandl data from Start Date: " + startDate + "to End date: " + endDate
					+ " for Stock: " + stock_code + " Error " + e.getMessage());
		}
		return null;
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
}