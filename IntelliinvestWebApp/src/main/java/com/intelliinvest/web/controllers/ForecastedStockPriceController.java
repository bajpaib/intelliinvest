package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.dao.ForecastedStockPriceRepository;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.forecast.ClosePriceForecastReport;
import com.intelliinvest.data.forecast.DailyClosePriceForecaster;
import com.intelliinvest.data.forecast.MonthlyClosePriceForecaster;
import com.intelliinvest.data.forecast.WeeklyClosePriceForecaster;
import com.intelliinvest.data.model.ForecastedStockPrice;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.util.Converter;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.web.bo.response.ForecastedStockPriceResponse;
import com.intelliinvest.web.bo.response.TimeSeriesResponse;

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
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private DateUtil dateUtil;
	
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

	/**
	 * If date = T, returns closing prices forecasted yesterday (T-1) for T, T+5 and T+20 days. Price is forecasted after T-1 closing is received.
	 * @param id
	 * @param date
	 * @return
	 */
	@RequestMapping(value = "/forecast/getForecastStockPriceForDate", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody ForecastedStockPriceResponse getForecastStockPriceForDate(@RequestParam("id") String id,
			@RequestParam("date") String dateStr) {
		ForecastedStockPriceResponse response = new ForecastedStockPriceResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		ForecastedStockPrice price = null;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(id) && Helper.isNotNullAndNonEmpty(dateStr)) {
			try {
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate date = LocalDate.parse(dateStr, dateFormat);			
				LocalDate lastBusinessDate = dateUtil.substractBusinessDays(date, 1);			
				price = forecastedStockPriceRepository.getForecastStockPriceFromDB(id, lastBusinessDate);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getDailyForecastStockPrice() " + e.getMessage());
				error = true;
			}
		}else {
			errorMsg = "Stock Code or Date is null or empty";
			logger.error("Exception inside getForecastStockPriceForDate() " + errorMsg);
			error = true;
		}
		
		if (price == null) {
			errorMsg = "Forecast price not found";
			logger.error("Inside getForecastStockPriceForDate() " + errorMsg);
			error = true;
		}
		if (price != null && !error) {
			response = Converter.getForecastedStockPriceResponse(price);
			response.setSuccess(true);
			response.setMessage("Forecasted Stock Price has been returned successfully.");
		} else {
			response.setSecurityId(id);
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}
		return response;
	}
	
	
	/**
	 * If date = T, returns closing prices forecasted yesterday (T-1) for T, T+5 and T+20 days. Price is forecasted after T-1 closing is received.
	 * @param date
	 * @return
	 */
	
	@RequestMapping(value = "/forecast/getForecastStockPricesForDate", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<ForecastedStockPriceResponse> getForecastStockPricesForDate(@RequestParam("date") String dateStr) {
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		List<ForecastedStockPrice> prices = null;
		boolean error = false;
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(dateStr, dateFormat);			
			LocalDate lastBusinessDate = dateUtil.substractBusinessDays(date, 1);	
			prices = forecastedStockPriceRepository.getForecastStockPricesFromDB(lastBusinessDate);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getForecastStockPricesForDate() " + errorMsg);
			error = true;
		}
		if (prices != null && !error) {
			return Converter.convertForecastedStockPriceList(prices);
		} else {
			List<ForecastedStockPriceResponse> list = new ArrayList<ForecastedStockPriceResponse>();
			ForecastedStockPriceResponse response = new ForecastedStockPriceResponse();
			response.setSuccess(false);
			response.setMessage(errorMsg);
			list.add(response);
			return list;
		}
	}

	@RequestMapping(value = "/forecast/generateAndEmailClosePriceForecastReport", method = RequestMethod.GET)
	public @ResponseBody String generateAndEmailClosePriceForecastReport(@RequestParam("today") String today) {
		return closePriceForecastReport.generateAndEmailClosePriceForecastReport(today);
	}
	
	/**
	 * If date= T, retrieve Times Series data for T 
	 * i.e. Return historical close prices until T-1 and closing prices forecasted on T-1 for T, T+5 and T+20
	 * @param id
	 * @param dateStr
	 * @return
	 */
	@RequestMapping(value = "/forecast/getTimeSeriesById", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody TimeSeriesResponse getTimeSeriesById(@RequestParam("id") String id,
			@RequestParam("date") String dateStr) {
		TimeSeriesResponse timeSeriesResponse = new TimeSeriesResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(dateStr) && Helper.isNotNullAndNonEmpty(dateStr)) {
			try {
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate date = LocalDate.parse(dateStr, dateFormat);				
				LocalDate lastBusinessDate = dateUtil.substractBusinessDays(date, 1);				
				int years = new Integer(IntelliInvestStore.properties.getProperty("times.series.history.years"))
						.intValue();
				int months = new Integer(IntelliInvestStore.properties.getProperty("times.series.history.months"))
						.intValue();
				LocalDate startDate = lastBusinessDate.minusYears(years).minusMonths(months);
				List<QuandlStockPrice> stockPrices = quandlEODStockPriceRepository.getStockPricesFromDB(id, startDate,
						lastBusinessDate);

				if (Helper.isNotNullAndNonEmpty(stockPrices)) {
					stockPrices.sort(new Comparator<QuandlStockPrice>() {
						public int compare(QuandlStockPrice price1, QuandlStockPrice price2) {
							return price1.getEodDate().compareTo(price2.getEodDate());

						}
					});
					ForecastedStockPrice price = forecastedStockPriceRepository.getForecastStockPriceFromDB(id, lastBusinessDate);

					timeSeriesResponse.setSecurityId(id);
					timeSeriesResponse.setDate(date);

					List<String> dateSeries = new ArrayList<String>();
					List<Double> priceSeries = new ArrayList<Double>();

					for (QuandlStockPrice temp : stockPrices) {
						dateSeries.add(dateFormat.format(temp.getEodDate()));
						priceSeries.add(MathUtil.round(temp.getClose()));
					}

					if (price != null) {
						LocalDate tomorrowForecastDate = price.getTomorrowForecastDate();
						LocalDate weeklyForecastDate = price.getWeeklyForecastDate();
						LocalDate monthlyForecastDate = price.getMonthlyForecastDate();
						if (tomorrowForecastDate != null) {
							dateSeries.add(dateFormat.format(price.getTomorrowForecastDate()));
							priceSeries.add(MathUtil.round(price.getTomorrowForecastPrice()));
						}

						if (weeklyForecastDate != null) {
							dateSeries.add(dateFormat.format(price.getWeeklyForecastDate()));
							priceSeries.add(MathUtil.round(price.getWeeklyForecastPrice()));
						}

						if (monthlyForecastDate != null) {
							dateSeries.add(dateFormat.format(price.getMonthlyForecastDate()));
							priceSeries.add(MathUtil.round(price.getMonthlyForecastPrice()));
						}
					}
					timeSeriesResponse.setDateSeries(dateSeries);
					timeSeriesResponse.setPriceSeries(priceSeries);
				}
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getTimeSeriesByCode() " + errorMsg);
				error = true;
			}
		} else {
			errorMsg = "Stock code or Date is null or empty";
			logger.error("Exception inside getTimeSeriesByCode() " + errorMsg);
			error = true;
		}

		if (!error) {
			timeSeriesResponse.setSuccess(true);
			timeSeriesResponse.setMessage("Time series has been returned successfully.");
		} else {
			timeSeriesResponse.setSecurityId(id);
			timeSeriesResponse.setSuccess(false);
			timeSeriesResponse.setMessage(errorMsg);
		}
		return timeSeriesResponse;
	}
}