package com.intelliinvest.data.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.CommonConstParams;
import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
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
	@Autowired
	private DateUtil dateUtil;
	private ExecutorService executorService = null;
	private final static String QUANDL_BSE_QUOTE_URL = "https://www.quandl.com/api/v3/datasets/BSE/BOM#CODE#.csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=#START#&end_date=#END#";
	private final static String QUANDL_NSE_QUOTE_URL = "https://www.quandl.com/api/v3/datasets/NSE/#CODE#.csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=#START#&end_date=#END#";

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
	}

	private void initializeScheduledTasks() {
		Runnable refreshEODPricesTask = new Runnable() {
			public void run() {
				if (!dateUtil.isBankHoliday(dateUtil.getLocalDate())) {
					try {
						updateLatestEODPrices();
					} catch (Exception e) {
						logger.error("Error refreshing EOD price data for stocks " + e.getMessage());
					}
				}
			}
		};
		LocalDateTime zonedNow = dateUtil.getLocalDateTime();
		int dailyEODPriceRefreshStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("daily.eod.price.refresh.start.hr"));
		int dailyEODPriceRefreshStartMin = new Integer(
				IntelliInvestStore.properties.getProperty("daily.eod.price.refresh.start.min"));
		LocalDateTime zonedNext = zonedNow.withHour(dailyEODPriceRefreshStartHour)
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

	public void updateLatestEODPrices() throws Exception {
		logger.info("Inside updateLatestEODPrices");
		LocalDate currentDate = dateUtil.getLocalDate();
		List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
		fetchEODPrices(currentDate, null, quandlStockPriceList);
		// if prices not available for T, try T-1
		if (!Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
			LocalDate lastBusinessDate = dateUtil.getLastBusinessDate();
			fetchEODPrices(lastBusinessDate, null, quandlStockPriceList);
		}

		if (Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
			quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
			quandlEODStockPriceRepository.updateCache(quandlStockPriceList);
		} else {
			logger.error("updateLatestEODPrices did not retrieved any prices");
		}
	}

	public void backloadEODPricesForDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
		logger.info("Inside backloadEODPricesForDateRange");
		List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
		fetchEODPrices(startDate, endDate, quandlStockPriceList);

		if (Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
			quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
			quandlEODStockPriceRepository.initialiseCacheFromDB();
		} else {
			logger.error("backloadEODPricesForDateRange did not retrieved any prices for startDate=" + startDate
					+ " and endDate=" + endDate);
		}
	}

	private void fetchEODPrices(LocalDate startDate, LocalDate endDate, List<QuandlStockPrice> quandlStockPriceList) {
		if (endDate == null) {
			endDate = startDate;
		}
		logger.info("Inside fetchEODPrices for all stocks for startDate=" + startDate + " and endDate=" + endDate);
		List<Stock> stockDetails = stockRepository.getStocks();
		List<Stock> nonWorldStocks = new ArrayList<Stock>();
		for (Stock stock : stockDetails) {
			if (!stock.isWorldStock()) {
				nonWorldStocks.add(stock);
			}
		}
		createExecutorService();
		Map<String, Future<List<QuandlStockPrice>>> futuresMap = new HashMap<String, Future<List<QuandlStockPrice>>>();
		
		for (Stock stock : nonWorldStocks) {
			FetchCloseTask forecastCloseTask = new FetchCloseTask(stock, startDate, endDate);
			Future<List<QuandlStockPrice>> future = executorService.submit(forecastCloseTask);
			futuresMap.put(stock.getSecurityId(), future);
		}
		
		for (Map.Entry<String, Future<List<QuandlStockPrice>>> entry : futuresMap.entrySet()) {
			String code = null;
			try {
				code = entry.getKey();
				Future<List<QuandlStockPrice>> future = entry.getValue();
				List<QuandlStockPrice> result = future.get();
//				logger.debug("Inside fetchEODPrices, # of quandlStockPriceList downloaded for stock "+ code + " and startDate=" + startDate + " and endDate=" + endDate + " is " + result.size());
				if(Helper.isNotNullAndNonEmpty(result)){
					quandlStockPriceList.addAll(result);
				}	
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("InterruptedException in FetchCloseTask for stock:" + code + ". Exception:" + e.getMessage());
			} catch (ExecutionException e) {
				logger.error("ExecutionException in FetchCloseTask for stock:" + code + ". Exception:" + e.getMessage());
			}
		}
		
		shutdownExecutorService();
		logger.info("Inside fetchEODPrices, # of quandlStockPriceList downloaded for startDate=" + startDate + " and endDate=" + endDate + " is " + quandlStockPriceList.size());
	}

	class FetchCloseTask implements Callable<List<QuandlStockPrice>> {
		private Stock stock;
		private LocalDate startDate;
		private LocalDate endDate;
		
		public FetchCloseTask(Stock stock, LocalDate startDate, LocalDate endDate) {
			super();
			this.stock = stock;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public List<QuandlStockPrice> call() throws Exception {		
			List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
			fetchEODPricesForStock(stock, startDate, endDate, quandlStockPriceList);
			return quandlStockPriceList;			
		}
	}
	
	private void fetchEODPricesForStock(Stock stock, LocalDate startDate, LocalDate endDate, List<QuandlStockPrice> quandlStockPriceList) {		
		if (endDate == null) {
			endDate = startDate;
		}
//		logger.debug("Inside fetchEODPricesForStock for stockCode " + stock.getSecurityId() + " startDate=" + startDate + " and endDate=" + endDate);

		boolean error = false;

		if (stock.isNseStock()) {
			try {
				// first try to fetch stock prices from NSE
				fetchEODPricesFromQuandl(stock, CommonConstParams.EXCHANGE_NSE, startDate, endDate, quandlStockPriceList);

				if (!Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
					error = true;
					logger.error("No prices were retrieved for stock: " + stock.getSecurityId()
					+ " from NSE for startDate=" + startDate + " and endDate=" + endDate);
				} 
			} catch (Exception e) {
				// log error and try to fetch from BSE
				logger.error("Exception while retrieving EOD prices for stock: " + stock.getSecurityId()
						+ " from NSE for startDate=" + startDate + " and endDate=" + endDate);
				quandlStockPriceList.clear();
				error = true;
			}
		}

		if ((stock.isNseStock() && error) || !stock.isNseStock()) {
			try {
				// Now try to fetch stock prices from BSE
				fetchEODPricesFromQuandl(stock, CommonConstParams.EXCHANGE_BSE, startDate, endDate, quandlStockPriceList);

				if (!Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
					logger.error("No prices were retrieved for stock: " + stock.getSecurityId()
					+ " from BSE for startDate=" + startDate + " and endDate=" + endDate);
				}
			} catch (Exception e) {
				// log error and move on to next stock
				logger.error("Exception while retrieving EOD prices for stock: " + stock.getSecurityId()
						+ " from BSE for startDate=" + startDate + " and endDate=" + endDate);
			}
		}
	}

	private void fetchEODPricesFromQuandl(Stock stock, String exchange, LocalDate startDate, LocalDate endDate,
			List<QuandlStockPrice> quandlStockPriceList) throws Exception {
//		logger.debug("Inside fetchEODPricesFromQuandl for stockCode:" + stock.getSecurityId() + " for exchange:" + exchange +" from " + startDate + " to " + endDate);
		try {
			String eodPricesAsString = getDataFromQuandl(stock, exchange, startDate, endDate);
			// logger.info("eodPricesAsString for stock "+ stock.getCode() + ":"+ eodPricesAsString);
			populateEODStockPrices(stock, exchange, eodPricesAsString, quandlStockPriceList);
		} catch (Exception e) {
			throw new IntelliinvestException("Error refreshing EOD price data for stock " + stock.getSecurityId()
					+ " from exchange: " + exchange + " " + e.getMessage());
		}
	}

	private void populateEODStockPrices(Stock stock, String exchange, String eodPricesAsString,
			List<QuandlStockPrice> quandlStockPriceList) throws Exception {
		if (!Helper.isNotNullAndNonEmpty(eodPricesAsString)) {
			throw new IntelliinvestException(
					"No prices were retrieved for stock " + stock.getSecurityId() + " from exchange: " + exchange);
		}

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String[] eodPricesAsArray = eodPricesAsString.split("\n");

		for (int i = 1; i < eodPricesAsArray.length; i++) {
			String eodPriceAsString = eodPricesAsArray[i];
			String[] eodPriceAsArray = eodPriceAsString.split(",");
			LocalDate eodDate = null;
			double open = 0d;
			double high = 0d;
			double low = 0d;
			double last = 0d;
			double close = 0d;
			int tradedQty = 0;
			double turnover = 0d;
			double wap = 0d;

			if (CommonConstParams.EXCHANGE_BSE.equals(exchange)) {
				if (eodPriceAsArray.length < 9) {
					throw new IntelliinvestException("Error while fetching EOD prices for date: " + eodPriceAsArray[0]
							+ " for " + stock.getSecurityId() + " from exchange: " + exchange);
				}
				eodDate = LocalDate.parse(eodPriceAsArray[0], dateFormat);
				open = new Double(eodPriceAsArray[1]);
				high = new Double(eodPriceAsArray[2]);
				low = new Double(eodPriceAsArray[3]);
				close = new Double(eodPriceAsArray[4]);
				wap = new Double(eodPriceAsArray[5]);
				tradedQty = new Double(eodPriceAsArray[6]).intValue();
				turnover = new Double(eodPriceAsArray[8]);
				last = close;
			} else {
				if (eodPriceAsArray.length < 8) {
					throw new IntelliinvestException("Error while fetching EOD prices for date: " + eodPriceAsArray[0]
							+ " for " + stock.getSecurityId() + " from exchange: " + exchange);
				}

				eodDate = LocalDate.parse(eodPriceAsArray[0], dateFormat);
				open = new Double(eodPriceAsArray[1]);
				high = new Double(eodPriceAsArray[2]);
				low = new Double(eodPriceAsArray[3]);
				last = new Double(eodPriceAsArray[4]);
				close = new Double(eodPriceAsArray[5]);
				tradedQty = new Double(eodPriceAsArray[6]).intValue();
				turnover = new Double(eodPriceAsArray[7]);
			}

			QuandlStockPrice quandlStockPrice = new QuandlStockPrice(stock.getSecurityId(), exchange, "EQ", open, high,
					low, close, last, wap, tradedQty, turnover, eodDate, dateUtil.getLocalDateTime());
			quandlStockPriceList.add(quandlStockPrice);
		}
	}

	private String getDataFromQuandl(Stock stock, String exchange, LocalDate startDate, LocalDate endDate)
			throws IntelliinvestException {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String retVal;
		String code;
		String url = null;
		;

		if (CommonConstParams.EXCHANGE_BSE.equals(exchange)) {
			code = getQuandlStockCode(stock.getBseCode());
			url = QUANDL_BSE_QUOTE_URL.replace("#CODE#", code.replace("&", "%26"))
					.replace("#START#", dateFormat.format(startDate)).replace("#END#", dateFormat.format(endDate));
		} else {
			code = getQuandlStockCode(stock.getNseCode());
			url = QUANDL_NSE_QUOTE_URL.replace("#CODE#", code.replace("&", "%26"))
					.replace("#START#", dateFormat.format(startDate)).replace("#END#", dateFormat.format(endDate));
		}

		try {
			// logger.debug("Sending Quandl Request:" + url);
			byte b[] = HttpUtil.getFromUrlAsBytes(url);
			retVal = new String(b);
			if (!Helper.isNotNullAndNonEmpty(retVal)) {
				throw new IntelliinvestException("Error while fetching EOD prices from date: " + startDate + " to date "
						+ endDate + " for stock " + stock.getSecurityId() + " from exchange: " + exchange);
			}
		} catch (Exception e) {
			throw new IntelliinvestException("Error while fetching Quandl data from Start Date: " + startDate
					+ "to End date: " + endDate + " for stock " + stock.getSecurityId() + " from exchange: " + exchange
					+ " Error " + e.getMessage());
		}
		return retVal;
	}

	private String getQuandlStockCode(String stockCode) {
		Iterator<String> itr = IntelliInvestStore.QUANDL_STOCK_CODES_MAPPING.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value = IntelliInvestStore.QUANDL_STOCK_CODES_MAPPING.get(key);
			stockCode = stockCode.replaceAll(key, value);
		}
		return stockCode;
	}

	private void createExecutorService() {
		int count = new Integer(
				IntelliInvestStore.properties.getProperty("daily.eod.price.refresh.thread.pool.count")).intValue();
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
	
	@ManagedOperation(description = "backloadLatestEODPrices")
	public String backloadLatestEODPrices() {
		try {
			updateLatestEODPrices();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Success";
	}

	@ManagedOperation(description = "backloadEODPricesForDateRange")
	@ManagedOperationParameters({
			@ManagedOperationParameter(name = "Start Date", description = "Start Date (yyyy-MM-dd)"),
			@ManagedOperationParameter(name = "End Date", description = "End Date (yyyy-MM-dd)") })
	public String backloadEODPricesForDateRange(String startDate, String endDate) {
		StringBuilder buf = new StringBuilder();
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate start = LocalDate.parse(startDate, dateFormat);
			LocalDate end = LocalDate.parse(endDate, dateFormat);
			backloadEODPricesForDateRange(start, end);
		} catch (Exception e) {
			return "Exception: " + buf.toString();
		}
		return "Success";
	}

	@ManagedOperation(description = "backloadEODPricesForStock")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "Stock Id", description = "Stock Id"),
			@ManagedOperationParameter(name = "Start Date", description = "Start Date (yyyy-MM-dd)"),
			@ManagedOperationParameter(name = "End Date", description = "End Date (yyyy-MM-dd)") })
	public String backloadEODPricesForStock(String id, String startDate, String endDate) {
		try {
			List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate start = LocalDate.parse(startDate, dateFormat);
			LocalDate end = LocalDate.parse(endDate, dateFormat);
			Stock stock = stockRepository.getStockById(id);
			if(stock!=null){
				fetchEODPricesForStock(stock, start, end, quandlStockPriceList);
			}else {
				return "Stock not found";
			}
			
			if (Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
				quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
				quandlEODStockPriceRepository.initialiseCacheFromDB();
			} else {
				logger.error("backloadEODPricesForStock did not retrieved any prices for startDate=" + startDate
						+ " and endDate=" + endDate);
			}
			
		} catch (Exception e) {
			logger.error("Error while backloadEODPricesForStock stock " + id + " " + e.getMessage());
			return e.getMessage();
		}
		return "Success";
	}

	@ManagedOperation(description = "backloadEODPricesFromNSEForStockFromFile")
	public String backloadEODPricesFromNSEForStockFromFile(String nseCode, String filePath) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] eodPriceAsArray = line.split(",");
				if (eodPriceAsArray.length >= 8) {
					LocalDate eodDate = LocalDate.parse(eodPriceAsArray[0].trim(), dateFormat);
					double open = new Double(eodPriceAsArray[1]);
					double high = new Double(eodPriceAsArray[2]);
					double low = new Double(eodPriceAsArray[3]);
					double last = new Double(eodPriceAsArray[4]);
					double close = new Double(eodPriceAsArray[5]);
					int tradedQty = new Double(eodPriceAsArray[6]).intValue();
					double turnover = new Double(eodPriceAsArray[7]);

					String securityId = stockRepository.getSecurityIdFromNSECode(nseCode);
					QuandlStockPrice quandlStockPrice = new QuandlStockPrice(securityId, CommonConstParams.EXCHANGE_NSE, "EQ", open, high, low,
							close, last, 0d, tradedQty, turnover, eodDate, null);
					quandlStockPriceList.add(quandlStockPrice);
				} else {
					throw new IntelliinvestException(
							"Error while fetching EOD prices for date: " + eodPriceAsArray[0] + " for NSE stock");
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

	@ManagedOperation(description = "backloadEODPricesFromBSEForStockFromFile")
	public String backloadEODPricesFromBSEForStockFromFile(String bseCode, String filePath) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			List<QuandlStockPrice> quandlStockPriceList = new ArrayList<QuandlStockPrice>();
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] eodPriceAsArray = line.split(",");
				if (eodPriceAsArray.length >= 9) {
					LocalDate eodDate = LocalDate.parse(eodPriceAsArray[0].trim(), dateFormat);
					double open = new Double(eodPriceAsArray[1]);
					double high = new Double(eodPriceAsArray[2]);
					double low = new Double(eodPriceAsArray[3]);
					double close = new Double(eodPriceAsArray[4]);
					double wap = new Double(eodPriceAsArray[5]);
					int tradedQty = new Double(eodPriceAsArray[6]).intValue();
					double turnover = new Double(eodPriceAsArray[8]);

					String securityId = stockRepository.getSecurityIdFromBSECode(bseCode);
					QuandlStockPrice quandlStockPrice = new QuandlStockPrice(securityId, CommonConstParams.EXCHANGE_BSE, "EQ", open, high, low,
							close, close, wap, tradedQty, turnover, eodDate, null);
					quandlStockPriceList.add(quandlStockPrice);
				} else {
					throw new IntelliinvestException(
							"Error while fetching EOD prices for date: " + eodPriceAsArray[0] + " for BSE stock");
				}
			}
			if (Helper.isNotNullAndNonEmpty(quandlStockPriceList)) {
				quandlEODStockPriceRepository.updateEODStockPrices(quandlStockPriceList);
				quandlEODStockPriceRepository.initialiseCacheFromDB();
			}
			
		} catch (Exception e) {
			logger.error("Error while loading stock " + e.getMessage());
			return e.getMessage();
		} finally {
			reader.close();
		}
		return "Success";
	}
}