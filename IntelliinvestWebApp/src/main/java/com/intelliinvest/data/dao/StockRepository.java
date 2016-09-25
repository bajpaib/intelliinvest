package com.intelliinvest.data.dao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
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
import com.intelliinvest.data.model.StockFundamentals;
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
	@Autowired
	private DateUtil dateUtil;
	private Map<String, Stock> stockCache = new ConcurrentHashMap<String, Stock>();
	private Map<String, StockPrice> stockPriceCache = new ConcurrentHashMap<String, StockPrice>();
	private Map<String, String> nseToSecurityIdMap = new ConcurrentHashMap<String, String>();
	private Map<String, String> bseToSecurityIdMap = new ConcurrentHashMap<String, String>();
	private Map<String, String> nseToBSEMap = new ConcurrentHashMap<String, String>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<Stock> stocks = getStocksFromDB();
		if (Helper.isNotNullAndNonEmpty(stocks)) {
			for (Stock stock : stocks) {
				stockCache.put(stock.getSecurityId(), stock);
				if (Helper.isNotNullAndNonEmpty(stock.getBseCode())) {
					bseToSecurityIdMap.put(stock.getBseCode(), stock.getSecurityId());
				}
				if (Helper.isNotNullAndNonEmpty(stock.getNseCode())) {
					nseToSecurityIdMap.put(stock.getNseCode(), stock.getSecurityId());
				}
				
				if (Helper.isNotNullAndNonEmpty(stock.getBseCode()) && Helper.isNotNullAndNonEmpty(stock.getNseCode())) {
					nseToBSEMap.put(stock.getNseCode(), stock.getBseCode());
				}
				
			}
			logger.info("Initialised stockCache in StockRepository from DB with size " + stockCache.size());
		} else {
			logger.error("Could not initialise stockCache from DB in StockRepository. STOCK is empty.");
		}

		List<StockPrice> prices = getStockPricesFromDB();
		if (Helper.isNotNullAndNonEmpty(prices)) {
			for (StockPrice price : prices) {
				stockPriceCache.put(price.getSecurityId(), price);
			}
			logger.info("Initialised stockPriceCache in StockRepository from DB with size " + stockPriceCache.size());
		} else {
			logger.error("Could not initialise stockPriceCache from DB in StockRepository. STOCK_PRICE is empty.");
		}
	}

	public String getSecurityIdFromNSECode(String nseCode) {
		return nseToSecurityIdMap.get(nseCode);
	}

	public String getSecurityIdFromBSECode(String bseCode) {
		return bseToSecurityIdMap.get(bseCode);
	}
	
	public String getBSECodeFromNSECode(String nseCode) {
		return nseToBSEMap.get(nseCode);
	}

	public Stock getStockById(String id) throws DataAccessException {
		logger.debug("Inside getStockById()...");
		Stock retVal = null;
		retVal = stockCache.get(id);
		if (retVal == null) {
			logger.error("Inside getStockById() Stock not found in cache for " + id);
		}
		return retVal;
	}

	public Stock getStockByBseCode(String bseCode) throws DataAccessException {
		logger.debug("Inside getStockByBseCode()...");
		Stock retVal = null;
		for (Stock stock : stockCache.values()) {
			if (bseCode.equals(stock.getBseCode())) {
				retVal = stock;
				break;
			}
		}
		if (retVal == null) {
			logger.error("Inside getStockByBseCode() Stock not found in cache for " + bseCode);
		}
		return retVal;
	}

	public Stock getStockByNseCode(String nseCode) throws DataAccessException {
		logger.debug("Inside getStockByNseCode()...");
		Stock retVal = null;
		for (Stock stock : stockCache.values()) {
			if (nseCode.equals(stock.getNseCode())) {
				retVal = stock;
				break;
			}
		}
		if (retVal == null) {
			logger.error("Inside getStockByNseCode() Stock not found in cache for " + nseCode);
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

		if (Helper.isNotNullAndNonEmpty(retVal)) {
			retVal.sort(new Comparator<Stock>() {
				public int compare(Stock stock1, Stock stock2) {
					return stock1.getSecurityId().compareTo(stock2.getSecurityId());

				}
			});
		}
		return retVal;
	}

	public List<Stock> getStocksFromDB() throws DataAccessException {
		logger.debug("Inside getStocksFromDB()...");
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "securityId"));
		return mongoTemplate.find(query, Stock.class, COLLECTION_STOCK);
	}

	public StockPrice getStockPriceById(String id) throws DataAccessException {
		logger.debug("Inside getStockPriceById()...");
		StockPrice retVal = null;
		retVal = stockPriceCache.get(id);
		if (retVal == null) {
			logger.error("Inside getStockPriceById() StockPrice not found in cache for " + id);
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
		if (Helper.isNotNullAndNonEmpty(retVal)) {
			retVal.sort(new Comparator<StockPrice>() {
				public int compare(StockPrice price1, StockPrice price2) {
					return price1.getSecurityId().compareTo(price2.getSecurityId());

				}
			});
		}
		return retVal;
	}

	public List<StockPrice> getStockPricesFromDB() throws DataAccessException {
		logger.debug("Inside getStockPricesFromDB()...");
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "securityId"));
		return mongoTemplate.find(query, StockPrice.class, COLLECTION_STOCK_PRICE);
	}

	public void bulkUploadLatestStockPrices(List<StockPrice> currentPrices) {
		logger.info("Inside bulkUploadLatestStockPrices()...");		
		// delete all existing records
		mongoTemplate.remove(new Query(), StockPrice.class, COLLECTION_STOCK_PRICE);		
		// batch inserts
		int start = -1000;
		int end = 0;
		while (end < currentPrices.size()) {
			start = start + 1000;
			end = end + 1000;
			if (end > currentPrices.size()) {
				end = currentPrices.size();
			}
			List<StockPrice> currentPricesTemp = currentPrices.subList(start, end);
			mongoTemplate.insert(currentPricesTemp, COLLECTION_STOCK_PRICE);
		}
		
		for(StockPrice price: currentPrices){
			stockPriceCache.put(price.getSecurityId(), price);
		}
	}

	public void bulkInsertStocks(List<String> stocks) {
		logger.debug("Inside bulkInsertStocks()...");
		for (String temp : stocks) {
			String[] stockValues = temp.split(",");
			String securityId = stockValues[0];
			String bseCode = stockValues[1];
			String nseCode = stockValues[2];
			String fundamentalCode = stockValues[3];
			String name = stockValues[4];
			String isin = stockValues[5];
			String industry = stockValues[6];
			boolean worldStock = Boolean.parseBoolean(stockValues[7]);
			boolean niftyStock = Boolean.parseBoolean(stockValues[8]);
			boolean nseStock = Boolean.parseBoolean(stockValues[9]);
			Stock stock = new Stock(securityId, bseCode, nseCode, fundamentalCode, name, isin, industry, worldStock, niftyStock, nseStock, dateUtil.getLocalDateTime());
			mongoTemplate.save(stock, COLLECTION_STOCK);
			stockCache.put(stock.getSecurityId(), stock);
		}
	}

	@ManagedOperation(description = "getStockById")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "id", description = "Stock Id") })
	public String getStock(String id) throws DataAccessException {
		Stock stock = getStockById(id.trim());
		if (stock != null) {
			return stock.toString();
		} else {
			return "Stock not found";
		}
	}

	@ManagedOperation(description = "getStockPriceById")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "id", description = "Stock Id") })
	public String getStockPrice(String id) throws DataAccessException {
		StockPrice price = getStockPriceById(id.trim());
		if (price != null) {
			return price.toString();
		} else {
			return "Stock Price not found";
		}
	}
}