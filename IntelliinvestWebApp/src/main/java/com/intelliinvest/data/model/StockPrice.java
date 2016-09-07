package com.intelliinvest.data.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class StockPrice {

	private String code;
	private double cp = 0d;
	private double currentPrice = 0d;
	private double eodPrice = 0d;
	@JsonSerialize(using=JsonDateSerializer.class)
	private LocalDate eodDate;
	@JsonSerialize(using=JsonDateTimeSerializer.class)
	private LocalDateTime updateDate;

	public StockPrice(String code) {
		super();
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public double getCp() {
		return cp;
	}

	public void setCp(double cp) {
		this.cp = cp;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public double getEodPrice() {
		return eodPrice;
	}

	public void setEodPrice(double eodPrice) {
		this.eodPrice = eodPrice;
	}

	public LocalDate getEodDate() {
		return eodDate;
	}

	public void setEodDate(LocalDate eodDate) {
		this.eodDate = eodDate;
	}

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockPrice other = (StockPrice) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StockPrice [code=" + code + ", cp=" + cp + ", currentPrice=" + currentPrice + ", eodPrice=" + eodPrice
				+ ", eodDate=" + eodDate + ", updateDate=" + updateDate + "]";
	}
}
