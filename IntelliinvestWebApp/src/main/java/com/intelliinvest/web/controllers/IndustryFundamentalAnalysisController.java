package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.dao.IndustryFundamentalsRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockFundamentals;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.web.bo.response.FundamentalAnalysisTimeSeriesResponse;
import com.intelliinvest.web.bo.response.IndustryFundamentalAnalysisResponse;

@Controller
public class IndustryFundamentalAnalysisController {

	@Autowired
	StockRepository stockRepository;

	@Autowired
	IndustryFundamentalsRepository industryFundamentalsRepository;

	@Autowired
	DateUtil dateUtil;
	private static Logger logger = Logger.getLogger(IndustryFundamentalAnalysisController.class);

	private static final String APPLICATION_JSON = "application/json";

	@RequestMapping(value = "/stock/getIndustryAnalysis", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody IndustryFundamentalAnalysisResponse getFundamentalAnalysisTimeSeries(
			@RequestParam("securityId") String securityId) {
		IndustryFundamentalAnalysisResponse response = new IndustryFundamentalAnalysisResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		List<IndustryFundamentals> industries = null;
		boolean error = false;
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (Helper.isNotNullAndNonEmpty(securityId)) {
			try {
				Stock stock = stockRepository.getStockById(securityId);
				if (stock == null) {
					throw new IntelliinvestException("Stock not found for id:" + securityId);
				}
				String indutsry_name = stock.getIndustry();
				if (!Helper.isNotNullAndNonEmpty(indutsry_name)) {
					throw new IntelliinvestException("Industry not present for security:" + securityId);
				}

				IndustryFundamentals industryFundamentals_t = industryFundamentalsRepository
						.getLatestIndustryFundamentals(indutsry_name);
				IndustryFundamentals industryFundamentals_t_1 = null;
				IndustryFundamentals industryFundamentals_t_2 = null;
				String yearQuarter = industryFundamentals_t.getYearQuarter();
				int year = Integer.parseInt(yearQuarter.substring(0, 4));
				String quarter = yearQuarter.substring(4, yearQuarter.length());
				String date = "";
				String date_1 = "";
				String date_2 = "";
				String date_3 = "";
				switch (quarter) {
				case "Q1":
					date = year + "-" + "03" + "-" + "31";
					date_1 = (year - 1) + "-" + "03" + "-" + "31";
					date_2 = (year - 2) + "-" + "03" + "-" + "31";
					date_3 = (year - 3) + "-" + "03" + "-" + "31";
					break;
				case "Q2":
					date = year + "-" + "06" + "-" + "30";
					date_1 = (year - 1) + "-" + "06" + "-" + "30";
					date_2 = (year - 2) + "-" + "06" + "-" + "30";
					date_3 = (year - 3) + "-" + "06" + "-" + "30";
					break;
				case "Q3":
					date = year + "-" + "09" + "-" + "30";
					date_1 = (year - 1) + "-" + "09" + "-" + "30";
					date_2 = (year - 2) + "-" + "09" + "-" + "30";
					date_3 = (year - 3) + "-" + "09" + "-" + "30";
					break;
				case "Q4":
					date = year + "-" + "12" + "-" + "31";
					date_1 = (year - 1) + "-" + "12" + "-" + "31";
					date_2 = (year - 2) + "-" + "12" + "-" + "31";
					date_3 = (year - 3) + "-" + "12" + "-" + "31";
					break;
				}

				LocalDate localDate_1 = LocalDate.parse(date_1, dateFormat);
				LocalDate localDate_2 = LocalDate.parse(date_2, dateFormat);
				LocalDate localDate_3 = LocalDate.parse(date_3, dateFormat);
				industries = industryFundamentalsRepository.getIndustryFundamentalsFromDBAfterDate(indutsry_name,
						localDate_3);
				logger.info("industries size:"+industries.size());
				logger.info("T-1:"+localDate_1);
				logger.info("T-2:"+localDate_2);
				for (int i = industries.size() - 1; i >= 0; i--) {
					IndustryFundamentals tempIndustryFundamentals = industries.get(i);

					if (tempIndustryFundamentals.getTodayDate().equals(localDate_1)) {
						industryFundamentals_t_1 = tempIndustryFundamentals;
					} else if (tempIndustryFundamentals.getTodayDate().equals(localDate_2)) {
						industryFundamentals_t_2 = tempIndustryFundamentals;
						break;
					} else if (tempIndustryFundamentals.getTodayDate().compareTo(localDate_1)< 0
							&& industryFundamentals_t_1 == null) {
						industryFundamentals_t_1 = tempIndustryFundamentals;
					} else if (tempIndustryFundamentals.getTodayDate().compareTo(localDate_2) < 0
							&& industryFundamentals_t_2 == null) {
						industryFundamentals_t_2 = tempIndustryFundamentals;
						break;
					}
				}

				logger.info("Current Industry Analysis Data:\n" + industryFundamentals_t.toString());
				logger.info("T-1 Industry Analysis Data:\n" + industryFundamentals_t_1.toString());
				logger.info("T-2 Industry Analysis Data\n:" + industryFundamentals_t_2.toString());

				response.setName(indutsry_name);
				response.setRoe(industryFundamentals_t.getAlReturnOnEquity());
				response.setAlCashToDebtRatio(industryFundamentals_t.getAlCashToDebtRatio());
				response.setAlEPSPct(industryFundamentals_t.getAlEPSPct());
				response.setAlLeveredBeta(industryFundamentals_t.getAlLeveredBeta());
				response.setQrOperatingMargin(industryFundamentals_t.getQrOperatingMargin());

				setSignalsData(response, industryFundamentals_t, industryFundamentals_t_1, industryFundamentals_t_2);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getFundamentalAnalysisTimeSeries() " + e.getMessage());
				error = true;
			}
		} else {
			errorMsg = "Stock Code is null or empty";
			logger.error("Exception inside getIndustryFundamentalAnlysis data " + errorMsg);
			error = true;
		}

		if (response != null && !error) {
			response.setSuccess(true);
			response.setMessage("Data has been returned successfully.");
		} else {
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}
		return response;
	}

	private void setSignalsData(IndustryFundamentalAnalysisResponse response,
			IndustryFundamentals industryFundamentals_t, IndustryFundamentals industryFundamentals_t_1,
			IndustryFundamentals industryFundamentals_t_2) {

		final Map<Integer, Integer> signalsCounter = new HashMap<Integer, Integer>() {
			{
				put(1, 0);
				put(0, 0);
				put(-1, 0);
			}
		};

		response.setRoe_signal(getSignalData(industryFundamentals_t.getAlReturnOnEquity(),
				industryFundamentals_t_1.getAlReturnOnEquity(), industryFundamentals_t_2.getAlReturnOnEquity(),
				signalsCounter));

		response.setAlEPSPct_signal(getSignalData(industryFundamentals_t.getAlEPSPct(),
				industryFundamentals_t_1.getAlEPSPct(), industryFundamentals_t_2.getAlEPSPct(), signalsCounter));

		response.setAlCashToDebtRatio_signal(getSignalData(industryFundamentals_t.getAlCashToDebtRatio(),
				industryFundamentals_t_1.getAlCashToDebtRatio(), industryFundamentals_t_2.getAlCashToDebtRatio(),
				signalsCounter));

		response.setAlLeveredBeta_signal(
				getSignalData(industryFundamentals_t.getAlLeveredBeta(), industryFundamentals_t_1.getAlLeveredBeta(),
						industryFundamentals_t_2.getAlLeveredBeta(), signalsCounter));

		response.setQrOperatingMargin_signal(getSignalData(industryFundamentals_t.getQrOperatingMargin(),
				industryFundamentals_t_1.getQrOperatingMargin(), industryFundamentals_t_2.getQrOperatingMargin(),
				signalsCounter));

		logger.info(signalsCounter.toString());
		if (signalsCounter.get(1) >= 3)
			response.setAggSignal(1);
		else if (signalsCounter.get(-1) >= 3) {
			response.setAggSignal(-1);
		} else {
			response.setAggSignal(0);
		}

	}

	private int getSignalData(double data, double data_1, double data_2, Map<Integer, Integer> signalsCounter) {
		if (data > data_1 && data > data_2) {
			signalsCounter.put(1, signalsCounter.get(1) + 1);
			return 1;
		} else if (data > data_1 && data < data_2) {
			signalsCounter.put(0, signalsCounter.get(0) + 1);
			return 0;
		} else {
			signalsCounter.put(-1, signalsCounter.get(-1) + 1);
			return -1;
		}
	}

}
