package com.intelliinvest.data.dao;

import java.util.Collection;

import com.intelliinvest.data.model.EODStockPrice;
import com.intelliinvest.data.model.LiveStockPrice;
import com.intelliinvest.data.model.StockPrice;

public interface StockPriceRepository {

	StockPrice getStockPrice(String code);

	Collection<StockPrice> getStockPrices();

	void updateStockPrices(Collection<EODStockPrice> eodStockPrices, Collection<LiveStockPrice> liveStockPrices);

	void updateEODStockPrices(Collection<EODStockPrice> eodStockPrices);

	void updateLiveStockPrices(Collection<LiveStockPrice> liveStockPrices);

}