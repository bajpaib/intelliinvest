package com.intelliinvest.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = UserPortfolio.COLLECTION_NAME)
public class UserPortfolio implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String COLLECTION_NAME = "USER_PORTFOLIO";
	
	@Id
	private String userId;
	
	private Collection<Portfolio> portfolios = new ArrayList<Portfolio>();

	public UserPortfolio() {
		super();
	}

	public UserPortfolio(String userId) {
		this.userId = userId;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Collection<Portfolio> getPortfolios() {
		return portfolios;
	}

	public void setPortfolios(Collection<Portfolio> portfolios) {
		this.portfolios = portfolios;
	}

	public void addPortfolio(Portfolio portfolio) {
		this.portfolios.add(portfolio);
	}

	public Collection<String> getPortfolioNames() {
		Collection<String> portfolioNames = new ArrayList<String>();
		for (Portfolio portfolio : portfolios) {
			portfolioNames.add(portfolio.getPortfolioName());
		}
		return portfolioNames;
	}
	
	public Portfolio getPortfolioByName(String portfolioName) {
		Portfolio portfolio = null;
		for (Portfolio item : portfolios) {
			if (portfolioName.equals(item.getPortfolioName())) {
				portfolio = item;
				break;
			}
		}
		return portfolio;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserPortfolio other = (UserPortfolio) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		return userId.hashCode();
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
