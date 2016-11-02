package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

@Document(collection = "STOCK_SIGNALS")
public class StockSignals implements Serializable {
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

	public StockSignals() {
	}

	public StockSignals(String securityId, String adxSignal, LocalDate signalDate, String adxSignalPresent,
			String oscillatorSignal, String signalPresentOscillator, String bollingerSignal,
			String signalPresentBollinger, String movingAverageSignal_SmallTerm, String movingAverageSignal_Main,
			String movingAverageSignal_MidTerm, String movingAverageSignal_LongTerm,
			String movingAverageSignal_SmallTerm_present, String movingAverageSignal_Main_present,
			String movingAverageSignal_MidTerm_present, String movingAverageSignal_LongTerm_present, String aggSignal,
			String aggSignal_present, String aggSignal_previous) {
		super();
		this.securityId = securityId;
		this.adxSignal = adxSignal;
		this.signalDate = signalDate;
		this.adxSignalPresent = adxSignalPresent;
		this.oscillatorSignal = oscillatorSignal;
		this.signalPresentOscillator = signalPresentOscillator;
		this.bollingerSignal = bollingerSignal;
		this.signalPresentBollinger = signalPresentBollinger;
		this.movingAverageSignal_SmallTerm = movingAverageSignal_SmallTerm;
		this.movingAverageSignal_Main = movingAverageSignal_Main;
		this.movingAverageSignal_MidTerm = movingAverageSignal_MidTerm;
		this.movingAverageSignal_LongTerm = movingAverageSignal_LongTerm;
		this.movingAverageSignal_SmallTerm_present = movingAverageSignal_SmallTerm_present;
		this.movingAverageSignal_Main_present = movingAverageSignal_Main_present;
		this.movingAverageSignal_MidTerm_present = movingAverageSignal_MidTerm_present;
		this.movingAverageSignal_LongTerm_present = movingAverageSignal_LongTerm_present;
		this.aggSignal = aggSignal;
		this.aggSignal_present = aggSignal_present;
		this.aggSignal_previous = aggSignal_previous;
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

	public String getSignalPresentOscillator() {
		return signalPresentOscillator;
	}

	public void setSignalPresentOscillator(String signalPresentOscillator) {
		this.signalPresentOscillator = signalPresentOscillator;
	}

	public String getSignalPresentBollinger() {
		return signalPresentBollinger;
	}

	public void setSignalPresentBollinger(String signalPresentBollinger) {
		this.signalPresentBollinger = signalPresentBollinger;
	}

	public String getOscillatorSignal() {
		return oscillatorSignal;
	}

	public void setOscillatorSignal(String oscillatorSignal) {
		this.oscillatorSignal = oscillatorSignal;
	}

	public String getBollingerSignal() {
		return bollingerSignal;
	}

	public void setBollingerSignal(String bollingerSignal) {
		this.bollingerSignal = bollingerSignal;
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	// public String getPreviousSignalType() {
	// return previousSignalType;
	// }
	//
	// public void setPreviousSignalType(String previousSignalType) {
	// this.previousSignalType = previousSignalType;
	// }

	public String getAdxSignal() {
		return adxSignal;
	}

	public void setAdxSignal(String adxSignal) {
		this.adxSignal = adxSignal;
	}

	public String getAdxSignalPresent() {
		return adxSignalPresent;
	}

	public void setAdxSignalPresent(String adxSignalPresent) {
		this.adxSignalPresent = adxSignalPresent;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getSignalDate() {
		return signalDate;
	}

	public void setSignalDate(LocalDate signalDate) {
		this.signalDate = signalDate;
	}

	@Override
	public String toString() {
		return "StockSignals [securityId=" + securityId + ", adxSignal=" + adxSignal + ", signalDate=" + signalDate
				+ ", adxSignalPresent=" + adxSignalPresent + ", oscillatorSignal=" + oscillatorSignal
				+ ", signalPresentOscillator=" + signalPresentOscillator + ", bollingerSignal=" + bollingerSignal
				+ ", signalPresentBollinger=" + signalPresentBollinger + ", movingAverageSignal_SmallTerm="
				+ movingAverageSignal_SmallTerm + ", movingAverageSignal_Main=" + movingAverageSignal_Main
				+ ", movingAverageSignal_MidTerm=" + movingAverageSignal_MidTerm + ", movingAverageSignal_LongTerm="
				+ movingAverageSignal_LongTerm + ", movingAverageSignal_SmallTerm_present="
				+ movingAverageSignal_SmallTerm_present + ", movingAverageSignal_Main_present="
				+ movingAverageSignal_Main_present + ", movingAverageSignal_MidTerm_present="
				+ movingAverageSignal_MidTerm_present + ", movingAverageSignal_LongTerm_present="
				+ movingAverageSignal_LongTerm_present + ", aggSignal=" + aggSignal + ", aggSignal_present="
				+ aggSignal_present + ", aggSignal_previous=" + aggSignal_previous + "]";
	}

}