package com.intelliinvest.web.controllers;

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

/*	@RequestMapping(value = "/stock/getStockFundamentalsByYearQuarterAndId", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockFundamentals> getStockFundamentalsByYearQuarterAndId(@RequestParam("id") String id,
			@RequestParam("yearQuarter") String yearQuarter) {
		List<StockFundamentals> list = null;
		if (Helper.isNotNullAndNonEmpty(id) && Helper.isNotNullAndNonEmpty(yearQuarter)) {
			try {
				list = stockFundamentalsRepository.getStockFundamentalsByYearQuarterAndId(yearQuarter, id);
			} catch (Exception e) {
				logger.error("Exception inside getStockFundamentalsByYearQuarterAndId() " + e.getMessage());
			}
		}
		return list;
	}*/

	@RequestMapping(value = "/stock/getStockFundamentalsByIdAndAttrName", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockFundamentals getStockFundamentalsByIdAndAttrName(
			@RequestParam("id") String id, @RequestParam("attrName") String attrName) {
		StockFundamentals stock = null;
		if (Helper.isNotNullAndNonEmpty(id) && Helper.isNotNullAndNonEmpty(attrName)) {
			try {
				String attrNameDB = IntelliinvestConstants.stockFundamentalDBAttrMap.get(attrName);
				if (!Helper.isNotNullAndNonEmpty(attrNameDB)) {
					throw new IntelliinvestException(" DB attribte mapping not found for attrName:" + attrName);
				}
				stock = stockFundamentalsRepository.getStockFundamentalsByIdAndAttrName(id, attrNameDB);
			} catch (Exception e) {
				logger.error("Exception inside getStockFundamentalsByYearQuarterAndIdAndAttrName() " + e.getMessage());
			}
		}
		return stock;
	}

	@RequestMapping(value = "/stock/backloadStockFundamentals", method = RequestMethod.GET)
	public @ResponseBody String backloadStockFundamentals() {
		try {
			logger.info("backload stock Fundamentals has been started.....");
			stockFundamentalsImporter.bulkUploadStockFundamentals();
			logger.info("backload stock Fundamentals has been completed successfully.....");
		} catch (Exception e) {
			logger.error("Error while backloadStockFundamentals " + e.getMessage());
			return e.getMessage();
		}
		return "Success";
	}
	
	@RequestMapping(value = "/stock/uploadStockFundamentals", method = RequestMethod.GET)
	public @ResponseBody String uploadStockFundamentals(@RequestParam("filePath") String filePath) {
		try {
			stockFundamentalsImporter.uploadStockFundamentals(filePath);
		} catch (Exception e) {
			logger.error("Error while uploadStockFundamentals " + e.getMessage());
			return e.getMessage();
		}
		return "Success";
	}
}