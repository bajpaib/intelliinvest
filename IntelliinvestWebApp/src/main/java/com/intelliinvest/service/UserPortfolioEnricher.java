package com.intelliinvest.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.StockPriceRepository;
import com.intelliinvest.data.model.Portfolio;
import com.intelliinvest.data.model.PortfolioItem;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.data.model.UserPortfolio;
import com.intelliinvest.util.MathUtil;

@Component
public class UserPortfolioEnricher {
	
	private final StockPriceRepository stockPriceRepository;
	
	@Autowired
	public UserPortfolioEnricher(StockPriceRepository stockPriceRepository) {
		this.stockPriceRepository = stockPriceRepository;
	}
	public void enrichUserPortfolio(UserPortfolio userPortfolio){
		if(null==userPortfolio){
			return;
		}
		for(Portfolio portfolio : userPortfolio.getPortfolios()){
			enrichPortfolio(portfolio);
		}
	}
	
	public void enrichPortfolio(Portfolio portfolio){
		if(null==portfolio){
			return;
		}
		portfolio.setSummaryPortfolioItems(getPortfolioSummary(portfolio));
	}
	
	public void populatePnlForPortfolioItems(Collection<PortfolioItem> portfolioItems) {
		if(null==portfolioItems){
			return;
		}
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

				double currentPrice = 0;
				double cp = 0;
				double eodPrice = 0;
				StockPrice stockPrice = stockPriceRepository.getStockPrice(code);
				if (stockPrice != null) {
					currentPrice = stockPrice.getCurrentPrice();
					cp = stockPrice.getCp();
					eodPrice = stockPrice.getEodPrice();
				}
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

	public  Map<String, Collection<PortfolioItem>> getUserPortfolioSummary(UserPortfolio userPortfolio){
		Map<String, Collection<PortfolioItem>> userPortfolioSummary = new HashMap<String, Collection<PortfolioItem>>();
		for(Portfolio portfolio : userPortfolio.getPortfolios()){
			userPortfolioSummary.put(portfolio.getPortfolioName(), getPortfolioSummary(portfolio));
		}
		return userPortfolioSummary;
	}
	
	public Collection<PortfolioItem> getPortfolioSummary(Portfolio portfolio) {
		return getSummaryPortfolioItems(portfolio.getPortfolioItems());
	}
	
	public Collection<PortfolioItem> getSummaryPortfolioItems(Collection<PortfolioItem> portfolioItems) {
		populatePnlForPortfolioItems(portfolioItems);
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

			double currentPrice = 0;
			double cp = 0;
			double eodPrice = 0;
			StockPrice stockPrice = stockPriceRepository.getStockPrice(summaryData.getCode());
			if (stockPrice != null) {
				currentPrice = stockPrice.getCurrentPrice();
				cp = stockPrice.getCp();
				eodPrice = stockPrice.getEodPrice();
			}
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
