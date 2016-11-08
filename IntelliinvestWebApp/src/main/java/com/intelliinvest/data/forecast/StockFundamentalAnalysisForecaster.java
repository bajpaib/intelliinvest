package com.intelliinvest.data.forecast;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.dao.IndustryFundamentalsRepository;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockFundamentalAnalysisRepository;
import com.intelliinvest.data.dao.StockFundamentalsRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockFundamentalAnalysis;
import com.intelliinvest.data.model.StockFundamentals;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.util.ScheduledThreadPoolHelper;

/**
 * FundamentalAnalysisForecast
 *
 * Fetch data from STOCK_FUNDAMENTALS. Segregate the data on basis of industry.
 * For all stocks belonging to a particular industry, calculate
 * 
 * 1. Market Capitalization Ratio for a stock = (Market Capitalization for a
 * stock / Sum of Market Capitalization of all stocks)
 * 
 * 2. Calculate average EPS(%) for the industry = AVERAGE (Earning per share for
 * a stock/Close price of the stock)
 * 
 * 3. Calculate the following industry averages. Industry Cash to Debt Ratio =
 * SUM (Cash to Debt Ratio for a stock * Market Capitalization Ratio for a
 * stock) Industry Current Ratio = SUM (Current Ratio for a stock * Market
 * Capitalization Ratio for a stock) Industry Equity to Asset Ratio = SUM
 * (Equity to Asset Ratio for a stock * Market Capitalization Ratio for a stock)
 * Industry Debt to Capital Ratio = SUM (Debt to Capital Ratio for a stock *
 * Market Capitalization Ratio for a stock) Industry Levered Beta = SUM (Levered
 * Beta for a stock * Market Capitalization Ratio for a stock) Industry Return
 * on Equity = SUM (Return on Equity Ratio for a stock * Market Capitalization
 * Ratio for a stoc* Industry Industry Industry Solvency Ratio = SUM (Solvency
 * Ratio for a stock * Market Capitalization Ratio for a stock) Industry Cost of
 * Equity = SUM (Cost of Equity for a stock * Market Capitalization Ratio for a
 * stock) Industry Cost of Debt = SUM (Cost of Debt for a stock * Market
 * Capitalization Ratio for a stock) Industry EBIDTA Margin = SUM (EBIDTA Margin
 * for a stock * Market Capitalization Ratio for a stock) Industry Operating
 * Margin = SUM (Operating Margin for a stock * Market Capitalization Ratio for
 * a stock) Industry Net Margin = SUM (Net Margin for a stock * Market
 * Capitalization Ratio for a stock) Industry Dividend Percentage = SUM
 * (Dividend Percentage for a stock * Market Capitalization Ratio for a stock)
 *
 * 4. Assign rating for each stock as 1 (better than Industry Avg) or 0 (worse
 * than Industry Avg) EPS(%) Price to Earning Cash to Debt Ratio Current Ratio
 * Equity to Asset Ratio Debt to Capital Ratio Levered Beta Return on Equity
 * Solvency Ratio Cost of Equity Cost of Debt EBIDTA Margin Operating Margin Net
 * Margin Dividend Percentage
 * 
 * 5. Calculate SUM (All the ratings for a given stock calculated in Step 4) for
 * each stock
 * 
 * 6. Calculate AVERAGE (SUM for all stocks in Step 5)
 * 
 * 7. If SUM of ratings for each stock (Calculated in Step 5) > AVERAGE, then
 * stock = BUY = AVERAGE, then stock = HOLD < AVERAGE, then stock = SELL
 * 
 */

@ManagedResource(objectName = "bean:name=StockFundamentalAnalysisForecaster", description = "StockFundamentalAnalysisForecaster")
public class StockFundamentalAnalysisForecaster {
	private static Logger logger = Logger.getLogger(StockFundamentalAnalysisForecaster.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private StockFundamentalsRepository stockFundamentalsRepository;
	@Autowired
	private IndustryFundamentalsRepository industryFundamentalsRepository;
	@Autowired
	private StockFundamentalAnalysisRepository stockFundamentalAnalysisRepository;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private DateUtil dateUtil;
	private String minYearQuarter = "2005Q1";

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
		minYearQuarter = IntelliInvestStore.properties.getProperty("forecast.fundamentals.analysis.min.year.quarter");
	}

	private void initializeScheduledTasks() {
		LocalDateTime timeNow = dateUtil.getLocalDateTime();
		Runnable forecastFundamentalAnalysisTask = new Runnable() {
			public void run() {
				if (!dateUtil.isBankHoliday(dateUtil.getLocalDate())) {
					try {
						forecastFundamentalAnalysis(dateUtil.getLocalDate());
					} catch (Exception e) {
						logger.error("Error while forecasting fundamental analysis for stocks " + e.getMessage());
					}
				}
			}
		};

		int forecastFundamentalAnalysisStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("forecast.fundamental.analysis.start.hr"));
		int forecastFundamentalAnalysisMin = new Integer(
				IntelliInvestStore.properties.getProperty("forecast.fundamental.analysis.start.min"));
		LocalDateTime timeNext = timeNow.withHour(forecastFundamentalAnalysisStartHour)
				.withMinute(forecastFundamentalAnalysisMin).withSecond(0);
		if (timeNow.compareTo(timeNext) > 0) {
			timeNext = timeNext.plusDays(1);
		}
		Duration duration = Duration.between(timeNow, timeNext);
		long initialDelay = duration.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(forecastFundamentalAnalysisTask,
				initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);

		logger.info("Next ForecastFundamentalAnalysisTask scheduled at " + timeNext);
	}

	private boolean validateStockFundamentals(Map<String, Double> map1, Map<String, Double> map2) {
		boolean retVal = true;
		// The two maps should be of equal size and have the same stock
		if (map1.size() != map2.size()) {
			return false;
		}
		for (Map.Entry<String, Double> entry : map1.entrySet()) {
			String securityId = entry.getKey();
			if (!map2.keySet().contains(securityId)) {
				return false;
			}
		}
		return retVal;
	}

	private Set<String> filterStocksForZeroValues(Map<String, Double> presentStocks) {
		Set<String> retainSet = new HashSet<String>();
		for (Map.Entry<String, Double> entry : presentStocks.entrySet()) {
			String securityId = entry.getKey();
			if (!MathUtil.isNearZero(entry.getValue())) {
				retainSet.add(securityId);
			}
		}
		return retainSet;
	}

	private String findPreviousYearQuarter(String yearQuarter) {
		String retVal = yearQuarter;
		String year = yearQuarter.substring(0, 4);
		String quarter = yearQuarter.substring(4, yearQuarter.length());
		switch (quarter) {
		case "Q1":
			int yearVal = new Integer(year).intValue();
			retVal = yearVal - 1 + "Q4";
			break;
		case "Q2":
			retVal = year + "Q1";
			break;
		case "Q3":
			retVal = year + "Q2";
			break;
		case "Q4":
			retVal = year + "Q3";
			break;
		}

		return retVal;
	}

	private boolean isValidAttribute(String attrVal) {
		if (!Helper.isNotNullAndNonEmpty(attrVal)) {
			return false;
		}
		try {
			new Double(attrVal).doubleValue();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public Map<String, Double> findStockFundamentalsForYearQuarterAndIdAndAttrName(String yearQuarter, List<String> ids,
			String attrName) {
		Map<String, Double> retVal = new HashMap<String, Double>();
		Map<String, StockFundamentals> stockFundamentals = stockFundamentalsRepository
				.getStockFundamentalsByIdAndAttrName(ids, attrName);

		Set<String> missingSet = new HashSet<String>(ids);
		missingSet.removeAll(stockFundamentals.keySet());
		if (missingSet.size() > 0) {
			logger.error("Setting Attr value to 0. AttrName:" + attrName + " not found in DB for " + missingSet);
		}

		for (String id : missingSet) {
			retVal.put(id, new Double(0));
		}

		List<String> missingAttrList = new ArrayList<String>();
		for (Map.Entry<String, StockFundamentals> entry : stockFundamentals.entrySet()) {
			StockFundamentals stock = entry.getValue();
			Map<String, String> yearQuarterAttrVal = stock.getYearQuarterAttrVal();
			String prevYearQuarter = yearQuarter;
			while (true) {
				if (minYearQuarter.compareTo(prevYearQuarter) > 0) {
					missingAttrList.add(entry.getKey());
					retVal.put(entry.getKey(), new Double(0));
					break;
				}
				String attrVal = yearQuarterAttrVal.get(prevYearQuarter);
				if (isValidAttribute(attrVal)) {
					retVal.put(entry.getKey(), new Double(attrVal).doubleValue());
					break;
				} else {
					prevYearQuarter = findPreviousYearQuarter(prevYearQuarter);
				}
			}
		}
		
		if(missingAttrList.size() > 0){
			logger.error("Setting Attr value to 0. Exiting historical search for attrName:" + attrName + " and stocks "+  missingAttrList);
		}
		
		return retVal;
	}

	public Map<String, QuandlStockPrice> findClosingPrices(List<String> securityIds, LocalDate date) {
		Map<String, QuandlStockPrice> closePriceMap = quandlEODStockPriceRepository.getStockPricesFromDB(securityIds,
				date);
		Set<String> missingSet = new HashSet<String>(securityIds);
		missingSet.removeAll(closePriceMap.keySet());

		Iterator<Map.Entry<String, QuandlStockPrice>> it = closePriceMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, QuandlStockPrice> entry = it.next();
			if (entry.getValue() == null || MathUtil.isNearZero(entry.getValue().getClose())) {
				missingSet.add(entry.getKey());
				it.remove();
			}
		}

		List<String> missingCloses = new ArrayList<String>();
		
		for (String id : missingSet) {
			QuandlStockPrice price = null;
			for (int i = 0; i < 5; ++i) {
				price = quandlEODStockPriceRepository.getStockPriceFromDB(id, dateUtil.getLastBusinessDate(date));
				if (price != null && !MathUtil.isNearZero(price.getClose())) {
					closePriceMap.put(price.getSecurityId(), price);
					break;
				}
			}

			if (price == null || MathUtil.isNearZero(price.getClose())) {
				missingCloses.add(id);
				continue;
			}
		}
		if(missingCloses.size() > 0){
			logger.error("Exiting last 5 days search for closing prices. Stock filtered for missing or zero closing price:" + missingCloses);
		}
		
		return closePriceMap;
	}

	public String getQuarterFromDate(LocalDate date) {
		int month = date.getMonthValue();
		String retVal = null;
		switch (month) {
		case 1:
		case 2:
		case 3:
			retVal = "Q1";
			break;
		case 4:
		case 5:
		case 6:
			retVal = "Q2";
			break;
		case 7:
		case 8:
		case 9:
			retVal = "Q3";
			break;
		case 10:
		case 11:
		case 12:
			retVal = "Q4";
			break;
		}
		return retVal;
	}

	public void forecastFundamentalAnalysis(LocalDate date) throws Exception {
		// for each industry, get the stocks and do the analysis
		for (String industry : IntelliinvestConstants.stockIndustryList) {
			 if ("MISCELLANEOUS".equals(industry)) {
				continue;
			}
			try {
				forecastFundamentalAnalysisForIndustry(industry, date);
			} catch (Exception e) {
				logger.error(
						"Exception in forecastFundamentalAnalysisForIndustry for " + industry + " and date:" + date);
			}
		}
	}

	private void forecastFundamentalAnalysisForIndustry(String industry, LocalDate date) throws Exception {
		String quarter = getQuarterFromDate(date);
		int year = date.getYear();
		String yearQuarter = year + quarter;
		logger.info(" Starting fundamentals forcast for industry:" + industry + " and date:" + date);

		// find stocks of a particular industry
		Set<String> securityIds = stockRepository.getSecurityIdsForIndustry(industry);
		if (!Helper.isNotNullAndNonEmpty(securityIds)) {
			logger.error("No stocks found for industry " + industry);
			throw new IntelliinvestException("No stocks found for industry " + industry);
		}
		try {
			// Get stock fundamentals
			Map<String, Double> alBookValuePerShareMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(
					yearQuarter, new ArrayList<String>(securityIds),
					IntelliinvestConstants.ANNUAL_BOOK_VALUE_PER_SHARE);

			Map<String, Double> alEarningPerShareMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.ANNUAL_EARNING_PER_SHARE);

			Map<String, Double> alPriceToEarningMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.ANNUAL_PRICE_TO_EARNING);

			Map<String, Double> alCashToDebtRatioMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.ANNUAL_CASH_TO_DEBT_RATIO);

			Map<String, Double> alCurrentRatioMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.ANNUAL_CURRENT_RATIO);

			Map<String, Double> alEquityToAssetRatioMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(
					yearQuarter, new ArrayList<String>(securityIds),
					IntelliinvestConstants.ANNUAL_EQUITY_TO_ASSET_RATIO);

			Map<String, Double> alDebtToCapitalRatioMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(
					yearQuarter, new ArrayList<String>(securityIds),
					IntelliinvestConstants.ANNUAL_DEBT_TO_CAPITAL_RATIO);

			Map<String, Double> alLeveredBetaMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.ANNUAL_LEVERED_BBETA);

			Map<String, Double> alReturnOnEquityMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.ANNUAL_RETURN_ON_EQUITY);

			Map<String, Double> alSolvencyRatioMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.ANNUAL_SOLVENCY_RATIO);

			Map<String, Double> alCostOfEquityMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.ANNUAL_COST_OF_EQUITY);

			Map<String, Double> alCostOfDebtMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.ANNUAL_COST_OF_DEBT);

			Map<String, Double> qrEBIDTAMarginMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.QUARTER_EBIDTA_MARGIN);

			Map<String, Double> qrOperatingMarginMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.QUARTER_OPERATING_MARGIN);

			Map<String, Double> qrNetMarginMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.QUARTER_NET_MARGIN);

			Map<String, Double> qrDividendPercentMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.QUARTER_DIVIDEND_PERCENT);

			Map<String, Double> qrUnadjBseClsPriceMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(securityIds), IntelliinvestConstants.QUARTER_UNADJUSTED_BSE_CLOSE_PRICE);

			Map<String, Double> qrOutstandingSharesMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(
					yearQuarter, new ArrayList<String>(securityIds), IntelliinvestConstants.QUARTER_OUTSTANDING_SHARES);

			Set<String> remainingSecurityIds = filterStocksForZeroValues(qrOutstandingSharesMap);

			Map<String, QuandlStockPrice> closePriceMap = findClosingPrices(new ArrayList<String>(remainingSecurityIds),
					date);
			remainingSecurityIds = closePriceMap.keySet();

/*			logger.info("alBookValuePerShareMap" + alBookValuePerShareMap);
			logger.info("alEarningPerShareMap" + alEarningPerShareMap);
			logger.info("alPriceToEarningMap" + alPriceToEarningMap);
			logger.info("alCashToDebtRatioMap" + alCashToDebtRatioMap);
			logger.info("alCurrentRatioMap" + alCurrentRatioMap);
			logger.info("alEquityToAssetRatioMap" + alEquityToAssetRatioMap);
			logger.info("alDebtToCapitalRatioMap" + alDebtToCapitalRatioMap);
			logger.info("alLeveredBetaMap" + alLeveredBetaMap);
			logger.info("alReturnOnEquityMap" + alReturnOnEquityMap);
			logger.info("alSolvencyRatioMap" + alSolvencyRatioMap);
			logger.info("alCostOfEquityMap" + alCostOfEquityMap);
			logger.info("alCostOfDebtMap" + alCostOfDebtMap);
			logger.info("qrEBIDTAMarginMap" + qrEBIDTAMarginMap);
			logger.info("qrOperatingMarginMap" + qrOperatingMarginMap);
			logger.info("qrNetMarginMap" + qrNetMarginMap);
			logger.info("qrDividendPercentMap" + qrDividendPercentMap);
			logger.info("qrUnadjBseClsPriceMap" + qrUnadjBseClsPriceMap);*/
			
			// MarketCap will be calculated daily (Market Cap =
			// QUARTER_OUTSTANDING_SHARES * Daily Closing Price)
			// for all the stocks in qrOutstandingSharesMap, get the closing
			// price
			Map<String, Double> marketCapMap = new HashMap<String, Double>();
			for (String id : remainingSecurityIds) {
				double outstandingShares = qrOutstandingSharesMap.get(id).doubleValue();
				QuandlStockPrice price = closePriceMap.get(id);
				marketCapMap.put(id, outstandingShares * price.getClose());
			}

			// filteredSecurityIds = securityIds - remainingSecurityIds
			Set<String> filteredSecurityIds = new HashSet<String>();
			filteredSecurityIds.addAll(securityIds);
			filteredSecurityIds.removeAll(remainingSecurityIds);

			if (filteredSecurityIds.size() > 0) {
				logger.error(
						"Trying to fetch ANNUAL_MARKET_CAPITALIZATION from DB for stocks filtered for missing or zero QUARTER_OUTSTANDING_SHARES or CLOSING_PRICE for industry "
								+ industry + " are " + filteredSecurityIds.toString());
			}

			// for filteredIds try to get alMarketCapMap from DB
			Map<String, Double> alMarketCapMap = findStockFundamentalsForYearQuarterAndIdAndAttrName(yearQuarter,
					new ArrayList<String>(filteredSecurityIds), IntelliinvestConstants.ANNUAL_MARKET_CAPITALIZATION);

			Set<String> missingSecurityIds = new HashSet<String>();
			for (Map.Entry<String, Double> temp : alMarketCapMap.entrySet()) {
				if (!MathUtil.isNearZero(temp.getValue())) {
					marketCapMap.put(temp.getKey(), temp.getValue());
				} else {
					missingSecurityIds.add(temp.getKey());
				}
			}

			if (missingSecurityIds.size() > 0) {
				logger.error("Stocks filtered for missing or zero ANNUAL_MARKET_CAPITALIZATION for industry " + industry
						+ " are " + missingSecurityIds.toString());
			}

			// finalSecurityIds = securityIds - missingSecurityIds
			Set<String> finalSecurityIds = new HashSet<String>();
			finalSecurityIds.addAll(securityIds);
			finalSecurityIds.removeAll(missingSecurityIds);

			if (finalSecurityIds.size() == 0) {
				logger.error("No stocks present for carrying out fundamental analysis for industry " + industry);
				throw new IntelliinvestException(
						"No stocks present for carrying out fundamental analysis for industry " + industry);
			}

			// Now filter attribute maps for zero or null attributes

			for (String toRemove : missingSecurityIds) {
				marketCapMap.remove(toRemove);
				alBookValuePerShareMap.remove(toRemove);
				alEarningPerShareMap.remove(toRemove);
				alPriceToEarningMap.remove(toRemove);
				alCashToDebtRatioMap.remove(toRemove);
				alCurrentRatioMap.remove(toRemove);
				alEquityToAssetRatioMap.remove(toRemove);
				alDebtToCapitalRatioMap.remove(toRemove);
				alLeveredBetaMap.remove(toRemove);
				alReturnOnEquityMap.remove(toRemove);
				alSolvencyRatioMap.remove(toRemove);
				alCostOfEquityMap.remove(toRemove);
				alCostOfDebtMap.remove(toRemove);
				qrEBIDTAMarginMap.remove(toRemove);
				qrOperatingMarginMap.remove(toRemove);
				qrNetMarginMap.remove(toRemove);
				qrDividendPercentMap.remove(toRemove);
				qrUnadjBseClsPriceMap.remove(toRemove);
			}

			// All the maps should be of equal size and contain the same
			// pair of securityIds and yearQuarter
			if (!validateStockFundamentals(marketCapMap, alBookValuePerShareMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_BOOK_VALUE_PER_SHARE and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_BOOK_VALUE_PER_SHARE and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alEarningPerShareMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_EARNING_PER_SHARE and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_EARNING_PER_SHARE and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alPriceToEarningMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_PRICE_TO_EARNING and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_PRICE_TO_EARNING and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alCashToDebtRatioMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_CASH_TO_DEBT_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_CASH_TO_DEBT_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alCurrentRatioMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_CURRENT_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_CURRENT_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alEquityToAssetRatioMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_EQUITY_TO_ASSET_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_EQUITY_TO_ASSET_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alDebtToCapitalRatioMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_DEBT_TO_CAPITAL_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_DEBT_TO_CAPITAL_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alLeveredBetaMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_LEVERED_BBETA and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_LEVERED_BBETA and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alReturnOnEquityMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_RETURN_ON_EQUITY and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_RETURN_ON_EQUITY and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alSolvencyRatioMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_SOLVENCY_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_SOLVENCY_RATIO and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alCostOfEquityMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_COST_OF_EQUITY and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_COST_OF_EQUITY and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, alCostOfDebtMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_COST_OF_DEBT and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes ANNUAL_COST_OF_DEBT and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, qrEBIDTAMarginMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_EBIDTA_MARGIN and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_EBIDTA_MARGIN and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, qrOperatingMarginMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_OPERATING_MARGIN and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_OPERATING_MARGIN and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, qrNetMarginMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_NET_MARGIN and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_NET_MARGIN and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, qrDividendPercentMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_DIVIDEND_PERCENT and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_DIVIDEND_PERCENT and ANNUAL_MARKET_CAPITALIZATION ");
			}

			if (!validateStockFundamentals(marketCapMap, qrUnadjBseClsPriceMap)) {
				logger.error("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_UNADJUSTED_BSE_CLOSE_PRICE and ANNUAL_MARKET_CAPITALIZATION ");
				throw new IntelliinvestException("Stocks not matching for industry " + industry
						+ " and attributes QUARTER_UNADJUSTED_BSE_CLOSE_PRICE and ANNUAL_MARKET_CAPITALIZATION ");
			}

			double totAlMarketCap = 0;
			double avgAlBookValuePerShare = 0;
			double avgAlEarningPerShare = 0;
			double avgAlEPSPct = 0;
			double avgAlPriceToEarning = 0;
			double avgAlCashToDebtRatio = 0;
			double avgAlCurrentRatio = 0;
			double avgAlEquityToAssetRatio = 0;
			double avgAlDebtToCapitalRatio = 0;
			double avgAlLeveredBeta = 0;
			double avgAlReturnOnEquity = 0;
			double avgAlSolvencyRatio = 0;
			double avgAlCostOfEquity = 0;
			double avgAlCostOfDebt = 0;
			double avgQrEBIDTAMargin = 0;
			double avgQrOperatingMargin = 0;
			double avgQrNetMargin = 0;
			double avgQrDividendPercent = 0;
			LocalDateTime updateDate = dateUtil.getLocalDateTime();

			List<StockFundamentals> alEPSPctList = new ArrayList<StockFundamentals>();
			Map<String, Double> alEPSPctMap = new HashMap<String, Double>();

			for (Double temp : marketCapMap.values()) {
				totAlMarketCap += temp.doubleValue();
			}

			// calculate alMarketCapRatio for each stock
			Map<String, Double> alMarketCapRatioMap = new HashMap<String, Double>();
			for (Map.Entry<String, Double> temp : marketCapMap.entrySet()) {
				alMarketCapRatioMap.put(temp.getKey(), temp.getValue().doubleValue() / totAlMarketCap);
			}

			// calculate avgAlBookValuePerShare
			for (Map.Entry<String, Double> fundamental : alBookValuePerShareMap.entrySet()) {
				double alBookValuePerShare = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlBookValuePerShare += alBookValuePerShare * alMarketCapRatio;
				}
			}

			// calculate avgAlEarningPerShare

			for (Map.Entry<String, Double> fundamental : alEarningPerShareMap.entrySet()) {
				double alEarningPerShare = fundamental.getValue();
				double qrUnadjBseClsPrice = qrUnadjBseClsPriceMap.get(fundamental.getKey());

				double alEPSPct = 0;
				if (!MathUtil.isNearZero(qrUnadjBseClsPrice)) {
					alEPSPct = alEarningPerShare / qrUnadjBseClsPrice;
				}
				alEPSPctMap.put(fundamental.getKey(), alEPSPct);
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlEarningPerShare += alEarningPerShare * alMarketCapRatio;
					avgAlEPSPct += alEPSPct * alMarketCapRatio;
				}

				// we need to add ANNUAL_EARNING_PER_SHARE_PCT (alEPSPct)
				// attribute to STOCK_FUNDAEMNTALS table
				StockFundamentals toAdd = new StockFundamentals();
				toAdd.setSecurityId(fundamental.getKey());
				toAdd.setAttrName(IntelliinvestConstants.ANNUAL_EARNING_PER_SHARE_PCT);
				toAdd.setUpdateDate(updateDate);
				toAdd.addYearQuarterAttrVal(yearQuarter, new Double(alEPSPct).toString());
				alEPSPctList.add(toAdd);
			}

			// calculate avgAlPriceToEarning
			for (Map.Entry<String, Double> fundamental : alPriceToEarningMap.entrySet()) {
				double alPriceToEarning = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlPriceToEarning += alPriceToEarning * alMarketCapRatio;
				}
			}

			// calculate avgAlCashToDebtRatio
			for (Map.Entry<String, Double> fundamental : alCashToDebtRatioMap.entrySet()) {
				double alCashToDebtRatio = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlCashToDebtRatio += alCashToDebtRatio * alMarketCapRatio;
				}
			}

			// calculate avgAlCurrentRatio
			for (Map.Entry<String, Double> fundamental : alCurrentRatioMap.entrySet()) {
				double alCurrentRatio = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlCurrentRatio += alCurrentRatio * alMarketCapRatio;
				}
			}

			// calculate avgAlEquityToAssetRatio
			for (Map.Entry<String, Double> fundamental : alEquityToAssetRatioMap.entrySet()) {
				double alEquityToAssetRatio = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlEquityToAssetRatio += alEquityToAssetRatio * alMarketCapRatio;
				}
			}

			// calculate avgAlDebtToCapitalRatio
			for (Map.Entry<String, Double> fundamental : alDebtToCapitalRatioMap.entrySet()) {
				double alDebtToCapitalRatio = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlDebtToCapitalRatio += alDebtToCapitalRatio * alMarketCapRatio;
				}
			}

			// calculate avgAlLeveredBeta
			for (Map.Entry<String, Double> fundamental : alLeveredBetaMap.entrySet()) {
				double alLeveredBeta = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlLeveredBeta += alLeveredBeta * alMarketCapRatio;
				}
			}

			// calculate avgAlReturnOnEquity
			for (Map.Entry<String, Double> fundamental : alReturnOnEquityMap.entrySet()) {
				double alReturnOnEquity = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlReturnOnEquity += alReturnOnEquity * alMarketCapRatio;
				}
			}

			// calculate avgAlSolvencyRatio
			for (Map.Entry<String, Double> fundamental : alSolvencyRatioMap.entrySet()) {
				double alSolvencyRatio = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlSolvencyRatio += alSolvencyRatio * alMarketCapRatio;
				}
			}

			// calculate avgAlCostOfEquity
			for (Map.Entry<String, Double> fundamental : alCostOfEquityMap.entrySet()) {
				double alCostOfEquity = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlCostOfEquity += alCostOfEquity * alMarketCapRatio;
				}
			}

			// calculate avgAlCostOfDebt
			for (Map.Entry<String, Double> fundamental : alCostOfDebtMap.entrySet()) {
				double alCostOfDebt = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgAlCostOfDebt += alCostOfDebt * alMarketCapRatio;
				}
			}

			// calculate avgQrEBIDTAMargin
			for (Map.Entry<String, Double> fundamental : qrEBIDTAMarginMap.entrySet()) {
				double qrEBIDTAMargin = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgQrEBIDTAMargin += qrEBIDTAMargin * alMarketCapRatio;
				}
			}

			// calculate avgQrOperatingMargin
			for (Map.Entry<String, Double> fundamental : qrOperatingMarginMap.entrySet()) {
				double qrOperatingMargin = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgQrOperatingMargin += qrOperatingMargin * alMarketCapRatio;
				}
			}

			// calculate avgQrNetMargin
			for (Map.Entry<String, Double> fundamental : qrNetMarginMap.entrySet()) {
				double qrNetMargin = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgQrNetMargin += qrNetMargin * alMarketCapRatio;
				}
			}

			// calculate avgQrDividendPercent
			for (Map.Entry<String, Double> fundamental : qrDividendPercentMap.entrySet()) {
				double qrDividendPercent = fundamental.getValue();
				Double alMarketCapRatio = alMarketCapRatioMap.get(fundamental.getKey());
				if (alMarketCapRatio != null) {
					avgQrDividendPercent += qrDividendPercent * alMarketCapRatio;
				}
			}

			// set industry fundamentals
			IndustryFundamentals industryFundamentals = new IndustryFundamentals(industry, yearQuarter, totAlMarketCap,
					avgAlBookValuePerShare, avgAlEarningPerShare, avgAlEPSPct, avgAlPriceToEarning,
					avgAlCashToDebtRatio, avgAlCurrentRatio, avgAlEquityToAssetRatio, avgAlDebtToCapitalRatio,
					avgAlLeveredBeta, avgAlReturnOnEquity, avgAlSolvencyRatio, avgAlCostOfEquity, avgAlCostOfDebt,
					avgQrEBIDTAMargin, avgQrOperatingMargin, avgQrNetMargin, avgQrDividendPercent, date, updateDate);

			List<StockFundamentalAnalysis> stockForecasts = new ArrayList<StockFundamentalAnalysis>();
			int totalPoints = 0;

			for (String id : finalSecurityIds) {
				StockFundamentalAnalysis stockForecast = new StockFundamentalAnalysis();
				stockForecast.setSecurityId(id);
				stockForecast.setYearQuarter(yearQuarter);
				stockForecast.setTodayDate(date);
				stockForecast.setUpdateDate(updateDate);
				int points = 0;

				double alEPSPctTemp = alEPSPctMap.get(id);
				double alCashToDebtRatioTemp = alCashToDebtRatioMap.get(id);
				double alCurrentRatioTemp = alCurrentRatioMap.get(id);
				double alEquityToAssetRatioTemp = alEquityToAssetRatioMap.get(id);
				double alDebtToCapitalRatioTemp = alDebtToCapitalRatioMap.get(id);
				double alLeveredBetaTemp = alLeveredBetaMap.get(id);
				double alReturnOnEquityTemp = alReturnOnEquityMap.get(id);
				double alSolvencyRatioTemp = alSolvencyRatioMap.get(id);
				double alCostOfEquityTemp = alCostOfEquityMap.get(id);
				double alCostOfDebtTemp = alCostOfDebtMap.get(id);
				double qrEBIDTAMarginTemp = qrEBIDTAMarginMap.get(id);
				double qrOperatingMarginTemp = qrOperatingMarginMap.get(id);
				double qrNetMarginTemp = qrNetMarginMap.get(id);
				double qrDividendPercentTemp = qrDividendPercentMap.get(id);

				if (alEPSPctTemp > industryFundamentals.getAlEPSPct()) {
					stockForecast.setAlEPSPct(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alEPSPctTemp == industryFundamentals.getAlEPSPct()) {
					stockForecast.setAlEPSPct(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlEPSPct(IntelliinvestConstants.SELL_ID);
				}

				if (alCashToDebtRatioTemp > industryFundamentals.getAlCashToDebtRatio()) {
					stockForecast.setAlCashToDebtRatio(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alCashToDebtRatioTemp == industryFundamentals.getAlCashToDebtRatio()) {
					stockForecast.setAlCashToDebtRatio(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlCashToDebtRatio(IntelliinvestConstants.SELL_ID);
				}

				if (alCurrentRatioTemp > industryFundamentals.getAlCurrentRatio()) {
					stockForecast.setAlCurrentRatio(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alCurrentRatioTemp == industryFundamentals.getAlCurrentRatio()) {
					stockForecast.setAlCurrentRatio(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlCurrentRatio(IntelliinvestConstants.SELL_ID);
				}

				if (alEquityToAssetRatioTemp > industryFundamentals.getAlEquityToAssetRatio()) {
					stockForecast.setAlEquityToAssetRatio(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alEquityToAssetRatioTemp == industryFundamentals.getAlEquityToAssetRatio()) {
					stockForecast.setAlEquityToAssetRatio(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlEquityToAssetRatio(IntelliinvestConstants.SELL_ID);
				}

				if (alDebtToCapitalRatioTemp < industryFundamentals.getAlDebtToCapitalRatio()) {
					stockForecast.setAlDebtToCapitalRatio(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alDebtToCapitalRatioTemp == industryFundamentals.getAlDebtToCapitalRatio()) {
					stockForecast.setAlDebtToCapitalRatio(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlDebtToCapitalRatio(IntelliinvestConstants.SELL_ID);
				}

				if (alLeveredBetaTemp < industryFundamentals.getAlLeveredBeta()) {
					stockForecast.setAlLeveredBeta(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alLeveredBetaTemp == industryFundamentals.getAlLeveredBeta()) {
					stockForecast.setAlLeveredBeta(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlLeveredBeta(IntelliinvestConstants.SELL_ID);
				}

				if (alReturnOnEquityTemp > industryFundamentals.getAlReturnOnEquity()) {
					stockForecast.setAlReturnOnEquity(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alReturnOnEquityTemp == industryFundamentals.getAlReturnOnEquity()) {
					stockForecast.setAlReturnOnEquity(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlReturnOnEquity(IntelliinvestConstants.SELL_ID);
				}

				if (alSolvencyRatioTemp > industryFundamentals.getAlSolvencyRatio()) {
					stockForecast.setAlSolvencyRatio(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alSolvencyRatioTemp == industryFundamentals.getAlSolvencyRatio()) {
					stockForecast.setAlSolvencyRatio(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlSolvencyRatio(IntelliinvestConstants.SELL_ID);
				}

				if (alCostOfEquityTemp < industryFundamentals.getAlCostOfEquity()) {
					stockForecast.setAlCostOfEquity(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alCostOfEquityTemp == industryFundamentals.getAlCostOfEquity()) {
					stockForecast.setAlCostOfEquity(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlCostOfEquity(IntelliinvestConstants.SELL_ID);
				}

				if (alCostOfDebtTemp < industryFundamentals.getAlCostOfDebt()) {
					stockForecast.setAlCostOfDebt(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (alCostOfDebtTemp == industryFundamentals.getAlCostOfDebt()) {
					stockForecast.setAlCostOfDebt(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setAlCostOfDebt(IntelliinvestConstants.SELL_ID);
				}

				if (qrEBIDTAMarginTemp > industryFundamentals.getQrEBIDTAMargin()) {
					stockForecast.setQrEBIDTAMargin(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (qrEBIDTAMarginTemp == industryFundamentals.getQrEBIDTAMargin()) {
					stockForecast.setQrEBIDTAMargin(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setQrEBIDTAMargin(IntelliinvestConstants.SELL_ID);
				}

				if (qrOperatingMarginTemp > industryFundamentals.getQrOperatingMargin()) {
					stockForecast.setQrOperatingMargin(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (qrOperatingMarginTemp == industryFundamentals.getQrOperatingMargin()) {
					stockForecast.setQrOperatingMargin(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setQrOperatingMargin(IntelliinvestConstants.SELL_ID);
				}

				if (qrNetMarginTemp > industryFundamentals.getQrNetMargin()) {
					stockForecast.setQrNetMargin(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (qrNetMarginTemp == industryFundamentals.getQrNetMargin()) {
					stockForecast.setQrNetMargin(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setQrNetMargin(IntelliinvestConstants.SELL_ID);
				}

				if (qrDividendPercentTemp > industryFundamentals.getQrDividendPercent()) {
					stockForecast.setQrDividendPercent(IntelliinvestConstants.BUY_ID);
					++points;
				} else if (qrDividendPercentTemp == industryFundamentals.getQrDividendPercent()) {
					stockForecast.setQrDividendPercent(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setQrDividendPercent(IntelliinvestConstants.SELL_ID);
				}

				totalPoints += points;
				stockForecast.setPoints(points);
				stockForecasts.add(stockForecast);
			}

			// calculate overall stock forecast

			double avgPoints = totalPoints / stockForecasts.size();
			for (StockFundamentalAnalysis stockForecast : stockForecasts) {
				if (stockForecast.getPoints() > avgPoints) {
					stockForecast.setSummary(IntelliinvestConstants.BUY_ID);
				} else if (stockForecast.getPoints() == avgPoints) {
					stockForecast.setSummary(IntelliinvestConstants.HOLD_ID);
				} else {
					stockForecast.setSummary(IntelliinvestConstants.SELL_ID);
				}
			}
			// update stock fundamentals for alEPSPct
			stockFundamentalsRepository.updateStockFundamentals(alEPSPctList);
			// update INDUSTRY_FUNDAMENTALS
			industryFundamentalsRepository.updateIndustryFundamentals(industryFundamentals);
			// update STOCK_FUNDAMENTALS_FORECAST
			stockFundamentalAnalysisRepository.updateStockFundamentalAnalysis(stockForecasts);
		} catch (Exception e) {
			logger.error("Exception in forecastFundamentalAnalysis for date=" + date + " and industry=" + industry
					+ " Exception:" + e.getMessage());
			throw e;
		}
	}

}
