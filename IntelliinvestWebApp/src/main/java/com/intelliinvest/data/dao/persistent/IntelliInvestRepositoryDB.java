package com.intelliinvest.data.dao.persistent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.IntelliInvestRepository;
import com.intelliinvest.data.model.NSEtoBSEMapping;

@Component("intelliInvestRepository")
class IntelliInvestRepositoryDB implements IntelliInvestRepository {
	private static Logger LOGGER = LoggerFactory.getLogger(IntelliInvestRepositoryDB.class);
	private final MongoTemplate mongoTemplate;
	private Map<String, String> nse2bseMap = new ConcurrentHashMap<String, String>();
	private Map<String, String> bse2nseMap = new ConcurrentHashMap<String, String>();

	@Autowired
	public IntelliInvestRepositoryDB(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		init();
	}
	
	public void init(){
		LOGGER.debug("Inside getNSEtoBSEMap()...");
		List<NSEtoBSEMapping> mappings = mongoTemplate.findAll(NSEtoBSEMapping.class, NSEtoBSEMapping.COLLECTION_NAME);
		Map<String, String> nse2bseMap = new ConcurrentHashMap<String, String>();
		Map<String, String> bse2nseMap = new ConcurrentHashMap<String, String>();
		for (NSEtoBSEMapping mapping : mappings) {
			nse2bseMap.put(mapping.getNseCode(), mapping.getBseCode());
			bse2nseMap.put(mapping.getBseCode(), mapping.getNseCode());
		}
		this.nse2bseMap = nse2bseMap;
		this.bse2nseMap = bse2nseMap;
	}

	public String getBSECode(String nseCode) {
		return nse2bseMap.get(nseCode);
	}
	
	public String getNSECode(String bseCode) {
		return bse2nseMap.get(bseCode);
	}
	
}
