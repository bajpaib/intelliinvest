package com.intelliinvest.data.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private static final String COLLECTION_STOCK_FUNDAMENTALS = "STOCK_FUNDAMENTALS";
	@Autowired
	private MongoTemplate mongoTemplate;

	public List<StockFundamentals> getStockFundamentalsById(String id) throws DataAccessException {
		logger.debug("Inside getStockFundamentalsById()...");
		Query query = new Query();
		List<org.springframework.data.domain.Sort.Order> orders = new ArrayList<org.springframework.data.domain.Sort.Order>();
		orders.add(new org.springframework.data.domain.Sort.Order(Sort.Direction.ASC, "attrName"));
		query.with(new Sort(orders));
		query.addCriteria(Criteria.where("securityId").is(id));
		return mongoTemplate.find(query, StockFundamentals.class, COLLECTION_STOCK_FUNDAMENTALS);
	}

	public StockFundamentals getStockFundamentalsByIdAndAttrName(String id, String attrName)
			throws DataAccessException {
//		logger.debug("Inside getStockFundamentalsByIdAndAttrName()...");
		Query query = new Query();
		query.addCriteria(Criteria.where("securityId").is(id).and("attrName").is(attrName));
		return mongoTemplate.findOne(query, StockFundamentals.class, COLLECTION_STOCK_FUNDAMENTALS);
	}

	public Map<String, StockFundamentals> getStockFundamentalsByIdAndAttrName(List<String> ids, String attrName) throws DataAccessException {
		Map<String, StockFundamentals> retVal = new HashMap<String, StockFundamentals>();
//		logger.debug("Start: getStockFundamentalsByIdAndAttrName for attrName:"+attrName+" and ids:"+ids);
		// Batch query 100 stocks at a time
		int start = -100;
		int end = 0;
		while (end < ids.size()) {
			start = start + 100;
			end = end + 100;
			if (end > ids.size()) {
				end = ids.size();
			}
			List<String> idsTemp = ids.subList(start, end);
			Query query = new Query();
			query.addCriteria(Criteria.where("securityId").in(idsTemp).and("attrName").is(attrName));
			List<StockFundamentals> tempList = mongoTemplate.find(query, StockFundamentals.class,
					COLLECTION_STOCK_FUNDAMENTALS);

			for (StockFundamentals temp : tempList) {
				retVal.put(temp.getSecurityId(), temp);
			}
		}
//		logger.debug("End: getStockFundamentalsByIdAndAttrName for attrName:"+attrName+" and ids:"+ids);
		return retVal;
	}

	public void bulkUploadStockFundamentals(List<StockFundamentals> stockFundamentals, boolean firstCall) {
//		logger.info("Inside bulkUploadStockFundamentals()...");		
		// delete all existing records on firstCall only
		if(firstCall){
			mongoTemplate.remove(new Query(), StockFundamentals.class, COLLECTION_STOCK_FUNDAMENTALS);
		}
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
			mongoTemplate.insert(stockFundamentalsTemp, COLLECTION_STOCK_FUNDAMENTALS);
		}
	}

	public void updateStockFundamentals(List<StockFundamentals> stockFundamentals) {
		logger.debug("Inside updateStockFundamentals()...");
		for (StockFundamentals fundamental : stockFundamentals) {
			//first try to fetch the fundamental
			StockFundamentals temp = getStockFundamentalsByIdAndAttrName(fundamental.getSecurityId(), fundamental.getAttrName());
			if(temp==null){
				mongoTemplate.insert(fundamental, COLLECTION_STOCK_FUNDAMENTALS);
			}else {
				Map<String, String> map = fundamental.getYearQuarterAttrVal();
				for(Map.Entry<String, String> entry: map.entrySet()){
					temp.addYearQuarterAttrVal(entry.getKey(), entry.getValue());
					temp.setUpdateDate(fundamental.getUpdateDate());
				}
				mongoTemplate.save(temp, COLLECTION_STOCK_FUNDAMENTALS);
			}
		}
	}
}