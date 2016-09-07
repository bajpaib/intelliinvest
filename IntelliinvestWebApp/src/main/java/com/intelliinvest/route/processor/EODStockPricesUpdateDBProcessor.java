package com.intelliinvest.route.processor;

import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.EODStockPrice;
import com.intelliinvest.service.EODStockPriceService;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(EODStockPricesUpdateDBProcessor.PROCESSOR_NAME)
public class EODStockPricesUpdateDBProcessor extends IntelliInvestProcessor{
	
	private final EODStockPriceService eodStockPriceService;
	
	public static final String PROCESSOR_NAME = "EODStockPricesUpdateDBProcessor";
	
	@Autowired
	public EODStockPricesUpdateDBProcessor(CamelContext camelContext, EODStockPriceService eodStockPriceService) {
		super(camelContext, PROCESSOR_NAME, "updateEODPricesInDatabase");
		this.eodStockPriceService = eodStockPriceService;
	}
	
	public void updateEODPricesInDatabase(@Body List<EODStockPrice> eodStockPrices) {
		eodStockPriceService.updateEODPricesInDatabase(eodStockPrices);
	 };
}