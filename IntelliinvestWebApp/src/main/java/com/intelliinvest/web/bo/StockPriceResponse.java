package com.intelliinvest.web.bo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

@JsonAutoDetect
public class StockPriceResponse implements Serializable {

	private String code;
	private double cp;
	private double currentPrice;
	private double eodPrice;
	private Date eodDate;
	private Date updateDate;
	private boolean success;
	private String message;

	public StockPriceResponse() {
		super();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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

	public double getEodPrice() {
		return eodPrice;
	}

	public void setEodPrice(double eodPrice) {
		this.eodPrice = eodPrice;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getEodDate() {
		return eodDate;
	}

	public void setEodDate(Date eodDate) {
		this.eodDate = eodDate;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
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
