package com.intelliinvest.data.dao;

import java.util.Collection;

import com.intelliinvest.data.model.EODStockPrice;

public interface EODPriceRepository {

	EODStockPrice getEODStockPrice(String exchange, String symbol);
	
	Collection<EODStockPrice> getEODStockPrices(String exchange);
	
	void updateEODStockPrices(Collection<EODStockPrice> eodStockPrices);
	
	EODPriceRepository getUnderlyingEODRepository();

}
