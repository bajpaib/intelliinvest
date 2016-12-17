package com.intelliinvest.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockFundamentalAnalysis;
import com.intelliinvest.data.model.StockFundamentals;
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
	StockFundamentalsRepository stockFundamentalsRepository;

	private static Logger logger = Logger.getLogger(StockAnalysisRepository.class);

	public StockAnalysisResponse getStockAnalysisData(String securityId) {
		StockAnalysisResponse response = new StockAnalysisResponse();
		response.setSecurityId(securityId);
		StockSignals stockSignals = stockSignalsRepository.getStockSignalsFromCache(securityId);
		StockPrice stockPrice = stockRepository.getStockPriceById(securityId);
		Stock  stock= stockRepository.getStockById(securityId);
		QuandlStockPrice quandlStockPrice = quandlEODStockPriceRepository.getLatestEODStockPrice(securityId);

		String news = "";
		try {
			news = newsFetcherRepository.getNews(securityId, 3);
		} catch (Exception e) {
			logger.error("Exception while fetching news:::" + e.getMessage());
		}
		IndustryFundamentals industryFundamentalsResponse = industryFundamentalsRepository
				.getLatestIndustryFundamentals(stock.getIndustry());

		setFundamentalData(response, securityId);

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
		if (news != null)
			response.setNews(news);
		if (industryFundamentalsResponse != null) {
			logger.info("Industry analysis data has been found....");
			response.setQrOperatingMargin_industry_analysis(industryFundamentalsResponse.getQrOperatingMargin());
			response.setAlReturnOnEquity(industryFundamentalsResponse.getAlReturnOnEquity());

		}

		return response;
	}

	private void setFundamentalData(StockAnalysisResponse response, String securityId) {

		List<StockFundamentals> stockFundamentals = stockFundamentalsRepository.getStockFundamentalsById(securityId);

		for (StockFundamentals stockFundamental : stockFundamentals) {

			switch (stockFundamental.getAttrName()) {
			case IntelliinvestConstants.ANNUAL_MARKET_CAPITALIZATION:
				response.setMktCap(getAttValue(stockFundamental));
				break;
			case IntelliinvestConstants.ANNUAL_NET_CASHFLOW:
				response.setFreeCashFlow(getAttValue(stockFundamental));
				break;

			case IntelliinvestConstants.ANNUAL_SHAREHOLDER_EQUITY:
				response.setAnnNetWorth(getAttValue(stockFundamental));
				break;

			case IntelliinvestConstants.ANNUAL_DIVIDEND:
				response.setAnnDividendPercent(getAttValue(stockFundamental));
				break;

			case IntelliinvestConstants.ANNUAL_ENTERPRISE_VALUE:
				response.setEnterpriseValue(getAttValue(stockFundamental));
				break;

			case IntelliinvestConstants.ANNUAL_FACE_VALUE:
				response.setFaceValue(getAttValue(stockFundamental));
				break;

			case IntelliinvestConstants.ANNUAL_EARNING_PER_SHARE:
				response.setAlEPSRatio(getAttValue(stockFundamental));
				break;

			case IntelliinvestConstants.ANNUAL_OPERATING_MARGIN:
				response.setAnnOperatingMargin_fundamental_analysis(getAttValue(stockFundamental));
				break;
			}

		}

		// stockFundamentals = stockFundamentalsRepository
		// .getStockFundamentalsByIdAndAttrName(securityId,
		// IntelliinvestConstants.ANNUAL_SHAREHOLDER_EQUITY);
		// response.setAnnNetWorth(getAttValue(stockFundamentals));
		//
		// stockFundamentals = stockFundamentalsRepository
		// .getStockFundamentalsByIdAndAttrName(securityId,
		// IntelliinvestConstants.ANNUAL_DIVIDEND);
		// response.setAnnDividendPercent(getAttValue(stockFundamentals));
		//
		// stockFundamentals = stockFundamentalsRepository
		// .getStockFundamentalsByIdAndAttrName(securityId,
		// IntelliinvestConstants.ANNUAL_ENTERPRISE_VALUE);
		// response.setEnterpriseValue(getAttValue(stockFundamentals));
		//
		// stockFundamentals = stockFundamentalsRepository
		// .getStockFundamentalsByIdAndAttrName(securityId,
		// IntelliinvestConstants.ANNUAL_FACE_VALUE);
		// response.setFaceValue(getAttValue(stockFundamentals));
		//
		// stockFundamentals = stockFundamentalsRepository
		// .getStockFundamentalsByIdAndAttrName(securityId,
		// IntelliinvestConstants.ANNUAL_EARNING_PER_SHARE);
		// response.setAlEPSRatio(getAttValue(stockFundamentals));
		//
		// stockFundamentals = stockFundamentalsRepository
		// .getStockFundamentalsByIdAndAttrName(securityId,
		// IntelliinvestConstants.ANNUAL_OPERATING_MARGIN);
		// response.setAnnOperatingMargin_fundamental_analysis(getAttValue(stockFundamentals));
		//
		// stockFundamentals = stockFundamentalsRepository
		// .getStockFundamentalsByIdAndAttrName(securityId,
		// IntelliinvestConstants.ANNUAL_NET_CASHFLOW);
		// response.setFreeCashFlow(getAttValue(stockFundamentals));

	}

	private String getAttValue(StockFundamentals stockFundamentals) {
		logger.info("StockFundamentals object is: " + stockFundamentals.toString());
		Map<String, String> values = stockFundamentals.getYearQuarterAttrVal();
		String attVal = "";
		int latest_quarter = 0;
		int latest_year = 0;
		for (Map.Entry<String, String> entry : values.entrySet()) {
			if (values.size() == 1)
				attVal = entry.getValue();
			else {
				String quarterYear = entry.getKey();
				String val = entry.getValue();

				int year = Integer.parseInt(quarterYear.substring(0, 4));
				int quarter = Integer.parseInt(quarterYear.substring(5, 6));

				if (year > latest_year) {
					latest_year = year;
					attVal = val;
				} else if (year == latest_year) {
					if (quarter > latest_quarter) {
						latest_quarter = quarter;
						attVal = val;
					}
				}
			}
		}
		return attVal;
	}

}
