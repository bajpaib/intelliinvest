package com.intelliinvest.data.model;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = "STOCK_FUNDAMENTALS_HISTORY")
public class StockFundamentals {
	private String securityId;
	private String quarterYear;
	private double annualMarketCap;
	private double annualEarningPerShare;
	private double annualPriceToEarning;
	private double annualCashToDebtRatio;
	private double annualCurrentRatio;
	private double annualEquityToAssetRatio;
	private double annualDebtToCapitalRatio;
	private double annualLeveredBeta;
	private double annualReturnOnEquity;
	private double annualSolvencyRatio;
	private double annualCostOfEquity;
	private double annualCostOfDebt;
	private double quarterlyEBIDTAMargin;
	private double quarterlyOperatingMargin;
	private double quarterlyNetMargin;
	private double quarterlyDividendPercent;
	private double quarterlyUnadjBseClosePrice;

	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;

	public StockFundamentals() {
		super();
	}

	public StockFundamentals(String securityId, String quarterYear, double annualMarketCap,
			double annualEarningPerShare, double annualPriceToEarning, double annualCashToDebtRatio,
			double annualCurrentRatio, double annualEquityToAssetRatio, double annualDebtToCapitalRatio,
			double annualLeveredBeta, double annualReturnOnEquity, double annualSolvencyRatio,
			double annualCostOfEquity, double annualCostOfDebt, double quarterlyEBIDTAMargin,
			double quarterlyOperatingMargin, double quarterlyNetMargin, double quarterlyDividendPercent,
			double quarterlyUnadjBseClosePrice, LocalDateTime updateDate) {
		super();
		this.securityId = securityId;
		this.quarterYear = quarterYear;
		this.annualMarketCap = annualMarketCap;
		this.annualEarningPerShare = annualEarningPerShare;
		this.annualPriceToEarning = annualPriceToEarning;
		this.annualCashToDebtRatio = annualCashToDebtRatio;
		this.annualCurrentRatio = annualCurrentRatio;
		this.annualEquityToAssetRatio = annualEquityToAssetRatio;
		this.annualDebtToCapitalRatio = annualDebtToCapitalRatio;
		this.annualLeveredBeta = annualLeveredBeta;
		this.annualReturnOnEquity = annualReturnOnEquity;
		this.annualSolvencyRatio = annualSolvencyRatio;
		this.annualCostOfEquity = annualCostOfEquity;
		this.annualCostOfDebt = annualCostOfDebt;
		this.quarterlyEBIDTAMargin = quarterlyEBIDTAMargin;
		this.quarterlyOperatingMargin = quarterlyOperatingMargin;
		this.quarterlyNetMargin = quarterlyNetMargin;
		this.quarterlyDividendPercent = quarterlyDividendPercent;
		this.quarterlyUnadjBseClosePrice = quarterlyUnadjBseClosePrice;
		this.updateDate = updateDate;
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public String getQuarterYear() {
		return quarterYear;
	}

	public void setQuarterYear(String quarterYear) {
		this.quarterYear = quarterYear;
	}

	public double getAnnualMarketCap() {
		return annualMarketCap;
	}

	public void setAnnualMarketCap(double annualMarketCap) {
		this.annualMarketCap = annualMarketCap;
	}

	public double getAnnualEarningPerShare() {
		return annualEarningPerShare;
	}

	public void setAnnualEarningPerShare(double annualEarningPerShare) {
		this.annualEarningPerShare = annualEarningPerShare;
	}

	public double getAnnualPriceToEarning() {
		return annualPriceToEarning;
	}

	public void setAnnualPriceToEarning(double annualPriceToEarning) {
		this.annualPriceToEarning = annualPriceToEarning;
	}

	public double getAnnualCashToDebtRatio() {
		return annualCashToDebtRatio;
	}

	public void setAnnualCashToDebtRatio(double annualCashToDebtRatio) {
		this.annualCashToDebtRatio = annualCashToDebtRatio;
	}

	public double getAnnualCurrentRatio() {
		return annualCurrentRatio;
	}

	public void setAnnualCurrentRatio(double annualCurrentRatio) {
		this.annualCurrentRatio = annualCurrentRatio;
	}

	public double getAnnualEquityToAssetRatio() {
		return annualEquityToAssetRatio;
	}

	public void setAnnualEquityToAssetRatio(double annualEquityToAssetRatio) {
		this.annualEquityToAssetRatio = annualEquityToAssetRatio;
	}

	public double getAnnualDebtToCapitalRatio() {
		return annualDebtToCapitalRatio;
	}

	public void setAnnualDebtToCapitalRatio(double annualDebtToCapitalRatio) {
		this.annualDebtToCapitalRatio = annualDebtToCapitalRatio;
	}

	public double getAnnualLeveredBeta() {
		return annualLeveredBeta;
	}

	public void setAnnualLeveredBeta(double annualLeveredBeta) {
		this.annualLeveredBeta = annualLeveredBeta;
	}

	public double getAnnualReturnOnEquity() {
		return annualReturnOnEquity;
	}

	public void setAnnualReturnOnEquity(double annualReturnOnEquity) {
		this.annualReturnOnEquity = annualReturnOnEquity;
	}

	public double getAnnualSolvencyRatio() {
		return annualSolvencyRatio;
	}

	public void setAnnualSolvencyRatio(double annualSolvencyRatio) {
		this.annualSolvencyRatio = annualSolvencyRatio;
	}

	public double getAnnualCostOfEquity() {
		return annualCostOfEquity;
	}

	public void setAnnualCostOfEquity(double annualCostOfEquity) {
		this.annualCostOfEquity = annualCostOfEquity;
	}

	public double getAnnualCostOfDebt() {
		return annualCostOfDebt;
	}

	public void setAnnualCostOfDebt(double annualCostOfDebt) {
		this.annualCostOfDebt = annualCostOfDebt;
	}

	public double getQuarterlyEBIDTAMargin() {
		return quarterlyEBIDTAMargin;
	}

	public void setQuarterlyEBIDTAMargin(double quarterlyEBIDTAMargin) {
		this.quarterlyEBIDTAMargin = quarterlyEBIDTAMargin;
	}

	public double getQuarterlyOperatingMargin() {
		return quarterlyOperatingMargin;
	}

	public void setQuarterlyOperatingMargin(double quarterlyOperatingMargin) {
		this.quarterlyOperatingMargin = quarterlyOperatingMargin;
	}

	public double getQuarterlyNetMargin() {
		return quarterlyNetMargin;
	}

	public void setQuarterlyNetMargin(double quarterlyNetMargin) {
		this.quarterlyNetMargin = quarterlyNetMargin;
	}

	public double getQuarterlyDividendPercent() {
		return quarterlyDividendPercent;
	}

	public void setQuarterlyDividendPercent(double quarterlyDividendPercent) {
		this.quarterlyDividendPercent = quarterlyDividendPercent;
	}

	public double getQuarterlyUnadjBseClosePrice() {
		return quarterlyUnadjBseClosePrice;
	}

	public void setQuarterlyUnadjBseClosePrice(double quarterlyUnadjBseClosePrice) {
		this.quarterlyUnadjBseClosePrice = quarterlyUnadjBseClosePrice;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((quarterYear == null) ? 0 : quarterYear.hashCode());
		result = prime * result + ((securityId == null) ? 0 : securityId.hashCode());
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
		StockFundamentals other = (StockFundamentals) obj;
		if (quarterYear == null) {
			if (other.quarterYear != null)
				return false;
		} else if (!quarterYear.equals(other.quarterYear))
			return false;
		if (securityId == null) {
			if (other.securityId != null)
				return false;
		} else if (!securityId.equals(other.securityId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StockFundamentals [securityId=" + securityId + ", quarterYear=" + quarterYear + ", annualMarketCap="
				+ annualMarketCap + ", annualEarningPerShare=" + annualEarningPerShare + ", annualPriceToEarning="
				+ annualPriceToEarning + ", annualCashToDebtRatio=" + annualCashToDebtRatio + ", annualCurrentRatio="
				+ annualCurrentRatio + ", annualEquityToAssetRatio=" + annualEquityToAssetRatio
				+ ", annualDebtToCapitalRatio=" + annualDebtToCapitalRatio + ", annualLeveredBeta=" + annualLeveredBeta
				+ ", annualReturnOnEquity=" + annualReturnOnEquity + ", annualSolvencyRatio=" + annualSolvencyRatio
				+ ", annualCostOfEquity=" + annualCostOfEquity + ", annualCostOfDebt=" + annualCostOfDebt
				+ ", quarterlyEBIDTAMargin=" + quarterlyEBIDTAMargin + ", quarterlyOperatingMargin="
				+ quarterlyOperatingMargin + ", quarterlyNetMargin=" + quarterlyNetMargin
				+ ", quarterlyDividendPercent=" + quarterlyDividendPercent + ", quarterlyUnadjBseClosePrice="
				+ quarterlyUnadjBseClosePrice + ", quarterlyUpdateDate=" + ", updateDate=" + updateDate + "]";
	}

}
