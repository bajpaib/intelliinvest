package com.intelliinvest.web.bo.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

@JsonAutoDetect
public class TimeSeriesResponse implements Serializable {

	private String securityId;
	private LocalDate date;	
	private List<String> dateSeries;
	private List<Double> openPriceSeries;
	private List<Double> highPriceSeries;
	private List<Double> lowPriceSeries;
	private List<Double> priceSeries;
	private List<Double> tradedQtySeries;	
	private boolean success;
	private String message;

	public TimeSeriesResponse() {
		super();
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public List<String> getDateSeries() {
		return dateSeries;
	}

	public void setDateSeries(List<String> dateSeries) {
		this.dateSeries = dateSeries;
	}

	public List<Double> getPriceSeries() {
		return priceSeries;
	}

	public void setPriceSeries(List<Double> priceSeries) {
		this.priceSeries = priceSeries;
	}

	public List<Double> getOpenPriceSeries() {
		return openPriceSeries;
	}

	public void setOpenPriceSeries(List<Double> openPriceSeries) {
		this.openPriceSeries = openPriceSeries;
	}

	public List<Double> getHighPriceSeries() {
		return highPriceSeries;
	}

	public void setHighPriceSeries(List<Double> highPriceSeries) {
		this.highPriceSeries = highPriceSeries;
	}

	public List<Double> getLowPriceSeries() {
		return lowPriceSeries;
	}

	public void setLowPriceSeries(List<Double> lowPriceSeries) {
		this.lowPriceSeries = lowPriceSeries;
	}

	public List<Double> getTradedQtySeries() {
		return tradedQtySeries;
	}

	public void setTradedQtySeries(List<Double> tradedQtySeries) {
		this.tradedQtySeries = tradedQtySeries;
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
		return "TimeSeriesResponse [securityId=" + securityId + ", date=" + date + ", dateSeries=" + dateSeries
				+ ", openPriceSeries=" + openPriceSeries + ", highPriceSeries=" + highPriceSeries + ", lowPriceSeries="
				+ lowPriceSeries + ", priceSeries=" + priceSeries + ", tradedQtySeries=" + tradedQtySeries
				+ ", success=" + success + ", message=" + message + "]";
	}
}
