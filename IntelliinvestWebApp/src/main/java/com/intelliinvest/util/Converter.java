package com.intelliinvest.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.data.model.User;
import com.intelliinvest.web.bo.StockPriceResponse;
import com.intelliinvest.web.bo.StockResponse;
import com.intelliinvest.web.bo.UserResponse;

public class Converter {

	public static List<UserResponse> convertUsersList(List<User> userDetails) {
		List<UserResponse> userResponseList = new ArrayList<UserResponse>();
		if (userDetails != null) {
			for (User user : userDetails) {
				userResponseList.add(getUserResponse(user));
			}
		}
		return userResponseList;
	}

	public static UserResponse getUserResponse(User user) {
		UserResponse userResponse = new UserResponse();
		userResponse.setUserId(user.getUserId());
		userResponse.setUsername(user.getUsername());
		userResponse.setPhone(user.getPhone());
		userResponse.setPlan(user.getPlan());
		userResponse.setUserType(user.getUserType());
		userResponse.setActive(user.getActive());
		userResponse.setActivationCode(user.getActivationCode());
		userResponse.setCreateDate(user.getCreateDate());
		userResponse.setUpdateDate(user.getUpdateDate());
		userResponse.setRenewalDate(user.getRenewalDate());
		userResponse.setExpiryDate(user.getExpiryDate());
		userResponse.setLoggedIn(user.getLoggedIn());
		userResponse.setLastLoginDate(user.getLastLoginDate());
		userResponse.setSendNotification(user.getSendNotification());
		return userResponse;
	}

	public static List<StockResponse> convertStockList(List<Stock> stocks) {
		List<StockResponse> stockResponseList = new ArrayList<StockResponse>();
		if (stocks != null) {
			for (Stock stock : stocks) {
				stockResponseList.add(getStockResponse(stock));
			}
		}

		stockResponseList.sort(new Comparator<StockResponse>() {
			public int compare(StockResponse item1, StockResponse item2) {
				return item1.getSecurityId().compareTo(item2.getSecurityId());
			}
		});
		return stockResponseList;
	}
	
	public static StockResponse getStockResponse(Stock stock) {
		StockResponse stockResponse = new StockResponse();
		stockResponse.setSecurityId(stock.getSecurityId());
		stockResponse.setBseCode(stock.getBseCode());
		stockResponse.setNseCode(stock.getNseCode());
		stockResponse.setName(stock.getName());
		stockResponse.setIsin(stock.getIsin());
		stockResponse.setIndustry(stock.getIndustry());		
		stockResponse.setWorldStock(stock.isWorldStock());
		stockResponse.setNiftyStock(stock.isNiftyStock());
		stockResponse.setNseStock(stock.isNseStock());
		stockResponse.setUpdateDate(stock.getUpdateDate());
		stockResponse.setSuccess(true);
		return stockResponse;
	}

	public static List<StockPriceResponse> convertStockPriceList(List<StockPrice> prices, Map<String, QuandlStockPrice> quandlStockPrices) {
		List<StockPriceResponse> stockResponseList = new ArrayList<StockPriceResponse>();
		if (prices != null) {
			for (StockPrice price : prices) {
				stockResponseList.add(getStockPriceResponse(price, quandlStockPrices.get(price.getSecurityId())));
			}
		}

		stockResponseList.sort(new Comparator<StockPriceResponse>() {
			public int compare(StockPriceResponse item1, StockPriceResponse item2) {
				return item1.getSecurityId().compareTo(item2.getSecurityId());
			}
		});

		return stockResponseList;
	}

	public static StockPriceResponse getStockPriceResponse(StockPrice price, QuandlStockPrice quandlStockPrice) {
		StockPriceResponse stockPriceResponse = new StockPriceResponse();
		stockPriceResponse.setSecurityId(price.getSecurityId());
		stockPriceResponse.setCp(price.getCp());
		stockPriceResponse.setCurrentPrice(price.getCurrentPrice());
		stockPriceResponse.setCurrentPriceExchange(price.getExchange());
		stockPriceResponse.setCurrentPriceUpdateDate(price.getUpdateDate());
		if(quandlStockPrice!=null){
			stockPriceResponse.setEodPrice(quandlStockPrice.getClose());
			stockPriceResponse.setEodDate(quandlStockPrice.getEodDate());
			stockPriceResponse.setEodPriceExchange(quandlStockPrice.getExchange());
			stockPriceResponse.setEodPriceUpdateDate(quandlStockPrice.getUpdateDate());
		}

		stockPriceResponse.setSuccess(true);
		return stockPriceResponse;
	}

}
