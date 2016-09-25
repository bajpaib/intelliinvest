package com.intelliinvest.data.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.data.model.StockFundamentals;

@ManagedResource(objectName = "bean:name=StockFundamentalsRepository", description = "StockFundamentalsRepository")
public class StockFundamentalsRepository {
	private static Logger logger = Logger.getLogger(StockFundamentalsRepository.class);
	private static final String COLLECTION_STOCK_FUNDAMENTALS_HISTORY = "STOCK_FUNDAMENTALS_HISTORY";
	@Autowired
	private MongoTemplate mongoTemplate;

	public StockFundamentals getStockFundamentalsByIdAndQuarterYear(String id, String quarterYear)
			throws DataAccessException {
		logger.debug("Inside getStockFundamentalsByIdAndQuarterYear()...");
		return mongoTemplate.findOne(
				Query.query(Criteria.where("quarterYear").is(quarterYear).and("securityId").is(id)),
				StockFundamentals.class, COLLECTION_STOCK_FUNDAMENTALS_HISTORY);
	}
	
	public List<StockFundamentals> getStockFundamentalsById(String id)
			throws DataAccessException {
		logger.debug("Inside getStockFundamentalsById()...");
		Query query = new Query();
		query.with(new Sort(Sort.Direction.DESC, "quarterYear"));
		query.addCriteria(Criteria.where("securityId").is(id));
		return mongoTemplate.find(query, StockFundamentals.class, COLLECTION_STOCK_FUNDAMENTALS_HISTORY);
	}

	public void bulkUploadStockFundamentals(List<StockFundamentals> stockFundamentals) {
		logger.info("Inside bulkUploadStockFundamentals()...");		
		// delete all existing records
		mongoTemplate.remove(new Query(), StockFundamentals.class, COLLECTION_STOCK_FUNDAMENTALS_HISTORY);		
		// batch inserts
		int start = -1000;
		int end = 0;
		while (end < stockFundamentals.size()) {
			start = start + 1000;
			end = end + 1000;
			if (end > stockFundamentals.size()) {
				end = stockFundamentals.size();
			}
			List<StockFundamentals> stockFundamentalsTemp = stockFundamentals.subList(start, end);
			mongoTemplate.insert(stockFundamentalsTemp, COLLECTION_STOCK_FUNDAMENTALS_HISTORY);
		}
	}
}