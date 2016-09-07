package com.intelliinvest.route.processor;

import java.util.Set;

import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.service.StockService;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(GetStockCodesProcessor.PROCESSOR_NAME)
public class GetStockCodesProcessor extends IntelliInvestProcessor{
	
	public static final String PROCESSOR_NAME = "GetStockCodesProcessor";
	
	private final StockService stockService;

	@Autowired
	public GetStockCodesProcessor(CamelContext camelContext, StockService stockService) {
		super(camelContext, PROCESSOR_NAME, "getStockCodes");
		this.stockService = stockService;
	}
	
	public Set<String> getStockCodes() {
		return stockService.getStockCodes();
	}
}