package com.intelliinvest.route.processor;

import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.LiveStockPrice;
import com.intelliinvest.service.StockPriceService;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(StockPricesUpdateLiveProcessor.PROCESSOR_NAME)
public class StockPricesUpdateLiveProcessor extends IntelliInvestProcessor{
	
	private final StockPriceService stockPriceService;
	
	public static final String PROCESSOR_NAME = "StockPricesUpdateLiveProcessor";
	
	@Autowired
	public StockPricesUpdateLiveProcessor(CamelContext camelContext, StockPriceService stockPriceService) {
		super(camelContext, PROCESSOR_NAME, "updateLiveStockPrices");
		this.stockPriceService = stockPriceService;
	}
	
	public void updateLiveStockPrices(@Body List<LiveStockPrice> liveStockPrices) {
		stockPriceService.updateLiveStockPrices(liveStockPrices);
	 };
}