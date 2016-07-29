package com.intelliinvest.web.bo;

import java.io.Serializable;

public class StockFormParameters implements Serializable {

	private String code;

	public StockFormParameters() {
		super();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
