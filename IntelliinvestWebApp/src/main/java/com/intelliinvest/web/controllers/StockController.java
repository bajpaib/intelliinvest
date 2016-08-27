package com.intelliinvest.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.CommonConstParams;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.importer.GoogleLiveStockPriceImporter;
import com.intelliinvest.data.importer.QuandlEODStockPriceImporter;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.util.Converter;
import com.intelliinvest.util.Helper;
import com.intelliinvest.web.bo.StockPriceResponse;
import com.intelliinvest.web.bo.StockResponse;

@Controller
public class StockController {

	private static Logger logger = Logger.getLogger(StockController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private GoogleLiveStockPriceImporter googleLiveStockPriceImporter;
	@Autowired
	private QuandlEODStockPriceImporter quandlEODStockPriceImporter;
	
	@RequestMapping(value = "/stock/getStockByCode", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockResponse getStockByCode(@RequestParam("stockCode") String stockCode) {
		StockResponse stockResponse = new StockResponse();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		Stock stock = null;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(stockCode)) {
			try {
				stock = stockRepository.getStockByCode(stockCode);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getStockByCode() " + errorMsg);
				error = true;
			}
		} else {
			errorMsg = "Stock code is null or empty";
			logger.error("Exception inside getStockByCode() " + errorMsg);
			error = true;
		}
		if (stock == null) {
			errorMsg = "Stock does not exists.";
			logger.error("Exception inside getStockByCode() " + errorMsg);
			error = true;
		}
		if (stock != null && !error) {
			stockResponse = Converter.getStockResponse(stock);
			stockResponse.setSuccess(true);
			stockResponse.setMessage("Stock details have been returned successfully.");
		} else {
			stockResponse.setCode(stockCode);
			stockResponse.setSuccess(false);
			stockResponse.setMessage(errorMsg);
		}
		return stockResponse;
	}

	@RequestMapping(value = "/stock/getStocks", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockResponse> getStocks() {
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		List<Stock> stocks = null;
		boolean error = false;
		try {
			stocks = stockRepository.getStocks();
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getStocks() " + errorMsg);
			error = true;
		}
		if (stocks != null && !error) {
			return Converter.convertStockList(stocks);
		} else {
			List<StockResponse> list = new ArrayList<StockResponse>();
			StockResponse stockResponse = new StockResponse();
			stockResponse.setSuccess(false);
			stockResponse.setMessage(errorMsg);
			list.add(stockResponse);
			return list;
		}
	}

	@RequestMapping(value = "/stock/getStockPriceByCode", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockPriceResponse getStockPriceByCode(@RequestParam("stockCode") String stockCode) {
		StockPriceResponse stockPriceResponse = new StockPriceResponse();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		StockPrice stockPrice = null;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(stockCode)) {
			try {
				stockPrice = stockRepository.getStockPriceByCode(stockCode);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getStockPriceByCode() " + errorMsg);
				error = true;
			}
		} else {
			errorMsg = "Stock code is null or empty";
			logger.error("Exception inside getStockPriceByCode() " + errorMsg);
			error = true;
		}
		if (stockPrice == null) {
			errorMsg = "StockPrice does not exists.";
			logger.error("Exception inside getStockPriceByCode() " + errorMsg);
			error = true;
		}
		if (stockPrice != null && !error) {
			stockPriceResponse = Converter.getStockPriceResponse(stockPrice);
			stockPriceResponse.setSuccess(true);
			stockPriceResponse.setMessage("StockPrice details have been returned successfully.");
		} else {
			stockPriceResponse.setCode(stockCode);
			stockPriceResponse.setSuccess(false);
			stockPriceResponse.setMessage(errorMsg);
		}
		return stockPriceResponse;
	}

	@RequestMapping(value = "/stock/getStockPrices", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockPriceResponse> getStockPrices() {
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		List<StockPrice> stockPrices = null;
		boolean error = false;
		try {
			stockPrices = stockRepository.getStockPrices();
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getStockPrices() " + errorMsg);
			error = true;
		}
		if (stockPrices != null && !error) {
			return Converter.convertStockPriceList(stockPrices);
		} else {
			List<StockPriceResponse> list = new ArrayList<StockPriceResponse>();
			StockPriceResponse stockPriceResponse = new StockPriceResponse();
			stockPriceResponse.setSuccess(false);
			stockPriceResponse.setMessage(errorMsg);
			list.add(stockPriceResponse);
			return list;
		}
	}

}