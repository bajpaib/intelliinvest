package com.intelliinvest.web.bo.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class NewsResponse implements Serializable {

	private List<String> descriptions;
	private String message = "Success";
	private boolean success = true;
	private String stockCode;

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public List<String> getDescriptions() {
		return descriptions;
	}
	
	public void setDescriptions(List<String> descriptions) {
		this.descriptions = descriptions;
	}
	
	@Override
	public String toString() {
		return "NewsResponse [stockCode=" + stockCode + "description size ="
				+ descriptions.size() + ", message=" + message + ", success=" + success
				+ "]";
	}

}
