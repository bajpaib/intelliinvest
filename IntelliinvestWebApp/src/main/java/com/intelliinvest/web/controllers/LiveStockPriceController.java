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
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.response.Status;
import com.intelliinvest.route.LivePriceRouteBuilder;

@Controller
public class LiveStockPriceController {
	private static Logger LOGGER = LoggerFactory.getLogger(LiveStockPriceController.class);

	@Autowired
	CamelContext camelContext;
	
	@RequestMapping(value = "/load/current/prices", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status loadCurrentPrice() {
		Exchange exchange = new DefaultExchange(camelContext);
		camelContext.createProducerTemplate().send(LivePriceRouteBuilder.LOAD_LIVE_PRICES, exchange);
		return Status.STATUS_SUCCESS;
	}
}