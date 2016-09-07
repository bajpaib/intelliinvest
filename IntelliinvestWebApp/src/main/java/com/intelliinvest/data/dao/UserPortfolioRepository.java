package com.intelliinvest.data.dao;

import java.util.Collection;

import com.intelliinvest.data.model.Portfolio;
import com.intelliinvest.data.model.PortfolioItem;
import com.intelliinvest.data.model.UserPortfolio;

public interface UserPortfolioRepository {

	UserPortfolio getUserPortfolio(String userId);

	Portfolio getPortfolio(String userId, String portfolioName);

	Collection<String> getPortfolioNames(String userId);

	Collection<PortfolioItem> getPortfolioItems(String userId, String portfolioName);

	Collection<PortfolioItem> getPortfolioItems(String userId, String portfolioName, String code);

	void addUserPortfolio(UserPortfolio userPortfolio);

	UserPortfolio addPortfolio(String userId, Portfolio portfolio);

	Collection<PortfolioItem> addPortfolioItem(String userId, String portfolioName, PortfolioItem portfolioItem);
	
	Collection<PortfolioItem> addPortfolioItems(String userId, String portfolioName, Collection<PortfolioItem> portfolioItems);

	Portfolio updatePortfolioName(String userId, String oldPortfolioName, String newPortfolioName);

	Portfolio updatePortfolio(String userId, Portfolio portfolio);

	Collection<PortfolioItem> updatePortfolioItem(String userId, String portfolioName, PortfolioItem portfolioItem);

	Collection<PortfolioItem> updatePortfolioItems(String userId, String portfolioName, Collection<PortfolioItem> portfolioItems);

	void deleteUserPortfolio(String userId);

	void deletePortfolio(String userId, String portfolioName);

	Collection<PortfolioItem> deletePortfolioItem(String userId, String portfolioName, String portfolioItemId);

	Collection<PortfolioItem> deletePortfolioItem(String userId, String portfolioName, PortfolioItem portfolioItem);

	Collection<PortfolioItem> deletePortfolioItem(String userId, String portfolioName, Collection<PortfolioItem> portfolioItems);

	Collection<PortfolioItem> deletePortfolioItemByCode(String userId, String portfolioName, String code);
	
	Collection<PortfolioItem> deletePortfolioItemsInCode(String userId, String portfolioName, String code, Collection<PortfolioItem> portfolioItems);

}