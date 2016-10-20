package com.intelliinvest.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.data.model.MagicNumberData;
import com.intelliinvest.util.Helper;

@ManagedResource(objectName = "bean:name=MagicNumberRepository", description = "MagicNumberRepository")
public class MagicNumberRepository {
	private static Logger logger = Logger.getLogger(MagicNumberRepository.class);
	private static final String COLLECTION_MAGIC_NUMBER_DATA = "MAGIC_NUMBER_DATA";
	@Autowired
	private MongoTemplate mongoTemplate;
	private Map<String, MagicNumberData> magicNumberCache = new ConcurrentHashMap<String, MagicNumberData>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<MagicNumberData> magicNumbers = getMagicNumbersFromDB();
		if (Helper.isNotNullAndNonEmpty(magicNumbers)) {
			for (MagicNumberData magicNumberData : magicNumbers) {

				magicNumberCache.put(magicNumberData.getSecurityId(), magicNumberData);
			}
			logger.info(
					"Initialised magic numbers in MagicNumberRepository from DB with size " + magicNumberCache.size());
		} else {
			logger.error(
					"Could not initialise magicNumberCache from DB in MagicNumberRepository. MagicNumbers are empty.");
		}
	}

	public MagicNumberData getMagicNumber(String securityId) {
		return magicNumberCache.get(securityId);
	}

	public List<MagicNumberData> getMagicNumbers() {
		return new ArrayList<MagicNumberData>(magicNumberCache.values());
	}

	public List<MagicNumberData> getMagicNumbersFromDB() {
		logger.debug("Inside getMagicNumbersFromDB()...");
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "securityId"));
		return mongoTemplate.find(query, MagicNumberData.class, COLLECTION_MAGIC_NUMBER_DATA);
	}

	public void updateMagicNumber(MagicNumberData magicNumberData) {
		Query query = new Query();
		query.addCriteria(Criteria.where("securityId").is(magicNumberData.getSecurityId()).and("movingAverage")
				.is(magicNumberData.getMovingAverage()));
		Update update = new Update();
		update.set("magicNumberADX", magicNumberData.getMagicNumberADX());
		update.set("magicNumberBollinger", magicNumberData.getMagicNumberBollinger());
		update.set("magicNumberOscillator", magicNumberData.getMagicNumberOscillator());
		update.set("pnlADX", magicNumberData.getPnlADX());
		update.set("pnlBollinger", magicNumberData.getPnlBollinger());
		update.set("pnlOscillator", magicNumberData.getPnlOscillator());
		mongoTemplate.updateFirst(query, update, MagicNumberData.class);
		magicNumberCache.put(magicNumberData.getSecurityId(), magicNumberData);
	}

	public void updateMagicNumbers(List<MagicNumberData> magicNumberDatas) {
		Map<String, MagicNumberData> tmpMap = new ConcurrentHashMap<String, MagicNumberData>();
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, MagicNumberData.class);
		for (MagicNumberData magicNumberData : magicNumberDatas) {
			Query query = new Query();
			query.addCriteria(Criteria.where("securityId").is(magicNumberData.getSecurityId()).and("movingAverage")
					.is(magicNumberData.getMovingAverage()));
			Update update = new Update();
			update.set("magicNumberADX", magicNumberData.getMagicNumberADX());
			update.set("magicNumberBollinger", magicNumberData.getMagicNumberBollinger());
			update.set("magicNumberOscillator", magicNumberData.getMagicNumberOscillator());
			update.set("pnlADX", magicNumberData.getPnlADX());
			update.set("pnlBollinger", magicNumberData.getPnlBollinger());
			update.set("pnlOscillator", magicNumberData.getPnlOscillator());
			operation.upsert(query, update);
			tmpMap.put(magicNumberData.getSecurityId(), magicNumberData);
		}
		operation.execute();
		magicNumberCache.putAll(tmpMap);
	}
}