package com.intelliinvest.web.bo.response;

import java.util.List;

import com.intelliinvest.data.model.PriceVolumeData;

public class IntradayPriceVolumeDataResponse {
	String exchange;
	String securityId;
	List<PriceVolumeData> priceVolumeDatas;
	private boolean success = true;
	private String message = "SUCCCESS";
	
	public IntradayPriceVolumeDataResponse(String exchange, String securityId) {
		super();
		this.exchange = exchange;
		this.securityId = securityId;
	}
	
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public String getSecurityId() {
		return securityId;
	}
	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}
	public List<PriceVolumeData> getPriceVolumeDatas() {
		return priceVolumeDatas;
	}
	public void setPriceVolumeDatas(List<PriceVolumeData> priceVolumeDatas) {
		this.priceVolumeDatas = priceVolumeDatas;
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
