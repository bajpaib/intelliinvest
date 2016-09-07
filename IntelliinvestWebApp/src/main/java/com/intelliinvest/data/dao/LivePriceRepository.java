package com.intelliinvest.data.dao;

import java.util.Collection;
import java.util.List;

import com.intelliinvest.data.model.LiveStockPrice;

public interface LivePriceRepository{

	public LiveStockPrice getLiveStockPrice(String symbol);

	public Collection<LiveStockPrice> getLiveStockPrices();
	
	public void updateLiveStockPrices(List<LiveStockPrice> liveStockPrices);

}
