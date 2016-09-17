package com.intelliinvest.data.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = "STOCK_PRICE")
public class StockPrice {
	@Id
	private String securityId;
	private String exchange;
	private double cp;
	private double currentPrice;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;

	public StockPrice() {
		super();
	}

	public StockPrice(String securityId, String exchange, double cp, double currentPrice, LocalDateTime updateDate) {
		super();
		this.securityId = securityId;
		this.exchange = exchange;
		this.cp = cp;
		this.currentPrice = currentPrice;
		this.updateDate = updateDate;
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
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

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((securityId == null) ? 0 : securityId.hashCode());
		return result;
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
		if (securityId == null) {
			if (other.securityId != null)
				return false;
		} else if (!securityId.equals(other.securityId))
			return false;
		return true;
	}

	
	@Override
	public String toString() {
		return "StockPrice [securityId=" + securityId + ", exchange=" + exchange + ", cp=" + cp + ", currentPrice="
				+ currentPrice + ", updateDate=" + updateDate + "]";
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new StockPrice(securityId, exchange, cp, currentPrice, updateDate);
	}

}
