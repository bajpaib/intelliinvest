package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.intelliinvest.data.dao.StockFundamentalAnalysisRepository;
import com.intelliinvest.data.dao.StockFundamentalsRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.forecast.StockFundamentalAnalysisForecaster;
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockFundamentalAnalysis;
import com.intelliinvest.data.model.StockFundamentals;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.IntelliinvestConverter;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.web.bo.response.FundamentalAnalysisTimeSeriesResponse;
import com.intelliinvest.web.bo.response.StockFundamentalAnalysisResponse;

@Controller
public class StockFundamentalAnalysisController {
	private static Logger logger = Logger.getLogger(StockFundamentalAnalysisController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private IndustryFundamentalsRepository industryFundamentalsRepository;
	@Autowired
	private StockFundamentalsRepository stockFundamentalsRepository;
	@Autowired
	private StockFundamentalAnalysisRepository stockFundamentalAnalysisRepository;
	@Autowired
	private StockFundamentalAnalysisForecaster stockFundamentalAnalysisForecaster;

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
	public @ResponseBody StockFundamentalAnalysisResponse getFundamentalAnalysisForDate(@RequestParam("id") String id, @RequestParam("date") String dateStr) {
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

				List<String> attrDateSeries = new ArrayList<String>();
				List<String> indDateSeries = new ArrayList<String>();
				List<Double> attrValSeries = new ArrayList<Double>();
				List<Double> indValSeries = new ArrayList<Double>();
				
				Map<String, String> yearQuarterAttrVal = fundamental.getYearQuarterAttrVal();				
				TreeMap<String, String> sorted = new TreeMap<String, String>(yearQuarterAttrVal);
			    Set<Entry<String, String>> mappings = sorted.entrySet();

				for (Map.Entry<String, String> entry: mappings) {
					
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
						logger.error("Error "+attrName+" is null or empty:" + fundamental.getSecurityId());
						continue;
					}
					try {
						attrVal = new Double(attrValStr).doubleValue();
					} catch (Exception e) {						
						logger.error("Error "+attrName+" is is not a number:" + fundamental.getSecurityId());
						continue;
					}					
					attrDateSeries.add(date);
					attrValSeries.add(MathUtil.round(attrVal));
				}
				
				for (IndustryFundamentals temp : industries) {					
					LocalDate date = temp.getTodayDate();
					double indVal = temp.getAttributeValue(attrName);					
					indDateSeries.add(dateFormat.format(date));
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

		if (fundamental==null) {
			errorMsg = "StockFundamentals not found";
			logger.error("Inside getFundamentalAnalysisTimeSeries() " + errorMsg);
			error = true;
		}
		if (fundamental!=null && !error) {
			response.setSuccess(true);
			response.setMessage("StockFundamentalAnalysis has been returned successfully.");
		} else {
			response.setId(id);
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}
		return response;
	}
}