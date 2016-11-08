package com.intelliinvest.web.bo.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class ForecastedStockPriceResponse {
	private String securityId;
	private double tomorrowForecastPrice;
	private double weeklyForecastPrice;
	private double monthlyForecastPrice;
	private LocalDate todayDate;
	private LocalDate tomorrowForecastDate;
	private LocalDate weeklyForecastDate;
	private LocalDate monthlyForecastDate;
	private double tomorrowPctReturn;
	private double weeklyPctReturn;
	private double monthlyPctReturn;
	private String tomorrowView;
	private String weeklyView;
	private String monthlyView;
	private LocalDateTime updateDate;
	private boolean success;
	private String message;

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

	public double getTomorrowPctReturn() {
		return tomorrowPctReturn;
	}

	public void setTomorrowPctReturn(double tomorrowPctReturn) {
		this.tomorrowPctReturn = tomorrowPctReturn;
	}

	public double getWeeklyPctReturn() {
		return weeklyPctReturn;
	}

	public void setWeeklyPctReturn(double weeklyPctReturn) {
		this.weeklyPctReturn = weeklyPctReturn;
	}

	public double getMonthlyPctReturn() {
		return monthlyPctReturn;
	}

	public void setMonthlyPctReturn(double monthlyPctReturn) {
		this.monthlyPctReturn = monthlyPctReturn;
	}

	public String getTomorrowView() {
		return tomorrowView;
	}

	public void setTomorrowView(String tomorrowView) {
		this.tomorrowView = tomorrowView;
	}

	public String getWeeklyView() {
		return weeklyView;
	}

	public void setWeeklyView(String weeklyView) {
		this.weeklyView = weeklyView;
	}

	public String getMonthlyView() {
		return monthlyView;
	}

	public void setMonthlyView(String monthlyView) {
		this.monthlyView = monthlyView;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
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
}
