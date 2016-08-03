package com.intelliinvest.web.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.intelliinvest.data.model.NSEtoBSEMap;
import com.intelliinvest.web.dao.StockRepository;

public class IntelliInvestStore {
	private static Logger logger = Logger.getLogger(IntelliInvestStore.class);
	@Autowired
	private StockRepository stockRepository;
	public static Map<String, String> QUANDL_STOCK_CODES_MAPPING = new ConcurrentHashMap<String, String>();
	public static Map<String, String> NSEToBSEMap = new ConcurrentHashMap<String, String>();
	public static Map<String, String> BSEToNSEMap = new ConcurrentHashMap<String, String>();
	public static Properties properties = null;

	@PostConstruct
	public void init() {
		loadProperties();
		initialiseCacheFromDB();
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

	public void initialiseCacheFromDB() {
		List<NSEtoBSEMap> nseToBseMap = stockRepository.getNSEtoBSEMap();
		if (nseToBseMap != null && !nseToBseMap.isEmpty()) {
			IntelliInvestStore.NSEToBSEMap = getMapFromList(nseToBseMap);
			IntelliInvestStore.BSEToNSEMap = getMapFromListReverse(nseToBseMap);
		}
		logger.info("Initialised IntelliInvestStore from DB");
	}

	private Map<String, String> getMapFromList(List<NSEtoBSEMap> datas) {
		Map<String, String> map = new HashMap<String, String>();
		for (NSEtoBSEMap data : datas) {
			map.put(data.getNseCode(), data.getBseCode());
		}
		return map;
	}

	private Map<String, String> getMapFromListReverse(List<NSEtoBSEMap> datas) {
		Map<String, String> map = new HashMap<String, String>();
		for (NSEtoBSEMap data : datas) {
			map.put(data.getBseCode(), data.getNseCode());
		}
		return map;
	}
}
