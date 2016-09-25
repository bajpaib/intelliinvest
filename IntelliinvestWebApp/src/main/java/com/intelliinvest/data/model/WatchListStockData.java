package com.intelliinvest.data.model;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

public class WatchListStockData {
	String code;
	String yesterdaySignalType;
	String signalType;
	Double signalPrice;
	LocalDate signalDate;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getYesterdaySignalType() {
		return yesterdaySignalType;
	}

	public void setYesterdaySignalType(String yesterdaySignalType) {
		this.yesterdaySignalType = yesterdaySignalType;
	}

	public String getSignalType() {
		return signalType;
	}

	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}

	public Double getSignalPrice() {
		return signalPrice;
	}

	public void setSignalPrice(Double signalPrice) {
		this.signalPrice = signalPrice;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getSignalDate() {
		return signalDate;
	}

	public void setSignalDate(LocalDate signalDate) {
		this.signalDate = signalDate;
	}

	@Override
	public String toString() {
		return "WatchListStockData [code=" + code + ", yesterdaySignalType="
				+ yesterdaySignalType + ", signalType=" + signalType
				+ ", signalPrice=" + signalPrice + ", signalDate=" + signalDate
				+ "]";
	}

	
	

}
