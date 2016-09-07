package com.intelliinvest.data.dao.persistent;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.intelliinvest.common.exception.IntelliInvestException;
import com.intelliinvest.data.dao.EODHistoryPriceRepository;
import com.intelliinvest.data.model.EODStockPrice;

@Component("eodHistoryPriceRepository")
class EODHistoryPriceRepositoryDB extends EODPriceRepositoryDB implements EODHistoryPriceRepository{
private static Logger LOGGER = LoggerFactory.getLogger(EODPriceRepositoryDB.class);
	
	@Autowired
	public EODHistoryPriceRepositoryDB(MongoTemplate mongoTemplate) {
		super(mongoTemplate);
	}
	
	public List<EODStockPrice> getHistoryStockPrices(String exchange, LocalDate eodDate){
		Query query = new Query();
		query.addCriteria(Criteria.where("exchange").is(exchange).and("eodDate").is(eodDate));
		return mongoTemplate.find(query, EODStockPrice.class, EODStockPrice.COLLECTION_NAME);
	}
	
	public List<EODStockPrice> getHistoryStockPrices(String exchange, LocalDate startDate, LocalDate endDate){
		Query query = new Query();
		query.addCriteria(Criteria.where("exchange").is(exchange).and("eodDate").gte(startDate)
				.lte(endDate));
		return mongoTemplate.find(query, EODStockPrice.class, EODStockPrice.COLLECTION_NAME);
	}
	
	public EODStockPrice getHistoryStockPricesForCode(String exchange, String symbol, LocalDate date){
		Query query = new Query();
		query.addCriteria(Criteria.where("exchange").is(exchange).and("symbol").is(symbol).and("eodDate").is(date));
		return mongoTemplate.findOne(query, EODStockPrice.class, EODStockPrice.COLLECTION_NAME);
	}
	
	public List<EODStockPrice> getHistoryStockPricesForCode(String exchange, String symbol, LocalDate startDate, LocalDate endDate){
		Query query = new Query();
		query.addCriteria(Criteria.where("exchange").is(exchange).and("symbol").is(symbol).and("eodDate").gte(startDate)
				.lte(endDate));
		return mongoTemplate.find(query, EODStockPrice.class, EODStockPrice.COLLECTION_NAME);
	}
	
	public void updateHistoryPrices(String exchange, List<EODStockPrice> eodStockPrices) throws IntelliInvestException {
		super.updateEODStockPrices(eodStockPrices);
	}

}
