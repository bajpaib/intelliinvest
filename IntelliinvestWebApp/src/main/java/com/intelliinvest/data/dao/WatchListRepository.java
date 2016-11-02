package com.intelliinvest.data.dao;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.data.model.Portfolio;
import com.intelliinvest.data.model.PortfolioItem;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.data.model.User;
import com.intelliinvest.data.model.UserPortfolio;
import com.intelliinvest.data.model.WatchListData;
import com.intelliinvest.data.model.WatchListStockData;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.MailUtil;
import com.intelliinvest.web.bo.response.WatchListResponse;
import com.mongodb.WriteResult;

@ManagedResource(objectName = "bean:name=WatchListRepository", description = "WatchListRepository")
public class WatchListRepository {

	private static Logger logger = Logger.getLogger(WatchListRepository.class);

	private static String green = "color:green";
	private static String red = "color:red";
	private static String orange = "color:orang	e";
	private static String lime = "color:lime";

	@Autowired
	private StockSignalsRepository stockSignalsRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	UserPortfolioRepository userPortfolioRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	QuandlEODStockPriceRepository quandlEODStockPriceRepository;

	@Autowired
	DateUtil dateUtil;

	@Autowired
	MailUtil mailUtil;
	
//	private static Map<String, WatchListStockData> watchListStockDataCache = new ConcurrentHashMap<String, WatchListStockData>();

	private static final String COLLECTION_WATCHLIST = "WATCHLIST";
	@Autowired
	private MongoTemplate mongoTemplate;

	@PostConstruct
	public void init() {
//		refreshCache();
	}

//	@ManagedOperation(description = "initialiseCacheFromDB")
//	public boolean refreshCache() {
//		logger.debug("in refreshCache method....");
//		List<WatchListStockData> watchListStocksDatasList = getAllTradingAccountData();
//		logger.debug("WatchList Data for Cache, size is " + watchListStocksDatasList.size());
//		if (watchListStocksDatasList != null && watchListStocksDatasList.size() > 0)
//			for (WatchListStockData watchListStockData : watchListStocksDatasList) {
//				if (watchListStockData.getCode() != null)
//					watchListStockDataCache.put(watchListStockData.getCode(), watchListStockData);
//			}
//		return true;
//	}

	private WatchListStockData getWatchListData(String stockCode) {
		logger.debug("in getAllTradingAccountData method....");
//		WatchListStockData retVal = new WatchListStockData();
//		for (Stock stock : stockRepository.getStocks()) {

//			String symbol = stock.getSecurityId();
			// QuandlStockPrice priceObj = quandlEODStockPriceRepository
			// .getEODStockPriceObjFromCache(symbol);
			StockSignals stockSignals = stockSignalsRepository.getStockSignalsFromCache(stockCode);

			// StockPrice stockPrice =
			// stockRepository.getStockPriceById(symbol);
			QuandlStockPrice stockPrice = quandlEODStockPriceRepository.getEODStockPrice(stockCode);
			if (stockPrice != null && stockSignals != null) {
				return getWatchListDataObj(stockPrice.getClose(), stockSignals);
			}
//		}
		return null;
	}

	private WatchListStockData getWatchListDataObj(double eodPrice, StockSignals stockSignals) {
//		logger.debug("in getWatchListDataObj method....");
		WatchListStockData watchListStockData = new WatchListStockData();
		watchListStockData.setSignalPrice(eodPrice);
		if (stockSignals != null) {
			watchListStockData.setCode(stockSignals.getSecurityId());
			watchListStockData.setSignalDate(stockSignals.getSignalDate());
			watchListStockData.setSignalType(stockSignals.getAggSignal());
			watchListStockData.setYesterdaySignalType(stockSignals.getAggSignal_previous());
		}
		return watchListStockData;
	}

	public WatchListResponse getTradingAccountData(String userId) {
		logger.debug("in getTradingAccountData method....");
		WatchListResponse response = new WatchListResponse();
		response.setUserId(userId);

		if (isValidUser(userId)) {
			List<WatchListStockData> stocksData = new ArrayList<WatchListStockData>();
			Query query = Query.query(Criteria.where("userId").is(userId));
			// query.fields().include("code");
			// query.fields().exclude("userId");
			List<WatchListData> watchListDatas = mongoTemplate.find(query, WatchListData.class, COLLECTION_WATCHLIST);

			if (watchListDatas != null && watchListDatas.size() > 0)
				for (WatchListData watchListData : watchListDatas) {
					WatchListStockData watchListStockData = getWatchListDataForCodeFromCache(watchListData.getCode());
					stocksData.add(watchListStockData);
				}
			response.setStocksData(stocksData);
			response.setSuccess(true);
			response.setMessage("Data successfully returned...");
			return response;
		} else {
			response.setSuccess(false);
			response.setMessage("User with id: " + userId + " doesn't exist or not logged in...");
			return response;
		}
	}

	private boolean isValidUser(String userId) {
		if (userId != null) {
			User user = userRepository.getUserByUserId(userId);
			if (user != null && user.getLoggedIn()) {
				return true;
			}
		}
		return false;
	}

	private WatchListStockData getWatchListDataForCodeFromCache(String code) {
		logger.debug("in getWatchListDataForCodeFromCache method....");
		return getWatchListData(code);
	}

	public WatchListResponse addTradingAccountData(String userId, String stockCode) {
		logger.debug("in addTradingAccountData method....");
		WatchListResponse response = new WatchListResponse();
		if (isValidUser(userId) && isValidStockCode(stockCode)) {
			int n = 0;
			if (null != userId) {
				response.setUserId(userId);
				Query query = new Query();
				query.addCriteria(Criteria.where("userId").is(userId).and("code").is(stockCode));
				Update update = new Update();
				update.set("code", stockCode);
				update.set("userId", userId);
				WriteResult result = mongoTemplate.upsert(query, update, COLLECTION_WATCHLIST);
				n = result.getN();
			}
			if (n >= 1) {
				List<WatchListStockData> stocksData = new ArrayList<WatchListStockData>();
				if (null != getWatchListDataForCodeFromCache(stockCode)) {
					stocksData.add(getWatchListDataForCodeFromCache(stockCode));
				} else {
					WatchListStockData stockData = new WatchListStockData();
					stockData.setCode(stockCode);
					stockData.setSignalPrice(new Double("-1"));
					stockData.setSignalType("--");
					stocksData.add(stockData);
				}
				response.setStocksData(stocksData);
				response.setSuccess(true);
				response.setMessage("Stock code: "+stockCode+" has been added successfully...");
				return response;
			} else {
				return null;
			}
		} else {
			response.setUserId(userId);
			response.setSuccess(false);
			response.setMessage("Some invalid input there in the request, so please check your request...");
			return response;
		}
	}

	private boolean isValidStockCode(String stockCode) {
		Stock stock = stockRepository.getStockById(stockCode);
		if (stock != null)
			return true;
		else
			return false;
	}

	public WatchListResponse removeTradingAccountData(String userId, String stockCode) {
		logger.debug("in removeTradingAccountData method....");
		WatchListResponse response = new WatchListResponse();
		if (null != userId && isValidUser(userId)) {
			// for (String stockCode : stockCode) {
			WriteResult result = mongoTemplate.remove(
					Query.query(Criteria.where("userId").is(userId).and("code").is(stockCode)), WatchListData.class);
			int no = result.getN();
			response.setUserId(userId);
			if (no == 0) {
				response.setSuccess(false);
				response.setMessage("Some internal error occurred, so not able to remove code: " + stockCode);
			} else{
				response.setMessage("Stock code: "+stockCode+" has been removed successfully...");
				response.setSuccess(true);
			}
		} else {
			response.setSuccess(false);
			response.setMessage("User with id: " + userId + " doesn't exist or not logged in...");
		}
		return response;
	}

	public boolean sendDailyTradingAccountUpdateMail() {
		logger.debug("in sendDailyTradingAccountUpdateMail method....");
		HashMap<String, List<String>> userStocksMap = new HashMap<String, List<String>>();
		// HashMap<String, String> userMailMapping = new HashMap<String,
		// String>();
		// Aggregation aggregation = Aggregation
		// .newAggregation((new CustomAggregationOperation(
		// new BasicDBObject("$lookup", new BasicDBObject("from",
		// "").append("localField", "")
		// .append("foreignField", "").append("as", "")))));
		//
		// AggregationResults<WatchListData> result = mongoTemplate.aggregate(
		// aggregation, COLLECTION_WATCHLIST, WatchListData.class);

		List<WatchListData> watchListDatas = mongoTemplate.findAll(WatchListData.class, COLLECTION_WATCHLIST);
		for (WatchListData watchListData : watchListDatas) {

			String user = watchListData.getUserId();
			String stock = watchListData.getCode();
			if (!userStocksMap.containsKey(user)) {
				userStocksMap.put(user, new ArrayList<String>());
			}
			userStocksMap.get(user).add(stock);
			// userMailMapping.put(user, watchListData.getMailId());

		}

		// List<String> distinctMailIds=
		// mongoTemplate.getCollection(COLLECTION_WATCHLIST).distinct("userId");

		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
		// String formattedDate = format.format();

		// SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String date = format.format(dateUtil.getLocalDate());

		for (String user : userStocksMap.keySet()) {
			try {
				String message = "<table>";

				message = message + "<tr>" + "<td><h2>Daily signal update</h2></td>"
						+ "<td><img src=\"http://intelliinvest.com/data/images/intelliinvest_logo.png\" style=\"display:block;\" width=\"200\" height=\"60\" border=\"0\">"
						+ "</tr>";

				message = message + "<tr><br><b>Signals Changed<b><br></tr>";

				message = message + "<tr>";
				message = message + getChangedSignals(date, userStocksMap.get(user));
				message = message + "</tr>";

				message = message + "<tr><br><b>PNL Information<b><br></tr>";

				message = message + "<tr>";
				message = message + getPNLInfo(user);
				message = message + "</tr>";
				logger.info("Sending mail to user: "+user);
				message = message + "</table><br>Regards,<br>IntelliInvest Team.";
				mailUtil.sendMail(new String[] { user }, "Daily signal update from IntelliInvest for " + date, message);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Error sending daily update mail for user " + user + " " + e.getMessage());
			}
		}
		return false;
	}

	private String getBuySellColor(String bs) {
		logger.debug("in getBuySellColor method....");
		if (bs.toUpperCase().contains("WAIT")) {
			return orange;
		} else if (bs.toUpperCase().contains("HOLD")) {
			return lime;
		} else if (bs.toUpperCase().contains("BUY")) {
			return green;
		} else if (bs.toUpperCase().contains("SELL")) {
			return red;
		} else {
			return "";
		}
	}

	private String getChangedSignals(String date, List<String> stocks) {
		logger.debug("in getChangedSignals method....");
		boolean signalChanged = false;
		String message = "<table cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;\">";
		message = message + "<tr>"
				+ "<th cellpadding=\"5\" cellspacing=\"3\" style=\"background:lightgray;border:1px solid black;border-collapse:collapse;text-align:center;\">Stock</th>"
				+ "<th cellpadding=\"5\" cellspacing=\"3\" style=\"background:lightgray;border:1px solid black;border-collapse:collapse;text-align:center;\">Previous Signal</th>"
				+ "<th cellpadding=\"5\" cellspacing=\"3\" style=\"background:lightgray;border:1px solid black;border-collapse:collapse;text-align:center;\">Signal</th>"
				+ "</tr>";
		for (String stock : stocks) {
			WatchListStockData stockData = getWatchListDataForCodeFromCache(stock);
			if (null == stockData || null == stockData.getSignalDate()) {
				continue;
			}
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
			// String signalDate = new SimpleDateFormat("yyyyMMdd")
			// .format(stockData.getSignalDate());
			String signalDate = format.format(stockData.getSignalDate());
			logger.debug("Signal Date: " + signalDate + " &&& last business date is:" + date);
			if (null != stockData && signalDate.equals(date)
					&& !stockData.getYesterdaySignalType().equalsIgnoreCase(stockData.getSignalType())) {
				signalChanged = true;
				message = message + "<tr>"
						+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;\">"
						+ stock + "</td>"
						+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;"
						+ getBuySellColor(stockData.getYesterdaySignalType()) + "\">"
						+ stockData.getYesterdaySignalType() + "</td>"
						+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;"
						+ getBuySellColor(stockData.getSignalType()) + "\">" + stockData.getSignalType() + "</td>"
						+ "</tr>";
			}
		}
		message = message + "</table>";
		if (!signalChanged) {
			message = "<table><tr>No signals Changed for today from your Watch List</tr></table>";
		}
		return message;
	}

	public String getPNLInfo(String userId) {
		logger.debug("in getPNLInfo method....");
		// HashMap<String, ManagePortfolioData> portfolioDataMap =
		// ManagePortfolioDao
		// .getInstance().getManagePortfolioSummaryData(
		// ManagePortfolioDao.getInstance()
		// .getManagePortfolioData(userId));
		//
		UserPortfolio userPortfolioDatas = userPortfolioRepository.getUserPortfolioByUserId(userId);
		Double totalPnl = 0D;
		Double totalTodaysPnl = 0D;
		String message = "<table cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;\">";
		message = message + "<tr>"
				+ "<th cellpadding=\"5\" cellspacing=\"3\" style=\"background:lightgray;border:1px solid black;border-collapse:collapse;text-align:center;\">Stock</th>"
				+ "<th cellpadding=\"5\" cellspacing=\"3\" style=\"background:lightgray;border:1px solid black;border-collapse:collapse;text-align:center;\">Balance</th>"
				+ "<th cellpadding=\"5\" cellspacing=\"3\" style=\"background:lightgray;border:1px solid black;border-collapse:collapse;text-align:center;\">Average Price</th>"
				+ "<th cellpadding=\"5\" cellspacing=\"3\" style=\"background:lightgray;border:1px solid black;border-collapse:collapse;text-align:center;\">EOD Price</th>"
				+ "<th cellpadding=\"5\" cellspacing=\"3\" style=\"background:lightgray;border:1px solid black;border-collapse:collapse;text-align:center;\">Todays PNL</th>"
				+ "<th cellpadding=\"5\" cellspacing=\"3\" style=\"background:lightgray;border:1px solid black;border-collapse:collapse;text-align:center;\">Overall PNL</th>"
				+ "</tr>";
		DecimalFormat nf = new DecimalFormat("##,##,##,##,##,##0.00");
		if (userPortfolioDatas != null) {
			for (Portfolio portfolios : userPortfolioDatas.getPortfolios()) {
				Double pnl = 0D;
				Double todaysPnl = 0D;
				List<PortfolioItem> portfolioItems = portfolios.getPortfolioItems();
				userPortfolioRepository.populatePnlForPortfolioItems(portfolioItems);
				for (PortfolioItem portfolioItem : userPortfolioRepository.getPortfolioSummary(portfolioItems)) {

					// logger.debug(portfolioItem);
					// ManagePortfolioData managePortfolioData =
					// portfolioDataMap
					// .get(stock);
					String stockCode = portfolioItem.getCode();

					// StockDetailStaticHolder.getEODPrice(stock);
					StockPrice stockPrice = stockRepository.getStockPriceById(stockCode);
					QuandlStockPrice quandlStockPrice = quandlEODStockPriceRepository.getEODStockPrice(stockCode);
					Double cp = stockPrice.getCp();
					// StockDetailStaticHolder.getCP(stock);
					Double currentPrice = stockPrice.getCurrentPrice();
					Double eodPrice = quandlStockPrice.getClose();
					// StockDetailStaticHolder .getCurrentPrice(stock);
					todaysPnl = (portfolioItem.getRemainingQuantity() * eodPrice * cp) / 100;
					pnl = portfolioItem.getRemainingQuantity() * (currentPrice - portfolioItem.getPrice());

					totalPnl = totalPnl + pnl;
					totalTodaysPnl = totalTodaysPnl + todaysPnl;

					message = message + "<tr>"
							+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;"
							+ ((pnl >= 0) ? green : red) + "\">" + stockCode + "</td>"
							+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;\">"
							+ portfolioItem.getRemainingQuantity() + "</td>"
							+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;\">"
							+ nf.format(portfolioItem.getPrice()) + "</td>"
							+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;\">"
							+ nf.format(eodPrice) + "</td>"
							+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;"
							+ ((todaysPnl >= 0) ? green : red) + "\">" + nf.format(todaysPnl) + "</td>"
							+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"border:1px solid black;border-collapse:collapse;text-align:center;"
							+ ((pnl >= 0) ? green : red) + "\">" + nf.format(pnl) + "</td>" + "</tr>";
				}

				message = message + "<tr>"
						+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"font-weight:bold;border:1px solid black;border-collapse:collapse;text-align:center;"
						+ ((totalPnl >= 0) ? green : red) + "\">" + "Total" + "</td>"
						+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"font-weight:bold;border:1px solid black;border-collapse:collapse;text-align:center;\">"
						+ "-" + "</td>"
						+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"font-weight:bold;border:1px solid black;border-collapse:collapse;text-align:center;\">"
						+ "-" + "</td>"
						+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"font-weight:bold;border:1px solid black;border-collapse:collapse;text-align:center;\">"
						+ "-" + "</td>"
						+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"font-weight:bold;border:1px solid black;border-collapse:collapse;text-align:center;"
						+ ((totalTodaysPnl >= 0) ? green : red) + "\">" + nf.format(totalTodaysPnl) + "</td>"
						+ "<td cellpadding=\"5\" cellspacing=\"3\" style=\"font-weight:bold;border:1px solid black;border-collapse:collapse;text-align:center;"
						+ ((totalPnl >= 0) ? green : red) + "\">" + nf.format(totalPnl) + "</td>" + "</tr>";

				message = message + "</table>";
				if (portfolios.getPortfolioItems().size() == 0) {
					message = "<table><tr>Please add information in your Manage portfolio tab for receiving update of your PNL information.</tr></table>";
				}
			}
			return message;
		} else {
			return "";
		}
	}

}