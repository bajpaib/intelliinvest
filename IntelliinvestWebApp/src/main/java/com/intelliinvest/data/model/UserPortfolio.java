package com.intelliinvest.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "USER_PORTFOLIO")
public class UserPortfolio implements Serializable {

	@Id
	private String userId;
	private List<Portfolio> portfolios = new ArrayList<Portfolio>();

	public UserPortfolio() {
		super();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<Portfolio> getPortfolios() {
		return portfolios;
	}

	public void setPortfolios(List<Portfolio> portfolios) {
		this.portfolios = portfolios;
	}

	public void addPortfolio(Portfolio portfolio) {
		this.portfolios.add(portfolio);
	}

	public Portfolio getPortfolioByName(String portfolioName) {
		Portfolio retVal = null;
		for (Portfolio item : portfolios) {
			if (portfolioName.equals(item.getPortfolioName())) {
				retVal = item;
				break;
			}
		}
		return retVal;
	}

	public void removePortfolioByName(String portfolioName) {
		Iterator<Portfolio> iter = portfolios.iterator();
		while (iter.hasNext()) {
			Portfolio temp = iter.next();
			if (portfolioName.equals(temp.getPortfolioName())) {
				iter.remove();
				break;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserPortfolio [");
		builder.append("\n");
		builder.append("userId=" + userId);
		builder.append("\n");
		for (Portfolio item : portfolios) {
			builder.append(item.toString());
			builder.append("\n");
		}
		builder.append("]");
		return builder.toString();
	}

}
