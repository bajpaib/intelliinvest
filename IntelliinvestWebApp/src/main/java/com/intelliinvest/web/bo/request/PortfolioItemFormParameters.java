package com.intelliinvest.web.bo.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class PortfolioItemFormParameters implements Serializable {	
	private String portfolioItemId;
	private String code;
	private double price;
	private int quantity;
	private String direction;
	private String tradeDate;

	
	public PortfolioItemFormParameters() {
		super();
	}

	public PortfolioItemFormParameters(String portfolioItemId, String code, double price, int quantity, String direction,
			String tradeDate) {
		super();
		this.portfolioItemId = portfolioItemId;
		this.code = code;
		this.price = price;
		this.quantity = quantity;
		this.direction = direction;
		this.tradeDate = tradeDate;
	}

	public String getPortfolioItemId() {
		return portfolioItemId;
	}

	public void setPortfolioItemId(String portfolioItemId) {
		this.portfolioItemId = portfolioItemId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}
}
