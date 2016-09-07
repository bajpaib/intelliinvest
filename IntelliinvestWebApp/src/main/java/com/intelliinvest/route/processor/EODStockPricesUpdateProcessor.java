package com.intelliinvest.route.processor;

import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.EODStockPrice;
import com.intelliinvest.service.EODStockPriceService;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(EODStockPricesUpdateProcessor.PROCESSOR_NAME)
public class EODStockPricesUpdateProcessor extends IntelliInvestProcessor{
	
	private final EODStockPriceService eodStockPriceService;
	
	public static final String PROCESSOR_NAME = "EODStockPricesUpdateProcessor";
	
	@Autowired
	public EODStockPricesUpdateProcessor(CamelContext camelContext, EODStockPriceService eodStockPriceService) {
		super(camelContext, PROCESSOR_NAME, "updateEODStockPrices");
		this.eodStockPriceService = eodStockPriceService;
	}
	
	public void updateEODStockPrices(@Body List<EODStockPrice> eodStockPrices) {
		eodStockPriceService.updateEODStockPrices(eodStockPrices);
	 };
}