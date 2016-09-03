package com.intelliinvest.data.dao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.model.SequenceId;

public class SequenceRepository {

	private static Logger logger = Logger.getLogger(SequenceRepository.class);
	private static final String COLLECTION_SEQUENCE = "SEQUENCE";
	@Autowired
	private MongoTemplate mongoTemplate;

	public String getNextSequenceId(String key) throws IntelliinvestException {
		Query query = new Query(Criteria.where("_id").is(key));
		Update update = new Update();
		update.inc("seq", 1);
		SequenceId seqId = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true),
				SequenceId.class, COLLECTION_SEQUENCE);
		if (seqId == null) {
			logger.error("Unable to get sequence Id for key " + key);
			throw new IntelliinvestException("Unable to get sequence Id for key " + key);
		}
		return new Long(seqId.getSeq()).toString();
	}
}