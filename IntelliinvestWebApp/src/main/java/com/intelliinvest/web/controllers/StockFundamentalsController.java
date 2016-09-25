package com.intelliinvest.web.controllers;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.dao.StockFundamentalsRepository;
import com.intelliinvest.data.importer.StockFundamentalsImporter;
import com.intelliinvest.data.model.StockFundamentals;
import com.intelliinvest.util.Helper;

@Controller
public class StockFundamentalsController {

	private static Logger logger = Logger.getLogger(StockFundamentalsController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private StockFundamentalsRepository stockFundamentalsRepository;
	@Autowired
	private StockFundamentalsImporter stockFundamentalsImporter;

	@RequestMapping(value = "/stock/getStockFundamentalsById", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockFundamentals> getStockFundamentalsById(@RequestParam("id") String id) {
		List<StockFundamentals> list = null;
		if (Helper.isNotNullAndNonEmpty(id)) {
			try {
				list = stockFundamentalsRepository.getStockFundamentalsById(id);
			} catch (Exception e) {
				logger.error("Exception inside getStockFundamentalsById() " + e.getMessage());
			}
		}
		return list;
	}

	@RequestMapping(value = "/stock/getStockFundamentalsByIdAndQuarterYear", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockFundamentals getStockFundamentalsByIdAndQuarterYear(@RequestParam("id") String id,
			@RequestParam("quarterYear") String quarterYear) {
		StockFundamentals stock = null;
		if (Helper.isNotNullAndNonEmpty(id)) {
			try {
				stock = stockFundamentalsRepository.getStockFundamentalsByIdAndQuarterYear(id, quarterYear);
			} catch (Exception e) {
				logger.error("Exception inside getStockFundamentalsByIdAndQuarterYear() " + e.getMessage());
			}
		}
		return stock;
	}

	@RequestMapping(value = "/stock/backloadStockFundamentals", method = RequestMethod.GET)
	public @ResponseBody String backloadStockFundamentals() {
		try {
			stockFundamentalsImporter.bulkUploadStockFundamentals();
		} catch (Exception e) {
			logger.error("Error while backloadStockFundamentals " + e.getMessage());
			return e.getMessage();
		}
		return "Success";
	}
}