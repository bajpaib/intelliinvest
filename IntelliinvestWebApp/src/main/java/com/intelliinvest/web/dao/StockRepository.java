package com.intelliinvest.web.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.intelliinvest.data.model.NSEtoBSEMap;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;

public class StockRepository {
	private static Logger logger = Logger.getLogger(UserRepository.class);
	private static final String COLLECTION_STOCK = "STOCK";
	private static final String COLLECTION_STOCK_PRICE = "STOCK_PRICE";
	private static final String COLLECTION_NSE_BSE_CODES = "NSE_BSE_CODES";
	@Autowired
	private MongoTemplate mongoTemplate;

	public Stock getStockByCode(String code) throws DataAccessException {
		logger.info("Inside getStockByCode()...");
		return mongoTemplate.findOne(Query.query(Criteria.where("code").is(code)), Stock.class, COLLECTION_STOCK);
	}

	public List<Stock> getStocks() throws DataAccessException {
		logger.info("Inside getStocks()...");
		return mongoTemplate.findAll(Stock.class, COLLECTION_STOCK);
	}

	public StockPrice getStockPriceByCode(String code) throws DataAccessException {
		logger.info("Inside getStockPriceByCode()...");
		return mongoTemplate.findOne(Query.query(Criteria.where("code").is(code)), StockPrice.class, COLLECTION_STOCK_PRICE);
	}

	public List<StockPrice> getStockPrices() throws DataAccessException {
		logger.info("Inside getStockPrices()...");
		return mongoTemplate.findAll(StockPrice.class, COLLECTION_STOCK_PRICE);
	}

	public List<NSEtoBSEMap> getNSEtoBSEMap() throws DataAccessException {
		logger.info("Inside getNSEtoBSEMap()...");
		return mongoTemplate.findAll(NSEtoBSEMap.class, COLLECTION_NSE_BSE_CODES);
	}

	public List<StockPrice> updateCurrentStockPrices(List<StockPrice> currentPrices) {
		logger.info("Inside updateCurrentStockPrices()...");
		List<StockPrice> retVal = new ArrayList<StockPrice>();
		Date currentDateTime = new Date();
		for (StockPrice price : currentPrices) {
			Query query = new Query();
			Update update = new Update();
			update.set("code", price.getCode());
			update.set("currentPrice", price.getCurrentPrice());
			update.set("cp", price.getCp());
			update.set("updateDate", currentDateTime);
			query.addCriteria(Criteria.where("code").is(price.getCode()));
			retVal.add(mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true).upsert(true),
					StockPrice.class, COLLECTION_STOCK_PRICE));
		}
		return retVal;
	}

	public List<StockPrice> updateEODStockPrices(List<StockPrice> eodPrices) {
		logger.info("Inside updateEODStockPrices()...");
		List<StockPrice> retVal = new ArrayList<StockPrice>();
		Date currentDateTime = new Date();
		for (StockPrice price : eodPrices) {
			Query query = new Query();
			Update update = new Update();
			update.set("code", price.getCode());
			update.set("eodPrice", price.getEodPrice());
			update.set("eodDate", price.getEodDate());
			update.set("updateDate", currentDateTime);
			query.addCriteria(Criteria.where("code").is(price.getCode()));
			retVal.add(mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true).upsert(true),
					StockPrice.class, COLLECTION_STOCK_PRICE));
		}
		return retVal;
	}

	public void bulkInsertStocks(List<String> stocks) {
		logger.info("Inside bulkInsertStocks()...");
		for (String temp : stocks) {
			String[] stockValues = temp.split(",");
			String code = stockValues[0];
			String name = stockValues[1];
			boolean worldStock = Boolean.parseBoolean(stockValues[2]);
			boolean niftyStock = Boolean.parseBoolean(stockValues[2]);
			Stock stock = new Stock(code, name, worldStock, niftyStock, new Date());
			mongoTemplate.save(stock, COLLECTION_STOCK);
		}
	}

}
