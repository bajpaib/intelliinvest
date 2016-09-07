package com.intelliinvest.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.util.DateUtil;
@Component
public class StockService {
		private static Logger LOGGER = LoggerFactory.getLogger(StockService.class);
		
		private final StockRepository stockRepository;
		
		@Autowired
		public StockService(StockRepository stockRepository) {
			this.stockRepository = stockRepository;
		}
		
		public Stock getStock(String code){
			LOGGER.debug("Inside getStock for code {}", code);
			return stockRepository.getStock(code);
		}

		public Collection<Stock> getStocks() {
			return stockRepository.getStocks();
		}
		
		public Set<String> getStockCodes(){
			return stockRepository.getStockCodes();
		}

		public void loadStocks(List<String> stocks) {
			LOGGER.debug("Inside bulkInsertStocks()...");
			List<Stock> stocksList = new ArrayList<Stock>(); 
			for (String temp : stocks) {
				String[] stockValues = temp.split(",");
				String code = stockValues[0];
				String name = stockValues[1];
				boolean worldStock = Boolean.parseBoolean(stockValues[2]);
				boolean niftyStock = Boolean.parseBoolean(stockValues[2]);
				Stock stock = new Stock(code, name, worldStock, niftyStock, DateUtil.getLocalDateTime());
				stocksList.add(stock);
			}
			insertStocks(stocksList);
		}

		public void insertStocks(Collection<Stock> stocks) {
			stockRepository.insertStocks(stocks);
		}

	}
