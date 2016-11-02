package com.intelliinvest.web.bo.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class StockFundamentalAnalysisResponse implements Serializable {
	private String securityId;
	private String yearQuarter;
	private String alEPSPct;
	private String alCashToDebtRatio;
	private String alCurrentRatio;
	private String alEquityToAssetRatio;
	private String alDebtToCapitalRatio;
	private String alLeveredBeta;
	private String alReturnOnEquity;
	private String alSolvencyRatio;
	private String alCostOfEquity;
	private String alCostOfDebt;
	private String qrEBIDTAMargin;
	private String qrOperatingMargin;
	private String qrNetMargin;
	private String qrDividendPercent;
	private String summary;
	private int points;
	private LocalDate todayDate;
	private LocalDateTime updateDate;
	private boolean success;
	private String message;
	
	public StockFundamentalAnalysisResponse() {
		super();
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public String getYearQuarter() {
		return yearQuarter;
	}

	public void setYearQuarter(String yearQuarter) {
		this.yearQuarter = yearQuarter;
	}

	public String getAlEPSPct() {
		return alEPSPct;
	}

	public void setAlEPSPct(String alEPSPct) {
		this.alEPSPct = alEPSPct;
	}

	public String getAlCashToDebtRatio() {
		return alCashToDebtRatio;
	}

	public void setAlCashToDebtRatio(String alCashToDebtRatio) {
		this.alCashToDebtRatio = alCashToDebtRatio;
	}

	public String getAlCurrentRatio() {
		return alCurrentRatio;
	}

	public void setAlCurrentRatio(String alCurrentRatio) {
		this.alCurrentRatio = alCurrentRatio;
	}

	public String getAlEquityToAssetRatio() {
		return alEquityToAssetRatio;
	}

	public void setAlEquityToAssetRatio(String alEquityToAssetRatio) {
		this.alEquityToAssetRatio = alEquityToAssetRatio;
	}

	public String getAlDebtToCapitalRatio() {
		return alDebtToCapitalRatio;
	}

	public void setAlDebtToCapitalRatio(String alDebtToCapitalRatio) {
		this.alDebtToCapitalRatio = alDebtToCapitalRatio;
	}

	public String getAlLeveredBeta() {
		return alLeveredBeta;
	}

	public void setAlLeveredBeta(String alLeveredBeta) {
		this.alLeveredBeta = alLeveredBeta;
	}

	public String getAlReturnOnEquity() {
		return alReturnOnEquity;
	}

	public void setAlReturnOnEquity(String alReturnOnEquity) {
		this.alReturnOnEquity = alReturnOnEquity;
	}

	public String getAlSolvencyRatio() {
		return alSolvencyRatio;
	}

	public void setAlSolvencyRatio(String alSolvencyRatio) {
		this.alSolvencyRatio = alSolvencyRatio;
	}

	public String getAlCostOfEquity() {
		return alCostOfEquity;
	}

	public void setAlCostOfEquity(String alCostOfEquity) {
		this.alCostOfEquity = alCostOfEquity;
	}

	public String getAlCostOfDebt() {
		return alCostOfDebt;
	}

	public void setAlCostOfDebt(String alCostOfDebt) {
		this.alCostOfDebt = alCostOfDebt;
	}

	public String getQrEBIDTAMargin() {
		return qrEBIDTAMargin;
	}

	public void setQrEBIDTAMargin(String qrEBIDTAMargin) {
		this.qrEBIDTAMargin = qrEBIDTAMargin;
	}

	public String getQrOperatingMargin() {
		return qrOperatingMargin;
	}

	public void setQrOperatingMargin(String qrOperatingMargin) {
		this.qrOperatingMargin = qrOperatingMargin;
	}

	public String getQrNetMargin() {
		return qrNetMargin;
	}

	public void setQrNetMargin(String qrNetMargin) {
		this.qrNetMargin = qrNetMargin;
	}

	public String getQrDividendPercent() {
		return qrDividendPercent;
	}

	public void setQrDividendPercent(String qrDividendPercent) {
		this.qrDividendPercent = qrDividendPercent;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
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
