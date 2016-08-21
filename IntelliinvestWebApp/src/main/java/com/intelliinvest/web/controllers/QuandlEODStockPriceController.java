package com.intelliinvest.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.importer.QuandlEODStockPriceImporter;

@Controller
public class QuandlEODStockPriceController {
	private static Logger logger = Logger.getLogger(QuandlEODStockPriceController.class);
	@Autowired
	private QuandlEODStockPriceImporter quandlEODStockPriceImporter;

	@RequestMapping(value = "/stock/uploadLatestEODPricesFromNSE", method = RequestMethod.GET)
	public @ResponseBody String uploadLatestEODPricesFromNSE() {
		return quandlEODStockPriceImporter.uploadLatestEODPricesFromNSE();
	}

	@RequestMapping(value = "/stock/backloadEODPricesFromNSE", method = RequestMethod.GET)
	public @ResponseBody String backloadEODPricesFromNSE(@RequestParam("startDate") String startDate,
			@RequestParam("endDate") String endDate) {
		return quandlEODStockPriceImporter.backloadEODPricesFromNSE(startDate, endDate);
	}

}