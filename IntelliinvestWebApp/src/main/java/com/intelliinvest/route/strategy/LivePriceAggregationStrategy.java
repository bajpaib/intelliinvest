package com.intelliinvest.route.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.model.LiveStockPrice;

@Component
public class LivePriceAggregationStrategy implements AggregationStrategy {
	
	@SuppressWarnings("unchecked")
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		if(Boolean.valueOf(newExchange.getProperty(Exchange.ERRORHANDLER_HANDLED, Boolean.FALSE, Boolean.class))){
			newExchange.getIn().setBody(new ArrayList<LiveStockPrice>());
			newExchange.setProperty(Exchange.ERRORHANDLER_HANDLED, false);
			newExchange.setProperty(Exchange.EXCEPTION_CAUGHT, "");
		}
        if (oldExchange == null) {
            return newExchange;
        }
        List<LiveStockPrice> existingLiveStockPrices = oldExchange.getIn().getBody(List.class);
        List<LiveStockPrice> newLiveStockPrices = newExchange.getIn().getBody(List.class);
        existingLiveStockPrices.addAll(newLiveStockPrices);
        oldExchange.getIn().setBody(existingLiveStockPrices);
        return oldExchange;
    } 
 
}