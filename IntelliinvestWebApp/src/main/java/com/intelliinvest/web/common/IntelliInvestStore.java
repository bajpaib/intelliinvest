package com.intelliinvest.web.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.intelliinvest.data.model.NSEtoBSEMap;

public class IntelliInvestStore {
	private static Logger logger = Logger.getLogger(IntelliInvestStore.class);
	@Autowired
	private MongoTemplate mongoTemplate;

	private static final String COLLECTION_NSE_BSE_CODES = "NSE_BSE_CODES";
	public static Map<String, String> QUANDL_STOCK_CODES_MAPPING = new ConcurrentHashMap<String, String>();
	private static Map<String, String> NSEToBSEMap = new ConcurrentHashMap<String, String>();
	private static Map<String, String> BSEToNSEMap = new ConcurrentHashMap<String, String>();
	
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

	private void initialiseCacheFromDB() {
		List<NSEtoBSEMap> nseToBseMap = getNSEtoBSEMapFromDB();
		if (nseToBseMap != null && !nseToBseMap.isEmpty()) {
			IntelliInvestStore.NSEToBSEMap = getMapFromList(nseToBseMap);
			IntelliInvestStore.BSEToNSEMap = getReverseMapFromList(nseToBseMap);
			logger.info("Initialised nseToBseMap and bseToNseMap from DB with size "+NSEToBSEMap.size());
		} else {
			logger.error("Could not Initialised nseToBseMap from DB. NSE_BSE_CODES is empty ");
		}
	}

	private List<NSEtoBSEMap> getNSEtoBSEMapFromDB() throws DataAccessException {
		logger.debug("Inside getNSEtoBSEMap()...");
		return mongoTemplate.findAll(NSEtoBSEMap.class, COLLECTION_NSE_BSE_CODES);
	}

	private Map<String, String> getMapFromList(List<NSEtoBSEMap> datas) {
		Map<String, String> map = new HashMap<String, String>();
		for (NSEtoBSEMap data : datas) {
			map.put(data.getNseCode(), data.getBseCode());
		}
		return map;
	}
	
	private Map<String, String> getReverseMapFromList(List<NSEtoBSEMap> datas) {
		Map<String, String> map = new HashMap<String, String>();
		for (NSEtoBSEMap data : datas) {
			map.put(data.getBseCode(), data.getNseCode());
		}
		return map;
	}

	public String getBSECode(String nseCode) {
		return NSEToBSEMap.get(nseCode);
	}
	
	public String getNSECode(String bseCode) {
		return BSEToNSEMap.get(bseCode);
	}
}
