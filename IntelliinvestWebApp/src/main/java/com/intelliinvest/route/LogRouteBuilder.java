package com.intelliinvest.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogRouteBuilder extends RouteBuilder{

	private static Logger LOGGER = LoggerFactory.getLogger(LogRouteBuilder.class);
	
	public static String LOG_DEBUG = "direct:logDebug";
	public static String LOG_INFO = "direct:logInfo";
	public static String LOG_ERROR = "direct:logError";
	
	@Override
	public void configure() throws Exception {
		from(LOG_DEBUG)
			.bean(LogRouteBuilder.class, "logDebug")
		.end();
		
		from(LOG_INFO)
			.bean(LogRouteBuilder.class, "logInfo")
		.end();
		
		from(LOG_ERROR)
			.bean(LogRouteBuilder.class, "logError")
		.end();
	}
	
	public static void logDebug(Exchange exchange){
		LOGGER.debug(exchange.getIn().getBody().toString(), exchange.getProperty("CamelExceptionCaught"));
	}
	
	public static void logInfo(Exchange exchange){
		LOGGER.info(exchange.getIn().getBody().toString(), exchange.getException());
	}
	
	public static void logError(Exchange exchange){
		LOGGER.error("Error occured during processing of exchange : {}", exchange.getProperty("CamelExceptionCaught"), exchange.getException());
	}
}

