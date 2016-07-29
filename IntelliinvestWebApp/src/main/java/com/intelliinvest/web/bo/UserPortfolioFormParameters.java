package com.intelliinvest.web.bo;

import java.io.Serializable;
import java.util.List;

import com.intelliinvest.data.model.Portfolio;
import com.intelliinvest.data.model.PortfolioItem;

public class UserPortfolioFormParameters implements Serializable {

	private String userId;
	private String portfolioName;
	private String portfolioItemCode;
	private List<PortfolioItem> portfolioItems;

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

	public List<PortfolioItem> getPortfolioItems() {
		return portfolioItems;
	}

	public void setPortfolioItems(List<PortfolioItem> portfolioItems) {
		this.portfolioItems = portfolioItems;
	}

}
