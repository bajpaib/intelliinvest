package com.intelliinvest.web.bo.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class StockSignalsArchiveResponse {

	private String adxPnl;
	private String bollingerPnl;
	private String oscillatorPnl;
	private String movingAveragePnl;
	private String securityId;
	private String holdBuyPnl;
	private String movingAvgLongTermPnl;

	private double cp;
	private double currentPrice;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime currentPriceUpdateDate;
	private String currentPriceExchange;

	private double eodPrice;
	private String eodPriceExchange;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate eodDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime eodPriceUpdateDate;

	List<StockSignals> stockSignalsList;
	String message;
	boolean success;

	
	
	public String getMovingAvgLongTermPnl() {
		return movingAvgLongTermPnl;
	}

	public void setMovingAvgLongTermPnl(String movingAvgLongTermPnl) {
		this.movingAvgLongTermPnl = movingAvgLongTermPnl;
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

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getCurrentPriceUpdateDate() {
		return currentPriceUpdateDate;
	}

	public void setCurrentPriceUpdateDate(LocalDateTime currentPriceUpdateDate) {
		this.currentPriceUpdateDate = currentPriceUpdateDate;
	}

	public String getCurrentPriceExchange() {
		return currentPriceExchange;
	}

	public void setCurrentPriceExchange(String currentPriceExchange) {
		this.currentPriceExchange = currentPriceExchange;
	}

	public double getEodPrice() {
		return eodPrice;
	}

	public void setEodPrice(double eodPrice) {
		this.eodPrice = eodPrice;
	}

	public String getEodPriceExchange() {
		return eodPriceExchange;
	}

	public void setEodPriceExchange(String eodPriceExchange) {
		this.eodPriceExchange = eodPriceExchange;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getEodDate() {
		return eodDate;
	}

	public void setEodDate(LocalDate eodDate) {
		this.eodDate = eodDate;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getEodPriceUpdateDate() {
		return eodPriceUpdateDate;
	}

	public void setEodPriceUpdateDate(LocalDateTime eodPriceUpdateDate) {
		this.eodPriceUpdateDate = eodPriceUpdateDate;
	}

	public String getMovingAveragePnl() {
		return movingAveragePnl;
	}

	public void setMovingAveragePnl(String movingAveragePnl) {
		this.movingAveragePnl = movingAveragePnl;
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

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public String getHoldBuyPnl() {
		return holdBuyPnl;
	}

	public void setHoldBuyPnl(String holdBuyPnl) {
		this.holdBuyPnl = holdBuyPnl;
	}

	public List<StockSignals> getStockSignalsList() {
		return stockSignalsList;
	}

	public void setStockSignalsList(List<StockSignals> stockSignalsList) {
		this.stockSignalsList = stockSignalsList;
	}

	public String getAdxPnl() {
		return adxPnl;
	}

	public void setAdxPnl(String adxPnl) {
		this.adxPnl = adxPnl;
	}

	public String getOscillatorPnl() {
		return oscillatorPnl;
	}

	public void setOscillatorPnl(String oscillatorPnl) {
		this.oscillatorPnl = oscillatorPnl;
	}

	public String getBollingerPnl() {
		return bollingerPnl;
	}

	public void setBollingerPnl(String bollingerPnl) {
		this.bollingerPnl = bollingerPnl;
	}

}
