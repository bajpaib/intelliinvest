package com.intelliinvest.data.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

public class BubbleData {

	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate signalDate;
	private String aggSignal;
	private double count;

	public BubbleData() {
		// TODO Auto-generated constructor stub
	}

	public String getAggSignal() {
		return aggSignal;
	}

	public void setAggSignal(String aggSignal) {
		this.aggSignal = aggSignal;
	}

	public double getCount() {
		return count;
	}

	public void setCount(double count) {
		this.count = count;
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
		return "BubbleData [signalDate=" + signalDate + ", aggSignal="
				+ aggSignal + ", count=" + count + "]";
	}

}
