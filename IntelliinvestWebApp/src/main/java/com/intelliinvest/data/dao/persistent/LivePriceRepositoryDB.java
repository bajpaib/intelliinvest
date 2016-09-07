package com.intelliinvest.data.dao.persistent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.LivePriceRepository;
import com.intelliinvest.data.model.LiveStockPrice;

@Component("livePriceRepositoryDB")
class LivePriceRepositoryDB implements LivePriceRepository{
	private static Logger LOGGER = LoggerFactory.getLogger(LivePriceRepositoryDB.class);
	private final MongoTemplate mongoTemplate;
	
	@Autowired
	public LivePriceRepositoryDB(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	public LiveStockPrice getLiveStockPrice(String symbol){
		Query query = new Query();
		query.addCriteria(Criteria.where("symbol").is(symbol));
		return mongoTemplate.findOne(query, LiveStockPrice.class, LiveStockPrice.COLLECTION_NAME);
	}

	public List<LiveStockPrice> getLiveStockPrices(){	
		return mongoTemplate.findAll(LiveStockPrice.class, LiveStockPrice.COLLECTION_NAME);
	}

	public void updateLiveStockPrices(List<LiveStockPrice> liveStockPrices) {
		LOGGER.info("Inside updateLiveStockPrices()");
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, LiveStockPrice.class);
//		for (LiveStockPrice liveStockPrice : liveStockPrices) {
//			Query query = new Query();
//			query.addCriteria(Criteria.where("symbol").is(liveStockPrice.getCode()));
//			Update update = new Update();
//			update.set("price", liveStockPrice.getPrice());
//			update.set("changePercent", liveStockPrice.getChangePercent());
//			update.set("lastTraded", liveStockPrice.getLastTraded());
//			update.set("lastUpdated", liveStockPrice.getLastUpdated());
//		}
		operation.remove(new Query());
		operation.insert(liveStockPrices);
		operation.execute();
	}
	
}
