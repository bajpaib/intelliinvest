package com.intelliinvest.data.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class StockSignalsDTO {

	String symbol;
	String previousSignalType = "";
	String signalType = "Wait";

	LocalDate signalDate;
	String signalPresent;

	String oscillatorSignal;
	String previousOscillatorSignal = "";
	String signalPresentOscillator;

	String bollingerSignal;
	String previousBollingerSignal = "";
	String signalPresentBollinger;

	Double TR;
	Double plusDM1 = 0D;
	Double minusDM1 = 0D;
	Double TRn = 0D;
	Double plusDMn = 0D;
	Double minusDMn = 0D;
	Double plusDIn = 0D;
	Double minusDIn = 0D;
	Double diffDIn = 0D;
	Double sumDIn = 0D;
	Double DX = 0D;
	Double ADXn = 0D;
	Double splitMultiplier = 0D;

	@DateTimeFormat(iso = ISO.DATE)
	// Oscillator Algo Parameters
	Double high10Day = -1D;
	Double low10Day = -1D;
	Double range10Day = -1D;
	Double stochastic10Day = -1D;
	Double percentKFlow = -1D;
	Double percentDFlow = -1D;
	// Bollinger Algo Parameters
	Double sma;
	Double upperBound;
	Double lowerBound;
	Double bandwidth;

	public StockSignalsDTO() {
		// TODO Auto-generated constructor stub
	}

	public StockSignalsDTO(String symbol, String previousSignalType, String signalType, LocalDate signalDate,
			String signalPresent, String oscillatorSignal, String previousOscillatorSignal,
			String signalPresentOscillator, String bollingerSignal, String previousBollingerSignal,
			String signalPresentBollinger, Double tR, Double plusDM1, Double minusDM1, Double tRn, Double plusDMn,
			Double minusDMn, Double plusDIn, Double minusDIn, Double diffDIn, Double sumDIn, Double dX, Double aDXn,
			Double splitMultiplier, Double high10Day, Double low10Day, Double range10Day, Double stochastic10Day,
			Double percentKFlow, Double percentDFlow, Double sma, Double upperBound, Double lowerBound,
			Double bandwidth) {
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
		TR = tR;
		this.plusDM1 = plusDM1;
		this.minusDM1 = minusDM1;
		TRn = tRn;
		this.plusDMn = plusDMn;
		this.minusDMn = minusDMn;
		this.plusDIn = plusDIn;
		this.minusDIn = minusDIn;
		this.diffDIn = diffDIn;
		this.sumDIn = sumDIn;
		DX = dX;
		ADXn = aDXn;
		this.splitMultiplier = splitMultiplier;
		this.high10Day = high10Day;
		this.low10Day = low10Day;
		this.range10Day = range10Day;
		this.stochastic10Day = stochastic10Day;
		this.percentKFlow = percentKFlow;
		this.percentDFlow = percentDFlow;
		this.sma = sma;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		this.bandwidth = bandwidth;
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

	public LocalDate getSignalDate() {
		return signalDate;
	}

	public void setSignalDate(LocalDate signalDate) {
		this.signalDate = signalDate;
	}

	public String getSignalPresent() {
		return signalPresent;
	}

	public void setSignalPresent(String signalPresent) {
		this.signalPresent = signalPresent;
	}

	public String getOscillatorSignal() {
		return oscillatorSignal;
	}

	public void setOscillatorSignal(String oscillatorSignal) {
		this.oscillatorSignal = oscillatorSignal;
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

	public String getBollingerSignal() {
		return bollingerSignal;
	}

	public void setBollingerSignal(String bollingerSignal) {
		this.bollingerSignal = bollingerSignal;
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

	public Double getTR() {
		return TR;
	}

	public void setTR(Double tR) {
		TR = tR;
	}

	public Double getPlusDM1() {
		return plusDM1;
	}

	public void setPlusDM1(Double plusDM1) {
		this.plusDM1 = plusDM1;
	}

	public Double getMinusDM1() {
		return minusDM1;
	}

	public void setMinusDM1(Double minusDM1) {
		this.minusDM1 = minusDM1;
	}

	public Double getTRn() {
		return TRn;
	}

	public void setTRn(Double tRn) {
		TRn = tRn;
	}

	public Double getPlusDMn() {
		return plusDMn;
	}

	public void setPlusDMn(Double plusDMn) {
		this.plusDMn = plusDMn;
	}

	public Double getMinusDMn() {
		return minusDMn;
	}

	public void setMinusDMn(Double minusDMn) {
		this.minusDMn = minusDMn;
	}

	public Double getPlusDIn() {
		return plusDIn;
	}

	public void setPlusDIn(Double plusDIn) {
		this.plusDIn = plusDIn;
	}

	public Double getMinusDIn() {
		return minusDIn;
	}

	public void setMinusDIn(Double minusDIn) {
		this.minusDIn = minusDIn;
	}

	public Double getDiffDIn() {
		return diffDIn;
	}

	public void setDiffDIn(Double diffDIn) {
		this.diffDIn = diffDIn;
	}

	public Double getSumDIn() {
		return sumDIn;
	}

	public void setSumDIn(Double sumDIn) {
		this.sumDIn = sumDIn;
	}

	public Double getDX() {
		return DX;
	}

	public void setDX(Double dX) {
		DX = dX;
	}

	public Double getADXn() {
		return ADXn;
	}

	public void setADXn(Double aDXn) {
		ADXn = aDXn;
	}

	public Double getSplitMultiplier() {
		return splitMultiplier;
	}

	public void setSplitMultiplier(Double splitMultiplier) {
		this.splitMultiplier = splitMultiplier;
	}

	public Double getHigh10Day() {
		return high10Day;
	}

	public void setHigh10Day(Double high10Day) {
		this.high10Day = high10Day;
	}

	public Double getLow10Day() {
		return low10Day;
	}

	public void setLow10Day(Double low10Day) {
		this.low10Day = low10Day;
	}

	public Double getRange10Day() {
		return range10Day;
	}

	public void setRange10Day(Double range10Day) {
		this.range10Day = range10Day;
	}

	public Double getStochastic10Day() {
		return stochastic10Day;
	}

	public void setStochastic10Day(Double stochastic10Day) {
		this.stochastic10Day = stochastic10Day;
	}

	public Double getPercentKFlow() {
		return percentKFlow;
	}

	public void setPercentKFlow(Double percentKFlow) {
		this.percentKFlow = percentKFlow;
	}

	public Double getPercentDFlow() {
		return percentDFlow;
	}

	public void setPercentDFlow(Double percentDFlow) {
		this.percentDFlow = percentDFlow;
	}

	public Double getSma() {
		return sma;
	}

	public void setSma(Double sma) {
		this.sma = sma;
	}

	public Double getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(Double upperBound) {
		this.upperBound = upperBound;
	}

	public Double getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(Double lowerBound) {
		this.lowerBound = lowerBound;
	}

	public Double getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(Double bandwidth) {
		this.bandwidth = bandwidth;
	}

	@Override
	public String toString() {
		return "StockSignalsBO [symbol=" + symbol + ", previousSignalType=" + previousSignalType + ", signalType="
				+ signalType + ", signalDate=" + signalDate + ", signalPresent=" + signalPresent + ", oscillatorSignal="
				+ oscillatorSignal + ", previousOscillatorSignal=" + previousOscillatorSignal
				+ ", signalPresentOscillator=" + signalPresentOscillator + ", bollingerSignal=" + bollingerSignal
				+ ", previousBollingerSignal=" + previousBollingerSignal + ", signalPresentBollinger="
				+ signalPresentBollinger + ", TR=" + TR + ", plusDM1=" + plusDM1 + ", minusDM1=" + minusDM1 + ", TRn="
				+ TRn + ", plusDMn=" + plusDMn + ", minusDMn=" + minusDMn + ", plusDIn=" + plusDIn + ", minusDIn="
				+ minusDIn + ", diffDIn=" + diffDIn + ", sumDIn=" + sumDIn + ", DX=" + DX + ", ADXn=" + ADXn
				+ ", splitMultiplier=" + splitMultiplier + ", high10Day=" + high10Day + ", low10Day=" + low10Day
				+ ", range10Day=" + range10Day + ", stochastic10Day=" + stochastic10Day + ", percentKFlow="
				+ percentKFlow + ", percentDFlow=" + percentDFlow + ", sma=" + sma + ", upperBound=" + upperBound
				+ ", lowerBound=" + lowerBound + ", bandwidth=" + bandwidth + "]";
	}

}
