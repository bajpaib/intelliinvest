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

	@RequestMapping(value = "/stock/backloadLatestEODPricesFromNSE", method = RequestMethod.GET)
	public @ResponseBody String backloadLatestEODPricesFromNSE() {
		return quandlEODStockPriceImporter.backloadEODPricesFromNSE();
	}

	@RequestMapping(value = "/stock/backloadEODPricesFromNSEForStock", method = RequestMethod.GET)
	public @ResponseBody String backloadEODPricesFromNSEForStock(@RequestParam("stockCode") String stockCode,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
		return quandlEODStockPriceImporter.backloadEODPricesFromNSEForStock(stockCode, startDate, endDate);
	}

	@RequestMapping(value = "/stock/backloadEODPricesFromNSE", method = RequestMethod.GET)
	public @ResponseBody String backloadEODPricesFromNSE(@RequestParam("startDate") String startDate,
			@RequestParam("endDate") String endDate) {
		return quandlEODStockPriceImporter.backloadEODPricesFromNSE(startDate, endDate);
	}

	@RequestMapping(value = "/stock/getEODStockPrice", method = RequestMethod.GET)
	public @ResponseBody QuandlStockPrice getStockPricesFromDB(@RequestParam("stockCode") String stockCode,
			@RequestParam("eodDate") String eodDate) {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate date = LocalDate.parse(eodDate, dateFormat);
		return quandlEODStockPriceRepository.getStockPriceFromDB(stockCode, date);
	}

}