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

import com.intelliinvest.data.model.Stock;
import com.intelliinvest.service.StockService;

@Controller
public class StockController {

	private static Logger LOGGER = LoggerFactory.getLogger(StockController.class);

	private final StockService stockService;

	@Autowired
	public StockController(StockService stockService) {
		this.stockService = stockService;
	}
	
	@RequestMapping(value = "/stock/{code}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Stock getStock(@PathVariable("code") String code) {
		return stockService.getStock(code);
	}

	@RequestMapping(value = "/stocks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<Stock> getStocks() {
		 return stockService.getStocks();
	}

}