package com.intelliinvest.web.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
				return item1.getCode().compareTo(item2.getCode());
			}
		});
		return stockResponseList;
	}

	public static StockResponse getStockResponse(Stock stock) {
		StockResponse stockResponse = new StockResponse();
		stockResponse.setCode(stock.getCode());
		stockResponse.setName(stock.getName());
		stockResponse.setNiftyStock(stock.isNiftyStock());
		stockResponse.setWorldStock(stock.isWorldStock());
		stockResponse.setUpdateDate(stock.getUpdateDate());
		stockResponse.setSuccess(true);
		return stockResponse;
	}

	public static List<StockPriceResponse> convertStockPriceList(List<StockPrice> prices) {
		List<StockPriceResponse> stockResponseList = new ArrayList<StockPriceResponse>();
		if (prices != null) {
			for (StockPrice price : prices) {
				stockResponseList.add(getStockPriceResponse(price));
			}
		}

		stockResponseList.sort(new Comparator<StockPriceResponse>() {
			public int compare(StockPriceResponse item1, StockPriceResponse item2) {
				return item1.getCode().compareTo(item2.getCode());
			}
		});

		return stockResponseList;
	}

	public static StockPriceResponse getStockPriceResponse(StockPrice price) {
		StockPriceResponse stockPriceResponse = new StockPriceResponse();
		stockPriceResponse.setCode(price.getCode());
		stockPriceResponse.setCp(price.getCp());
		stockPriceResponse.setCurrentPrice(price.getCurrentPrice());
		stockPriceResponse.setEodDate(price.getEodDate());
		stockPriceResponse.setEodPrice(price.getEodPrice());
		stockPriceResponse.setUpdateDate(price.getUpdateDate());
		stockPriceResponse.setSuccess(true);
		return stockPriceResponse;
	}

}
