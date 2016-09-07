package com.intelliinvest.route.strategy;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.EODStockPrice;

@Component
public class EODBackloadPriceAggregationStrategy implements AggregationStrategy {
	
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		if(Boolean.valueOf(newExchange.getProperty(Exchange.ERRORHANDLER_HANDLED, Boolean.FALSE, Boolean.class))){
			newExchange.getIn().setBody(new ArrayList<EODStockPrice>());
			newExchange.setProperty(Exchange.ERRORHANDLER_HANDLED, false);
			newExchange.setProperty(Exchange.EXCEPTION_CAUGHT, "");
		}
        if (oldExchange == null) {
            return newExchange;
        }else{
        	return newExchange;
        }
    } 

}