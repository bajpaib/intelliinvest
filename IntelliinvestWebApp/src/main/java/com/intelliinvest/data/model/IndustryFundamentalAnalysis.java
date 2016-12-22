package com.intelliinvest.data.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class IndustryFundamentalAnalysis {
	private String name;
	private double alReturnOnEquity;
	private double qrOperatingMargin;
	private double alLeveredBeta;
	private double alEPSPct;
	private double alCashToDebtRatio;
	private int alReturnOnEquity_signal;
	private double qrOperatingMargin_signal;
	private double alLeveredBeta_signal;
	private double alEPSPct_signal;
	private double alCashToDebtRatio_signal;
	private int aggSignal;

	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate todayDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate t_minus_1;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate t_minus_2;
	
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getAlReturnOnEquity() {
		return alReturnOnEquity;
	}

	public void setAlReturnOnEquity(double alReturnOnEquity) {
		this.alReturnOnEquity = alReturnOnEquity;
	}

	public double getQrOperatingMargin() {
		return qrOperatingMargin;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getTodayDate() {
		return todayDate;
	}

	public void setTodayDate(LocalDate todayDate) {
		this.todayDate = todayDate;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getT_minus_1() {
		return t_minus_1;
	}

	public void setT_minus_1(LocalDate t_minus_1) {
		this.t_minus_1 = t_minus_1;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getT_minus_2() {
		return t_minus_2;
	}

	public void setT_minus_2(LocalDate t_minus_2) {
		this.t_minus_2 = t_minus_2;
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

	public int getAlReturnOnEquity_signal() {
		return alReturnOnEquity_signal;
	}

	public void setAlReturnOnEquity_signal(int alReturnOnEquity_signal) {
		this.alReturnOnEquity_signal = alReturnOnEquity_signal;
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

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public String toString() {
		return "IndustryFundamentalAnalysis [name=" + name + ", alReturnOnEquity=" + alReturnOnEquity + ", qrOperatingMargin=" + qrOperatingMargin
				+ ", alLeveredBeta=" + alLeveredBeta + ", alEPSPct=" + alEPSPct + ", alCashToDebtRatio="
				+ alCashToDebtRatio + ", alReturnOnEquity_signal=" + alReturnOnEquity_signal + ", qrOperatingMargin_signal="
				+ qrOperatingMargin_signal + ", alLeveredBeta_signal=" + alLeveredBeta_signal + ", alEPSPct_signal="
				+ alEPSPct_signal + ", alCashToDebtRatio_signal=" + alCashToDebtRatio_signal + ", aggSignal="
				+ aggSignal + ", todayDate=" + todayDate + ", t_minus_1=" + t_minus_1 + ", t_minus_2=" + t_minus_2
				+ ", updateDate=" + updateDate + "]";
	}

}
