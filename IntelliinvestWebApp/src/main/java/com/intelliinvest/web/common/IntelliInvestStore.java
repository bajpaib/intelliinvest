package com.intelliinvest.web.common;

import java.util.Properties;

import org.apache.log4j.Logger;



public class IntelliInvestStore {
	
	private static Logger logger = Logger.getLogger(IntelliInvestStore.class);
		
	public static Properties properties = null;
	
	static{
		loadProperties();
	}
	
	
	private static void loadProperties() {
		try{
			logger.info("Loading property file for intelliinvest");
			properties = new Properties();
			properties.load(IntelliInvestStore.class.getResourceAsStream("/intelliinvest.properties"));
			logger.info("Loaded property file " + properties);
		}catch(Exception e){
			logger.info("Error loading properties  " + e.getMessage());
			throw new RuntimeException(e);
		}
		
	}	
}
