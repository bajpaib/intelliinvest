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

import com.intelliinvest.common.CommonConstParams;
import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.ForecastedStockPriceRepository;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.model.ForecastedStockPrice;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.web.bo.TimeSeriesResponse;

@Controller
public class TimeSeriesController {
	private static Logger logger = Logger.getLogger(TimeSeriesController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private ForecastedStockPriceRepository forecastedStockPriceRepository;

	@RequestMapping(value = "/timeseries/getTimeSeriesByCode", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody TimeSeriesResponse getTimeSeriesByCode(@RequestParam("code") String code,
			@RequestParam("date") String dateStr) {
		TimeSeriesResponse timeSeriesResponse = new TimeSeriesResponse();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		Stock stock = null;
		boolean error = false;
		LocalDate date = null;
		if (Helper.isNotNullAndNonEmpty(dateStr) && Helper.isNotNullAndNonEmpty(dateStr)) {
			try {
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				date = LocalDate.parse(dateStr, dateFormat);
				int years = new Integer(IntelliInvestStore.properties.getProperty("times.series.history.years")).intValue();
				int months = new Integer(IntelliInvestStore.properties.getProperty("times.series.history.months")).intValue();
				LocalDate startDate = date.minusYears(years).minusMonths(months);
				List<QuandlStockPrice> stockPrices = quandlEODStockPriceRepository.getStockPricesFromDB(code,startDate, date);
				
				if(Helper.isNotNullAndNonEmpty(stockPrices)){
					stockPrices.sort(new Comparator<QuandlStockPrice>() {
						public int compare(QuandlStockPrice price1, QuandlStockPrice price2) {
							return price1.getEodDate().compareTo(price2.getEodDate());

						}
					});			
					ForecastedStockPrice price = forecastedStockPriceRepository.getForecastStockPriceFromDB(code, date);
					
					timeSeriesResponse.setCode(code);
					timeSeriesResponse.setDate(date);
					
					List<String> dateSeries = new ArrayList<String>();
					List<Double> priceSeries = new ArrayList<Double>();
					
					
					for(QuandlStockPrice temp: stockPrices){
						dateSeries.add(dateFormat.format(temp.getEodDate()));
						priceSeries.add(MathUtil.round(temp.getClose()));
					}
					
					if(price!=null){						
						dateSeries.add(dateFormat.format(price.getTomorrowForecastDate()));
						dateSeries.add(dateFormat.format(price.getWeeklyForecastDate()));
						dateSeries.add(dateFormat.format(price.getMonthlyForecastDate()));
						priceSeries.add(MathUtil.round(price.getTomorrowForecastPrice()));
						priceSeries.add(MathUtil.round(price.getWeeklyForecastPrice()));
						priceSeries.add(MathUtil.round(price.getMonthlyForecastPrice()));
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
		if (stock == null) {
			errorMsg = "Stock does not exists.";
			logger.error("Exception inside getTimeSeriesByCode() " + errorMsg);
			error = true;
		}
		if (stock != null && !error) {
			timeSeriesResponse.setSuccess(true);
			timeSeriesResponse.setMessage("Stock details have been returned successfully.");
		} else {
			timeSeriesResponse.setCode(code);
			timeSeriesResponse.setSuccess(false);
			timeSeriesResponse.setMessage(errorMsg);
		}
		return timeSeriesResponse;
	}

}