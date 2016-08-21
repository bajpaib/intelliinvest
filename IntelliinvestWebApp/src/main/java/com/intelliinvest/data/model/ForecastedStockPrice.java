package com.intelliinvest.data.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

public class ForecastedStockPrice {

	private String code;
	private double forecastPrice;
	@DateTimeFormat(iso = ISO.DATE)
	private Date forecastDate;
	@DateTimeFormat(iso = ISO.DATE)
	private Date updateDate;
	
	public ForecastedStockPrice(String code, double forecastPrice, Date forecastDate, Date updateDate) {
		super();
		this.code = code;
		this.forecastPrice = forecastPrice;
		this.forecastDate = forecastDate;
		this.updateDate = updateDate;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public double getForecastPrice() {
		return forecastPrice;
	}

	public void setForecastPrice(double forecastPrice) {
		this.forecastPrice = forecastPrice;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getForecastDate() {
		return forecastDate;
	}

	public void setForecastDate(Date forecastDate) {
		this.forecastDate = forecastDate;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public String toString() {
		return "ForecastedStockPrice [code=" + code + ", forecastPrice=" + forecastPrice + ", forecastDate="
				+ forecastDate + ", updateDate=" + updateDate + "]";
	}

}
