package com.intelliinvest.data.dao;

import java.util.Collection;
import java.util.Set;

import com.intelliinvest.data.model.Stock;

public interface StockRepository {

	Stock getStock(String code);

	Collection<Stock> getStocks();

	Set<String> getStockCodes();

	void insertStocks(Collection<Stock> stocks);

}