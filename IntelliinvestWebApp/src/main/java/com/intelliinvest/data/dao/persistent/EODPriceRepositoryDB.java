package com.intelliinvest.data.dao.persistent;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.intelliinvest.common.exception.IntelliInvestException;
import com.intelliinvest.data.dao.EODPriceRepository;
import com.intelliinvest.data.model.EODStockPrice;
import com.intelliinvest.util.DateUtil;

@Component("eodPriceRepositoryDB")
class EODPriceRepositoryDB implements EODPriceRepository{
	private static Logger LOGGER = LoggerFactory.getLogger(EODPriceRepositoryDB.class);

	protected final MongoTemplate mongoTemplate;

	@Autowired
	public EODPriceRepositoryDB(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	public EODPriceRepository getUnderlyingEODRepository() {
		return null;
	}

	public EODStockPrice getEODStockPrice(String exchange, String symbol){
		Query query = new Query();
		query.limit(1);
		query.with(new Sort(Sort.Direction.DESC, "eodDate"));
		query.addCriteria(Criteria.where("exchange").is(exchange).and("symbol").is(symbol));
		return mongoTemplate.findOne(query, EODStockPrice.class, EODStockPrice.COLLECTION_NAME);
	}
	
	public List<EODStockPrice> getEODStockPrices(String exchange){
		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "eodDate"),
				group("exchange", "symbol").first("eodDate").as("eodDate"))
						.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<EODStockPrice> results = mongoTemplate.aggregate(aggregation,
				EODStockPrice.COLLECTION_NAME, EODStockPrice.class);
		List<EODStockPrice> eodStockPrices = new ArrayList<EODStockPrice>();
		for(EODStockPrice eodStockPrice : results.getMappedResults() ){
			eodStockPrices.add(getEODStockPrice(eodStockPrice.getExchange(), eodStockPrice.getSymbol(), eodStockPrice.getEodDate()));
		}
		return eodStockPrices;
	}
	
	private EODStockPrice getEODStockPrice(String exchange, String symbol, LocalDate eodDate){
		Query query = new Query();
		query.addCriteria(Criteria.where("exchange").is(exchange).and("symbol").is(symbol).and("eodDate").is(eodDate));
		return mongoTemplate.findOne(query, EODStockPrice.class, EODStockPrice.COLLECTION_NAME);
	}
	
	public void updateEODStockPrices(Collection<EODStockPrice> eodStockPrices) throws IntelliInvestException {
		LOGGER.info("Inside updateEODStockPrices()...");
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, EODStockPrice.class);
		LocalDateTime currentDate = DateUtil.getLocalDateTime();
		for (EODStockPrice eodStockPrice : eodStockPrices) {
			Query query = new Query();
			query.addCriteria(Criteria.where("exchange").is(eodStockPrice.getExchange()).and("symbol").is(eodStockPrice.getSymbol())
					.and("eodDate").is(eodStockPrice.getEodDate()));
			Update update = new Update();
			update.set("exchange", eodStockPrice.getExchange());
			update.set("symbol", eodStockPrice.getSymbol());
			update.set("series", eodStockPrice.getSeries());
			update.set("open", eodStockPrice.getOpen());
			update.set("high", eodStockPrice.getHigh());
			update.set("low", eodStockPrice.getLow());
			update.set("close", eodStockPrice.getClose());
			update.set("last", eodStockPrice.getLast());
			update.set("tottrdqty", eodStockPrice.getTottrdqty());
			update.set("tottrdval", eodStockPrice.getTottrdval());
			update.set("eodDate", eodStockPrice.getEodDate());
			update.set("updateDate", currentDate);
			operation.upsert(query, update);
		}
		operation.execute();
	}

}
