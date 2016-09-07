package com.intelliinvest.data.dao.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.Stock;

@Component("stockRepository")
class StockRepositoryCache implements StockRepository, Cache<Stock>{
	private static Logger LOGGER = LoggerFactory.getLogger(StockRepositoryCache.class);
	
	private Map<String, Stock> stockCache = new ConcurrentHashMap<String, Stock>();

	private final StockRepository stockRepositoryDB;
	
	@Autowired
	public StockRepositoryCache(StockRepository stockRepositoryDB) {
		this.stockRepositoryDB = stockRepositoryDB;
		init();
	}
	
	private void init() {
		updateCache(stockRepositoryDB.getStocks());
	}
	
	public void updateCache(Collection<Stock> stocks) {
		for(Stock stock : stocks){
			stockCache.put(stock.getCode(), stock);
		}
	}
	
	
	public Stock getStock(String code){
		LOGGER.debug("Inside getStock for code {}", code);
		return stockCache.get(code);
	}

	public Collection<Stock> getStocks() {
		return stockCache.values();
	}
	
	public Set<String> getStockCodes(){
		return stockCache.keySet();
	}
	
	public void insertStocks(Collection<Stock> stocks) {
		stockRepositoryDB.insertStocks(stocks);
		updateCache(stocks);
	}
}
