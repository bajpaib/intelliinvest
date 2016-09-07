package com.intelliinvest.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.UserPortfolioRepository;
import com.intelliinvest.data.model.Portfolio;
import com.intelliinvest.data.model.PortfolioItem;
import com.intelliinvest.data.model.UserPortfolio;

@Component
public class UserPortfolioService {

	private static Logger LOGGER = LoggerFactory.getLogger(UserPortfolioService.class);
	
	private final UserPortfolioRepository userPortfolioRepository;
	private final UserPortfolioEnricher userPortfolioEnricher;
	
	@Autowired
	public UserPortfolioService(UserPortfolioRepository userPortfolioRepository, UserPortfolioEnricher userPortfolioEnricher) {
		this.userPortfolioRepository = userPortfolioRepository;
		this.userPortfolioEnricher = userPortfolioEnricher;
	}
	
	public UserPortfolio getUserPortfolio(String userId){
		UserPortfolio userPortfolio = userPortfolioRepository.getUserPortfolio(userId);
		userPortfolioEnricher.enrichUserPortfolio(userPortfolio);
		return userPortfolio;
	}

	public Portfolio getPortfolio(String userId, String portfolioName){
		Portfolio portfolio = userPortfolioRepository.getPortfolio(userId, portfolioName);
		userPortfolioEnricher.enrichPortfolio(portfolio);
		return portfolio;
	}
	
	public Collection<String> getPortfolioNames(String userId){
		return userPortfolioRepository.getPortfolioNames(userId);
	}

	public Collection<PortfolioItem> getPortfolioItems(String userId, String portfolioName){
		Collection<PortfolioItem> portfolioItems = userPortfolioRepository.getPortfolioItems(userId, portfolioName);
		userPortfolioEnricher.populatePnlForPortfolioItems(portfolioItems);
		return portfolioItems;
	}
	
	public Collection<PortfolioItem> getPortfolioItems(String userId, String portfolioName, String code){
		Collection<PortfolioItem> portfolioItems =  userPortfolioRepository.getPortfolioItems(userId, portfolioName, code);
		userPortfolioEnricher.populatePnlForPortfolioItems(portfolioItems);
		return portfolioItems;
	}
	
	public UserPortfolio addUserPortfolio(String userId){
		UserPortfolio userPortfolio = new UserPortfolio(userId);
		return addUserPortfolio(userPortfolio);
	}
	
	public UserPortfolio addUserPortfolio(UserPortfolio userPortfolio){
		userPortfolioRepository.addUserPortfolio(userPortfolio);
		userPortfolioEnricher.enrichUserPortfolio(userPortfolio);
		return userPortfolio;
	}
	
	public Portfolio addPortfolio(String userId, String portfolioName){
		return addPortfolio(userId, new Portfolio(portfolioName));
	}
	
	public Portfolio addPortfolio(String userId, Portfolio portfolio){
		userPortfolioRepository.addPortfolio(userId, portfolio);
		userPortfolioEnricher.enrichPortfolio(portfolio);
		return portfolio;
	}
	
	public Collection<PortfolioItem> addPortfolioItem(String userId, String portfolioName, PortfolioItem portfolioItem){
		Collection<PortfolioItem> portfolioItems = userPortfolioRepository.addPortfolioItem(userId, portfolioName, portfolioItem);
		return userPortfolioEnricher.getSummaryPortfolioItems(portfolioItems);
	}
	
	public Collection<PortfolioItem> addPortfolioItems(String userId, String portfolioName, Collection<PortfolioItem> portfolioItems){
		portfolioItems = userPortfolioRepository.addPortfolioItems(userId, portfolioName, portfolioItems);
		return userPortfolioEnricher.getSummaryPortfolioItems(portfolioItems);
	}

	public Portfolio updatePortfolioName(String userId, String oldPortfolioName, String newPortfolioName)
	{
		Portfolio portfolio = userPortfolioRepository.updatePortfolioName(userId, oldPortfolioName, newPortfolioName);
		userPortfolioEnricher.enrichPortfolio(portfolio);
		return portfolio;
	}
	
	public Portfolio updatePortfolio(String userId, Portfolio portfolio)
	{
		userPortfolioRepository.updatePortfolio(userId, portfolio);
		userPortfolioEnricher.enrichPortfolio(portfolio);
		return portfolio;
	}
	
	public Collection<PortfolioItem> updatePortfolioItem(String userId, String portfolioName, PortfolioItem portfolioItem)
	{
		Collection<PortfolioItem> portfolioItems = userPortfolioRepository.updatePortfolioItems(userId, portfolioName, Collections.singletonList(portfolioItem));
		return userPortfolioEnricher.getSummaryPortfolioItems(portfolioItems);
	}
	
	public Collection<PortfolioItem> updatePortfolioItems(String userId, String portfolioName, Collection<PortfolioItem> portfolioItems)
	{
		portfolioItems = userPortfolioRepository.updatePortfolioItems(userId, portfolioName, portfolioItems);
		return userPortfolioEnricher.getSummaryPortfolioItems(portfolioItems);
	}
	
	public void deleteUserPortfolio(String userId){
		userPortfolioRepository.deleteUserPortfolio(userId);
	}
	
	public void deletePortfolio(String userId, String portfolioName){
		userPortfolioRepository.deletePortfolio(userId, portfolioName);
	}
	
	public Collection<PortfolioItem> deletePortfolioItem(String userId, String portfolioName, String portfolioItemId){
		Collection<PortfolioItem> portfolioItems = userPortfolioRepository.deletePortfolioItem(userId, portfolioName, portfolioItemId);
		return userPortfolioEnricher.getSummaryPortfolioItems(portfolioItems);
	}
	
	public Collection<PortfolioItem> deletePortfolioItem(String userId, String portfolioName, PortfolioItem portfolioItem){
		Collection<PortfolioItem> portfolioItems = userPortfolioRepository.deletePortfolioItem(userId, portfolioName, Collections.singletonList(portfolioItem));
		return userPortfolioEnricher.getSummaryPortfolioItems(portfolioItems);
	}
	
	public Collection<PortfolioItem> deletePortfolioItem(String userId, String portfolioName, Collection<PortfolioItem> portfolioItems){
		portfolioItems = userPortfolioRepository.deletePortfolioItem(userId, portfolioName, portfolioItems);
		return userPortfolioEnricher.getSummaryPortfolioItems(portfolioItems);
	}
	
	public Collection<PortfolioItem> deletePortfolioItemByCode(String userId, String portfolioName, String code){
		Collection<PortfolioItem> portfolioItems = userPortfolioRepository.deletePortfolioItemByCode(userId, portfolioName, code);
		return userPortfolioEnricher.getSummaryPortfolioItems(portfolioItems);
	}
	
	public Collection<PortfolioItem> deletePortfolioItemsInCode(String userId, String portfolioName, String code, Collection<PortfolioItem> portfolioItems){
		portfolioItems = userPortfolioRepository.deletePortfolioItemsInCode(userId, portfolioName, code, portfolioItems);
		return userPortfolioEnricher.getSummaryPortfolioItems(portfolioItems);
	}
	
	public Map<String, Collection<PortfolioItem>> getUserPortfolioSummary(String userId) {
		UserPortfolio userPortfolio = userPortfolioRepository.getUserPortfolio(userId);
		return userPortfolioEnricher.getUserPortfolioSummary(userPortfolio);
	}
	
	public Collection<PortfolioItem> getPortfolioSummary(String userId, String portfolioName) {
		Portfolio portfolio = userPortfolioRepository.getPortfolio(userId, portfolioName);
		return userPortfolioEnricher.getPortfolioSummary(portfolio);
	}

}
