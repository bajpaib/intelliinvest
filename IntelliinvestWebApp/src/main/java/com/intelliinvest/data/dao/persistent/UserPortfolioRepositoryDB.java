package com.intelliinvest.data.dao.persistent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.UserPortfolioRepository;
import com.intelliinvest.data.model.Portfolio;
import com.intelliinvest.data.model.PortfolioItem;
import com.intelliinvest.data.model.UserPortfolio;
import com.intelliinvest.util.DateUtil;
import com.mongodb.Mongo;

@Component("userPortfolioRepository")
class UserPortfolioRepositoryDB implements UserPortfolioRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(UserPortfolioRepositoryDB.class);

	private final MongoTemplate mongoTemplate;

	@Autowired
	public UserPortfolioRepositoryDB(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
//	private void validatePortfolioItem(PortfolioItem item){
//		if (!(Helper.isNotNullAndNonEmpty(item.getCode()) && Helper.isNotNullAndNonEmpty(item.getDirection())
//				&& item.getTradeDate() != null && MathUtil.round(item.getPrice()) > 0 && item.getQuantity() > 0)) {
//			throw new IntelliInvestException(
//					"Invalid Portfolio Item. Please check code, direction, trade date, price and quantity.");
//		}
//	}

	public UserPortfolio getUserPortfolio(String userId){
		LOGGER.info("Retrieving user portfolio for userId {}", userId);
		return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(userId)), UserPortfolio.class, UserPortfolio.COLLECTION_NAME);
	}
	
	public Portfolio getPortfolio(String userId, String portfolioName){
		LOGGER.info("Retrieving portfolio for userId {} and portfolio name {}", userId, portfolioName);
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		return userPortfolio.getPortfolioByName(portfolioName);
	}
	
	public Collection<String> getPortfolioNames(String userId){
		LOGGER.info("Retrieving portfolio names for userId {}", userId);
		return getUserPortfolio(userId).getPortfolioNames();
	}

	public Collection<PortfolioItem> getPortfolioItems(String userId, String portfolioName){
		LOGGER.info("Retrieving portfolio items for userId {}  and portfolio name {}", userId, portfolioName);
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		return userPortfolio.getPortfolioByName(portfolioName).getPortfolioItems();
	}
	
	public Collection<PortfolioItem> getPortfolioItems(String userId, String portfolioName, String code){
		LOGGER.info("Retrieving portfolio items for userId {}  and portfolio name {} and code {}", userId, portfolioName, code);
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		return userPortfolio.getPortfolioByName(portfolioName).getPortfolioItemsByCode(code);
	}
	
	public void addUserPortfolio(UserPortfolio userPortfolio){
		LOGGER.info("Adding user portfolio for userId {}", userPortfolio.getUserId());
		mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
	}
	
	public UserPortfolio addPortfolio(String userId, Portfolio portfolio){
		LOGGER.info("Adding portfolio for userId {} with portfolio name {}", userId, portfolio.getPortfolioName() );
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		userPortfolio.addPortfolio(portfolio);
		mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		return userPortfolio;
	}
	
	public Collection<PortfolioItem> addPortfolioItem(String userId, String portfolioName, PortfolioItem portfolioItem){
		LOGGER.info("Adding portfolio items for userId {} with portfolio name {} with code {}", userId, portfolioName, portfolioItem.getCode());
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		Portfolio portfolio = userPortfolio.getPortfolioByName(portfolioName);
		portfolio.addPortfolioItem(portfolioItem);
		mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		return portfolio.getPortfolioItemsByCode(portfolioItem.getCode());
	}
	
	public Collection<PortfolioItem> addPortfolioItems(String userId, String portfolioName, Collection<PortfolioItem> portfolioItems){
		LOGGER.info("Adding portfolio items for userId {} with portfolio name {} with size {}", userId, portfolioName, portfolioItems.size() );
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		Portfolio portfolio = userPortfolio.getPortfolioByName(portfolioName);
		portfolio.addPortfolioItems(portfolioItems);
		mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		return portfolio.getPortfolioItems();
	}

	public Portfolio updatePortfolioName(String userId, String oldPortfolioName, String newPortfolioName)
	{
		LOGGER.info("updating portfolio name for userId {} from old portfolio name {} to new portfolio name {}", userId, oldPortfolioName, newPortfolioName);
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		Portfolio portfolio = userPortfolio.getPortfolioByName(oldPortfolioName);
		portfolio.setPortfolioName(newPortfolioName);
		mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		return portfolio;
	}
	
	public Portfolio updatePortfolio(String userId, Portfolio portfolio)
	{
		LOGGER.info("updating portfolio for userId {} with portfolio name {}", userId, portfolio.getPortfolioName());
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		userPortfolio.getPortfolios().remove(portfolio);
		userPortfolio.addPortfolio(portfolio);
		mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		return portfolio;
	}
	
	
	public Collection<PortfolioItem> updatePortfolioItem(String userId, String portfolioName, PortfolioItem portfolioItem)
	{
		LOGGER.info("Updating portfolio item for userId {} with portfolio name {} for portfolioItem {}:{}", userId, portfolioName, portfolioName
															, portfolioItem.getCode(), portfolioItem.getTradeDate());
		return updatePortfolioItems(userId, portfolioName, Collections.singletonList(portfolioItem));
	}
	
	public Collection<PortfolioItem> updatePortfolioItems(String userId, String portfolioName, Collection<PortfolioItem> portfolioItems)
	{
		LOGGER.info("Updating portfolio item for userId {} with portfolio name {} for portfolioItem size {}", userId, portfolioName, portfolioName
															, portfolioItems.size());
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		Portfolio portfolio = userPortfolio.getPortfolioByName(portfolioName);
		if(portfolio.getPortfolioItems().removeAll(portfolioItems)){
			portfolio.getPortfolioItems().addAll(portfolioItems);
			mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		}
		return portfolio.getPortfolioItems();
	}
	
	public void deleteUserPortfolio(String userId){
		LOGGER.info("Deleting user portfolio for userId {}", userId);
		mongoTemplate.remove(Query.query(Criteria.where("userId").is(userId)), UserPortfolio.class, UserPortfolio.COLLECTION_NAME);
	}
	
	public void deletePortfolio(String userId, String portfolioName){
		LOGGER.info("Deleting portfolio for userId {} with portfolio name {}", userId, portfolioName);
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		if(userPortfolio.getPortfolios().remove(new Portfolio(portfolioName))){
			mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		}
	}
	
	public Collection<PortfolioItem> deletePortfolioItem(String userId, String portfolioName, String portfolioItemId){
		LOGGER.info("Deleting portfolio for userId {} with portfolio name {} and portfolioItemId {}", userId, portfolioName, portfolioItemId);
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		Portfolio portfolio = userPortfolio.getPortfolioByName(portfolioName);
		PortfolioItem portfolioItem = portfolio.getPortfolioItem(portfolioItemId);
		if(portfolio.getPortfolioItems().remove(portfolioItem)){
			mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		}
		return portfolio.getPortfolioItemsByCode(portfolioItem.getCode());
	}
	
	public Collection<PortfolioItem> deletePortfolioItem(String userId, String portfolioName, PortfolioItem portfolioItem){
		LOGGER.info("Deleting portfolio item for userId {} with portfolio name {} and portfolioItemId {}", userId, portfolioName, portfolioItem.getPortfolioItemId());
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		Portfolio portfolio = userPortfolio.getPortfolioByName(portfolioName);
		if(portfolio.getPortfolioItems().remove(portfolioItem)){
			portfolio.getPortfolioItems().remove(portfolioItem);
		}
		mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		return portfolio.getPortfolioItems();
	}
	
	public Collection<PortfolioItem> deletePortfolioItem(String userId, String portfolioName, Collection<PortfolioItem> portfolioItems){
		LOGGER.info("Deleting portfolio item for userId {} with portfolio name {} and portfolioItems size {}", userId, portfolioName, portfolioItems.size());
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		Portfolio portfolio = userPortfolio.getPortfolioByName(portfolioName);
		if(portfolio.getPortfolioItems().removeAll(portfolioItems)){
			mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		}
		return portfolio.getPortfolioItems();
	}

	public Collection<PortfolioItem> deletePortfolioItemByCode(String userId, String portfolioName, String code){
		LOGGER.info("Deleting portfolio item for userId {} with portfolio name {} and code {}", userId, portfolioName, code);
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		Portfolio portfolio = userPortfolio.getPortfolioByName(portfolioName);
		if(portfolio.getPortfolioItems().removeAll(portfolio.getPortfolioItemsByCode(code))){
			mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		}
		return portfolio.getPortfolioItems();
	}
	
	public Collection<PortfolioItem> deletePortfolioItemsInCode(String userId, String portfolioName, String code, Collection<PortfolioItem> portfolioItems){
		LOGGER.info("Deleting portfolio item for userId {} with portfolio name {} and code {} and portfolioItem size {}", userId, portfolioName, code, portfolioItems.size());
		UserPortfolio userPortfolio = getUserPortfolio(userId);
		Portfolio portfolio = userPortfolio.getPortfolioByName(portfolioName);
		if(portfolio.getPortfolioItems().removeAll(portfolioItems)){
			mongoTemplate.save(userPortfolio, UserPortfolio.COLLECTION_NAME);
		}
		return portfolio.getPortfolioItemsByCode(code);
	}

	public static void main(String[] args){
		String userId = "raja.rengasamy@gmail.com";
		String portfolioName = "Portfolio";
		
		MongoTemplate mongoTemplate = new MongoTemplate(new Mongo("localhost", 27017), "test");
		UserPortfolioRepository userPortfolioRepository = new UserPortfolioRepositoryDB(mongoTemplate);
		
		UserPortfolio userPortfolio = new UserPortfolio();
		userPortfolio.setUserId(userId);
		userPortfolioRepository.addUserPortfolio(userPortfolio);
		System.out.println("Added UserPortfolio " + userPortfolio);
		
		userPortfolio = userPortfolioRepository.getUserPortfolio(userId);
		System.out.println("Retrieved UserPortfolio " + userPortfolio);
		
		Portfolio portfolio = new Portfolio();
		portfolio.setPortfolioName(portfolioName);
		userPortfolio = userPortfolioRepository.addPortfolio(userId, portfolio);
		System.out.println("Added Portfolio " + userPortfolio);
		
		portfolio = userPortfolioRepository.getPortfolio(userId, portfolioName);
		System.out.println("Got using  Portfolio Id " + portfolio);
		
		PortfolioItem portfolioItem1 = new PortfolioItem("INFY", 100D, 100, "BUY", DateUtil.getLocalDate());
		PortfolioItem portfolioItem2 = new PortfolioItem("ABAN", 100D, 100, "BUY", DateUtil.getLocalDate());
		userPortfolioRepository.addPortfolioItems(userId, portfolioName, Arrays.asList(portfolioItem1, portfolioItem2));
		userPortfolio = userPortfolioRepository.getUserPortfolio(userId);
		System.out.println("Added PortfolioItems " + userPortfolio);
		
		Collection<PortfolioItem> portfolioItems = userPortfolioRepository.getPortfolioItems(userId, portfolioName, "INFY");
		portfolioItem1 = new ArrayList<PortfolioItem>(portfolioItems).get(0);
		System.out.println("Got using  Portfolio Item Code " + portfolioItems);
		
		userPortfolioRepository.updatePortfolioName(userId, portfolioName, "Portfolio2");
		portfolioName = "Portfolio2";
		portfolio = userPortfolioRepository.getPortfolio(userId, portfolioName);
		System.out.println("Got Portfolio after update " + portfolio);
		
		portfolioItem1.setPrice(200d);
		userPortfolioRepository.updatePortfolioItem(userId, portfolioName, portfolioItem1);
		portfolioItems = userPortfolioRepository.getPortfolioItems(userId, portfolioName, "INFY");
		portfolioItem1 = new ArrayList<PortfolioItem>(portfolioItems).get(0);
		System.out.println("Got Portfolio Item after update " + portfolioItem1);
		
		userPortfolioRepository.deletePortfolioItem(userId, portfolioName, portfolioItem1.getPortfolioItemId());
		userPortfolio = userPortfolioRepository.getUserPortfolio(userId);
		System.out.println("Deleted PortfolioItems by id" + userPortfolio);
		
		userPortfolioRepository.deletePortfolioItemByCode(userId, portfolioName, portfolioItem2.getCode());
		userPortfolio = userPortfolioRepository.getUserPortfolio(userId);
		System.out.println("Deleted PortfolioItems by code" + userPortfolio);
		
		userPortfolioRepository.deletePortfolio(userId, portfolioName);
		userPortfolio = userPortfolioRepository.getUserPortfolio(userId);
		System.out.println("Deleted Portfolio " + userPortfolio);
		
		userPortfolioRepository.deleteUserPortfolio(userId);
		userPortfolio = userPortfolioRepository.getUserPortfolio(userId);
		System.out.println("Deleted UserPortfolio " + userPortfolio);
	}

}
