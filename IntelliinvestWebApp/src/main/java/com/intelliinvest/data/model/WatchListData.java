package com.intelliinvest.data.model;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "WATCHLIST")
public class WatchListData implements Serializable {
	String userId;
	String code;
	
	public WatchListData() {
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public boolean equals(Object obj) {
		WatchListData simulationData = (WatchListData) obj;
		if (simulationData.code.equals(this.code) && simulationData.userId.equals(this.userId)) {
			return true;
		} else {
			return false;
		}
	}

}
