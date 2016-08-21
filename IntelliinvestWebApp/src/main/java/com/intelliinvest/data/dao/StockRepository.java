package com.intelliinvest.data.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;

@ManagedResource(objectName = "bean:name=StockRepository", description = "StockRepository")
public class StockRepository {
	private static Logger logger = Logger.getLogger(StockRepository.class);
	private static final String COLLECTION_STOCK = "STOCK";
	private static final String COLLECTION_STOCK_PRICE = "STOCK_PRICE";
	@Autowired
	private MongoTemplate mongoTemplate;
	private Map<String, Stock> stockCache = new ConcurrentHashMap<String, Stock>();
	private Map<String, StockPrice> stockPriceCache = new ConcurrentHashMap<String, StockPrice>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<Stock> stocks = getStocksFromDB();
		if (Helper.isNotNullAndNonEmpty(stocks)) {
			for (Stock stock : stocks) {
				stockCache.put(stock.getCode(), stock);
			}
			logger.info("Initialised stockCache in StockRepository from DB with size " + stockCache.size());
		} else {
			logger.error("Could not initialise stockCache from DB in StockRepository. STOCK is empty.");
		}

		List<StockPrice> prices = getStockPricesFromDB();
		if (Helper.isNotNullAndNonEmpty(prices)) {
			for (StockPrice price : prices) {
				stockPriceCache.put(price.getCode(), price);
			}
			logger.info("Initialised stockPriceCache in StockRepository from DB with size " + stockPriceCache.size());
		} else {
			logger.error("Could not initialise stockPriceCache from DB in StockRepository. STOCK_PRICE is empty.");
		}
	}

	public Stock getStockByCode(String code) throws DataAccessException {
		logger.debug("Inside getStockByCode()...");
		Stock retVal = null;
		retVal = stockCache.get(code);
		if (retVal == null) {
			logger.error("Inside getStockByCode() Stock not found in cache for " + code);
		}
		return retVal;
	}

	public List<Stock> getStocks() {
		logger.debug("Inside getStocks()...");
		List<Stock> retVal = new ArrayList<Stock>();
		if (stockCache.size() == 0) {
			logger.error("Inside getStocks() stockCache is empty");
		}
		for (Stock stock : stockCache.values()) {
			retVal.add(stock);
		}
		return retVal;
	}

	public List<Stock> getStocksFromDB() throws DataAccessException {
		logger.debug("Inside getStocksFromDB()...");
		return mongoTemplate.findAll(Stock.class, COLLECTION_STOCK);
	}

	public StockPrice getStockPriceByCode(String code) throws DataAccessException {
		logger.debug("Inside getStockPriceByCode()...");
		StockPrice retVal = null;
		retVal = stockPriceCache.get(code);
		if (retVal == null) {
			logger.error("Inside getStockPriceByCode() StockPrice not found in cache for " + code);
		}
		return retVal;
	}

	public List<StockPrice> getStockPrices() throws DataAccessException {
		logger.debug("Inside getStockPrices()...");
		List<StockPrice> retVal = new ArrayList<StockPrice>();
		if (stockPriceCache.size() == 0) {
			logger.error("Inside getStockPrices() stockPriceCache is empty");
		}
		for (StockPrice price : stockPriceCache.values()) {
			retVal.add(price);
		}
		return retVal;
	}

	public List<StockPrice> getStockPricesFromDB() throws DataAccessException {
		logger.debug("Inside getStockPricesFromDB()...");
		return mongoTemplate.findAll(StockPrice.class, COLLECTION_STOCK_PRICE);
	}

	public List<StockPrice> updateCurrentStockPrices(List<StockPrice> currentPrices) {
		logger.debug("Inside updateCurrentStockPrices()...");
		List<StockPrice> retVal = new ArrayList<StockPrice>();
		Date currentDateTime = DateUtil.getCurrentDate();
		for (StockPrice price : currentPrices) {
			Query query = new Query();
			Update update = new Update();
			update.set("code", price.getCode());
			update.set("currentPrice", price.getCurrentPrice());
			update.set("cp", price.getCp());
			update.set("updateDate", currentDateTime);
			query.addCriteria(Criteria.where("code").is(price.getCode()));
			price = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true).upsert(true),
					StockPrice.class, COLLECTION_STOCK_PRICE);
			retVal.add(price);
			// update cache
			stockPriceCache.put(price.getCode(), price);
		}
		return retVal;
	}

	public List<StockPrice> updateEODStockPrices(List<StockPrice> eodPrices) {
		logger.debug("Inside updateEODStockPrices()...");
		List<StockPrice> retVal = new ArrayList<StockPrice>();
		Date currentDateTime = DateUtil.getCurrentDate();
		for (StockPrice price : eodPrices) {
			Query query = new Query();
			Update update = new Update();
			update.set("code", price.getCode());
			update.set("eodPrice", price.getEodPrice());
			update.set("eodDate", price.getEodDate());
			update.set("updateDate", currentDateTime);
			query.addCriteria(Criteria.where("code").is(price.getCode()));
			price = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true).upsert(true),
					StockPrice.class, COLLECTION_STOCK_PRICE);
			retVal.add(price);
			// update cache
			stockPriceCache.put(price.getCode(), price);
		}
		return retVal;
	}

	public void bulkInsertStocks(List<String> stocks) {
		logger.debug("Inside bulkInsertStocks()...");
		for (String temp : stocks) {
			String[] stockValues = temp.split(",");
			String code = stockValues[0];
			String name = stockValues[1];
			boolean worldStock = Boolean.parseBoolean(stockValues[2]);
			boolean niftyStock = Boolean.parseBoolean(stockValues[2]);
			Stock stock = new Stock(code, name, worldStock, niftyStock, DateUtil.getCurrentDate());
			mongoTemplate.save(stock, COLLECTION_STOCK);
			stockCache.put(stock.getCode(), stock);
		}
	}

	@ManagedOperation(description = "getStockByCode")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "code", description = "Stock Code") })
	public String getStock(String code) throws DataAccessException {
		Stock stock = getStockByCode(code.trim());
		if (stock != null) {
			return stock.toString();
		} else {
			return "Stock not found";
		}

	}

	@ManagedOperation(description = "getStockPriceByCode")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "code", description = "Stock Code") })
	public String getStockPrice(String code) throws DataAccessException {
		StockPrice price = getStockPriceByCode(code.trim());
		if (price != null) {
			return price.toString();
		} else {
			return "Stock Price not found";
		}
	}
}
