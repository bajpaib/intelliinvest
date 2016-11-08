package com.intelliinvest.data.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;

@ManagedResource(objectName = "bean:name=QuandlEODStockPriceRepository", description = "QuandlEODStockPriceRepository")
public class QuandlEODStockPriceRepository {
	private static Logger logger = Logger.getLogger(QuandlEODStockPriceRepository.class);
	private static final String COLLECTION_QUANDL_STOCK_PRICE = "QUANDL_STOCK_PRICE";

	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private DateUtil dateUtil;
	private Map<String, QuandlStockPrice> priceCache = new ConcurrentHashMap<String, QuandlStockPrice>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<QuandlStockPrice> prices = getLatestStockPricesFromDB();
		if (Helper.isNotNullAndNonEmpty(prices)) {
			for (QuandlStockPrice price : prices) {
				priceCache.put(price.getSecurityId(), price);
			}
			logger.info("Initialised priceCache from DB in QuandlStockPriceRepository with size " + priceCache.size());
		} else {
			logger.error(
					"Could not initialise priceCache from DB in QuandlStockPriceRepository. QUANDL_STOCK_PRICE is empty.");
		}
	}

	public QuandlStockPrice getLatestEODStockPrice(String id) {
		QuandlStockPrice price = priceCache.get(id);
		if (price == null) {
			logger.error("Inside getEODStockPrice() QuandlStockPrice not found in cache for " + id);
			return null;
		}
		return price.clone();
	}

	public Map<String, QuandlStockPrice> getLatestEODStockPrices() throws Exception {
		Map<String, QuandlStockPrice> retVal = new HashMap<String, QuandlStockPrice>();
		for (Map.Entry<String, QuandlStockPrice> entry : priceCache.entrySet()) {
			retVal.put(entry.getKey(), entry.getValue().clone());
		}
		return retVal;
	}

	public QuandlStockPrice getStockPriceFromDB(String id, LocalDate eodDate) throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("eodDate").is(eodDate).and("securityId").is(id));
		return mongoTemplate.findOne(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
	}
	
	public Map<String, QuandlStockPrice> getStockPricesFromDB(List<String> ids, LocalDate eodDate) throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("eodDate").is(eodDate).and("securityId").in(ids));
		List<QuandlStockPrice> tempList =  mongoTemplate.find(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
		Map<String, QuandlStockPrice> retVal = new HashMap<String, QuandlStockPrice>();
		for (QuandlStockPrice temp : tempList) {
			retVal.put(temp.getSecurityId(), temp);
		}
		return retVal;
	}

	public List<QuandlStockPrice> getStockPricesFromDB(String id, LocalDate startDate, LocalDate endDate)
			throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("eodDate").gte(startDate).lte(endDate).and("securityId").is(id));
		return mongoTemplate.find(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
	}

	public List<QuandlStockPrice> getStockPricesFromDB(LocalDate eodDate) throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("eodDate").is(eodDate));
		return mongoTemplate.find(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
	}

	public Map<String, QuandlStockPrice> getEODStockPrices(LocalDate date) throws Exception {
		Map<String, QuandlStockPrice> retVal = new HashMap<String, QuandlStockPrice>();
		List<QuandlStockPrice> prices = getStockPricesFromDB(date);
		for (QuandlStockPrice price : prices) {
			retVal.put(price.getSecurityId(), price.clone());
		}
		return retVal;
	}

	public List<QuandlStockPrice> getLatestStockPricesFromDB() throws DataAccessException {
		logger.info("Inside getLatestStockPrices()...");
		// retrieve record having max eodDate for each stock
		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "eodDate"),
				group("securityId").first("securityId").as("securityId").first("eodDate").as("eodDate")
						.first("exchange").as("exchange").first("series").as("series").first("open").as("open")
						.first("high").as("high").first("low").as("low").first("close").as("close").first("last")
						.as("last").first("wap").as("wap").first("tradedQty").as("tradedQty").first("turnover")
						.as("turnover").first("updateDate").as("updateDate"))
								.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<QuandlStockPrice> results = mongoTemplate.aggregate(aggregation,
				COLLECTION_QUANDL_STOCK_PRICE, QuandlStockPrice.class);
		List<QuandlStockPrice> retVal = new ArrayList<QuandlStockPrice>();

		for (QuandlStockPrice price : results.getMappedResults()) {
			retVal.add(price);
		}
		return retVal;
	}

	public Map<String, List<QuandlStockPrice>> getEODStockPricesFromStartDate(LocalDate startDate){
		logger.info("Inside getEODStockPricesFromStartDate()..." + startDate);
		Map<String, List<QuandlStockPrice>> retVal = new HashMap<String, List<QuandlStockPrice>>();

		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "eodDate"));
		query.addCriteria(Criteria.where("eodDate").gte(startDate));
		List<QuandlStockPrice> prices = mongoTemplate.find(query, QuandlStockPrice.class,
				COLLECTION_QUANDL_STOCK_PRICE);

		for (QuandlStockPrice price : prices) {
			String securityId = price.getSecurityId();
			List<QuandlStockPrice> stocksPricesList = retVal.get(securityId);
			if (stocksPricesList == null) {
				stocksPricesList = new ArrayList<QuandlStockPrice>();
				retVal.put(securityId, stocksPricesList);
			}
			stocksPricesList.add(price);			
		}
		return retVal;
	}

	public List<QuandlStockPrice> getStockPricesFromDB(String id)
			throws DataAccessException {
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "eodDate"));
		query.addCriteria(Criteria.where("securityId").is(id));
		return mongoTemplate.find(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
	}
	
	public void updateEODStockPrices(List<QuandlStockPrice> quandlPrices) throws IntelliinvestException {
		logger.info("Inside updateQuandlStockPrices()...");
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, QuandlStockPrice.class);

		// Batch inserts
		int start = -1000;
		int end = 0;
		while (end < quandlPrices.size()) {
			start = start + 1000;
			end = end + 1000;
			if (end > quandlPrices.size()) {
				end = quandlPrices.size();
			}
			List<QuandlStockPrice> quandlPricesTemp = quandlPrices.subList(start, end);

			for (QuandlStockPrice price : quandlPricesTemp) {
				Query query = new Query();
				query.addCriteria(Criteria.where("eodDate").is(dateUtil.getDateFromLocalDate(price.getEodDate()))
						.and("securityId").is(price.getSecurityId()));
				Update update = new Update();
				update.set("securityId", price.getSecurityId());
				update.set("exchange", price.getExchange());
				update.set("series", price.getSeries());
				update.set("open", price.getOpen());
				update.set("high", price.getHigh());
				update.set("low", price.getLow());
				update.set("close", price.getClose());
				update.set("last", price.getLast());
				update.set("wap", price.getWap());
				update.set("tradedQty", price.getTradedQty());
				update.set("turnover", price.getTurnover());
				update.set("eodDate", dateUtil.getDateFromLocalDate(price.getEodDate()));
				update.set("updateDate", dateUtil.getDateFromLocalDateTime(price.getUpdateDate()));
				operation.upsert(query, update);
			}
			operation.execute();
		}
	}

	public void updateCache(List<QuandlStockPrice> quandlPrices) {
		// update priceCache with latest price
		for (QuandlStockPrice price : quandlPrices) {
			priceCache.put(price.getSecurityId(), price);
		}
	}

	@ManagedOperation(description = "getEODStockPriceFromCache")
	public String getEODStockPriceFromCache(String id) {
		try {
			QuandlStockPrice price = getLatestEODStockPrice(id);
			if (price != null) {
				return price.toString();
			} else {
				return "Price not found";
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@ManagedOperation(description = "dumpEODPriceCache")
	public String dumpEODPriceCache() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, QuandlStockPrice> entry : priceCache.entrySet()) {
			builder.append(entry.getKey().toString());
			builder.append("=");
			builder.append(entry.getValue().toString());
			builder.append("\n");
		}
		return builder.toString();
	}
	
	public Map<String, List<QuandlStockPrice>> getEODStockPrices() {
		logger.info("Inside getEODStockPrices()...");
		Map<String, List<QuandlStockPrice>> retVal = new HashMap<String, List<QuandlStockPrice>>();
		try {
			Query query = new Query();
			query.with(new Sort(Sort.Direction.ASC, "eodDate"));
			List<QuandlStockPrice> prices = mongoTemplate.find(query,
					QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);

			for (QuandlStockPrice price : prices) {
				String securityId = price.getSecurityId();
				List<QuandlStockPrice> stocksPricesList = retVal
						.get(securityId);
				if (stocksPricesList == null) {
					stocksPricesList = new ArrayList<QuandlStockPrice>();
					retVal.put(securityId, stocksPricesList);
				}
				stocksPricesList.add(price);
			}
		} catch (Exception e) {
			logger.info("Exception while getting complete stock prices..."
					+ e.getMessage());

		}
		return retVal;
	}
	
	public List<QuandlStockPrice> getStockPricesFromStartDate(String id,
			LocalDate date) throws DataAccessException {
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "eodDate"));
		query.addCriteria(Criteria.where("securityId").is(id).and("eodDate")
				.gte(date));
		return mongoTemplate.find(query, QuandlStockPrice.class,
				COLLECTION_QUANDL_STOCK_PRICE);
	}
}