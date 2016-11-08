package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.dao.IndustryFundamentalsRepository;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockFundamentalAnalysisRepository;
import com.intelliinvest.data.dao.StockFundamentalsRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.forecast.StockFundamentalAnalysisForecaster;
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockFundamentalAnalysis;
import com.intelliinvest.data.model.StockFundamentals;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.IntelliinvestConverter;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.web.bo.response.FundamentalAnalysisTimeSeriesResponse;
import com.intelliinvest.web.bo.response.StockFundamentalAnalysisResponse;
import com.intelliinvest.web.bo.response.StockPriceResponse;

@Controller
public class StockFundamentalAnalysisController {
	private static Logger logger = Logger.getLogger(StockFundamentalAnalysisController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private IndustryFundamentalsRepository industryFundamentalsRepository;
	@Autowired
	private StockFundamentalsRepository stockFundamentalsRepository;
	@Autowired
	private StockFundamentalAnalysisRepository stockFundamentalAnalysisRepository;
	@Autowired
	private StockFundamentalAnalysisForecaster stockFundamentalAnalysisForecaster;
	@Autowired
	private DateUtil dateUtil;

	// Forecast fundamental analysis for a given date
	@RequestMapping(value = "/stock/forecastFundamentalAnalysis", method = RequestMethod.GET)
	public @ResponseBody String forecastFundamentalAnalysis(@RequestParam("date") String dateStr) {
		String retVal = "";
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(dateStr, dateFormat);
			stockFundamentalAnalysisForecaster.forecastFundamentalAnalysis(date);
			retVal = "Success";
		} catch (Exception e) {
			logger.error("Exception inside forecastFundamentalAnalysis() " + e.getMessage());
			retVal = e.getMessage();
		}

		return retVal;
	}

	// For a given stock, get latestFundamentalAnalysis
	@RequestMapping(value = "/stock/getLatestFundamentalAnalysis", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockFundamentalAnalysisResponse getLatestFundamentalAnalysis(@RequestParam("id") String id) {
		StockFundamentalAnalysisResponse response = new StockFundamentalAnalysisResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		StockFundamentalAnalysis stock = null;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(id)) {
			try {
				stock = stockFundamentalAnalysisRepository.getLatestStockFundamentalAnalysis(id);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getLatestStockFundamentalAnalysis() " + e.getMessage());
				error = true;
			}
		} else {
			errorMsg = "Stock Code is null or empty";
			logger.error("Exception inside getLatestStockFundamentalAnalysis() " + errorMsg);
			error = true;
		}

		if (stock == null) {
			errorMsg = "StockFundamentalAnalysis not found";
			logger.error("Inside getLatestStockFundamentalAnalysis() " + errorMsg);
			error = true;
		}
		if (stock != null && !error) {
			response = IntelliinvestConverter.getStockFundamentalAnalysisResponse(stock);
			response.setSuccess(true);
			response.setMessage("StockFundamentalAnalysis has been returned successfully.");
		} else {
			response.setSecurityId(id);
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}
		return response;
	}

	// For a given stock and date, get fundamentalAnalysisForDate
	@RequestMapping(value = "/stock/getFundamentalAnalysisForDate", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockFundamentalAnalysisResponse getFundamentalAnalysisForDate(@RequestParam("id") String id,
			@RequestParam("date") String dateStr) {
		StockFundamentalAnalysisResponse response = new StockFundamentalAnalysisResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		StockFundamentalAnalysis stock = null;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(id)) {
			try {
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate date = LocalDate.parse(dateStr, dateFormat);
				stock = stockFundamentalAnalysisRepository.getStockFundamentalAnalysisFromDB(id, date);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getFundamentalAnalysisForDate() " + e.getMessage());
				error = true;
			}
		} else {
			errorMsg = "Stock Code is null or empty";
			logger.error("Exception inside getFundamentalAnalysisForDate() " + errorMsg);
			error = true;
		}

		if (stock == null) {
			errorMsg = "StockFundamentalAnalysis not found";
			logger.error("Inside getFundamentalAnalysisForDate() " + errorMsg);
			error = true;
		}
		if (stock != null && !error) {
			response = IntelliinvestConverter.getStockFundamentalAnalysisResponse(stock);
			response.setSuccess(true);
			response.setMessage("StockFundamentalAnalysis has been returned successfully.");
		} else {
			response.setSecurityId(id);
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}
		return response;
	}

	// For a given stock and attribute, return the time series for
	// stock(yearQuarter, attributeValue)
	// and industry average(date, attributeValue).

	@RequestMapping(value = "/stock/getFundamentalAnalysisTimeSeries", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody FundamentalAnalysisTimeSeriesResponse getFundamentalAnalysisTimeSeries(
			@RequestParam("id") String id, @RequestParam("attrName") String attrName) {
		FundamentalAnalysisTimeSeriesResponse response = new FundamentalAnalysisTimeSeriesResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		StockFundamentals fundamental = null;
		List<IndustryFundamentals> industries = null;
		boolean error = false;
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (Helper.isNotNullAndNonEmpty(id) && Helper.isNotNullAndNonEmpty(attrName)) {
			try {
				Stock stock = stockRepository.getStockById(id);
				if (stock == null) {
					throw new IntelliinvestException("Stock not found for id:" + id);
				}

				if (!Helper.isNotNullAndNonEmpty(stock.getIndustry())) {
					throw new IntelliinvestException("Industry not present for security:" + id);
				}

				String attrNameDB = IntelliinvestConstants.stockFundamentalDBAttrMap.get(attrName);
				if (!Helper.isNotNullAndNonEmpty(attrNameDB)) {
					throw new IntelliinvestException(" DB attribte mapping not found for attrName:" + attrName);
				}

				fundamental = stockFundamentalsRepository.getStockFundamentalsByIdAndAttrName(id, attrNameDB);
				industries = industryFundamentalsRepository.getIndustryFundamentalsFromDB(stock.getIndustry());

				Map<LocalDate, Double> attrDateMap = new HashMap<LocalDate, Double>();
				for (IndustryFundamentals temp : industries) {
					LocalDate date = temp.getTodayDate();
					double indVal = temp.getAttributeValue(attrName);
					if (!MathUtil.isNearZero(indVal)) {
						attrDateMap.put(date, indVal);
					}
				}

				List<String> attrDateSeries = new ArrayList<String>();
				List<String> indDateSeries = new ArrayList<String>();
				List<Double> attrValSeries = new ArrayList<Double>();
				List<Double> indValSeries = new ArrayList<Double>();

				Map<String, String> yearQuarterAttrVal = fundamental.getYearQuarterAttrVal();
				TreeMap<String, String> sorted = new TreeMap<String, String>(yearQuarterAttrVal);
				Set<Entry<String, String>> mappings = sorted.entrySet();

				for (Map.Entry<String, String> entry : mappings) {

					String yearQuarter = entry.getKey();
					String year = yearQuarter.substring(0, 4);
					String quarter = yearQuarter.substring(4, yearQuarter.length());
					String date = "";
					switch (quarter) {
					case "Q1":
						date = year + "-" + "03" + "-" + "31";
						break;
					case "Q2":
						date = year + "-" + "06" + "-" + "30";
						break;
					case "Q3":
						date = year + "-" + "09" + "-" + "30";
						break;
					case "Q4":
						date = year + "-" + "12" + "-" + "31";
						break;
					}

					String attrValStr = entry.getValue();
					double attrVal = 0;

					if (!Helper.isNotNullAndNonEmpty(attrValStr)) {
						logger.error("Error " + attrName + " is null or empty:" + fundamental.getSecurityId());
						continue;
					}
					try {
						attrVal = new Double(attrValStr).doubleValue();
					} catch (Exception e) {
						logger.error("Error " + attrName + " is is not a number:" + fundamental.getSecurityId());
						continue;
					}
					attrDateSeries.add(date);
					attrValSeries.add(MathUtil.round(attrVal));

					LocalDate localDate = LocalDate.parse(date, dateFormat);
					Double indVal = null;

					for (int i = 0; i < 15; ++i) {
						indVal = attrDateMap.get(localDate);
						if (indVal != null) {
							break;
						} else {
							localDate = dateUtil.getLastBusinessDate(localDate);
						}

					}

					if (indVal == null) {
						indVal = new Double(0);
					}

					indDateSeries.add(date);
					indValSeries.add(MathUtil.round(indVal));

				}

				response.setId(id);
				response.setAttrName(attrName);
				response.setAttrValSeries(attrValSeries);
				response.setAttrDateSeries(attrDateSeries);
				response.setIndDateSeries(indDateSeries);
				response.setIndValSeries(indValSeries);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getFundamentalAnalysisTimeSeries() " + e.getMessage());
				error = true;
			}
		} else {
			errorMsg = "Stock Code or Attribute Name is null or empty";
			logger.error("Exception inside getFundamentalAnalysisTimeSeries() " + errorMsg);
			error = true;
		}

		if (fundamental == null) {
			errorMsg = "StockFundamentals not found";
			logger.error("Inside getFundamentalAnalysisTimeSeries() " + errorMsg);
			error = true;
		}
		if (fundamental != null && !error) {
			response.setSuccess(true);
			response.setMessage("StockFundamentalAnalysis has been returned successfully.");
		} else {
			response.setId(id);
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}
		return response;
	}

	@RequestMapping(value = "/stock/getTopStocksForIndustry", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockPriceResponse> getTopStocksForIndustry(@RequestParam("name") String name) {
		List<StockPriceResponse> response = new ArrayList<StockPriceResponse>();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(name)) {
			try {
				// find stocks of a particular industry
				Set<String> securityIds = stockRepository.getSecurityIdsForIndustry(name);
				if (!Helper.isNotNullAndNonEmpty(securityIds)) {
					logger.error("No stocks found for industry " + name);
					throw new IntelliinvestException("No stocks found for industry " + name);
				}

				List<StockFundamentalAnalysis> stockAnalysisList = new ArrayList<StockFundamentalAnalysis>();

				for (String id : securityIds) {
					StockFundamentalAnalysis stockAnalysis = stockFundamentalAnalysisRepository
							.getLatestStockFundamentalAnalysis(id);
					if (stockAnalysis != null) {
						stockAnalysisList.add(stockAnalysis);
					}
				}
				// sort the list by points in desc order
				stockAnalysisList.sort(new Comparator<StockFundamentalAnalysis>() {
					public int compare(StockFundamentalAnalysis item1, StockFundamentalAnalysis item2) {
						return item2.getPoints() - item1.getPoints();
					}
				});

				for (int i = 0; i < stockAnalysisList.size() && i < 10; ++i) {
					StockFundamentalAnalysis stockAnalysis = stockAnalysisList.get(i);
					Stock stock = stockRepository.getStockById(stockAnalysis.getSecurityId());
					StockPrice stockPrice = stockRepository.getStockPriceById(stockAnalysis.getSecurityId());
					QuandlStockPrice quandlStockPrice = quandlEODStockPriceRepository
							.getLatestEODStockPrice(stockAnalysis.getSecurityId());
					StockFundamentals fundamental = stockFundamentalsRepository.getStockFundamentalsByIdAndAttrName(
							stockAnalysis.getSecurityId(), IntelliinvestConstants.ANNUAL_RETURN_ON_EQUITY);
					StockPriceResponse res = new StockPriceResponse();
					res.setSecurityId(stockAnalysis.getSecurityId());
					if (stock != null) {
						res.setName(stock.getName());
					}

					res.setCurrentPrice(MathUtil.round(stockPrice != null ? stockPrice.getCurrentPrice() : 0));
					res.setEodPrice(MathUtil.round(quandlStockPrice != null ? quandlStockPrice.getClose() : 0));
					
					if (!MathUtil.isNearZero(res.getCurrentPrice()) && !MathUtil.isNearZero(res.getEodPrice())) {
						double pctChange = ((res.getCurrentPrice() - res.getEodPrice()) / res.getEodPrice()) * 100;
						res.setPctChange(MathUtil.round(pctChange));
					} else {
						res.setPctChange(0);
					}
					
					String alReturnOnEquity = "";
					if(fundamental!=null){
						Map<String, String> yearQuarterAttrVal = fundamental.getYearQuarterAttrVal();
						TreeMap<String, String> sorted = new TreeMap<String, String>(yearQuarterAttrVal);
						Map.Entry<String, String> entry = sorted.lastEntry();
						if(entry!=null){
							alReturnOnEquity = entry.getValue();
						}				
					}
					
					res.setAlReturnOnEquity(alReturnOnEquity);
					res.setSuccess(true);
					response.add(res);
				}

			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getTopStocksForIndustry() " + e.getMessage());
				error = true;
			}
		} else {
			errorMsg = "Industry is null or empty";
			logger.error("Exception inside getTopStocksForIndustry() " + errorMsg);
			error = true;
		}

		return response;
	}
}