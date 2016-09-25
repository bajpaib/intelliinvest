package com.intelliinvest.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.QuandlStockPrice;
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
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	
	@RequestMapping(value = "/stock/getStockById", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockResponse getStockById(@RequestParam("id") String id) {
		StockResponse stockResponse = new StockResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		Stock stock = null;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(id)) {
			try {
				stock = stockRepository.getStockById(id);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getStockById() " + errorMsg);
				error = true;
			}
		} else {
			errorMsg = "Stock code is null or empty";
			logger.error("Exception inside getStockById() " + errorMsg);
			error = true;
		}
		if (stock == null) {
			errorMsg = "Stock does not exists.";
			logger.error("Exception inside getStockById() " + errorMsg);
			error = true;
		}
		if (stock != null && !error) {
			stockResponse = Converter.getStockResponse(stock);
			stockResponse.setSuccess(true);
			stockResponse.setMessage("Stock details have been returned successfully.");
		} else {
			stockResponse.setSecurityId(id);
			stockResponse.setSuccess(false);
			stockResponse.setMessage(errorMsg);
		}
		return stockResponse;
	}

	@RequestMapping(value = "/stock/getStocks", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockResponse> getStocks() {
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
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

	@RequestMapping(value = "/stock/getStockPriceById", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StockPriceResponse getStockPriceByCode(@RequestParam("id") String id) {
		StockPriceResponse stockPriceResponse = new StockPriceResponse();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		StockPrice stockPrice = null;
		QuandlStockPrice quandlStockPrice = null;
		boolean error = false;
		if (Helper.isNotNullAndNonEmpty(id)) {
			try {
				stockPrice = stockRepository.getStockPriceById(id);
				quandlStockPrice = quandlEODStockPriceRepository.getEODStockPrice(id);
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
			stockPriceResponse = Converter.getStockPriceResponse(stockPrice,quandlStockPrice);
			stockPriceResponse.setSuccess(true);
			stockPriceResponse.setMessage("StockPrice details have been returned successfully.");
		} else {
			stockPriceResponse.setSecurityId(id);
			stockPriceResponse.setSuccess(false);
			stockPriceResponse.setMessage(errorMsg);
		}
		return stockPriceResponse;
	}

	@RequestMapping(value = "/stock/getStockPrices", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockPriceResponse> getStockPrices() {
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		List<StockPrice> stockPrices = null;
		Map<String, QuandlStockPrice> quandlStockPrices = null;
		boolean error = false;
		try {
			stockPrices = stockRepository.getStockPrices();
			quandlStockPrices = quandlEODStockPriceRepository.getLatestEODStockPrices();
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getStockPrices() " + errorMsg);
			error = true;
		}
		if (stockPrices != null && !error) {
			return Converter.convertStockPriceList(stockPrices,quandlStockPrices);
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