package com.intelliinvest.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.importer.GoogleLiveStockPriceImporter;

@Controller
public class GoogleLiveStockPriceController {
	private static Logger logger = Logger.getLogger(GoogleLiveStockPriceController.class);
	@Autowired
	private GoogleLiveStockPriceImporter googleLiveStockPriceImporter;

	@RequestMapping(value = "/stock/backLoadLivePrices", method = RequestMethod.GET)
	public @ResponseBody String backLoadLivePrices() {
		return googleLiveStockPriceImporter.backLoadLivePrices();
	}
}