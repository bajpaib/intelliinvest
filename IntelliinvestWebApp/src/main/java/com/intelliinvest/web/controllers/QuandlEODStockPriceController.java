package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.importer.QuandlEODStockPriceImporter;
import com.intelliinvest.data.model.QuandlStockPrice;

@Controller
public class QuandlEODStockPriceController {
	private static Logger logger = Logger.getLogger(QuandlEODStockPriceController.class);
	@Autowired
	private QuandlEODStockPriceImporter quandlEODStockPriceImporter;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	
	@RequestMapping(value = "/quandl/backloadLatestEODPrices", method = RequestMethod.GET)
	public @ResponseBody String backloadLatestEODPrices() {
		return quandlEODStockPriceImporter.backloadLatestEODPrices();
	}

	@RequestMapping(value = "/quandl/backloadEODPricesForStock", method = RequestMethod.GET)
	public @ResponseBody String backloadEODPricesForStock(@RequestParam("id") String id,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
		return quandlEODStockPriceImporter.backloadEODPricesForStock(id, startDate, endDate);
	}

	@RequestMapping(value = "/quandl/backloadEODPricesForDateRange", method = RequestMethod.GET)
	public @ResponseBody String backloadEODPricesFromNSE(@RequestParam("startDate") String startDate,
			@RequestParam("endDate") String endDate) {
		return quandlEODStockPriceImporter.backloadEODPricesForDateRange(startDate, endDate);
	}

	@RequestMapping(value = "/quandl/getEODStockPriceForDate", method = RequestMethod.GET)
	public @ResponseBody QuandlStockPrice getEODStockPriceForDate(@RequestParam("id") String id,
			@RequestParam("eodDate") String eodDate) {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate date = LocalDate.parse(eodDate, dateFormat);
		return quandlEODStockPriceRepository.getStockPriceFromDB(id, date);
	}

}