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

import com.intelliinvest.data.dao.ForecastedStockPriceRepository;
import com.intelliinvest.data.forecast.ClosePriceForecastReport;
import com.intelliinvest.data.forecast.DailyClosePriceForecaster;
import com.intelliinvest.data.forecast.MonthlyClosePriceForecaster;
import com.intelliinvest.data.forecast.WeeklyClosePriceForecaster;
import com.intelliinvest.data.model.ForecastedStockPrice;

@Controller
public class ForecastedStockPriceController {
	private static Logger logger = Logger.getLogger(ForecastedStockPriceController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private DailyClosePriceForecaster dailyClosePriceForecaster;
	@Autowired
	private ClosePriceForecastReport closePriceForecastReport;
	@Autowired
	private WeeklyClosePriceForecaster weeklyClosePriceForecaster;
	@Autowired
	private MonthlyClosePriceForecaster monthlyClosePriceForecaster;
	@Autowired
	private ForecastedStockPriceRepository forecastedStockPriceRepository;

	@RequestMapping(value = "/forecast/forecastAndUpdateTomorrowClose", method = RequestMethod.GET)
	public @ResponseBody String forecastAndUpdateTomorrowClose(@RequestParam("today") String today) {
		return dailyClosePriceForecaster.forecastAndUpdateTomorrowClose(today);
	}

	@RequestMapping(value = "/forecast/forecastAndUpdateWeeklyClose", method = RequestMethod.GET)
	public @ResponseBody String forecastAndUpdateWeeklyClose(@RequestParam("today") String today) {
		return weeklyClosePriceForecaster.forecastAndUpdateWeeklyClose(today);
	}

	@RequestMapping(value = "/forecast/forecastAndUpdateMonthlyClose", method = RequestMethod.GET)
	public @ResponseBody String forecastAndUpdateMonthlyClose(@RequestParam("today") String today) {
		return monthlyClosePriceForecaster.forecastAndUpdateMonthlyClose(today);
	}

	@RequestMapping(value = "/forecast/getForecastStockPrice", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody ForecastedStockPrice getForecastStockPrice(@RequestParam("id") String id,
			@RequestParam("today") String today) {
		ForecastedStockPrice price = null;
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(today, dateFormat);
			price = forecastedStockPriceRepository.getForecastStockPriceFromDB(id, date);
		} catch (Exception e) {
			logger.error("Exception inside getDailyForecastStockPrice() " + e.getMessage());
		}
		return price;
	}

	@RequestMapping(value = "/forecast/getForecastStockPrices", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<ForecastedStockPrice> getForecastStockPrices(@RequestParam("today") String today) {
		List<ForecastedStockPrice> priceList = null;
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(today, dateFormat);
			priceList = forecastedStockPriceRepository.getForecastStockPricesFromDB(date);
		} catch (Exception e) {
			logger.error("Exception inside getLatestDailyForecastStockPrices() " + e.getMessage());
		}
		return priceList;
	}

	@RequestMapping(value = "/forecast/generateAndEmailClosePriceForecastReport", method = RequestMethod.GET)
	public @ResponseBody String generateAndEmailClosePriceForecastReport(@RequestParam("today") String today) {
		return closePriceForecastReport.generateAndEmailClosePriceForecastReport(today);
	}
}