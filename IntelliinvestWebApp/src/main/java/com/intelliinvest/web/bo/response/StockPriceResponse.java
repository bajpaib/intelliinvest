package com.intelliinvest.web.bo.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

@JsonAutoDetect
public class StockPriceResponse implements Serializable {
	private String securityId;
	private String name;
	private double cp;
	private double currentPrice;
	private double eodPrice;
	private LocalDate eodDate;
	private String currentPriceExchange;
	private String eodPriceExchange;
	private LocalDateTime currentPriceUpdateDate;
	private LocalDateTime eodPriceUpdateDate;
	private String alReturnOnEquity;
	private double pctChange;
	private boolean success;
	private String message;

	public StockPriceResponse() {
		super();
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getCp() {
		return cp;
	}

	public void setCp(double cp) {
		this.cp = cp;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public double getEodPrice() {
		return eodPrice;
	}

	public void setEodPrice(double eodPrice) {
		this.eodPrice = eodPrice;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getEodDate() {
		return eodDate;
	}

	public void setEodDate(LocalDate eodDate) {
		this.eodDate = eodDate;
	}

	public String getCurrentPriceExchange() {
		return currentPriceExchange;
	}

	public void setCurrentPriceExchange(String currentPriceExchange) {
		this.currentPriceExchange = currentPriceExchange;
	}

	public String getEodPriceExchange() {
		return eodPriceExchange;
	}

	public void setEodPriceExchange(String eodPriceExchange) {
		this.eodPriceExchange = eodPriceExchange;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getCurrentPriceUpdateDate() {
		return currentPriceUpdateDate;
	}

	public void setCurrentPriceUpdateDate(LocalDateTime currentPriceUpdateDate) {
		this.currentPriceUpdateDate = currentPriceUpdateDate;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getEodPriceUpdateDate() {
		return eodPriceUpdateDate;
	}

	public void setEodPriceUpdateDate(LocalDateTime eodPriceUpdateDate) {
		this.eodPriceUpdateDate = eodPriceUpdateDate;
	}
	
	public double getPctChange() {
		return pctChange;
	}

	public void setPctChange(double pctChange) {
		this.pctChange = pctChange;
	}

	public String getAlReturnOnEquity() {
		return alReturnOnEquity;
	}

	public void setAlReturnOnEquity(String alReturnOnEquity) {
		this.alReturnOnEquity = alReturnOnEquity;
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
