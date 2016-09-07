package com.intelliinvest.web.controllers;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.service.StockPriceService;

@Controller
public class StockPriceController {

	private static Logger LOGGER = LoggerFactory.getLogger(StockPriceController.class);
	@Autowired
	private StockPriceService stockPriceService;

	@RequestMapping(value = "/stockPrice/{code}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody StockPrice getStockPriceByCode(@PathVariable("code") String code) {
		return stockPriceService.getStockPrice(code);
	}

	@RequestMapping(value = "/stockPrices", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<StockPrice> getStockPrices() {
		return stockPriceService.getStockPrices();
	}

}