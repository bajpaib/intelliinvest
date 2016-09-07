package com.intelliinvest.data.dao.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.LivePriceRepository;
import com.intelliinvest.data.model.LiveStockPrice;

@Component("livePriceRepository")
class LivePriceRepositoryCache implements LivePriceRepository, Cache<LiveStockPrice>{
	private static Logger LOGGER = LoggerFactory.getLogger(LivePriceRepositoryCache.class);
	private Map<String, LiveStockPrice> livePriceCache = new ConcurrentHashMap<String, LiveStockPrice>();
	private final LivePriceRepository livePriceRepository;
	
	@Autowired
	public LivePriceRepositoryCache(@Qualifier("livePriceRepositoryDB") LivePriceRepository livePriceRepository) {
		this.livePriceRepository = livePriceRepository;
		init();
	}
	
	public void init(){
		updateCache(livePriceRepository.getLiveStockPrices());
	}

	public void updateCache(Collection<LiveStockPrice> liveStockPrices) {
		for (LiveStockPrice liveStockPrice : liveStockPrices) {
			if(null!=liveStockPrice.getCode()){
				livePriceCache.put(liveStockPrice.getCode(), liveStockPrice);
			}
		}
	}
	
	public LiveStockPrice getLiveStockPrice(String symbol){
		return livePriceCache.get(symbol);
	}

	public Collection<LiveStockPrice> getLiveStockPrices(){	
		return livePriceCache.values();
	}

	public void updateLiveStockPrices(List<LiveStockPrice> liveStockPrices) {
		LOGGER.info("Inside updateLiveStockPrices()");
		livePriceRepository.updateLiveStockPrices(liveStockPrices);
		updateCache(liveStockPrices);
	}
	
}
