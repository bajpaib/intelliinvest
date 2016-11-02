package com.intelliinvest.web.bo.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class StockSignalsResponse {

	private String securityId;
	
	private String adxSignal = "Wait";

	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate signalDate;

	private String adxSignalPresent;

	private String oscillatorSignal;
	
	private String signalPresentOscillator;

	private String bollingerSignal;
	
	private String signalPresentBollinger;

	private String movingAverageSignal_SmallTerm;
	private String movingAverageSignal_Main;
	private String movingAverageSignal_MidTerm;
	private String movingAverageSignal_LongTerm;

	
	private String movingAverageSignal_SmallTerm_present;
	private String movingAverageSignal_Main_present;
	private String movingAverageSignal_MidTerm_present;
	private String movingAverageSignal_LongTerm_present;

	private String aggSignal;
	private String aggSignal_present;
	private String aggSignal_previous;

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
	
	public String getSecurityId() {
		return securityId;
	}
	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}
	public String getAdxSignal() {
		return adxSignal;
	}
	public void setAdxSignal(String adxSignal) {
		this.adxSignal = adxSignal;
	}
	
	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getSignalDate() {
		return signalDate;
	}
	public void setSignalDate(LocalDate signalDate) {
		this.signalDate = signalDate;
	}
	public String getAdxSignalPresent() {
		return adxSignalPresent;
	}
	public void setAdxSignalPresent(String adxSignalPresent) {
		this.adxSignalPresent = adxSignalPresent;
	}
	public String getOscillatorSignal() {
		return oscillatorSignal;
	}
	public void setOscillatorSignal(String oscillatorSignal) {
		this.oscillatorSignal = oscillatorSignal;
	}
	public String getSignalPresentOscillator() {
		return signalPresentOscillator;
	}
	public void setSignalPresentOscillator(String signalPresentOscillator) {
		this.signalPresentOscillator = signalPresentOscillator;
	}
	public String getBollingerSignal() {
		return bollingerSignal;
	}
	public void setBollingerSignal(String bollingerSignal) {
		this.bollingerSignal = bollingerSignal;
	}
	public String getSignalPresentBollinger() {
		return signalPresentBollinger;
	}
	public void setSignalPresentBollinger(String signalPresentBollinger) {
		this.signalPresentBollinger = signalPresentBollinger;
	}
	public String getMovingAverageSignal_SmallTerm() {
		return movingAverageSignal_SmallTerm;
	}
	public void setMovingAverageSignal_SmallTerm(String movingAverageSignal_SmallTerm) {
		this.movingAverageSignal_SmallTerm = movingAverageSignal_SmallTerm;
	}
	public String getMovingAverageSignal_Main() {
		return movingAverageSignal_Main;
	}
	public void setMovingAverageSignal_Main(String movingAverageSignal_Main) {
		this.movingAverageSignal_Main = movingAverageSignal_Main;
	}
	public String getMovingAverageSignal_MidTerm() {
		return movingAverageSignal_MidTerm;
	}
	public void setMovingAverageSignal_MidTerm(String movingAverageSignal_MidTerm) {
		this.movingAverageSignal_MidTerm = movingAverageSignal_MidTerm;
	}
	public String getMovingAverageSignal_LongTerm() {
		return movingAverageSignal_LongTerm;
	}
	public void setMovingAverageSignal_LongTerm(String movingAverageSignal_LongTerm) {
		this.movingAverageSignal_LongTerm = movingAverageSignal_LongTerm;
	}
	public String getMovingAverageSignal_SmallTerm_present() {
		return movingAverageSignal_SmallTerm_present;
	}
	public void setMovingAverageSignal_SmallTerm_present(String movingAverageSignal_SmallTerm_present) {
		this.movingAverageSignal_SmallTerm_present = movingAverageSignal_SmallTerm_present;
	}
	public String getMovingAverageSignal_Main_present() {
		return movingAverageSignal_Main_present;
	}
	public void setMovingAverageSignal_Main_present(String movingAverageSignal_Main_present) {
		this.movingAverageSignal_Main_present = movingAverageSignal_Main_present;
	}
	public String getMovingAverageSignal_MidTerm_present() {
		return movingAverageSignal_MidTerm_present;
	}
	public void setMovingAverageSignal_MidTerm_present(String movingAverageSignal_MidTerm_present) {
		this.movingAverageSignal_MidTerm_present = movingAverageSignal_MidTerm_present;
	}
	public String getMovingAverageSignal_LongTerm_present() {
		return movingAverageSignal_LongTerm_present;
	}
	public void setMovingAverageSignal_LongTerm_present(String movingAverageSignal_LongTerm_present) {
		this.movingAverageSignal_LongTerm_present = movingAverageSignal_LongTerm_present;
	}
	public String getAggSignal() {
		return aggSignal;
	}
	public void setAggSignal(String aggSignal) {
		this.aggSignal = aggSignal;
	}
	public String getAggSignal_present() {
		return aggSignal_present;
	}
	public void setAggSignal_present(String aggSignal_present) {
		this.aggSignal_present = aggSignal_present;
	}
	public String getAggSignal_previous() {
		return aggSignal_previous;
	}
	public void setAggSignal_previous(String aggSignal_previous) {
		this.aggSignal_previous = aggSignal_previous;
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
	
}
