package com.intelliinvest.data.dao.persistent;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.Stock;

@Component("stockRepositoryDB")
class StockRepositoryDB implements StockRepository{
	private static Logger LOGGER = LoggerFactory.getLogger(StockRepositoryDB.class);
	
	private final MongoTemplate mongoTemplate;
	
	@Autowired
	public StockRepositoryDB(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	public List<Stock> getStocks(){
		LOGGER.debug("Inside getStocksFromDB()");
		return mongoTemplate.findAll(Stock.class, Stock.COLLECTION_NAME);
	}
	
	public Stock getStock(String code){
		return mongoTemplate.findOne(Query.query(Criteria.where("code").is(code)), Stock.class, Stock.COLLECTION_NAME);
	}
	
	public Set<String> getStockCodes() {
		LOGGER.debug("Inside getStocksFromDB()");
		Set<String> stockCodes = new HashSet<String>();
		List<Stock> stocks = getStocks();
		for(Stock stock : stocks){
			stockCodes.add(stock.getCode());
		}
		return stockCodes;
	}

	public void insertStocks(Collection<Stock> stocks) {
		LOGGER.debug("Inside bulkInsertStocks()...");
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, Stock.class);
		operation.remove(new Query());
		operation.insert(stocks);
		operation.execute();
	}

}
