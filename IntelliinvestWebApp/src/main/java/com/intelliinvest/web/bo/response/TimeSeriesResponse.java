package com.intelliinvest.web.bo.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

@JsonAutoDetect
public class TimeSeriesResponse implements Serializable {
	private String securityId;
	private LocalDate date;
	private List<StockPriceTimeSeriesResponse> stockPriceTimeSeries = new ArrayList<StockPriceTimeSeriesResponse>();
//	private ForecastedStockPriceResponse forecastedStockPriceResponse = null;
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

	public List<StockPriceTimeSeriesResponse> getStockPriceTimeSeries() {
		return stockPriceTimeSeries;
	}

	public void addStockPriceTimeSeries(StockPriceTimeSeriesResponse response) {
		this.stockPriceTimeSeries.add(response);
	}

/*	public ForecastedStockPriceResponse getForecastedStockPriceResponse() {
		return forecastedStockPriceResponse;
	}

	public void setForecastedStockPriceResponse(ForecastedStockPriceResponse forecastedStockPriceResponse) {
		this.forecastedStockPriceResponse = forecastedStockPriceResponse;
	}*/

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
		return "TimeSeriesResponse [securityId=" + securityId + ", date=" + date + ", stockPriceTimeSeries="
				+ stockPriceTimeSeries + ", success=" + success + ", message=" + message + "]";
	}

}
