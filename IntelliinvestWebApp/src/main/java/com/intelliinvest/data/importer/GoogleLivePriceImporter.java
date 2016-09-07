package com.intelliinvest.data.importer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.camel.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliinvest.data.dao.IntelliInvestRepository;
import com.intelliinvest.data.model.LiveStockPrice;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.HttpUtil;

@Component
public class GoogleLivePriceImporter implements LivePriceImporter{
	private static Logger LOGGER = LoggerFactory.getLogger(GoogleLivePriceImporter.class);
	private final IntelliInvestRepository intelliInvestRepository;
	private final static String GOOGLE_QUOTE_URL = "https://www.google.com/finance/info?q=#CODE#";
	ObjectMapper objectMapper = new ObjectMapper();

//	public static void main(String[] args) throws Exception {
//		GoogleLivePriceImporter googleLiveStockPriceImporter = new GoogleLivePriceImporter();
//		System.out.println(googleLiveStockPriceImporter.importDataFromExchange("NSE", new HashSet<String>(Arrays.asList("INFY", "ABAN"))));
//	}
	
	@Autowired
	public GoogleLivePriceImporter(IntelliInvestRepository intelliInvestRepository) {
		this.intelliInvestRepository = intelliInvestRepository;
	}
	
	public List<LiveStockPrice> importData(@Body Set<String> stocks) throws Exception{
		return importDataFromExchange("NSE", stocks);
	}
	
	public List<LiveStockPrice> importDataFromExchange(String exchange, Set<String> stocks){
		String codes = "";
		ArrayList<LiveStockPrice> googleStockPrices = new ArrayList<LiveStockPrice>();
		codes = getCombinedCode(exchange, stocks, codes);
		if(!StringUtils.isEmpty(codes)){
			googleStockPrices = getPricesFromGoogle(codes);
			Set<String> validStocks = validatePricesRetrieved(googleStockPrices);
			if("NSE".equals(exchange)){
				stocks.removeAll(validStocks);
				if(!stocks.isEmpty()){
					try{
						LOGGER.info("Stocks which are been tried for retrieving from BOM : {}", stocks);
						googleStockPrices.addAll(importDataFromExchange("BOM", stocks));
					}catch(Exception e){
						LOGGER.info("Exception executing live price fetch : {}", e.getMessage(), e);
					}
				}
			}else{
				stocks.removeAll(validStocks);
				if(stocks.size()>0){
					LOGGER.info("Stocks which are failed even retrieving from BOM : {}", stocks);
				}
			}
		}
		return googleStockPrices;
	}

	private ArrayList<LiveStockPrice> getPricesFromGoogle(String codes){
		ArrayList<LiveStockPrice> googleStockPrices = new ArrayList<LiveStockPrice>();
		try{
			String response = HttpUtil.getFromUrlAsString(GOOGLE_QUOTE_URL.replace("#CODE#", codes.replace("&", "%26")));
			response = response.replaceFirst("//", "").trim();
			LiveStockPrice[] googleStockPricesArray = objectMapper.readValue(response, LiveStockPrice[].class);
			googleStockPrices.addAll(Arrays.asList(googleStockPricesArray));
		}catch (Exception e) {
			LOGGER.info("Stocks which are failed retrieval google : {}", codes);
		}
		return googleStockPrices;
	}

	private String getCombinedCode(String exchange, Set<String> stocks, String codes) {
		for (String stock : stocks) {
			String code = exchange + ":" + stock;
			if("BOM".equals(exchange)){
				String bseCode = intelliInvestRepository.getBSECode(stock);
				if(null!=bseCode){
					code = exchange + ":" + bseCode;
				}
			}
			codes = codes + code + ",";
		}
		if (!codes.isEmpty()) {
			codes = codes.substring(0, codes.lastIndexOf(","));
		}
		return codes;
	}
	
	public Set<String> validatePricesRetrieved(List<LiveStockPrice> googleStockPrices){
		Set<String> validStocks = new HashSet<String>();
		SimpleDateFormat format = new SimpleDateFormat("MMM dd, hh:mmaa z");
		Calendar currentCal = Calendar.getInstance();
		try {
			currentCal.setTime(format.parse(format.format(DateUtil.getDateFromLocalDateTime())));
		} catch (ParseException e) {
			LOGGER.info("Exception formatting current date");
		}
		currentCal.add(Calendar.MONTH, -1);
		LocalDate currentDate = currentCal.getTime().toInstant().atZone(DateUtil.ZONE_ID).toLocalDate();
		for (Iterator<LiveStockPrice> iterator = googleStockPrices.iterator(); iterator.hasNext();) {
			LiveStockPrice googleStockPrice = iterator.next();
			if(null==googleStockPrice.getCode() 
					|| null==googleStockPrice.getExchange() 
					|| null==googleStockPrice.getLastTraded() 
					|| null==googleStockPrice.getPrice() 
					|| null==googleStockPrice.getChangePercent()
					|| currentDate.compareTo(googleStockPrice.getLastTraded()) > 0) {
				iterator.remove();
			}else{
				if("BOM".equals(googleStockPrice.getExchange())){
					String nseCode = intelliInvestRepository.getNSECode(googleStockPrice.getCode());
					if(null==nseCode){
						continue;
					}
					googleStockPrice.setCode(intelliInvestRepository.getNSECode(googleStockPrice.getCode()));
					googleStockPrice.setExchange("NSE");
				}
				validStocks.add(googleStockPrice.getCode());
			}
		}
		return validStocks;
	}

}