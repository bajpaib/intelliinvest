package com.intelliinvest.data.dao.persistent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.intelliinvest.common.exception.IntelliInvestException;
import com.intelliinvest.data.model.SequenceId;

@Component("sequenceRepository")
public class SequenceRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(SequenceRepository.class);
	private static final String COLLECTION_SEQUENCE = "SEQUENCE";
	private final MongoTemplate mongoTemplate;

	@Autowired
	public SequenceRepository(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	public String getNextSequenceId(String key) throws IntelliInvestException {
		Query query = new Query(Criteria.where("_id").is(key));
		Update update = new Update();
		update.inc("seq", 1);
		SequenceId seqId = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true),
				SequenceId.class, COLLECTION_SEQUENCE);
		if (seqId == null) {
			LOGGER.error("Unable to get sequence Id for key " + key);
			throw new IntelliInvestException("Unable to get sequence Id for key " + key);
		}
		return new Long(seqId.getSeq()).toString();
	}
}