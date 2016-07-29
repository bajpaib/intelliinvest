package com.intelliinvest.web.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.intelliinvest.data.model.Portfolio;
import com.intelliinvest.data.model.PortfolioItem;
import com.intelliinvest.data.model.StockDetailStaticHolder;
import com.intelliinvest.data.model.User;
import com.intelliinvest.data.model.UserPortfolio;
import com.intelliinvest.web.common.IntelliinvestException;
import com.intelliinvest.web.util.Helper;
import com.intelliinvest.web.util.MathUtil;

public class UserPortfolioRepository {

	private static Logger logger = Logger.getLogger(UserPortfolioRepository.class);
	private static final String COLLECTION_USER_PORTFOLIO = "USER_PORTFOLIO";
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SequenceRepository sequenceRepository;

	private String SEQ_KEY = "SeqKey";

	private void validateUserLoggedin(String userId) throws IntelliinvestException {
		User user = userRepository.getUserByUserId(userId);
		if (user == null) {
			throw new IntelliinvestException("User " + userId + " already exists");
		}
		if (!user.getLoggedIn()) {
			throw new IntelliinvestException("User " + userId + " is not logged in");
		}
	}

	private void validatePortfolioItem(PortfolioItem item) throws IntelliinvestException {
		if (!(Helper.isNotNullAndNonEmpty(item.getCode()) && Helper.isNotNullAndNonEmpty(item.getDirection())
				&& item.getTradeDate() != null && MathUtil.round(item.getPrice()) > 0 && item.getQuantity() > 0)) {
			throw new IntelliinvestException(
					"Invalid Portfolio Item. Please check code, direction, trade date, price and quantity.");
		}
	}

	public UserPortfolio getUserPortfolioByUserId(String userId) throws DataAccessException {
		logger.info("Inside getUserPortfolioByUserId()...");
		return mongoTemplate.findOne(Query.query(Criteria.where("userId").is(userId)), UserPortfolio.class,
				COLLECTION_USER_PORTFOLIO);
	}

	public Portfolio getPortfolio(String userId, String portfolioName) throws IntelliinvestException {
		logger.info("Inside getPortfolio()...");
		validateUserLoggedin(userId);
		UserPortfolio userPortfolio = getUserPortfolioByUserId(userId);
		if (userPortfolio == null) {
			throw new IntelliinvestException("User " + userId + " does not has a portfolio with name " + portfolioName);
		}
		Portfolio portfolio = null;
		for (Portfolio temp : userPortfolio.getPortfolios()) {
			if (temp.getPortfolioName().equals(portfolioName)) {
				portfolio = temp;
				break;
			}
		}
		if (portfolio == null) {
			throw new IntelliinvestException("User " + userId + " does not has a portfolio with name " + portfolioName);
		}
		return portfolio;
	}

	public String getPortfolioNames(String userId) throws IntelliinvestException {
		logger.info("Inside getPortfolioNames()...");
		String retVal = null;
		validateUserLoggedin(userId);
		UserPortfolio userPortfolio = getUserPortfolioByUserId(userId);
		if (userPortfolio != null) {
			List<String> nameList = new ArrayList<String>();
			for (Portfolio temp : userPortfolio.getPortfolios()) {
				nameList.add(temp.getPortfolioName());
			}
			Collections.sort(nameList);
			StringBuilder builder = new StringBuilder();
			for (String name : nameList) {
				builder.append(name);
				builder.append(",");
			}
			retVal = builder.substring(0, builder.lastIndexOf(",")).toString();
		}
		return retVal;
	}

	public Portfolio addPortfolioItems(String userId, String portfolioName, List<PortfolioItem> portfolioItems)
			throws IntelliinvestException {
		logger.info("Inside addPortfolioItems()...");
		validateUserLoggedin(userId);
		UserPortfolio userPortfolio = getUserPortfolioByUserId(userId);
		if (userPortfolio == null) {
			userPortfolio = new UserPortfolio();
			userPortfolio.setUserId(userId);
		}
		Portfolio portfolio = null;
		for (Portfolio temp : userPortfolio.getPortfolios()) {
			if (temp.getPortfolioName().equals(portfolioName)) {
				portfolio = temp;
				break;
			}
		}
		Date currentDateTime = new Date();
		if (portfolio == null) {
			portfolio = new Portfolio();
			portfolio.setPortfolioName(portfolioName);
			portfolio.setCreateDate(currentDateTime);
			userPortfolio.addPortfolio(portfolio);
		}
		for (PortfolioItem item : portfolioItems) {
			validatePortfolioItem(item);
			item.setPortfolioItemId(sequenceRepository.getNextSequenceId(SEQ_KEY));
			portfolio.addPortfolioItem(item);
		}
		portfolio.setUpdateDate(currentDateTime);
		mongoTemplate.save(userPortfolio, COLLECTION_USER_PORTFOLIO);
		return portfolio;
	}

	public Portfolio updatePortfolioItems(String userId, String portfolioName, List<PortfolioItem> portfolioItems)
			throws IntelliinvestException {
		logger.info("Inside updatePortfolioItems()...");
		validateUserLoggedin(userId);
		UserPortfolio userPortfolio = getUserPortfolioByUserId(userId);
		if (userPortfolio == null) {
			throw new IntelliinvestException("User " + userId + " does not has a portfolio with name " + portfolioName);
		}
		Portfolio portfolio = null;
		for (Portfolio temp : userPortfolio.getPortfolios()) {
			if (temp.getPortfolioName().equals(portfolioName)) {
				portfolio = temp;
				break;
			}
		}
		if (portfolio == null) {
			throw new IntelliinvestException("User " + userId + " does not has a portfolio with name " + portfolioName);
		}
		for (PortfolioItem item : portfolioItems) {
			String portfolioItemId = item.getPortfolioItemId();
			PortfolioItem portfolioItem = portfolio.getPortfolioItemIdById(portfolioItemId);
			if (portfolioItem == null) {
				throw new IntelliinvestException("User " + userId + " does not has a portfolioItem " + portfolioItemId
						+ " in portfolio " + portfolioName);
			}
			validatePortfolioItem(item);
			portfolioItem.setPrice(item.getPrice());
			portfolioItem.setDirection(item.getDirection());
			portfolioItem.setQuantity(item.getQuantity());
			portfolioItem.setTradeDate(item.getTradeDate());
		}
		portfolio.setUpdateDate(new Date());
		mongoTemplate.save(userPortfolio, COLLECTION_USER_PORTFOLIO);
		return portfolio;
	}

	public void deletePortfolio(String userId, String portfolioName) throws IntelliinvestException {
		logger.info("Inside deletePortfolio()...");
		validateUserLoggedin(userId);
		UserPortfolio userPortfolio = getUserPortfolioByUserId(userId);
		if (userPortfolio == null) {
			throw new IntelliinvestException(
					"User " + userId + " does not have a portfolio with name " + portfolioName);
		}
		Portfolio portfolio = null;
		for (Portfolio temp : userPortfolio.getPortfolios()) {
			if (temp.getPortfolioName().equals(portfolioName)) {
				portfolio = temp;
				break;
			}
		}
		if (portfolio == null) {
			throw new IntelliinvestException(
					"User " + userId + " does not have a portfolio with name " + portfolioName);
		}
		userPortfolio.removePortfolioByName(portfolioName);
		mongoTemplate.save(userPortfolio, COLLECTION_USER_PORTFOLIO);
	}

	public Portfolio deletePortfolioItems(String userId, String portfolioName, List<PortfolioItem> portfolioItems)
			throws IntelliinvestException {
		logger.info("Inside deletePortfolioItems()...");
		validateUserLoggedin(userId);
		UserPortfolio userPortfolio = getUserPortfolioByUserId(userId);
		if (userPortfolio == null) {
			throw new IntelliinvestException("User " + userId + " does not has a portfolio with name " + portfolioName);
		}
		Portfolio portfolio = null;
		for (Portfolio temp : userPortfolio.getPortfolios()) {
			if (temp.getPortfolioName().equals(portfolioName)) {
				portfolio = temp;
				break;
			}
		}
		if (portfolio == null) {
			throw new IntelliinvestException("User " + userId + " does not has a portfolio with name " + portfolioName);
		}
		List<PortfolioItem> toBeDeleted = new ArrayList<PortfolioItem>();
		for (PortfolioItem item : portfolioItems) {
			String portfolioItemId = item.getPortfolioItemId();
			PortfolioItem portfolioItem = portfolio.getPortfolioItemIdById(portfolioItemId);
			if (portfolioItem == null) {
				throw new IntelliinvestException("User " + userId + " does not has a portfolioItem " + portfolioItemId
						+ " in portfolio " + portfolioName);
			}
			toBeDeleted.add(portfolioItem);
		}
		Iterator<PortfolioItem> iter = portfolio.getPortfolioItems().listIterator();
		while (iter.hasNext()) {
			PortfolioItem delete = iter.next();
			if (toBeDeleted.contains(delete)) {
				iter.remove();
			}
		}
		portfolio.setUpdateDate(new Date());
		mongoTemplate.save(userPortfolio, COLLECTION_USER_PORTFOLIO);
		return portfolio;
	}

	public List<PortfolioItem> getPortfolioItemsForCode(String code, List<PortfolioItem> items) {
		List<PortfolioItem> retVal = new ArrayList<PortfolioItem>();
		for (PortfolioItem temp : items) {
			if (temp.getCode().equals(code)) {
				retVal.add(temp);
			}
		}
		return retVal;
	}

	public void populatePnlForPortfolioItems(List<PortfolioItem> portfolioItems) {
		Map<String, List<PortfolioItem>> rawSummaryData = new HashMap<String, List<PortfolioItem>>();
		// get a collection of portfolio items for each stock
		for (PortfolioItem portfolioItem : portfolioItems) {
			String code = portfolioItem.getCode();
			if (!rawSummaryData.containsKey(code)) {
				rawSummaryData.put(code, new ArrayList<PortfolioItem>());
			}
			rawSummaryData.get(code).add(portfolioItem);
		}

		// for each stock
		for (String code : rawSummaryData.keySet()) {
			List<PortfolioItem> portfolioItemL = rawSummaryData.get(code);
			Collections.sort(portfolioItemL, new Comparator<PortfolioItem>() {
				@Override
				public int compare(PortfolioItem temp1, PortfolioItem temp2) {
					return temp1.getTradeDate().compareTo(temp2.getTradeDate());
				}
			});
			// divide into and sell list
			List<PortfolioItem> buyList = new ArrayList<PortfolioItem>();
			List<PortfolioItem> sellList = new ArrayList<PortfolioItem>();
			for (PortfolioItem portfolioItemTmp : portfolioItemL) {
				PortfolioItem temp = portfolioItemTmp.clone();
				if (temp.getDirection().equalsIgnoreCase("Sell")) {
					sellList.add(temp);
				} else {
					buyList.add(temp);
				}
			}

			int buyIndex = 0;
			int sellIndex = 0;
			// Set realisedPnL on each sellPortfolioItem. Set
			// setRemainingQuantity as Quantity on each PortfolioItem
			while (buyIndex < buyList.size() && sellIndex < sellList.size()) {
				PortfolioItem buyPortfolioItem = buyList.get(buyIndex);
				PortfolioItem sellPortfolioItem = sellList.get(sellIndex);
				Integer buyQuantity = buyPortfolioItem.getQuantity();
				Integer sellQuantity = sellPortfolioItem.getQuantity();
				double buyPrice = buyPortfolioItem.getPrice();
				double sellPrice = sellPortfolioItem.getPrice();

				if (sellQuantity == buyQuantity) {
					double pnl = (buyQuantity * (sellPrice - buyPrice));
					if (MathUtil.round(sellPortfolioItem.getRealisedPnl()) == 0) {
						sellPortfolioItem.setRealisedPnl(pnl);
					} else {
						sellPortfolioItem.setRealisedPnl(sellPortfolioItem.getRealisedPnl() + pnl);
					}
					buyPortfolioItem.setQuantity(buyQuantity - sellQuantity);
					sellPortfolioItem.setQuantity(buyQuantity - sellQuantity);
					buyIndex++;
					sellIndex++;
				} else if (sellQuantity > buyQuantity) {
					double pnl = (buyQuantity * (sellPrice - buyPrice));
					if (MathUtil.round(sellPortfolioItem.getRealisedPnl()) == 0) {
						sellPortfolioItem.setRealisedPnl(pnl);
					} else {
						sellPortfolioItem.setRealisedPnl(sellPortfolioItem.getRealisedPnl() + pnl);
					}
					sellPortfolioItem.setQuantity(sellQuantity - buyQuantity);
					buyPortfolioItem.setQuantity(0);
					buyIndex++;
				} else {
					double pnl = (sellQuantity * (sellPrice - buyPrice));
					if (MathUtil.round(sellPortfolioItem.getRealisedPnl()) == 0) {
						sellPortfolioItem.setRealisedPnl(pnl);
					} else {
						sellPortfolioItem.setRealisedPnl(sellPortfolioItem.getRealisedPnl() + pnl);
					}
					sellPortfolioItem.setQuantity(0);
					buyPortfolioItem.setQuantity(buyQuantity - sellQuantity);
					sellIndex++;
				}
			}

			for (PortfolioItem portfolioItem : portfolioItemL) {
				// set realisedPnl on original sell portfolio item and remaining
				// quantity on original buy or sell portfolio item
				if (buyList.contains(portfolioItem)) {
					portfolioItem.setRealisedPnl(buyList.get(buyList.indexOf(portfolioItem)).getRealisedPnl());
					Integer remainingQuantity = buyList.get(buyList.indexOf(portfolioItem)).getQuantity();
					portfolioItem.setRemainingQuantity(remainingQuantity);
				} else if (sellList.contains(portfolioItem)) {
					portfolioItem.setRealisedPnl(sellList.get(sellList.indexOf(portfolioItem)).getRealisedPnl());
					Integer remainingQuantity = sellList.get(sellList.indexOf(portfolioItem)).getQuantity();
					portfolioItem.setRemainingQuantity(remainingQuantity);
				}
				double currentPrice = StockDetailStaticHolder.getCurrentPrice(portfolioItem.getCode());
				double eodPrice = StockDetailStaticHolder.getEODPrice(portfolioItem.getCode());
				double cp = StockDetailStaticHolder.getCP(portfolioItem.getCode());
				portfolioItem.setCp(cp);
				portfolioItem.setCurrentPrice(currentPrice);
				portfolioItem.setAmount(portfolioItem.getRemainingQuantity() * currentPrice);
				portfolioItem.setTotalAmount(portfolioItem.getQuantity() * portfolioItem.getPrice());
				portfolioItem.setUnrealisedPnl(
						portfolioItem.getRemainingQuantity() * (currentPrice - portfolioItem.getPrice()));
				portfolioItem.setTodaysPnl((portfolioItem.getRemainingQuantity() * eodPrice * cp) / 100);
			}
		}
	}

	public List<PortfolioItem> getPortfolioSummary(List<PortfolioItem> portfolioItems) {
		Map<String, PortfolioItem> summaryDatas = new HashMap<String, PortfolioItem>();
		Map<String, List<PortfolioItem>> rawSummaryData = new HashMap<String, List<PortfolioItem>>();

		// get a collection of portfolio items for each code
		for (PortfolioItem portfolioItem : portfolioItems) {
			String code = portfolioItem.getCode();
			if (!rawSummaryData.containsKey(code)) {
				rawSummaryData.put(code, new ArrayList<PortfolioItem>());
			}
			rawSummaryData.get(code).add(portfolioItem);
		}
		// for each stock
		for (String code : rawSummaryData.keySet()) {
			PortfolioItem summaryData = new PortfolioItem();
			summaryData.setPortfolioItemId(code);
			summaryData.setCode(code);
			int buyQuantity = 0;
			int sellQuantity = 0;
			int totalBuyQuantity = 0;
			double buyAmount = 0D;
			double sellAmount = 0D;
			double realisedPnl = 0D;
			List<PortfolioItem> portfolioItemL = rawSummaryData.get(code);
			// Only one buy/sell portfolio item will have remaining quantity,
			// hence only one will will contribute to buyQuantity/sellQuantity
			// and
			// buyAmount/sellAmount.
			for (PortfolioItem portfolioItem : portfolioItemL) {
				if (portfolioItem.getDirection().equalsIgnoreCase("Buy")) {
					buyQuantity = buyQuantity + portfolioItem.getRemainingQuantity();
					buyAmount = buyAmount + (portfolioItem.getRemainingQuantity() * portfolioItem.getPrice());
					totalBuyQuantity = totalBuyQuantity + portfolioItem.getQuantity();
				} else {
					sellQuantity = sellQuantity + portfolioItem.getRemainingQuantity();
					sellAmount = sellAmount + (portfolioItem.getRemainingQuantity() * portfolioItem.getPrice());
					realisedPnl = realisedPnl + portfolioItem.getRealisedPnl();
				}
			}
			if ((buyQuantity - sellQuantity) == 0) {
				summaryData.setRemainingQuantity(0);
				summaryData.setQuantity(totalBuyQuantity);
				summaryData.setPrice(0D);
				summaryData.setDirection("Flat");
			} else {
				summaryData.setRemainingQuantity(buyQuantity - sellQuantity);
				summaryData.setQuantity(totalBuyQuantity);
				summaryData.setPrice((buyAmount - sellAmount) / (buyQuantity - sellQuantity));
				summaryData.setDirection(buyQuantity > sellQuantity ? "Long" : "Short");
			}
			summaryData.setRealisedPnl(realisedPnl);
			double currentPrice = StockDetailStaticHolder.getCurrentPrice(summaryData.getCode());
			double eodPrice = StockDetailStaticHolder.getEODPrice(summaryData.getCode());
			double cp = StockDetailStaticHolder.getCP(summaryData.getCode());
			summaryData.setCp(cp);
			summaryData.setCurrentPrice(currentPrice);
			summaryData.setAmount(summaryData.getRemainingQuantity() * currentPrice);
			summaryData.setTotalAmount(summaryData.getQuantity() * summaryData.getPrice());
			summaryData.setUnrealisedPnl(summaryData.getRemainingQuantity() * (currentPrice - summaryData.getPrice()));
			summaryData.setTodaysPnl((summaryData.getRemainingQuantity() * eodPrice * cp) / 100);
			summaryDatas.put(code, summaryData);
		}
		return new ArrayList<PortfolioItem>(summaryDatas.values());
	}
}
