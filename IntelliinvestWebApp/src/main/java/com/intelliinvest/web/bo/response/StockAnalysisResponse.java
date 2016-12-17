package com.intelliinvest.web.bo.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class StockAnalysisResponse {
	private String securityId;
	
	//technical Signals
	private String adxSignal;
	private String oscillatorSignal;
	private String bollingerSignal;
	private String movingAverageSignal_Main;
	private String movingAverageSignal_LongTerm;
	private String aggSignal;
	
	//fundamental signals
	private String alEPSRatio="";
	private String annOperatingMargin_fundamental_analysis="";
	private String annDividendPercent="";
	private String annNetWorth="";
	private String faceValue="";
	private String mktCap="";
	private String freeCashFlow="";
	private String enterpriseValue="";
	
	//industry signals
	private double alReturnOnEquity;
	private double qrOperatingMargin_industry_analysis;
	private String competitiveStrength="";
	
	//news
	String news="";
	
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate signalDate;


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
	
	private boolean success;
	private String message;
	
	public String getCompetitiveStrength() {
		return competitiveStrength;
	}
	public void setCompetitiveStrength(String competitiveStrength) {
		this.competitiveStrength = competitiveStrength;
	}
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
	public String getMovingAverageSignal_Main() {
		return movingAverageSignal_Main;
	}
	public void setMovingAverageSignal_Main(String movingAverageSignal_Main) {
		this.movingAverageSignal_Main = movingAverageSignal_Main;
	}
	public String getMovingAverageSignal_LongTerm() {
		return movingAverageSignal_LongTerm;
	}
	public void setMovingAverageSignal_LongTerm(String movingAverageSignal_LongTerm) {
		this.movingAverageSignal_LongTerm = movingAverageSignal_LongTerm;
	}
	public String getAggSignal() {
		return aggSignal;
	}
	public void setAggSignal(String aggSignal) {
		this.aggSignal = aggSignal;
	}
	public String getAlEPSRatio() {
		return alEPSRatio;
	}
	public void setAlEPSRatio(String alEPSRatio) {
		this.alEPSRatio = alEPSRatio;
	}
	public String getAnnOperatingMargin_fundamental_analysis() {
		return annOperatingMargin_fundamental_analysis;
	}
	public void setAnnOperatingMargin_fundamental_analysis(String annOperatingMargin_fundamental_analysis) {
		this.annOperatingMargin_fundamental_analysis = annOperatingMargin_fundamental_analysis;
	}
	public String getAnnDividendPercent() {
		return annDividendPercent;
	}
	public void setAnnDividendPercent(String annDividendPercent) {
		this.annDividendPercent = annDividendPercent;
	}
	public String getAnnNetWorth() {
		return annNetWorth;
	}
	public void setAnnNetWorth(String annNetWorth) {
		this.annNetWorth = annNetWorth;
	}
	public String getFaceValue() {
		return faceValue;
	}
	public void setFaceValue(String faceValue) {
		this.faceValue = faceValue;
	}
	public String getMktCap() {
		return mktCap;
	}
	public void setMktCap(String mktCap) {
		this.mktCap = mktCap;
	}
	public String getFreeCashFlow() {
		return freeCashFlow;
	}
	public void setFreeCashFlow(String freeCashFlow) {
		this.freeCashFlow = freeCashFlow;
	}
	public String getEnterpriseValue() {
		return enterpriseValue;
	}
	public void setEnterpriseValue(String enterpriseValue) {
		this.enterpriseValue = enterpriseValue;
	}
	public double getAlReturnOnEquity() {
		return alReturnOnEquity;
	}
	public void setAlReturnOnEquity(double alReturnOnEquity) {
		this.alReturnOnEquity = alReturnOnEquity;
	}
	public double getQrOperatingMargin_industry_analysis() {
		return qrOperatingMargin_industry_analysis;
	}
	public void setQrOperatingMargin_industry_analysis(double qrOperatingMargin_industry_analysis) {
		this.qrOperatingMargin_industry_analysis = qrOperatingMargin_industry_analysis;
	}
	public String getNews() {
		return news;
	}
	public void setNews(String news) {
		this.news = news;
	}
	
	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getSignalDate() {
		return signalDate;
	}
	public void setSignalDate(LocalDate signalDate) {
		this.signalDate = signalDate;
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
