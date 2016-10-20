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
	String symbol;
	// String previousSignalType = "";
	String signalType = "Wait";

	@DateTimeFormat(iso = ISO.DATE)
	LocalDate signalDate;
	String signalPresent;

	String oscillatorSignal;
	// String previousOscillatorSignal = "";
	String signalPresentOscillator;

	String bollingerSignal;
	// String previousBollingerSignal = "";
	String signalPresentBollinger;

	private String movingAverageSignal_SmallTerm;
	private String movingAverageSignal_Main;
	private String movingAverageSignal_MidTerm;
	private String movingAverageSignal_LongTerm;

	// private String previousMovingAverageSignal_SmallTerm;
	// private String previousMovingAverageSignal_Main;
	// private String previousMovingAverageSignal_MidTerm;
	// private String previousMovingAverageSignal_LongTerm;

	private String movingAverageSignal_SmallTerm_present;
	private String movingAverageSignal_Main_present;
	private String movingAverageSignal_MidTerm_present;
	private String movingAverageSignal_LongTerm_present;

	private String aggSignal;
	private String aggSignal_present;
	private String aggSignal_previous;

	public StockSignals() {
	}

	
	public StockSignals(String symbol, String signalType, LocalDate signalDate,
			String signalPresent, String oscillatorSignal,
			String signalPresentOscillator, String bollingerSignal,
			String signalPresentBollinger,
			String movingAverageSignal_SmallTerm,
			String movingAverageSignal_Main,
			String movingAverageSignal_MidTerm,
			String movingAverageSignal_LongTerm,
			String movingAverageSignal_SmallTerm_present,
			String movingAverageSignal_Main_present,
			String movingAverageSignal_MidTerm_present,
			String movingAverageSignal_LongTerm_present, String aggSignal,
			String aggSignal_present, String aggSignal_previous) {
		super();
		this.symbol = symbol;
		this.signalType = signalType;
		this.signalDate = signalDate;
		this.signalPresent = signalPresent;
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

	public void setMovingAverageSignal_SmallTerm(
			String movingAverageSignal_SmallTerm) {
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

	public void setMovingAverageSignal_MidTerm(
			String movingAverageSignal_MidTerm) {
		this.movingAverageSignal_MidTerm = movingAverageSignal_MidTerm;
	}

	public String getMovingAverageSignal_LongTerm() {
		return movingAverageSignal_LongTerm;
	}

	public void setMovingAverageSignal_LongTerm(
			String movingAverageSignal_LongTerm) {
		this.movingAverageSignal_LongTerm = movingAverageSignal_LongTerm;
	}

	public String getMovingAverageSignal_SmallTerm_present() {
		return movingAverageSignal_SmallTerm_present;
	}

	public void setMovingAverageSignal_SmallTerm_present(
			String movingAverageSignal_SmallTerm_present) {
		this.movingAverageSignal_SmallTerm_present = movingAverageSignal_SmallTerm_present;
	}

	public String getMovingAverageSignal_Main_present() {
		return movingAverageSignal_Main_present;
	}

	public void setMovingAverageSignal_Main_present(
			String movingAverageSignal_Main_present) {
		this.movingAverageSignal_Main_present = movingAverageSignal_Main_present;
	}

	public String getMovingAverageSignal_MidTerm_present() {
		return movingAverageSignal_MidTerm_present;
	}

	public void setMovingAverageSignal_MidTerm_present(
			String movingAverageSignal_MidTerm_present) {
		this.movingAverageSignal_MidTerm_present = movingAverageSignal_MidTerm_present;
	}

	public String getMovingAverageSignal_LongTerm_present() {
		return movingAverageSignal_LongTerm_present;
	}

	public void setMovingAverageSignal_LongTerm_present(
			String movingAverageSignal_LongTerm_present) {
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

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	// public String getPreviousSignalType() {
	// return previousSignalType;
	// }
	//
	// public void setPreviousSignalType(String previousSignalType) {
	// this.previousSignalType = previousSignalType;
	// }

	public String getSignalType() {
		return signalType;
	}

	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}

	public String getSignalPresent() {
		return signalPresent;
	}

	public void setSignalPresent(String signalPresent) {
		this.signalPresent = signalPresent;
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
		return "StockSignals [symbol=" + symbol + ", signalType=" + signalType
				+ ", signalDate=" + signalDate + ", signalPresent="
				+ signalPresent + ", oscillatorSignal=" + oscillatorSignal
				+ ", signalPresentOscillator=" + signalPresentOscillator
				+ ", bollingerSignal=" + bollingerSignal
				+ ", signalPresentBollinger=" + signalPresentBollinger
				+ ", movingAverageSignal_SmallTerm="
				+ movingAverageSignal_SmallTerm + ", movingAverageSignal_Main="
				+ movingAverageSignal_Main + ", movingAverageSignal_MidTerm="
				+ movingAverageSignal_MidTerm
				+ ", movingAverageSignal_LongTerm="
				+ movingAverageSignal_LongTerm
				+ ", movingAverageSignal_SmallTerm_present="
				+ movingAverageSignal_SmallTerm_present
				+ ", movingAverageSignal_Main_present="
				+ movingAverageSignal_Main_present
				+ ", movingAverageSignal_MidTerm_present="
				+ movingAverageSignal_MidTerm_present
				+ ", movingAverageSignal_LongTerm_present="
				+ movingAverageSignal_LongTerm_present + ", iisignal="
				+ aggSignal + ", iisignal_present=" + aggSignal_present
				+ ", iisignal_previous=" + aggSignal_previous + "]";
	}

}