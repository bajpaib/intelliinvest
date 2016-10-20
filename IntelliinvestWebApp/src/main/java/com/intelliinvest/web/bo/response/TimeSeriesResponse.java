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
	private List<Double> priceSeries;
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
		return "TimeSeriesResponse [securityId=" + securityId + ", date=" + date + ", dateSeries=" + dateSeries + ", priceSeries="
				+ priceSeries + ", success=" + success + ", message=" + message + "]";
	}
}
