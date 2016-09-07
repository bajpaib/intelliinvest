package com.intelliinvest.route.processor;

import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.EODStockPrice;
import com.intelliinvest.service.StockPriceService;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(StockPricesUpdateEODProcessor.PROCESSOR_NAME)
public class StockPricesUpdateEODProcessor extends IntelliInvestProcessor{
	
	private final StockPriceService stockPriceService;
	
	public static final String PROCESSOR_NAME = "StockPricesUpdateEODProcessor";
	
	@Autowired
	public StockPricesUpdateEODProcessor(CamelContext camelContext, StockPriceService stockPriceService) {
		super(camelContext, PROCESSOR_NAME, "updateEODStockPrices");
		this.stockPriceService = stockPriceService;
	}
	
	public void updateEODStockPrices(@Body List<EODStockPrice> eodStockPrices) {
		stockPriceService.updateEODStockPrices(eodStockPrices);
	 };
}