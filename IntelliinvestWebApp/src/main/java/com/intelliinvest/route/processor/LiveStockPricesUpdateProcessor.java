package com.intelliinvest.route.processor;

import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.LiveStockPrice;
import com.intelliinvest.service.LiveStockPriceService;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(LiveStockPricesUpdateProcessor.PROCESSOR_NAME)
public class LiveStockPricesUpdateProcessor extends IntelliInvestProcessor{
	
	private final LiveStockPriceService liveStockPriceService;
	
	public static final String PROCESSOR_NAME = "LiveStockPricesUpdateProcessor";
	
	@Autowired
	public LiveStockPricesUpdateProcessor(CamelContext camelContext, LiveStockPriceService liveStockPriceService) {
		super(camelContext, PROCESSOR_NAME, "updateLiveStockPrices");
		this.liveStockPriceService = liveStockPriceService;
	}
	
	public void updateLiveStockPrices(@Body List<LiveStockPrice> liveStockPrices) {
		liveStockPriceService.updateLiveStockPrices(liveStockPrices);
	 };
}