package com.intelliinvest.data.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockFundamentalAnalysis;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.web.bo.response.IndustryFundamentalsResponse;
import com.intelliinvest.web.bo.response.StockAnalysisResponse;

public class StockAnalysisRepository {

	@Autowired
	StockSignalsRepository stockSignalsRepository;

	@Autowired
	QuandlEODStockPriceRepository quandlEODStockPriceRepository;

	@Autowired
	StockRepository stockRepository;

	@Autowired
	IndustryFundamentalsRepository industryFundamentalsRepository;

	@Autowired
	NewsFetcherRepository newsFetcherRepository;

	@Autowired
	StockFundamentalAnalysisRepository stockFundamentalAnalysisRepository;

	public StockAnalysisResponse getStockAnalysisData(String securityId) {
		StockAnalysisResponse response = new StockAnalysisResponse();
		response.setSecurityId(securityId);
		StockSignals stockSignals = stockSignalsRepository.getStockSignalsFromCache(securityId);
		StockPrice stockPrice = stockRepository.getStockPriceById(securityId);
		QuandlStockPrice quandlStockPrice = quandlEODStockPriceRepository.getLatestEODStockPrice(securityId);
		String news = newsFetcherRepository.getNews(securityId, 3);
		IndustryFundamentals industryFundamentalsResponse = industryFundamentalsRepository
				.getLatestIndustryFundamentals(securityId);
		StockFundamentalAnalysis stockFundamentalAnalysis = stockFundamentalAnalysisRepository
				.getLatestStockFundamentalAnalysis(securityId);

		if (stockPrice != null) {
			response.setCurrentPrice(stockPrice.getCurrentPrice());
			response.setCurrentPriceExchange(stockPrice.getExchange());
			response.setCurrentPriceUpdateDate(stockPrice.getUpdateDate());
			response.setCp(stockPrice.getCp());

		}
		if (stockSignals != null) {
			response.setAggSignal(stockSignals.getAggSignal());
			response.setAdxSignal(stockSignals.getAdxSignal());
			response.setOscillatorSignal(stockSignals.getOscillatorSignal());
			response.setBollingerSignal(stockSignals.getBollingerSignal());
			response.setMovingAverageSignal_Main(stockSignals.getMovingAverageSignal_Main());
			response.setMovingAverageSignal_LongTerm(stockSignals.getMovingAverageSignal_LongTerm());
			response.setSignalDate(stockSignals.getSignalDate());

		}
		if (quandlStockPrice != null) {
			response.setEodDate(quandlStockPrice.getEodDate());
			response.setEodPrice(quandlStockPrice.getClose());
			response.setEodPriceExchange(quandlStockPrice.getExchange());
			response.setEodPriceUpdateDate(quandlStockPrice.getUpdateDate());
		}
		response.setNews(news);
		if (industryFundamentalsResponse != null) {
			response.setQrOperatingMargin_industry_analysis(industryFundamentalsResponse.getQrOperatingMargin());
			response.setAlReturnOnEquity(industryFundamentalsResponse.getAlReturnOnEquity());
		}
		if(stockFundamentalAnalysis!=null){
			response.setAlEPSRatio(stockFundamentalAnalysis.getAlEPSPct());//TODO change to ratio. 
		}
		
		return response;
	}

}
