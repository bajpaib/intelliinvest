package com.intelliinvest.common;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

public class IntelliInvestStore {
	private static Logger logger = Logger.getLogger(IntelliInvestStore.class);
	public static Map<String, String> QUANDL_STOCK_CODES_MAPPING = new ConcurrentHashMap<String, String>();
	public static Properties properties = null;

	@PostConstruct
	public void init() {
		loadProperties();
	}

	private void loadProperties() {
		try {
			logger.info("Loading property file for intelliinvest");
			properties = new Properties();
			properties.load(IntelliInvestStore.class.getResourceAsStream("/intelliinvest.properties"));
			logger.info("Loaded property file " + properties);
			if (QUANDL_STOCK_CODES_MAPPING.size() == 0) {
				String mapping_str = properties.getProperty("quandl_special_chracteres_mapping");
				String mappings[] = mapping_str.split("#");
				for (String mapping : mappings) {
					String[] key_value = mapping.split(":");
					if (key_value.length > 1)
						QUANDL_STOCK_CODES_MAPPING.put(key_value[0], key_value[1]);
					else
						QUANDL_STOCK_CODES_MAPPING.put(key_value[0], "");
				}
				logger.debug(QUANDL_STOCK_CODES_MAPPING.toString());
			}
		} catch (Exception e) {
			logger.error("Error loading properties  " + e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
