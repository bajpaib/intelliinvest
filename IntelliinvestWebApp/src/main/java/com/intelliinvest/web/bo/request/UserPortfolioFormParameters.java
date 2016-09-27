package com.intelliinvest.web.bo.request;

import java.io.Serializable;
import java.util.List;

public class UserPortfolioFormParameters implements Serializable {

	private String userId;
	private String portfolioName;
	private String portfolioItemCode;
	private List<PortfolioItemFormParameters> portfolioItems;	
	
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

	public List<PortfolioItemFormParameters> getPortfolioItems() {
		return portfolioItems;
	}

	public void setPortfolioItems(List<PortfolioItemFormParameters> portfolioItems) {
		this.portfolioItems = portfolioItems;
	}
}
