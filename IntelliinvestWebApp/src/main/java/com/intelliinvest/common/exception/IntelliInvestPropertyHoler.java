package com.intelliinvest.common.exception;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntelliInvestPropertyHoler {

	private static Logger LOGGER = LoggerFactory.getLogger(IntelliInvestPropertyHoler.class);
	private static Properties properties = new Properties();
	private static Map<String, String> quandlStockMapping = new ConcurrentHashMap<String, String>();
	static{
		try {
			properties.load(IntelliInvestPropertyHoler.class.getResourceAsStream("/intelliinvest.properties"));
			String mapping_str = properties.getProperty("quandl_special_chracteres_mapping");
			String mappings[] = mapping_str.split("#");
			for (String mapping : mappings) {
				String[] key_value = mapping.split(":");
				if (key_value.length > 1)
					quandlStockMapping.put(key_value[0], key_value[1]);
				else
					quandlStockMapping.put(key_value[0], "");
			}
		} catch (IOException e) {
			LOGGER.error("Error loading itelliinvest.properties", e);
			throw new IntelliInvestException("Error loading itelliinvest.properties", e);
		}
	}
	
	public static String getProperty(String propertyName){
		return properties.getProperty(propertyName);
	}
	
	public static String getProperty(String propertyName, String defaultValue){
		return properties.getProperty(propertyName, defaultValue);
	}
	
	public static String getQuandlStockCode(String code) {
		for(Entry<String, String> entry : quandlStockMapping.entrySet()){
			code = code.replaceAll(entry.getKey(), entry.getValue());
		}
		return code;
	}

}
