package com.intelliinvest.service;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.LivePriceRepository;
import com.intelliinvest.data.dao.cache.Cache;
import com.intelliinvest.data.model.LiveStockPrice;

@Component
public class LiveStockPriceService {
	private final LivePriceRepository livePriceRepository;
	
	@Autowired
	public LiveStockPriceService(LivePriceRepository livePriceRepository) {
		this.livePriceRepository = livePriceRepository;
	}
	
	public LiveStockPrice getLiveStockPrice(String symbol){
		return livePriceRepository.getLiveStockPrice(symbol);
	}

	public Collection<LiveStockPrice> getLiveStockPrices(){	
		return livePriceRepository.getLiveStockPrices();
	}

	public void updateLiveStockPrices(List<LiveStockPrice> liveStockPrices) {
		livePriceRepository.updateLiveStockPrices(liveStockPrices);
	}
	
	@SuppressWarnings("unchecked")
	public void updateLiveStockCahceOnly(List<LiveStockPrice> liveStockPrices) {
		if(livePriceRepository instanceof Cache){
			((Cache)livePriceRepository).updateCache(liveStockPrices);
		}
	}
	

}
