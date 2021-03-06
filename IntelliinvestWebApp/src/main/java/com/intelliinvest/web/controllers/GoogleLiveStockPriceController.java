package com.intelliinvest.web.controllers;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.importer.GoogleLiveStockPriceImporter;
import com.intelliinvest.data.model.PriceVolumeData;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.web.bo.response.IntradayPriceVolumeDataResponse;
import com.intelliinvest.web.bo.response.StockPriceTimeSeriesResponse;
import com.intelliinvest.web.bo.response.TimeSeriesResponse;

@Controller
public class GoogleLiveStockPriceController {
	private static Logger logger = Logger.getLogger(GoogleLiveStockPriceController.class);
	@Autowired
	private GoogleLiveStockPriceImporter googleLiveStockPriceImporter;

	@Autowired
	private DateUtil dateUtil;
	
	@RequestMapping(value = "/google/backLoadLivePrices", method = RequestMethod.GET)
	public @ResponseBody String backLoadLivePrices() {
		googleLiveStockPriceImporter.updateCurrentPrices();
		return "Success";
	}
	
	@RequestMapping(value = "/google/intraday/volumeprice", method = RequestMethod.GET)
	public @ResponseBody IntradayPriceVolumeDataResponse intradayPrice(@RequestParam("exchange") String exchange, @RequestParam("securityId") String securityId) {
		IntradayPriceVolumeDataResponse intradayPriceVolumeDataResponse = new IntradayPriceVolumeDataResponse(exchange, securityId);
		List<PriceVolumeData> priceVolumeDatas = googleLiveStockPriceImporter.getPriceVolumeData(exchange, securityId, 120, "1d");
		intradayPriceVolumeDataResponse.setPriceVolumeDatas(priceVolumeDatas);
		if(priceVolumeDatas.isEmpty()){
			intradayPriceVolumeDataResponse.setSuccess(false);
			intradayPriceVolumeDataResponse.setMessage("Error fetching Intra Day Price Volume Data");
		}
		return intradayPriceVolumeDataResponse;
	}
	
	@RequestMapping(value = "/google/eod/timeseries", method = RequestMethod.GET)
	public @ResponseBody TimeSeriesResponse eodPrice(@RequestParam("exchange") String exchange, @RequestParam("securityId") String securityId) {
		TimeSeriesResponse timeSeriesResponse = new TimeSeriesResponse();
		timeSeriesResponse.setSecurityId(securityId);
		timeSeriesResponse.setSuccess(true);
		timeSeriesResponse.setDate(dateUtil.getLocalDate());
		List<StockPriceTimeSeriesResponse> stockPriceTimeSeriesResponses = googleLiveStockPriceImporter.getStockPriceTimeSeriesResponse(exchange, securityId, 86400, "2Y");
		timeSeriesResponse.getStockPriceTimeSeries().addAll(stockPriceTimeSeriesResponses);
		if(timeSeriesResponse.getStockPriceTimeSeries().isEmpty()){
			timeSeriesResponse.setSuccess(false);
			timeSeriesResponse.setMessage("Error fetching Time Series");
		}
		return timeSeriesResponse;
	}

}