package com.intelliinvest.web.controllers;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.response.Status;
import com.intelliinvest.route.EODPriceRouteBuilder;

@Controller
public class EODStockPriceController {
	private static Logger LOGGER = LoggerFactory.getLogger(EODStockPriceController.class);
	
	@Autowired
	private CamelContext camelContext;

	@RequestMapping(value = "load/eod/prices", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status loadEODPrices() {
		Exchange exchange = new DefaultExchange(camelContext);
		camelContext.createProducerTemplate().send(EODPriceRouteBuilder.LOAD_EOD_PRICES, exchange);
		return Status.STATUS_SUCCESS;
	}

	@RequestMapping(value = "/backload/eod/prices", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status backloadEODPrices(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
		Exchange exchange = new DefaultExchange(camelContext);
		exchange.setProperty("startDate", startDate);
		exchange.setProperty("endDate", endDate);
		camelContext.createProducerTemplate().send(EODPriceRouteBuilder.BACKLOAD_EOD_PRICES, exchange);
		return Status.STATUS_SUCCESS;
	}

}