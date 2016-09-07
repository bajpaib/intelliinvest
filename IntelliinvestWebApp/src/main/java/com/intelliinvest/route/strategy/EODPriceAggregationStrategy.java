package com.intelliinvest.route.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.EODStockPrice;

@Component
public class EODPriceAggregationStrategy implements AggregationStrategy {
	
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		if(Boolean.valueOf(newExchange.getProperty(Exchange.ERRORHANDLER_HANDLED, Boolean.FALSE, Boolean.class))){
			newExchange.getIn().setBody(new ArrayList<EODStockPrice>());
			newExchange.setProperty(Exchange.ERRORHANDLER_HANDLED, false);
			newExchange.setProperty(Exchange.EXCEPTION_CAUGHT, "");
		}
    	List<EODStockPrice> quandlStockPrices = getAsList(newExchange.getIn().getBody());
        if (oldExchange == null) {
        	newExchange.getIn().setBody(quandlStockPrices);
            return newExchange;
        }
        List<EODStockPrice> existingEODStockPrices = getAsList(oldExchange.getIn().getBody());
        existingEODStockPrices.addAll(quandlStockPrices);
        oldExchange.getIn().setBody(existingEODStockPrices);
        return oldExchange;
    } 
    
    @SuppressWarnings("unchecked")
	private List<EODStockPrice> getAsList(Object obj){
    	List<EODStockPrice> quandlStockPrices = new ArrayList<EODStockPrice>();
    	if(obj instanceof List){
    		quandlStockPrices.addAll((List<EODStockPrice>)obj);
    	}else{
    		quandlStockPrices.add((EODStockPrice)obj);
    	}
    	return quandlStockPrices;
    }

}