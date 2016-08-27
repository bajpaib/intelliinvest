package com.intelliinvest.web.bo;

import java.io.Serializable;
import java.util.List;

public class UserPortfolioFormParameters implements Serializable {

	private String userId;
	private String portfolioName;
	private String portfolioItemCode;
	private List<PortfolioItemRequest> portfolioItems;	
	
	public UserPortfolioFormParameters() {
		super();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPortfolioName() {
		return portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}

	public String getPortfolioItemCode() {
		return portfolioItemCode;
	}

	public void setPortfolioItemCode(String portfolioItemCode) {
		this.portfolioItemCode = portfolioItemCode;
	}

	public List<PortfolioItemRequest> getPortfolioItems() {
		return portfolioItems;
	}

	public void setPortfolioItems(List<PortfolioItemRequest> portfolioItems) {
		this.portfolioItems = portfolioItems;
	}
}
