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
	String previousSignalType = "";
	String signalType = "Wait";

	@DateTimeFormat(iso = ISO.DATE)
	LocalDate signalDate;
	String signalPresent;

	String oscillatorSignal;
	String previousOscillatorSignal = "";
	String signalPresentOscillator;

	String bollingerSignal;
	String previousBollingerSignal = "";
	String signalPresentBollinger;

	public StockSignals() {
	}

	public StockSignals(String symbol, String previousSignalType, String signalType, LocalDate signalDate,
			String signalPresent, String oscillatorSignal, String previousOscillatorSignal,
			String signalPresentOscillator, String bollingerSignal, String previousBollingerSignal,
			String signalPresentBollinger) {
		super();
		this.symbol = symbol;
		this.previousSignalType = previousSignalType;
		this.signalType = signalType;
		this.signalDate = signalDate;
		this.signalPresent = signalPresent;
		this.oscillatorSignal = oscillatorSignal;
		this.previousOscillatorSignal = previousOscillatorSignal;
		this.signalPresentOscillator = signalPresentOscillator;
		this.bollingerSignal = bollingerSignal;
		this.previousBollingerSignal = previousBollingerSignal;
		this.signalPresentBollinger = signalPresentBollinger;
	}

	public String getPreviousOscillatorSignal() {
		return previousOscillatorSignal;
	}

	public void setPreviousOscillatorSignal(String previousOscillatorSignal) {
		this.previousOscillatorSignal = previousOscillatorSignal;
	}

	public String getSignalPresentOscillator() {
		return signalPresentOscillator;
	}

	public void setSignalPresentOscillator(String signalPresentOscillator) {
		this.signalPresentOscillator = signalPresentOscillator;
	}

	public String getPreviousBollingerSignal() {
		return previousBollingerSignal;
	}

	public void setPreviousBollingerSignal(String previousBollingerSignal) {
		this.previousBollingerSignal = previousBollingerSignal;
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

	public String getPreviousSignalType() {
		return previousSignalType;
	}

	public void setPreviousSignalType(String previousSignalType) {
		this.previousSignalType = previousSignalType;
	}

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
		return "StockSignals [symbol=" + symbol + ", previousSignalType=" + previousSignalType + ", signalType="
				+ signalType + ", signalDate=" + signalDate + ", signalPresent=" + signalPresent + ", oscillatorSignal="
				+ oscillatorSignal + ", previousOscillatorSignal=" + previousOscillatorSignal
				+ ", signalPresentOscillator=" + signalPresentOscillator + ", bollingerSignal=" + bollingerSignal
				+ ", previousBollingerSignal=" + previousBollingerSignal + ", signalPresentBollinger="
				+ signalPresentBollinger + "]";
	}

}
