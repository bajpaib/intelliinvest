package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.StockSignalsRepository;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.data.signals.StockSignalsGenerator;
import com.intelliinvest.web.bo.response.StatusResponse;

@Controller
public class StockSignalsController {

	private static Logger logger = Logger.getLogger(StockSignalsController.class);

	private static final String APPLICATION_JSON = "application/json";

	@Autowired
	StockSignalsRepository stockSignalsRepository;

	@Autowired
	StockSignalsGenerator stockSignalsGenerator;
	
	private static Integer MOVING_AVERAGE = new Integer(IntelliInvestStore.properties.get("ma").toString());;

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
	
	@RequestMapping(value = "/stockSignals/getTechnicalAnalysisData", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody
	List<StockSignals> getTechnicalAnalysisData() {
		return stockSignalsRepository.getTechnicalAnalysisData();
	}

	@RequestMapping(value = "/stockSignals/generateSignals/{stockCode}", method = RequestMethod.POST)
	public @ResponseBody StatusResponse generateSignals(@PathVariable("stockCode") String stockCode) {
		List<StockSignalsDTO> stockSignalsDTOs = stockSignalsGenerator.generateSignals(MOVING_AVERAGE, stockCode);
		if (!stockSignalsDTOs.isEmpty())
			return new StatusResponse(StatusResponse.SUCCESS, "Signals for " + stockCode + " Stock has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED, "Signals for " + stockCode
					+ " Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}
	
	@RequestMapping(value = "/stockSignals/generateSignals", method = RequestMethod.POST)
	public @ResponseBody StatusResponse generateSignals() {
		Boolean success = stockSignalsGenerator.generateSignals(MOVING_AVERAGE);
		if (success)
			return new StatusResponse(StatusResponse.SUCCESS, "Signals for all Stocks has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED, "Signals for all Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}

	@RequestMapping(value = "/stockSignals/generateTodaySignals/{stockCode}", method = RequestMethod.POST)
	public @ResponseBody StatusResponse generateSignalsToday(@PathVariable("stockCode") String stockCode) {
		StockSignalsDTO stockSignalsDTO = stockSignalsGenerator.generateTodaysSignal(MOVING_AVERAGE, stockCode);
		if (null!=stockSignalsDTO)
			return new StatusResponse(StatusResponse.SUCCESS,
					"Today Signals for " + stockCode + " Stock has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED, "Today Signals for " + stockCode
					+ " Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}
	
	@RequestMapping(value = "/stockSignals/generateTodaySignals", method = RequestMethod.POST)
	public @ResponseBody StatusResponse generateSignalsToday() {
		List<StockSignalsDTO> stockSignalsDTO = stockSignalsGenerator.generateTodaysSignal(MOVING_AVERAGE);
		if (!stockSignalsDTO.isEmpty())
			return new StatusResponse(StatusResponse.SUCCESS, "Today Signals for all Stock has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED, "Today Signals for all Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}
}
