package com.intelliinvest.web.bo.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class IndustryFundamentalsResponse implements Serializable {
	private String name;
	private String yearQuarter;
	private double alMarketCap;
	private double alBookValuePerShare;
	private double alEarningPerShare;
	private double alEPSPct;
	private double alPriceToEarning;
	private double alCashToDebtRatio;
	private double alCurrentRatio;
	private double alEquityToAssetRatio;
	private double alDebtToCapitalRatio;
	private double alLeveredBeta;
	private double alReturnOnEquity;
	private double alSolvencyRatio;
	private double alCostOfEquity;
	private double alCostOfDebt;
	private double qrEBIDTAMargin;
	private double qrOperatingMargin;
	private double qrNetMargin;
	private double qrDividendPercent;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate todayDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;	
	private boolean success;
	private String message;
	
	public IndustryFundamentalsResponse() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYearQuarter() {
		return yearQuarter;
	}

	public void setYearQuarter(String yearQuarter) {
		this.yearQuarter = yearQuarter;
	}

	public double getAlMarketCap() {
		return alMarketCap;
	}

	public void setAlMarketCap(double alMarketCap) {
		this.alMarketCap = alMarketCap;
	}

	public double getAlBookValuePerShare() {
		return alBookValuePerShare;
	}

	public void setAlBookValuePerShare(double alBookValuePerShare) {
		this.alBookValuePerShare = alBookValuePerShare;
	}

	public double getAlEarningPerShare() {
		return alEarningPerShare;
	}

	public void setAlEarningPerShare(double alEarningPerShare) {
		this.alEarningPerShare = alEarningPerShare;
	}

	public double getAlEPSPct() {
		return alEPSPct;
	}

	public void setAlEPSPct(double alEPSPct) {
		this.alEPSPct = alEPSPct;
	}

	public double getAlPriceToEarning() {
		return alPriceToEarning;
	}

	public void setAlPriceToEarning(double alPriceToEarning) {
		this.alPriceToEarning = alPriceToEarning;
	}

	public double getAlCashToDebtRatio() {
		return alCashToDebtRatio;
	}

	public void setAlCashToDebtRatio(double alCashToDebtRatio) {
		this.alCashToDebtRatio = alCashToDebtRatio;
	}

	public double getAlCurrentRatio() {
		return alCurrentRatio;
	}

	public void setAlCurrentRatio(double alCurrentRatio) {
		this.alCurrentRatio = alCurrentRatio;
	}

	public double getAlEquityToAssetRatio() {
		return alEquityToAssetRatio;
	}

	public void setAlEquityToAssetRatio(double alEquityToAssetRatio) {
		this.alEquityToAssetRatio = alEquityToAssetRatio;
	}

	public double getAlDebtToCapitalRatio() {
		return alDebtToCapitalRatio;
	}

	public void setAlDebtToCapitalRatio(double alDebtToCapitalRatio) {
		this.alDebtToCapitalRatio = alDebtToCapitalRatio;
	}

	public double getAlLeveredBeta() {
		return alLeveredBeta;
	}

	public void setAlLeveredBeta(double alLeveredBeta) {
		this.alLeveredBeta = alLeveredBeta;
	}

	public double getAlReturnOnEquity() {
		return alReturnOnEquity;
	}

	public void setAlReturnOnEquity(double alReturnOnEquity) {
		this.alReturnOnEquity = alReturnOnEquity;
	}

	public double getAlSolvencyRatio() {
		return alSolvencyRatio;
	}

	public void setAlSolvencyRatio(double alSolvencyRatio) {
		this.alSolvencyRatio = alSolvencyRatio;
	}

	public double getAlCostOfEquity() {
		return alCostOfEquity;
	}

	public void setAlCostOfEquity(double alCostOfEquity) {
		this.alCostOfEquity = alCostOfEquity;
	}

	public double getAlCostOfDebt() {
		return alCostOfDebt;
	}

	public void setAlCostOfDebt(double alCostOfDebt) {
		this.alCostOfDebt = alCostOfDebt;
	}

	public double getQrEBIDTAMargin() {
		return qrEBIDTAMargin;
	}

	public void setQrEBIDTAMargin(double qrEBIDTAMargin) {
		this.qrEBIDTAMargin = qrEBIDTAMargin;
	}

	public double getQrOperatingMargin() {
		return qrOperatingMargin;
	}

	public void setQrOperatingMargin(double qrOperatingMargin) {
		this.qrOperatingMargin = qrOperatingMargin;
	}

	public double getQrNetMargin() {
		return qrNetMargin;
	}

	public void setQrNetMargin(double qrNetMargin) {
		this.qrNetMargin = qrNetMargin;
	}

	public double getQrDividendPercent() {
		return qrDividendPercent;
	}

	public void setQrDividendPercent(double qrDividendPercent) {
		this.qrDividendPercent = qrDividendPercent;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getTodayDate() {
		return todayDate;
	}

	public void setTodayDate(LocalDate todayDate) {
		this.todayDate = todayDate;
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
