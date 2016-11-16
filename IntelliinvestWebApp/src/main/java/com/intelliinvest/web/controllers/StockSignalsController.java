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

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.StockSignalsRepository;
import com.intelliinvest.data.model.BubbleData;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.data.signals.StockSignalsGenerator;
import com.intelliinvest.web.bo.response.StatusResponse;
import com.intelliinvest.web.bo.response.StockPnlData;
import com.intelliinvest.web.bo.response.StockSignalsArchiveResponse;
import com.intelliinvest.web.bo.response.StockSignalsResponse;

@Controller
public class StockSignalsController {

	private static Logger logger = Logger.getLogger(StockSignalsController.class);

	private static final String APPLICATION_JSON = "application/json";

	@Autowired
	StockSignalsRepository stockSignalsRepository;

	@Autowired
	StockSignalsGenerator stockSignalsGenerator;

	private static Integer MOVING_AVERAGE = new Integer(IntelliInvestStore.properties.get("ma").toString());;

	@RequestMapping(value = "/stockSignals/getBySecurityId", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignalsResponse getStockSignals(@RequestParam("securityId") String securityId) {
		return stockSignalsRepository.getStockSignalsBySecurityId(securityId);
	}

	@RequestMapping(value = "/stockSignals/getArchive", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignalsArchiveResponse getStockSignalsArchive(
			@RequestParam("securityId") String securityId,
			@RequestParam(value = "timePeriod", required = false, defaultValue = "1") int timePeriod) {
		return stockSignalsRepository.getStockSignalsArchive(MOVING_AVERAGE, securityId, timePeriod);
	}

	@RequestMapping(value = "/stockSignals/getPnlData", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockPnlData getStockPnlData(@RequestParam("securityId") String securityId,
			@RequestParam(value = "timePeriod", required = false, defaultValue = "2") int timePeriod) {
		return stockSignalsRepository.getStockPnlData(MOVING_AVERAGE, securityId, timePeriod);
	}

	@RequestMapping(value = "/stockSignals/getADXSignalDetails", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignalsArchiveResponse getADXStockSignalsDetails(
			@RequestParam("securityId") String securityId,
			@RequestParam(value = "timePeriod", required = false, defaultValue = "2") int timePeriod) {
		return stockSignalsRepository.getStockSignalsDetails(MOVING_AVERAGE, securityId, timePeriod,
				"adxSignalPresent");
	}

	@RequestMapping(value = "/stockSignals/getBolSignalDetails", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignalsArchiveResponse getBolStockSignalsDetails(
			@RequestParam("securityId") String securityId,
			@RequestParam(value = "timePeriod", required = false, defaultValue = "2") int timePeriod) {
		return stockSignalsRepository.getStockSignalsDetails(MOVING_AVERAGE, securityId, timePeriod,
				"signalPresentBollinger");
	}

	@RequestMapping(value = "/stockSignals/getOscSignalDetails", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignalsArchiveResponse getOscStockSignalsDetails(
			@RequestParam("securityId") String securityId,
			@RequestParam(value = "timePeriod", required = false, defaultValue = "2") int timePeriod) {
		return stockSignalsRepository.getStockSignalsDetails(MOVING_AVERAGE, securityId, timePeriod,
				"signalPresentOscillator");
	}

	@RequestMapping(value = "/stockSignals/getMovAvgSignalDetails", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignalsArchiveResponse getMovAvgStockSignalsDetails(
			@RequestParam("securityId") String securityId,
			@RequestParam(value = "timePeriod", required = false, defaultValue = "2") int timePeriod) {
		return stockSignalsRepository.getStockSignalsDetails(MOVING_AVERAGE, securityId, timePeriod,
				"movingAverageSignal_Main_present");
	}

	@RequestMapping(value = "/stockSignals/getMovAvgLongTermSignalDetails", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignalsArchiveResponse getMovAvgLongTermStockSignalsDetails(
			@RequestParam("securityId") String securityId,
			@RequestParam(value = "timePeriod", required = false, defaultValue = "2") int timePeriod) {
		return stockSignalsRepository.getStockSignalsDetails(MOVING_AVERAGE, securityId, timePeriod,
				"movingAverageSignal_LongTerm_present");
	}

	
	@RequestMapping(value = "/stockSignals/getBySecurityIdAndDate", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockSignals getStockSignalsBysecurityIdAndDate(@RequestParam("securityId") String securityId,
			@RequestParam("signalDate") String signalDate) {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate date = LocalDate.parse(signalDate, dateFormat);
		return stockSignalsRepository.getStockSignals(securityId, date);
	}

	@RequestMapping(value = "/stockSignals/getAllLatestSignals", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockSignals> getAllLatestStockSignals() {
		return stockSignalsRepository.getLatestStockSignalFromDB();
	}

	@RequestMapping(value = "/stockSignals/getTechnicalAnalysisData", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockSignals> getTechnicalAnalysisData() {
		return stockSignalsRepository.getTechnicalAnalysisData();
	}

	@RequestMapping(value = "/stockSignals/generateSignals", method = RequestMethod.GET)
	public @ResponseBody StatusResponse generateSignals(@RequestParam("securityId") String securityId) {
		List<StockSignalsDTO> stockSignalsDTOs = stockSignalsGenerator.generateSignals(MOVING_AVERAGE, securityId);
		if (!stockSignalsDTOs.isEmpty())
			return new StatusResponse(StatusResponse.SUCCESS,
					"Signals for " + securityId + " Stock has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED, "Signals for " + securityId
					+ " Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}

	@RequestMapping(value = "/stockSignals/generateAllSignals", method = RequestMethod.GET)
	public @ResponseBody StatusResponse generateSignals() {
		Boolean success = stockSignalsGenerator.generateSignals(MOVING_AVERAGE);
		if (success)
			return new StatusResponse(StatusResponse.SUCCESS, "Signals for all Stocks has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED,
					"Signals for all Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}

	@RequestMapping(value = "/stockSignals/generateTodaySignals", method = RequestMethod.GET)
	public @ResponseBody StatusResponse generateSignalsToday(@RequestParam("securityId") String securityId) {
		StockSignalsDTO stockSignalsDTO = stockSignalsGenerator.generateTodaysSignal(MOVING_AVERAGE, securityId);
		if (null != stockSignalsDTO)
			return new StatusResponse(StatusResponse.SUCCESS,
					"Today Signals for " + securityId + " Stock has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED, "Today Signals for " + securityId
					+ " Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}

	@RequestMapping(value = "/stockSignals/generateAllTodaySignals", method = RequestMethod.GET)
	public @ResponseBody StatusResponse generateSignalsToday() {
		List<StockSignalsDTO> stockSignalsDTO = stockSignalsGenerator.generateTodaysSignal(MOVING_AVERAGE);
		if (!stockSignalsDTO.isEmpty())
			return new StatusResponse(StatusResponse.SUCCESS,
					"Today Signals for all Stock has been generated successfully");
		else
			return new StatusResponse(StatusResponse.FAILED,
					"Today Signals for all Stock has not been generated successfully, some internal error or invalid data. Please Check.");

	}
}
