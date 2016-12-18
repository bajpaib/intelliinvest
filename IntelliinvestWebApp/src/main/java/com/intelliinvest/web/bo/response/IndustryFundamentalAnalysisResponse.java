package com.intelliinvest.web.bo.response;

import java.time.LocalDateTime;

public class IndustryFundamentalAnalysisResponse {

private String name;
	
	private double roe;
	private double qrOperatingMargin;
	private double alLeveredBeta;
	private double alEPSPct;
	private double alCashToDebtRatio;
	
	private int roe_signal;
	private double qrOperatingMargin_signal;
	private double alLeveredBeta_signal;
	private double alEPSPct_signal;
	private double alCashToDebtRatio_signal;
	
	private int aggSignal;
	
	private boolean success;
	private String message;
	
	private LocalDateTime updateDate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRoe() {
		return roe;
	}

	public void setRoe(double roe) {
		this.roe = roe;
	}

	public double getQrOperatingMargin() {
		return qrOperatingMargin;
	}

	public void setQrOperatingMargin(double qrOperatingMargin) {
		this.qrOperatingMargin = qrOperatingMargin;
	}

	public double getAlLeveredBeta() {
		return alLeveredBeta;
	}

	public void setAlLeveredBeta(double alLeveredBeta) {
		this.alLeveredBeta = alLeveredBeta;
	}

	public double getAlEPSPct() {
		return alEPSPct;
	}

	public void setAlEPSPct(double alEPSPct) {
		this.alEPSPct = alEPSPct;
	}

	public double getAlCashToDebtRatio() {
		return alCashToDebtRatio;
	}

	public void setAlCashToDebtRatio(double alCashToDebtRatio) {
		this.alCashToDebtRatio = alCashToDebtRatio;
	}

	public int getRoe_signal() {
		return roe_signal;
	}

	public void setRoe_signal(int roe_signal) {
		this.roe_signal = roe_signal;
	}

	public double getQrOperatingMargin_signal() {
		return qrOperatingMargin_signal;
	}

	public void setQrOperatingMargin_signal(double qrOperatingMargin_signal) {
		this.qrOperatingMargin_signal = qrOperatingMargin_signal;
	}

	public double getAlLeveredBeta_signal() {
		return alLeveredBeta_signal;
	}

	public void setAlLeveredBeta_signal(double alLeveredBeta_signal) {
		this.alLeveredBeta_signal = alLeveredBeta_signal;
	}

	public double getAlEPSPct_signal() {
		return alEPSPct_signal;
	}

	public void setAlEPSPct_signal(double alEPSPct_signal) {
		this.alEPSPct_signal = alEPSPct_signal;
	}

	public double getAlCashToDebtRatio_signal() {
		return alCashToDebtRatio_signal;
	}

	public void setAlCashToDebtRatio_signal(double alCashToDebtRatio_signal) {
		this.alCashToDebtRatio_signal = alCashToDebtRatio_signal;
	}

	public int getAggSignal() {
		return aggSignal;
	}

	public void setAggSignal(int aggSignal) {
		this.aggSignal = aggSignal;
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

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

}
