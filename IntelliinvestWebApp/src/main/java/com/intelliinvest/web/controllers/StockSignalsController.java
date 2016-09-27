package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.dao.StockSignalsRepository;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.data.signals.StockSignalsImporter;
import com.intelliinvest.web.bo.response.StatusResponse;

@Controller
public class StockSignalsController {

	private static Logger logger = Logger.getLogger(StockSignalsController.class);

	private static final String APPLICATION_JSON = "application/json";

	@Autowired
	StockSignalsRepository stockSignalsRepository;

	@Autowired
	StockSignalsImporter stockSignalsImporter;

	@RequestMapping(value = "/stockSignals/getByStockCode", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignals getStockSignals(@RequestParam("stockCode") String stockCode) {
		return stockSignalsRepository.getStockSignalsFromCache(stockCode);
	}

	@RequestMapping(value = "/stockSignals/getArchive", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockSignals> getStockSignalsArchive(@RequestParam("stockCode") String stockCode) {
		return stockSignalsRepository.getStockSignals(stockCode);
	}

	@RequestMapping(value = "/stockSignals/getByStockCodeAndDate", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignals getStockSignalsByStockCodeAndDate(@RequestParam("stockCode") String stockCode,
			@RequestParam("signalDate") String signalDate) {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate date = LocalDate.parse(signalDate, dateFormat);
		return stockSignalsRepository.getStockSignals(stockCode, date);
	}

	@RequestMapping(value = "/stockSignals/getAllLatestSignals", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockSignals> getAllLatestStockSignals() {
		return stockSignalsRepository.getLatestStockSignalFromDB();
	}

	@RequestMapping(value = "/stockSignals/generateSignals", method = RequestMethod.GET)
	public @ResponseBody StatusResponse generateSignals(@RequestParam("stockCode") String stockCode) {
		boolean b = stockSignalsImporter.generateSignals(stockCode);
		if (b)
			return new StatusResponse(StatusResponse.SUCCESS, "Signals for " + stockCode + " Stock has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED, "Signals for " + stockCode
					+ " Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}

	@RequestMapping(value = "/stockSignals/generateTodaySignals", method = RequestMethod.GET)
	public @ResponseBody StatusResponse generateSignalsToday(@RequestParam("stockCode") String stockCode) {
		boolean b = stockSignalsImporter.generateTodaySignals(stockCode);
		if (b)
			return new StatusResponse(StatusResponse.SUCCESS,
					"Today Signals for " + stockCode + " Stock has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED, "Today Signals for " + stockCode
					+ " Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}
}
