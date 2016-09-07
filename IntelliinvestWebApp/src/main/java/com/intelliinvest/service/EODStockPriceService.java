package com.intelliinvest.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.EODHistoryPriceRepository;
import com.intelliinvest.data.dao.EODPriceRepository;
import com.intelliinvest.data.model.EODStockPrice;

@Component
public class EODStockPriceService {
	private static Logger LOGGER = LoggerFactory.getLogger(EODStockPriceService.class);
	
	private final EODPriceRepository eodPriceRepository;
	private final EODHistoryPriceRepository eodHistoryPriceRepository;
	
	@Autowired
	public EODStockPriceService(EODPriceRepository eodPriceRepository, EODHistoryPriceRepository eodHistoryPriceRepository) {
		this.eodPriceRepository = eodPriceRepository;
		this.eodHistoryPriceRepository = eodHistoryPriceRepository;
	}
	
	public EODStockPrice getEODStockPrice(String symbol){
		return getEODStockPrice(EODStockPrice.DEFAULT_EXCHANGE, symbol);
	}

	public EODStockPrice getEODStockPrice(String exchange, String symbol){
		return eodPriceRepository.getEODStockPrice(exchange, symbol);
	}
	
	public Collection<EODStockPrice> getEODStockPrices(){
		return eodPriceRepository.getEODStockPrices(EODStockPrice.DEFAULT_EXCHANGE);
	}
	
	public Collection<EODStockPrice> getEODStockPrices(String exchange){
		return eodPriceRepository.getEODStockPrices(exchange);
	}
	
	public void updateEODStockPrices(List<EODStockPrice> eodStockPrices){
		eodPriceRepository.updateEODStockPrices(eodStockPrices);
	}
	
	public void updateEODPricesInDatabase(List<EODStockPrice> eodStockPrices){
		if(null==eodPriceRepository.getUnderlyingEODRepository()){
			eodPriceRepository.updateEODStockPrices(eodStockPrices);
		}else{
			eodPriceRepository.getUnderlyingEODRepository().updateEODStockPrices(eodStockPrices);
		}
	}
	
	public List<EODStockPrice> getHistoryStockPrices(LocalDate eodDate) {
		return getHistoryStockPrices(EODStockPrice.DEFAULT_EXCHANGE, eodDate);
	}

	public List<EODStockPrice> getHistoryStockPrices(String exchange, LocalDate eodDate){
		return eodHistoryPriceRepository.getHistoryStockPrices(exchange, eodDate);
	}
	
	public List<EODStockPrice> getHistoryStockPrices(LocalDate startDate, LocalDate endDate){
		return getHistoryStockPrices(EODStockPrice.DEFAULT_EXCHANGE, startDate, endDate);
	}
	
	public List<EODStockPrice> getHistoryStockPrices(String exchange, LocalDate startDate, LocalDate endDate){
		return eodHistoryPriceRepository.getHistoryStockPrices(exchange, startDate, endDate);
	}
	
	public EODStockPrice getHistoryStockPricesForCode(String symbol, LocalDate date){
		return eodHistoryPriceRepository.getHistoryStockPricesForCode(EODStockPrice.DEFAULT_EXCHANGE, symbol, date);
	}
	
	public List<EODStockPrice> getHistoryStockPricesForCode(String symbol, LocalDate startDate, LocalDate endDate){
		return getHistoryStockPricesForCode(EODStockPrice.DEFAULT_EXCHANGE, symbol, startDate, endDate);
	}
	
	public List<EODStockPrice> getHistoryStockPricesForCode(String exchange, String symbol, LocalDate startDate, LocalDate endDate){
		return eodHistoryPriceRepository.getHistoryStockPricesForCode(exchange, symbol, startDate, endDate);
	}
	
	public Map<String, EODStockPrice> getAsMap(List<EODStockPrice> eodStockPrices){
		Map<String, EODStockPrice> eodStockPriceMap= new ConcurrentHashMap<String, EODStockPrice>();
		for(EODStockPrice eodStockPrice : eodStockPrices){
			eodStockPriceMap.put(eodStockPrice.getSymbol(), eodStockPrice);
		}
		return eodStockPriceMap;
	}
	
}
