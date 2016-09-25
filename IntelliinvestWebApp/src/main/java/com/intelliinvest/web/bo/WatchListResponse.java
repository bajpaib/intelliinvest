package com.intelliinvest.web.bo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.intelliinvest.data.model.WatchListStockData;

@JsonAutoDetect
public class WatchListResponse {

	private String userId;
	private List<WatchListStockData> stocksData;
	private boolean success;
	private String message;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<WatchListStockData> getStocksData() {
		return stocksData;
	}

	public void setStocksData(List<WatchListStockData> stocksData) {
		this.stocksData = stocksData;
	}

	@Override
	public String toString() {
		return "WatchListResponse [userId=" + userId + ", stockDatas=" + stocksData + "]";
	}

}
