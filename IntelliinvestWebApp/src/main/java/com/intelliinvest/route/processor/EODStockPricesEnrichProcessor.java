package com.intelliinvest.route.processor;

import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.EODStockPrice;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(EODStockPricesEnrichProcessor.PROCESSOR_NAME)
public class EODStockPricesEnrichProcessor extends IntelliInvestProcessor{
	
	public static final String PROCESSOR_NAME = "EODStockPricesEnrichProcessor";
	
	@Autowired
	public EODStockPricesEnrichProcessor(CamelContext camelContext) {
		super(camelContext, PROCESSOR_NAME, "enrichQuandlStockPrices");
	}
	
	public void enrichQuandlStockPrices(@Body List<EODStockPrice> quandlStockPrices, @Header("code") String code) {
		for(EODStockPrice quandlStockPrice : quandlStockPrices){
			quandlStockPrice.setExchange("NSE");
			quandlStockPrice.setSeries("");
			quandlStockPrice.setSymbol(code);
		}
	}
}