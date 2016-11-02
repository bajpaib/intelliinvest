package com.intelliinvest.web.bo.response;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

public class BubbleDataResponse {
	
	@DateTimeFormat(iso = ISO.DATE)
	LocalDate date;
	
	double noOfBuyHold;
	double noOfSellWait;
	double totalNo;
	
	double percentageBuyHold;
	double percentageSellWait;
	
	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public double getNoOfBuyHold() {
		return noOfBuyHold;
	}
	public void setNoOfBuyHold(double noOfBuyHold) {
		this.noOfBuyHold = noOfBuyHold;
	}
	public double getNoOfSellWait() {
		return noOfSellWait;
	}
	public void setNoOfSellWait(double noOfSellWait) {
		this.noOfSellWait = noOfSellWait;
	}
	public double getTotalNo() {
		return totalNo;
	}
	public void setTotalNo(double totalNo) {
		this.totalNo = totalNo;
	}
	public double getPercentageBuyHold() {
		return percentageBuyHold;
	}
	public void setPercentageBuyHold(double percentageBuyHold) {
		this.percentageBuyHold = percentageBuyHold;
	}
	public double getPercentageSellWait() {
		return percentageSellWait;
	}
	public void setPercentageSellWait(double percentageSellWait) {
		this.percentageSellWait = percentageSellWait;
	}
	@Override
	public String toString() {
		return "BubbleDataResponse [date=" + date + ", noOfBuyHold="
				+ noOfBuyHold + ", noOfSellWait=" + noOfSellWait + ", totalNo="
				+ totalNo + ", percentageBuyHold=" + percentageBuyHold
				+ ", percentageSellWait=" + percentageSellWait + "]";
	}
	
	

}
