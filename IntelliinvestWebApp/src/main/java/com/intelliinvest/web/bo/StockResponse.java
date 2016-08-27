package com.intelliinvest.web.bo;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

@JsonAutoDetect
public class StockResponse implements Serializable {

	private String code;
	private String name;
	private boolean worldStock;
	private boolean niftyStock;
	private LocalDateTime updateDate;
	private boolean success;
	private String message;

	public StockResponse() {
		super();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isWorldStock() {
		return worldStock;
	}

	public void setWorldStock(boolean worldStock) {
		this.worldStock = worldStock;
	}

	public boolean isNiftyStock() {
		return niftyStock;
	}

	public void setNiftyStock(boolean niftyStock) {
		this.niftyStock = niftyStock;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
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
