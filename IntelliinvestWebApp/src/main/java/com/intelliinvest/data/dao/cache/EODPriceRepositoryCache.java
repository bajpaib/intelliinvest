package com.intelliinvest.data.dao.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.EODPriceRepository;
import com.intelliinvest.data.model.EODStockPrice;

@Component("eodPriceRepository")
class EODPriceRepositoryCache implements EODPriceRepository, Cache<EODStockPrice>{
	private static Logger LOGGER = LoggerFactory.getLogger(EODPriceRepositoryCache.class);

	private Map<String, Map<String, EODStockPrice>> eodPriceCache = new ConcurrentHashMap<String, Map<String,EODStockPrice>>();

	private final EODPriceRepository eodPriceRepository;
	
	@Autowired
	public EODPriceRepositoryCache(@Qualifier("eodPriceRepositoryDB") EODPriceRepository eodPriceRepository) {
		this.eodPriceRepository = eodPriceRepository;
		init();
	}
	
	public EODPriceRepository getUnderlyingEODRepository() {
		return eodPriceRepository;
	}
	
	public void init() {
		updateCache(eodPriceRepository.getEODStockPrices(EODStockPrice.DEFAULT_EXCHANGE));
	}
	
	public void updateCache(Collection<EODStockPrice> eodStockPrices){
		Map<String, EODStockPrice> eodStockPriceMap= new ConcurrentHashMap<String, EODStockPrice>();
		for(EODStockPrice eodStockPrice : eodStockPrices){
			eodStockPriceMap.put(eodStockPrice.getSymbol(), eodStockPrice);
		}
		if(eodPriceCache.containsKey(EODStockPrice.DEFAULT_EXCHANGE)){
			eodPriceCache.get(EODStockPrice.DEFAULT_EXCHANGE).clear();
			eodPriceCache.get(EODStockPrice.DEFAULT_EXCHANGE).putAll(eodStockPriceMap);
		}else{
			eodPriceCache.put(EODStockPrice.DEFAULT_EXCHANGE, eodStockPriceMap);
		}
	}
	
	public EODStockPrice getEODStockPrice(String exchange, String symbol) {
		return eodPriceCache.get(exchange).get(symbol);
	}
	
	public Collection<EODStockPrice> getEODStockPrices(String exchange) {
		return eodPriceCache.get(exchange).values();
	}
	
	public void updateEODStockPrices(Collection<EODStockPrice> eodStockPrices) {
		eodPriceRepository.updateEODStockPrices(eodStockPrices);
		updateCache(eodStockPrices);
	}

}
