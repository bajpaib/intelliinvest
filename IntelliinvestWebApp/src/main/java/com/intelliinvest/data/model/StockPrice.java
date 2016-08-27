package com.intelliinvest.data.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Document(collection = "STOCK_PRICE")
public class StockPrice {

	@Id
	private String code;
	private double cp;
	private double currentPrice;
	private double eodPrice;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate eodDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;

	public StockPrice() {
		super();
	}

	public StockPrice(String code, double cp, double currentPrice, double eodPrice, LocalDate eodDate, LocalDateTime updateDate) {
		super();
		this.code = code;
		this.cp = cp;
		this.currentPrice = currentPrice;
		this.eodPrice = eodPrice;
		this.eodDate = eodDate;
		this.updateDate = updateDate;
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

	@Override
	protected StockPrice clone() throws CloneNotSupportedException {
		return new StockPrice(code, cp, currentPrice, eodPrice, eodDate, updateDate);
	}

}
