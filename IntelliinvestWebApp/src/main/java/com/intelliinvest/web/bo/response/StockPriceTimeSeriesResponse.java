package com.intelliinvest.web.bo.response;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

public class StockPriceTimeSeriesResponse {
	private String securityId;
	private double open;
	private double high;
	private double low;
	private double close;
	private double tradedQty;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate date;
	private boolean success;
	private String message;

	public StockPriceTimeSeriesResponse(String securityId, double open, double high, double low, double close,
			double tradedQty, LocalDate date) {
		super();
		this.securityId = securityId;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.tradedQty = tradedQty;
		this.date = date;
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
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

	public double getTradedQty() {
		return tradedQty;
	}

	public void setTradedQty(double tradedQty) {
		this.tradedQty = tradedQty;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

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

	@Override
	public String toString() {
		return "StockPriceTimeSeriesResponse [securityId=" + securityId + ", open=" + open + ", high=" + high + ", low="
				+ low + ", close=" + close + ", tradedQty=" + tradedQty + ", date=" + date + ", success=" + success
				+ ", message=" + message + "]";
	}

}
