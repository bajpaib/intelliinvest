package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = "INDUSTRY_FUNDAMENTALS")
public class IndustryFundamentals implements Serializable {
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

	public IndustryFundamentals() {
		super();
	}

	public IndustryFundamentals(String name, String yearQuarter, double alMarketCap, double alBookValuePerShare,
			double alEarningPerShare, double alEPSPct, double alPriceToEarning, double alCashToDebtRatio,
			double alCurrentRatio, double alEquityToAssetRatio, double alDebtToCapitalRatio, double alLeveredBeta,
			double alReturnOnEquity, double alSolvencyRatio, double alCostOfEquity, double alCostOfDebt,
			double qrEBIDTAMargin, double qrOperatingMargin, double qrNetMargin, double qrDividendPercent,
			LocalDate todayDate, LocalDateTime updateDate) {
		super();
		this.name = name;
		this.yearQuarter = yearQuarter;
		this.alMarketCap = alMarketCap;
		this.alBookValuePerShare = alBookValuePerShare;
		this.alEarningPerShare = alEarningPerShare;
		this.alEPSPct = alEPSPct;
		this.alPriceToEarning = alPriceToEarning;
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
		this.todayDate = todayDate;
		this.updateDate = updateDate;
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

	public double getAttributeValue(String attrName) {
		double retVal = 0;

		switch (attrName) {
		case "alMarketCap":
			retVal = getAlMarketCap();
			break;
		case "alBookValuePerShare":
			retVal = getAlBookValuePerShare();
			break;
		case "alEarningPerShare":
			retVal = getAlEarningPerShare();
			break;
		case "alEPSPct":
			retVal = getAlEPSPct();
			break;
		case "alPriceToEarning":
			retVal = getAlPriceToEarning();
			break;
		case "alCashToDebtRatio":
			retVal = getAlCashToDebtRatio();
			break;
		case "alCurrentRatio":
			retVal = getAlCurrentRatio();
			break;
		case "alEquityToAssetRatio":
			retVal = getAlEquityToAssetRatio();
			break;
		case "alDebtToCapitalRatio":
			retVal = getAlDebtToCapitalRatio();
			break;
		case "alLeveredBeta":
			retVal = getAlLeveredBeta();
			break;
		case "alReturnOnEquity":
			retVal = getAlReturnOnEquity();
			break;
		case "alSolvencyRatio":
			retVal = getAlSolvencyRatio();
			break;
		case "alCostOfEquity":
			retVal = getAlCostOfEquity();
			break;
		case "alCostOfDebt":
			retVal = getAlCostOfDebt();
			break;
		case "qrEBIDTAMargin":
			retVal = getQrEBIDTAMargin();
			break;
		case "qrOperatingMargin":
			retVal = getQrOperatingMargin();
			break;
		case "qrNetMargin":
			retVal = getQrNetMargin();
			break;
		case "qrDividendPercent":
			retVal = getQrDividendPercent();
			break;
		default:
			retVal = 0;
		}
		return retVal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		IndustryFundamentals other = (IndustryFundamentals) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
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
		return "IndustryFundamentals [name=" + name + ", yearQuarter=" + yearQuarter + ", alMarketCap=" + alMarketCap
				+ ", alBookValuePerShare=" + alBookValuePerShare + ", alEarningPerShare=" + alEarningPerShare
				+ ", alEPSPct=" + alEPSPct + ", alPriceToEarning=" + alPriceToEarning + ", alCashToDebtRatio="
				+ alCashToDebtRatio + ", alCurrentRatio=" + alCurrentRatio + ", alEquityToAssetRatio="
				+ alEquityToAssetRatio + ", alDebtToCapitalRatio=" + alDebtToCapitalRatio + ", alLeveredBeta="
				+ alLeveredBeta + ", alReturnOnEquity=" + alReturnOnEquity + ", alSolvencyRatio=" + alSolvencyRatio
				+ ", alCostOfEquity=" + alCostOfEquity + ", alCostOfDebt=" + alCostOfDebt + ", qrEBIDTAMargin="
				+ qrEBIDTAMargin + ", qrOperatingMargin=" + qrOperatingMargin + ", qrNetMargin=" + qrNetMargin
				+ ", qrDividendPercent=" + qrDividendPercent + ", todayDate=" + todayDate + ", updateDate=" + updateDate
				+ "]";
	}

	public IndustryFundamentals clone() {
		return new IndustryFundamentals(name, yearQuarter, alMarketCap, alBookValuePerShare, alEarningPerShare,
				alEPSPct, alPriceToEarning, alCashToDebtRatio, alCurrentRatio, alEquityToAssetRatio,
				alDebtToCapitalRatio, alLeveredBeta, alReturnOnEquity, alSolvencyRatio, alCostOfEquity, alCostOfDebt,
				qrEBIDTAMargin, qrOperatingMargin, qrNetMargin, qrDividendPercent, todayDate, updateDate);
	}

}
