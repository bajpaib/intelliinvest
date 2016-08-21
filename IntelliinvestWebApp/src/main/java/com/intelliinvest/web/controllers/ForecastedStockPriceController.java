package com.intelliinvest.web.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.dao.ForecastedStockPriceRepository;
import com.intelliinvest.data.forecast.DailyClosePriceForecastReport;
import com.intelliinvest.data.forecast.DailyClosePriceForecaster;
import com.intelliinvest.data.model.ForecastedStockPrice;

@Controller
public class ForecastedStockPriceController {
	private static Logger logger = Logger.getLogger(ForecastedStockPriceController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private DailyClosePriceForecaster dailyClosePriceForecaster;
	@Autowired
	private DailyClosePriceForecastReport dailyClosePriceForecastReport;
	@Autowired
	private ForecastedStockPriceRepository forecastedStockPriceRepository;

	@RequestMapping(value = "/stock/forecastAndUpdateTomorrowClose", method = RequestMethod.GET)
	public @ResponseBody String forecastAndUpdateTomorrowClose(@RequestParam("today") String today) {
		return dailyClosePriceForecaster.forecastAndUpdateTomorrowClose(today);
	}

	@RequestMapping(value = "/stock/getDailyForecastStockPrice", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody ForecastedStockPrice getDailyForecastStockPrice(@RequestParam("code") String code,
			@RequestParam("forecastDate") String forecastDate) {
		ForecastedStockPrice price = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = format.parse(forecastDate);
			price = forecastedStockPriceRepository.getDailyForecastStockPriceFromDB(code, date);
		} catch (Exception e) {
			logger.error("Exception inside getDailyForecastStockPrice() " + e.getMessage());
		}
		return price;
	}
	
	@RequestMapping(value = "/stock/getDailyForecastStockPrices", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<ForecastedStockPrice> getDailyForecastStockPrices(@RequestParam("forecastDate") String forecastDate) {
		List<ForecastedStockPrice> priceList = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = format.parse(forecastDate);
			priceList = forecastedStockPriceRepository.getDailyForecastStockPricesFromDB(date);
		} catch (Exception e) {
			logger.error("Exception inside getLatestDailyForecastStockPrices() " + e.getMessage());
		}
		return priceList;
	}
	
	@RequestMapping(value = "/stock/generateAndEmailForecastReport", method = RequestMethod.GET)
	public @ResponseBody String generateAndEmailForecastReport(@RequestParam("forecastDate") String forecastDate) {
		return dailyClosePriceForecastReport.generateAndEmailForecastReport(forecastDate);
	}
}
