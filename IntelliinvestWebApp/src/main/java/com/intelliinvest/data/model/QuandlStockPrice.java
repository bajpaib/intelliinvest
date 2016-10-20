package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = "QUANDL_STOCK_PRICE")
public class QuandlStockPrice  implements Serializable {
	private String securityId;
	private String exchange;
	private String series;
	private double open;
	private double high;
	private double low;
	private double close;
	private double last;
	private double wap;
	private int tradedQty;
	private double turnover;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate eodDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;

	public QuandlStockPrice() {
		super();
	}

	public QuandlStockPrice(String securityId, String exchange, String series, double open, double high, double low,
			double close, double last, double wap, int tradedQty, double turnover, LocalDate eodDate,
			LocalDateTime updateDate) {
		super();
		this.securityId = securityId;
		this.exchange = exchange;
		this.series = series;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.last = last;
		this.wap = wap;
		this.tradedQty = tradedQty;
		this.turnover = turnover;
		this.eodDate = eodDate;
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

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getLast() {
		return last;
	}

	public void setLast(double last) {
		this.last = last;
	}

	public double getWap() {
		return wap;
	}

	public void setWap(double wap) {
		this.wap = wap;
	}

	public int getTradedQty() {
		return tradedQty;
	}

	public void setTradedQty(int tradedQty) {
		this.tradedQty = tradedQty;
	}

	public double getTurnover() {
		return turnover;
	}

	public void setTurnover(double turnover) {
		this.turnover = turnover;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getEodDate() {
		return eodDate;
	}

	public void setEodDate(LocalDate eodDate) {
		this.eodDate = eodDate;
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
		result = prime * result + ((eodDate == null) ? 0 : eodDate.hashCode());
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
		QuandlStockPrice other = (QuandlStockPrice) obj;
		if (eodDate == null) {
			if (other.eodDate != null)
				return false;
		} else if (!eodDate.equals(other.eodDate))
			return false;
		if (securityId == null) {
			if (other.securityId != null)
				return false;
		} else if (!securityId.equals(other.securityId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QuandlStockPrice [securityId=" + securityId + ", exchange=" + exchange + ", series=" + series
				+ ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close + ", last=" + last
				+ ", wap=" + wap + ", tradedQty=" + tradedQty + ", turnover=" + turnover + ", eodDate=" + eodDate
				+ ", updateDate=" + updateDate + "]";
	}

	@Override
	public QuandlStockPrice clone() {
		return new QuandlStockPrice (securityId, exchange, series, open, high, low, close, last, wap, tradedQty, turnover, eodDate,updateDate);
	}
	
}