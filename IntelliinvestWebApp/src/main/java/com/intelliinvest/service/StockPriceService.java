package com.intelliinvest.service;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.StockPriceRepository;
import com.intelliinvest.data.model.EODStockPrice;
import com.intelliinvest.data.model.LiveStockPrice;
import com.intelliinvest.data.model.StockPrice;
@Component
public class StockPriceService {
		private static Logger LOGGER = LoggerFactory.getLogger(StockPriceService.class);
		
		private final StockPriceRepository stockPriceRepository;
		
		@Autowired
		public StockPriceService(StockPriceRepository stockPriceRepository) {
			this.stockPriceRepository = stockPriceRepository;
		}
		
		public StockPrice getStockPrice(String code){
			return stockPriceRepository.getStockPrice(code);
		}

		public Collection<StockPrice> getStockPrices(){
			return stockPriceRepository.getStockPrices();
		}

		public void updateStockPrices(Collection<EODStockPrice> eodStockPrices, Collection<LiveStockPrice> liveStockPrices){
			stockPriceRepository.updateStockPrices(eodStockPrices, liveStockPrices);
		}

		public void updateEODStockPrices(Collection<EODStockPrice> eodStockPrices){
			stockPriceRepository.updateEODStockPrices(eodStockPrices);
		}

		public void updateLiveStockPrices(Collection<LiveStockPrice> liveStockPrices){
			stockPriceRepository.updateLiveStockPrices(liveStockPrices);
		}

	}
