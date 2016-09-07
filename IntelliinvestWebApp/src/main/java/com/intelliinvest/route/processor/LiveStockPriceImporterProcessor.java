package com.intelliinvest.route.processor;

import java.util.List;
import java.util.Set;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.importer.LivePriceImporter;
import com.intelliinvest.data.model.LiveStockPrice;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(LiveStockPriceImporterProcessor.PROCESSOR_NAME)
public class LiveStockPriceImporterProcessor extends IntelliInvestProcessor{
	
	public static final String PROCESSOR_NAME = "LiveStockPriceImporterProcessor";
	private final LivePriceImporter livePriceImporter;
	
	@Autowired
	public LiveStockPriceImporterProcessor(CamelContext camelContext, LivePriceImporter livePriceImporter) {
		super(camelContext, PROCESSOR_NAME, "importData");
		this.livePriceImporter = livePriceImporter;
	}
	
	public List<LiveStockPrice> importData(@Body Set<String> codes) throws Exception{
		return livePriceImporter.importData(codes);
	}
}