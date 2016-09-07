package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intelliinvest.util.DateUtil;

@Document(collection = LiveStockPrice.COLLECTION_NAME)
@CompoundIndexes({
	@CompoundIndex(name = "LIVE_STOCK_PRICE_IDX", def = "{'exchange': 1, 'code': 1}") })
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveStockPrice implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String COLLECTION_NAME = "LIVE_STOCK_PRICE";
	@JsonProperty("t")
	private String code;

	@JsonProperty("l_fix")
	private Double price;

	@JsonProperty("cp")
	private Double changePercent;

	@JsonProperty("lt")
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="MMM dd, hh:mmaa z")
	private LocalDate lastTraded;

	@JsonProperty("e")
	private String exchange;
	
	private LocalDateTime lastUpdated = DateUtil.getLocalDateTime();
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getExchange() {
		return exchange;
	}
	
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	
	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getChangePercent() {
		return changePercent;
	}

	public void setChangePercent(Double changePercent) {
		this.changePercent = changePercent;
	}

	public LocalDate getLastTraded() {
		return lastTraded;
	}

	public void setLastTraded(LocalDate lastTraded) {
		this.lastTraded = lastTraded;
	}
	
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}
	
	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	@Override
	public String toString() {
		return "GoogleStockPrice [code=" + code + ", price=" + price + ", changePercent=" + changePercent + ", lastTraded=" + lastTraded + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiveStockPrice other = (LiveStockPrice) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

}
