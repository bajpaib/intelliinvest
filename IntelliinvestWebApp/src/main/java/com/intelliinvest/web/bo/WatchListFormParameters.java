package com.intelliinvest.web.bo;

import java.io.Serializable;
import java.util.List;

public class WatchListFormParameters implements Serializable {

	private String userId;

	private List<String> stockCode;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<String> getStockCode() {
		return stockCode;
	}

	public void setStockCode(List<String> stockCode) {
		this.stockCode = stockCode;
	}

	@Override
	public String toString() {
		return "WatchListFormParameters [userId=" + userId + ", stckCode=" + stockCode + "]";
	}

}
