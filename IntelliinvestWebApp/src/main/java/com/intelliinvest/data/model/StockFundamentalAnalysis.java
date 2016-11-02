package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = "STOCK_FUNDAMENTAL_ANALYSIS")
public class StockFundamentalAnalysis implements Serializable {
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
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate todayDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;
	private int points = 0;

	public StockFundamentalAnalysis() {
		super();
	}

	public StockFundamentalAnalysis(String securityId, String yearQuarter, String alEPSPct, String alCashToDebtRatio,
			String alCurrentRatio, String alEquityToAssetRatio, String alDebtToCapitalRatio, String alLeveredBeta,
			String alReturnOnEquity, String alSolvencyRatio, String alCostOfEquity, String alCostOfDebt,
			String qrEBIDTAMargin, String qrOperatingMargin, String qrNetMargin, String qrDividendPercent,
			String summary, int points, LocalDate todayDate, LocalDateTime updateDate) {
		super();
		this.securityId = securityId;
		this.yearQuarter = yearQuarter;
		this.alEPSPct = alEPSPct;
		this.alCashToDebtRatio = alCashToDebtRatio;
		this.alCurrentRatio = alCurrentRatio;
		this.alEquityToAssetRatio = alEquityToAssetRatio;
		this.alDebtToCapitalRatio = alDebtToCapitalRatio;
		this.alLeveredBeta = alLeveredBeta;
		this.alReturnOnEquity = alReturnOnEquity;
		this.alSolvencyRatio = alSolvencyRatio;
		this.alCostOfEquity = alCostOfEquity;
		this.alCostOfDebt = alCostOfDebt;
		this.qrEBIDTAMargin = qrEBIDTAMargin;
		this.qrOperatingMargin = qrOperatingMargin;
		this.qrNetMargin = qrNetMargin;
		this.qrDividendPercent = qrDividendPercent;
		this.summary = summary;
		this.points = points;
		this.todayDate = todayDate;
		this.updateDate = updateDate;
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

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((securityId == null) ? 0 : securityId.hashCode());
		result = prime * result + ((todayDate == null) ? 0 : todayDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockFundamentalAnalysis other = (StockFundamentalAnalysis) obj;
		if (securityId == null) {
			if (other.securityId != null)
				return false;
		} else if (!securityId.equals(other.securityId))
			return false;
		if (todayDate == null) {
			if (other.todayDate != null)
				return false;
		} else if (!todayDate.equals(other.todayDate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StockFundamentalsForecast [securityId=" + securityId + ", yearQuarter=" + yearQuarter + ", alEPSPct="
				+ alEPSPct + ", alCashToDebtRatio=" + alCashToDebtRatio + ", alCurrentRatio=" + alCurrentRatio
				+ ", alEquityToAssetRatio=" + alEquityToAssetRatio + ", alDebtToCapitalRatio=" + alDebtToCapitalRatio
				+ ", alLeveredBeta=" + alLeveredBeta + ", alReturnOnEquity=" + alReturnOnEquity + ", alSolvencyRatio="
				+ alSolvencyRatio + ", alCostOfEquity=" + alCostOfEquity + ", alCostOfDebt=" + alCostOfDebt
				+ ", qrEBIDTAMargin=" + qrEBIDTAMargin + ", qrOperatingMargin=" + qrOperatingMargin + ", qrNetMargin="
				+ qrNetMargin + ", qrDividendPercent=" + qrDividendPercent + ", qrUnadjBseClosePrice=" + ", summary="
				+ summary + ", points=" + points + ", todayDate=" + todayDate +", updateDate=" + updateDate + "]";
	}

	public StockFundamentalAnalysis clone() {
		return new StockFundamentalAnalysis(securityId, yearQuarter, alEPSPct, alCashToDebtRatio, alCurrentRatio,
				alEquityToAssetRatio, alDebtToCapitalRatio, alLeveredBeta, alReturnOnEquity, alSolvencyRatio,
				alCostOfEquity, alCostOfDebt, qrEBIDTAMargin, qrOperatingMargin, qrNetMargin, qrDividendPercent,
				summary, points, todayDate, updateDate);
	}

}
