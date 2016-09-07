package com.intelliinvest.route.processor;

import java.text.SimpleDateFormat;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.IntelliInvestProcessor;

@Component(PopulateEODDatesProcessor.PROCESSOR_NAME)
public class PopulateEODDatesProcessor extends IntelliInvestProcessor{
	
	public static final String PROCESSOR_NAME = "PopulateEODDatesProcessor";
	
	@Autowired
	public PopulateEODDatesProcessor(CamelContext camelContext) {
		super(camelContext, PROCESSOR_NAME, "populateEODDates");
	}
	
	public void populateEODDates(Exchange exchange) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		exchange.setProperty("startDate", dateFormat.format(DateUtil.getLocalDate()));
		exchange.setProperty("endDate", dateFormat.format(DateUtil.getLocalDate()));
		exchange.setProperty("startDate", "2016-09-02");
		exchange.setProperty("endDate", "2016-09-02");
	}
}