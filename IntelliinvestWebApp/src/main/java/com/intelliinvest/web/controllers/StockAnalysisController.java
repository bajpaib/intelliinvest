package com.intelliinvest.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.dao.StockAnalysisRepository;
import com.intelliinvest.web.bo.response.StockAnalysisResponse;
import com.intelliinvest.web.bo.response.StockSignalsResponse;

@Controller
public class StockAnalysisController {

	private static Logger logger = Logger.getLogger(StockAnalysisController.class);

	private static final String APPLICATION_JSON = "application/json";

	@Autowired
	StockAnalysisRepository stockAnalysisRepository;

	@RequestMapping(value = "/stockAnalysis/getBySecurityId", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockAnalysisResponse getStockAnalysisData(@RequestParam("securityId") String securityId) {
		return stockAnalysisRepository.getStockAnalysisData(securityId);
	}

}
