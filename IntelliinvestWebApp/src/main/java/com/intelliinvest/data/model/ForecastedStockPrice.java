package com.intelliinvest.data.model;



import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class ForecastedStockPrice {

	private String code;
	private double forecastPrice;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate forecastDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;
	
	public ForecastedStockPrice(String code, double forecastPrice, LocalDate forecastDate, LocalDateTime updateDate) {
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
	public LocalDate getForecastDate() {
		return forecastDate;
	}

	public void setForecastDate(LocalDate forecastDate) {
		this.forecastDate = forecastDate;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public String toString() {
		return "ForecastedStockPrice [code=" + code + ", forecastPrice=" + forecastPrice + ", forecastDate="
				+ forecastDate + ", updateDate=" + updateDate + "]";
	}

}
