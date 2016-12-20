package com.intelliinvest.web.controllers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockFundamentalsRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.util.IntelliinvestConverter;
import com.intelliinvest.util.Helper;
import com.intelliinvest.web.bo.response.StockPriceResponse;
import com.intelliinvest.web.bo.response.StockResponse;

@Controller
public class StockController {

	private static Logger logger = Logger.getLogger(StockController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private StockFundamentalsRepository stockFundamentalsRepository;

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
			stockResponse = IntelliinvestConverter.getStockResponse(stock);
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
			return IntelliinvestConverter.convertStockList(stocks);
		} else {
			List<StockResponse> list = new ArrayList<StockResponse>();
			StockResponse stockResponse = new StockResponse();
			stockResponse.setSuccess(false);
			stockResponse.setMessage(errorMsg);
			list.add(stockResponse);
			return list;
		}
	}
	
	@RequestMapping(value = "/stock/getWorldStocks", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockResponse> getWorldStocks() {
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		List<Stock> stocks = null;
		boolean error = false;
		try {
			stocks = stockRepository.getWorldStocks();
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getStocks() " + errorMsg);
			error = true;
		}
		if (stocks != null && !error) {
			return IntelliinvestConverter.convertStockList(stocks);
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
				quandlStockPrice = quandlEODStockPriceRepository.getLatestEODStockPrice(id);
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
			stockPriceResponse = IntelliinvestConverter.getStockPriceResponse(stockPrice, quandlStockPrice);
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
			return IntelliinvestConverter.convertStockPriceList(stockPrices, quandlStockPrices);
		} else {
			List<StockPriceResponse> list = new ArrayList<StockPriceResponse>();
			StockPriceResponse stockPriceResponse = new StockPriceResponse();
			stockPriceResponse.setSuccess(false);
			stockPriceResponse.setMessage(errorMsg);
			list.add(stockPriceResponse);
			return list;
		}
	}
	
	@RequestMapping(value = "/stock/getWorldStockPrices", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<StockPriceResponse> getWorldStockPrices() {
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		List<StockPrice> stockPrices = null;
		Map<String, QuandlStockPrice> quandlStockPrices = new HashMap<String, QuandlStockPrice>();
		boolean error = false;
		try {
			stockPrices = stockRepository.getWorldStockPrices();
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getStockPrices() " + errorMsg);
			error = true;
		}
		if (stockPrices != null && !error) {
			return IntelliinvestConverter.convertStockPriceList(stockPrices, quandlStockPrices);
		} else {
			List<StockPriceResponse> list = new ArrayList<StockPriceResponse>();
			StockPriceResponse stockPriceResponse = new StockPriceResponse();
			stockPriceResponse.setSuccess(false);
			stockPriceResponse.setMessage(errorMsg);
			list.add(stockPriceResponse);
			return list;
		}
	}

	@RequestMapping(value = "/stock/findMissingStockPrices", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody void findMissingStockPrices(@RequestParam("date") String dateStr) throws Exception {
		BufferedWriter writerLive = null;
		BufferedWriter writerEOD = null;
		BufferedWriter writerEODDate = null;
		BufferedWriter writerFundamentals = null;
		
		try {		
			List<Stock> stocks = null;
			List<StockPrice> stockPrices = null;
			List<QuandlStockPrice> quandlStockPrices = null;
			
			String reportDataDir = IntelliInvestStore.properties.getProperty("close.price.forecast.report.data.dir");
			stocks = stockRepository.getStocksFromDB();
			
			// find missing live stock prices
			stockPrices = stockRepository.getStockPricesFromDB();
			Map<String, StockPrice> stockPricesMap = new HashMap<String, StockPrice>();
			for (StockPrice price : stockPrices) {
				stockPricesMap.put(price.getSecurityId(), price);
			}
			List<String> missing = new ArrayList<String>();

			for (Stock stock : stocks) {
				if (!stockPricesMap.containsKey(stock.getSecurityId())) {
					missing.add(stock.getSecurityId());
				}
			}
			
			writerLive = new BufferedWriter(new FileWriter(reportDataDir + "/" + "MissingLivePrices.csv"));

			for (String code : missing) {
				LinkedList<String> valuesQueue = new LinkedList<String>();
				valuesQueue.add(code);
				String valueLine = valuesQueue.toString().replaceAll("\\[|\\]", "");
				writerLive.write(valueLine);
				writerLive.newLine();
				valuesQueue.clear();
			}
	
			// find missing EOD stock prices	
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate closeDate = LocalDate.parse(dateStr, dateFormat);
			Map<String, QuandlStockPrice> quandlPricesMap = new HashMap<String, QuandlStockPrice>();
			quandlStockPrices = quandlEODStockPriceRepository.getLatestStockPricesFromDB();
			for (QuandlStockPrice price : quandlStockPrices) {
				quandlPricesMap.put(price.getSecurityId(), price);
			}
			
			List<String> missingEOD = new ArrayList<String>();

			for (Stock stock : stocks) {
				if (quandlPricesMap.containsKey(stock.getSecurityId())) {
					QuandlStockPrice price = quandlPricesMap.get(stock.getSecurityId());
					if(!closeDate.equals(price.getEodDate())){
						missingEOD.add(stock.getSecurityId());
					}							
				}else {
					missingEOD.add(stock.getSecurityId());
				}
			}
			writerEOD = new BufferedWriter(new FileWriter(reportDataDir + "/" + "MissingEODPrices.csv"));
			for (String code : missingEOD) {
				LinkedList<String> valuesQueue = new LinkedList<String>();
				valuesQueue.add(code);
				String valueLine = valuesQueue.toString().replaceAll("\\[|\\]", "");
				writerEOD.write(valueLine);
				writerEOD.newLine();
				valuesQueue.clear();
			}
			
			//find all the quandl stocks for which date does not matches last closing date			
			List<QuandlStockPrice> missingEODDate = new ArrayList<QuandlStockPrice>();
			for (QuandlStockPrice price : quandlStockPrices) {
				if(!closeDate.equals(price.getEodDate())){
					missingEODDate.add(price);
				}				
			}
			writerEODDate = new BufferedWriter(new FileWriter(reportDataDir + "/" + "MissingEODDate.csv"));
			for (QuandlStockPrice price : missingEODDate) {
				LinkedList<String> valuesQueue = new LinkedList<String>();
				valuesQueue.add(price.getSecurityId());
				valuesQueue.add(dateFormat.format(price.getEodDate()));
				String valueLine = valuesQueue.toString().replaceAll("\\[|\\]", "");
				writerEODDate.write(valueLine);
				writerEODDate.newLine();
				valuesQueue.clear();
			}
			
			// find missing stock fundamentals
			List<String> missingFundamentals = new ArrayList<String>();
			for (Stock stock : stocks) {
				if(stockFundamentalsRepository.getStockFundamentalsById(stock.getSecurityId())==null){
					missingFundamentals.add(stock.getSecurityId());
				}
			}
			
			writerFundamentals = new BufferedWriter(new FileWriter(reportDataDir + "/" + "MissingFundamentals.csv"));

			for (String code : missingFundamentals) {
				LinkedList<String> valuesQueue = new LinkedList<String>();
				valuesQueue.add(code);
				String valueLine = valuesQueue.toString().replaceAll("\\[|\\]", "");
				writerFundamentals.write(valueLine);
				writerFundamentals.newLine();
				valuesQueue.clear();
			}
		} catch (Exception e) {
			logger.error("Exception inside getStockPrices() " + e.getMessage());
		} finally {
			writerLive.flush();
			writerLive.close();
			writerEOD.flush();
			writerEOD.close();
			writerEODDate.flush();
			writerEODDate.close();
			writerFundamentals.flush();
			writerFundamentals.close();
		}
	}
}