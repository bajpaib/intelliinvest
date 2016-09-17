package com.intelliinvest.data.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

/**
 * todayDate (T) dailyForecastDate (T+1) weeklyForecastDate (T+5)
 * monthlyForecastDate (T+20)
 *
 */
@Document(collection = "STOCK_PRICE_FORECAST")
public class ForecastedStockPrice {
	private String securityId;
	private double tomorrowForecastPrice;
	private double weeklyForecastPrice;
	private double monthlyForecastPrice;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate todayDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate tomorrowForecastDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate weeklyForecastDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate monthlyForecastDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;

	public ForecastedStockPrice() {
		super();
	}

	public ForecastedStockPrice(String securityId, double tomorrowForecastPrice, double weeklyForecastPrice,
			double monthlyForecastPrice, LocalDate todayDate, LocalDate tomorrowForecastDate,
			LocalDate weeklyForecastDate, LocalDate monthlyForecastDate, LocalDateTime updateDate) {
		super();
		this.securityId = securityId;
		this.tomorrowForecastPrice = tomorrowForecastPrice;
		this.weeklyForecastPrice = weeklyForecastPrice;
		this.monthlyForecastPrice = monthlyForecastPrice;
		this.todayDate = todayDate;
		this.tomorrowForecastDate = tomorrowForecastDate;
		this.weeklyForecastDate = weeklyForecastDate;
		this.monthlyForecastDate = monthlyForecastDate;
		this.updateDate = updateDate;
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public double getTomorrowForecastPrice() {
		return tomorrowForecastPrice;
	}

	public void setTomorrowForecastPrice(double tomorrowForecastPrice) {
		this.tomorrowForecastPrice = tomorrowForecastPrice;
	}

	public double getWeeklyForecastPrice() {
		return weeklyForecastPrice;
	}

	public void setWeeklyForecastPrice(double weeklyForecastPrice) {
		this.weeklyForecastPrice = weeklyForecastPrice;
	}

	public double getMonthlyForecastPrice() {
		return monthlyForecastPrice;
	}

	public void setMonthlyForecastPrice(double monthlyForecastPrice) {
		this.monthlyForecastPrice = monthlyForecastPrice;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getTodayDate() {
		return todayDate;
	}

	public void setTodayDate(LocalDate todayDate) {
		this.todayDate = todayDate;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getTomorrowForecastDate() {
		return tomorrowForecastDate;
	}

	public void setTomorrowForecastDate(LocalDate tomorrowForecastDate) {
		this.tomorrowForecastDate = tomorrowForecastDate;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getWeeklyForecastDate() {
		return weeklyForecastDate;
	}

	public void setWeeklyForecastDate(LocalDate weeklyForecastDate) {
		this.weeklyForecastDate = weeklyForecastDate;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getMonthlyForecastDate() {
		return monthlyForecastDate;
	}

	public void setMonthlyForecastDate(LocalDate monthlyForecastDate) {
		this.monthlyForecastDate = monthlyForecastDate;
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
		return "ForecastedStockPrice [securityId=" + securityId + ", tomorrowForecastPrice=" + tomorrowForecastPrice
				+ ", weeklyForecastPrice=" + weeklyForecastPrice + ", monthlyForecastPrice=" + monthlyForecastPrice
				+ ", todayDate=" + todayDate + ", tomorrowForecastDate=" + tomorrowForecastDate
				+ ", weeklyForecastDate=" + weeklyForecastDate + ", monthlyForecastDate=" + monthlyForecastDate
				+ ", updateDate=" + updateDate + "]";
	}

}