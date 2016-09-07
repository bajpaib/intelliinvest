package com.intelliinvest.route.processor;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.importer.EODPriceImporter;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(EODStockPriceImporterProcessor.PROCESSOR_NAME)
public class EODStockPriceImporterProcessor extends IntelliInvestProcessor{
	
	public static final String PROCESSOR_NAME = "EODStockPriceImporterProcessor";
	private final EODPriceImporter eodPriceImporter;
	
	@Autowired
	public EODStockPriceImporterProcessor(CamelContext camelContext, EODPriceImporter eodPriceImporter) {
		super(camelContext, PROCESSOR_NAME, "importData");
		this.eodPriceImporter = eodPriceImporter;
	}
	
	public String importData(@Body String code, @Header("startDate") String startDate, @Header("endDate") String endDate) throws Exception{
		return eodPriceImporter.importData(code, startDate, endDate);
	}
}