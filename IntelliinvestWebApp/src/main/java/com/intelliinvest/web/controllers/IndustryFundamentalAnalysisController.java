package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.dao.IndustryFundamentalAnalysisRepository;
import com.intelliinvest.data.dao.IndustryFundamentalsRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.IndustryFundamentalAnalysis;
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockFundamentals;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.web.bo.response.FundamentalAnalysisTimeSeriesResponse;
import com.intelliinvest.web.bo.response.IndustriesAnalysisResponse;

@Controller
public class IndustryFundamentalAnalysisController {

	private static Logger logger = Logger.getLogger(IndustryFundamentalAnalysisController.class);

	private static final String APPLICATION_JSON = "application/json";

	@Autowired
	IndustryFundamentalAnalysisRepository industryFundamentalAnalysisRepository;

	@RequestMapping(value = "/stock/backLoadIndustryFundamentalAnalysis", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody boolean backloadIndutryFundamentalAnalysis() {
		return industryFundamentalAnalysisRepository.refreshAllIndustriesFundamentalAnalysis();
	}
	
	@RequestMapping(value = "/stock/getAllIndustryFundamentalAnalysis", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody IndustriesAnalysisResponse getAllIndutryFundamentalAnalysis() {
		return industryFundamentalAnalysisRepository.getAllIndutryFundamentalAnalysis();
	}

	@RequestMapping(value = "/stock/getIndustryFundamentalAnalysisById", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody IndustryFundamentalAnalysis getIndutryFundamentalAnalysisByID(
			@RequestParam("securityId") String securityId) {

		return industryFundamentalAnalysisRepository.getIndutryFundamentalAnalysisFromSecurityId(securityId);
	}

	@RequestMapping(value = "/stock/getIndustryFundamentalAnalysisByName", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody IndustryFundamentalAnalysis getIndutryFundamentalAnalysisByName(
			@RequestParam("name") String name) {
		return industryFundamentalAnalysisRepository.getLatestIndustryFundamentalAnalysis(name);
	}

}
