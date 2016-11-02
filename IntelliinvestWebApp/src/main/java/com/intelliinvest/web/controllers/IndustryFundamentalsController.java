package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.IntelliinvestConverter;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.web.bo.response.FundamentalAnalysisTimeSeriesResponse;
import com.intelliinvest.web.bo.response.IndustryFundamentalsResponse;
import com.intelliinvest.web.bo.response.StockPriceResponse;

@Controller
public class IndustryFundamentalsController {
	private static Logger logger = Logger.getLogger(IndustryFundamentalsController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private IndustryFundamentalsRepository industryFundamentalsRepository;

	// For a given industry, get latest INDUSTRY_FUNDAMENTALS
	@RequestMapping(value = "/industry/getLatestIndustryFundamentals", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody IndustryFundamentalsResponse getLatestIndustryFundamentals(@RequestParam("name") String name) {
		IndustryFundamentalsResponse response = new IndustryFundamentalsResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		IndustryFundamentals industry = null;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(name)) {
			try {
				industry = industryFundamentalsRepository.getLatestIndustryFundamentals(name);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getLatestIndustryFundamentals() " + e.getMessage());
				error = true;
			}
		} else {
			errorMsg = "Industry is null or empty";
			logger.error("Exception inside getLatestIndustryFundamentals() " + errorMsg);
			error = true;
		}

		if (industry == null) {
			errorMsg = "IndustryFundamentals not found";
			logger.error("Inside getLatestIndustryFundamentals() " + errorMsg);
			error = true;
		}
		if (industry != null && !error) {
			response = IntelliinvestConverter.getIndustryFundamentalsResponse(industry);
			response.setSuccess(true);
			response.setMessage("IndustryFundamentalAnalysis has been returned successfully.");
		} else {
			response.setName(name);
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}
		return response;
	}

	// For a given industry, get IndustryFundamentalsForDate
	@RequestMapping(value = "/industry/getIndustryFundamentalsForDate", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody IndustryFundamentalsResponse getIndustryFundamentalsForDate(@RequestParam("name") String name,
			@RequestParam("date") String dateStr) {
		IndustryFundamentalsResponse response = new IndustryFundamentalsResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		IndustryFundamentals industry = null;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(name)) {
			try {
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate date = LocalDate.parse(dateStr, dateFormat);
				industry = industryFundamentalsRepository.getIndustryFundamentalsFromDB(name, date);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getIndustryFundamentalsForDate() " + e.getMessage());
				error = true;
			}
		} else {
			errorMsg = "Industry is null or empty";
			logger.error("Exception inside getIndustryFundamentalsForDate() " + errorMsg);
			error = true;
		}

		if (industry == null) {
			errorMsg = "IndustryFundamentals not found";
			logger.error("Inside getIndustryFundamentalsForDate() " + errorMsg);
			error = true;
		}
		if (industry != null && !error) {
			response = IntelliinvestConverter.getIndustryFundamentalsResponse(industry);
			response.setSuccess(true);
			response.setMessage("IndustryFundamentalAnalysis has been returned successfully.");
		} else {
			response.setName(name);
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}
		return response;
	}

	@RequestMapping(value = "/industry/getIndustryAnalysisTimeSeries", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody FundamentalAnalysisTimeSeriesResponse getIndustryAnalysisTimeSeries(
			@RequestParam("name") String name, @RequestParam("attrName") String attrName) {
		FundamentalAnalysisTimeSeriesResponse response = new FundamentalAnalysisTimeSeriesResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		List<IndustryFundamentals> industries = null;
		boolean error = false;
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (Helper.isNotNullAndNonEmpty(name) && Helper.isNotNullAndNonEmpty(attrName)) {
			try {
				String attrNameDB = IntelliinvestConstants.stockFundamentalDBAttrMap.get(attrName);
				if (!Helper.isNotNullAndNonEmpty(attrNameDB)) {
					throw new IntelliinvestException(" DB attribte mapping not found for attrName:" + attrName);
				}
				industries = industryFundamentalsRepository.getIndustryFundamentalsFromDB(name);

				List<String> indDateSeries = new ArrayList<String>();
				List<Double> indValSeries = new ArrayList<Double>();

				for (IndustryFundamentals temp : industries) {
					LocalDate date = temp.getTodayDate();
					double indVal = temp.getAttributeValue(attrName);
					indDateSeries.add(dateFormat.format(date));
					indValSeries.add(MathUtil.round(indVal));
				}
				response.setId(name);
				response.setAttrName(attrName);
				response.setIndDateSeries(indDateSeries);
				response.setIndValSeries(indValSeries);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getIndustryAnalysisTimeSeries() " + e.getMessage());
				error = true;
			}
		} else {
			errorMsg = "Industry or Attribute Name is null or empty";
			logger.error("Exception inside getIndustryAnalysisTimeSeries() " + errorMsg);
			error = true;
		}

		if (Helper.isNotNullAndNonEmpty(industries) && !error) {
			response.setSuccess(true);
			response.setMessage("IndustryAnalysisTimeSeries has been returned successfully.");
		} else {
			response.setId(name);
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}
		return response;
	}
}