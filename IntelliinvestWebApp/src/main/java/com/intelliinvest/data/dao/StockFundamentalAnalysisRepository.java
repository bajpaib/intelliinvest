package com.intelliinvest.data.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.time.LocalDate;
import java.util.ArrayList;
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

import com.intelliinvest.data.model.StockFundamentalAnalysis;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;

@ManagedResource(objectName = "bean:name=StockFundamentalAnalysisRepository", description = "StockFundamentalAnalysisRepository")
public class StockFundamentalAnalysisRepository {
	private static Logger logger = Logger.getLogger(StockFundamentalAnalysisRepository.class);
	private static final String COLLECTION_STOCK_FUNDAMENTAL_ANALYSIS = "STOCK_FUNDAMENTAL_ANALYSIS";
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private DateUtil dateUtil;
	private Map<String, StockFundamentalAnalysis> stockFundamentalAnalysisCache = new ConcurrentHashMap<String, StockFundamentalAnalysis>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<StockFundamentalAnalysis> stockFundamentalAnalysisList = getLatestStockFundamentalAnalysisFromDB();
		if (Helper.isNotNullAndNonEmpty(stockFundamentalAnalysisList)) {
			for (StockFundamentalAnalysis stockFundamentalAnalysis : stockFundamentalAnalysisList) {
				stockFundamentalAnalysisCache.put(stockFundamentalAnalysis.getSecurityId(),
						stockFundamentalAnalysis);
			}
			logger.info(
					"Initialised stockFundamentalAnalysisCache from DB in StockFundamentalAnalysisRepository with size "
							+ stockFundamentalAnalysisCache.size());
		} else {
			logger.error(
					"Could not initialise stockFundamentalAnalysisCache from DB in StockFundamentalAnalysisRepository. STOCK_FUNDAMENTALS_ANALYSIS is empty.");
		}
	}

	public StockFundamentalAnalysis getLatestStockFundamentalAnalysis(String id) {
		StockFundamentalAnalysis stockFundamentalAnalysis = stockFundamentalAnalysisCache.get(id);
		if (stockFundamentalAnalysis == null) {
			logger.error(
					"Inside getStockFundamentalAnalysis() StockFundamentalAnalysis not found in cache for " + id);
			return null;
		}
		return stockFundamentalAnalysis.clone();
	}

	public List<StockFundamentalAnalysis> getLatestStockFundamentalAnalysisFromDB() throws DataAccessException {
		logger.info("Inside getLatestStockFundamentalAnalysisFromDB()...");
		// retrieve record having max yearQuarter for each stock
		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "todayDate"),
				group("securityId").first("securityId").as("securityId").first("yearQuarter").as("yearQuarter")
						.first("alEPSPct").as("alEPSPct").first("alCashToDebtRatio").as("alCashToDebtRatio")
						.first("alCurrentRatio").as("alCurrentRatio").first("alEquityToAssetRatio")
						.as("alEquityToAssetRatio").first("alDebtToCapitalRatio").as("alDebtToCapitalRatio")
						.first("alLeveredBeta").as("alLeveredBeta").first("alReturnOnEquity").as("alReturnOnEquity")
						.first("alSolvencyRatio").as("alSolvencyRatio").first("alCostOfEquity").as("alCostOfEquity")
						.first("alCostOfDebt").as("alCostOfDebt").first("qrEBIDTAMargin").as("qrEBIDTAMargin")
						.first("qrOperatingMargin").as("qrOperatingMargin").first("qrNetMargin").as("qrNetMargin")
						.first("qrDividendPercent").as("qrDividendPercent").first("summary").as("summary")
						.first("todayDate").as("todayDate").first("updateDate").as("updateDate"))
								.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<StockFundamentalAnalysis> results = mongoTemplate.aggregate(aggregation,
				COLLECTION_STOCK_FUNDAMENTAL_ANALYSIS, StockFundamentalAnalysis.class);
		List<StockFundamentalAnalysis> retVal = new ArrayList<StockFundamentalAnalysis>();

		for (StockFundamentalAnalysis stockFundamentalAnalysis : results.getMappedResults()) {
			retVal.add(stockFundamentalAnalysis);
		}
		return retVal;
	}

	public List<StockFundamentalAnalysis> getStockFundamentalAnalysisFromDB(String id) throws DataAccessException {
		Query query = new Query();
		query.with(new Sort(Sort.Direction.DESC, "todayDate"));
		query.addCriteria(Criteria.where("securityId").is(id));
		return mongoTemplate.find(query, StockFundamentalAnalysis.class, COLLECTION_STOCK_FUNDAMENTAL_ANALYSIS);
	}

	public StockFundamentalAnalysis getStockFundamentalAnalysisFromDB(String id, LocalDate date)
			throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("todayDate").is(date).and("securityId").is(id));
		return mongoTemplate.findOne(query, StockFundamentalAnalysis.class, COLLECTION_STOCK_FUNDAMENTAL_ANALYSIS);
	}

	public void updateStockFundamentalAnalysis(List<StockFundamentalAnalysis> stockFundamentalAnalysisList) {
		logger.debug("Inside updateStockFundamentalAnalysis()...");
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, StockFundamentalAnalysis.class);

		// Batch inserts
		int start = -1000;
		int end = 0;
		while (end < stockFundamentalAnalysisList.size()) {
			start = start + 1000;
			end = end + 1000;
			if (end > stockFundamentalAnalysisList.size()) {
				end = stockFundamentalAnalysisList.size();
			}
			List<StockFundamentalAnalysis> stockFundamentalAnalysisTemp = stockFundamentalAnalysisList.subList(start,
					end);

			for (StockFundamentalAnalysis stockFundamentalAnalysis : stockFundamentalAnalysisTemp) {
//				logger.info("stockFundamentalAnalysis: "+ stockFundamentalAnalysis);
				Query query = new Query();
				Update update = new Update();
				update.set("securityId", stockFundamentalAnalysis.getSecurityId());
				update.set("yearQuarter", stockFundamentalAnalysis.getYearQuarter());
				update.set("alEPSPct", stockFundamentalAnalysis.getAlEPSPct());
				update.set("alCashToDebtRatio", stockFundamentalAnalysis.getAlCashToDebtRatio());
				update.set("alCurrentRatio", stockFundamentalAnalysis.getAlCurrentRatio());
				update.set("alEquityToAssetRatio", stockFundamentalAnalysis.getAlEquityToAssetRatio());
				update.set("alDebtToCapitalRatio", stockFundamentalAnalysis.getAlDebtToCapitalRatio());
				update.set("alLeveredBeta", stockFundamentalAnalysis.getAlLeveredBeta());
				update.set("alReturnOnEquity", stockFundamentalAnalysis.getAlReturnOnEquity());
				update.set("alSolvencyRatio", stockFundamentalAnalysis.getAlSolvencyRatio());
				update.set("alCostOfEquity", stockFundamentalAnalysis.getAlCostOfEquity());
				update.set("alCostOfDebt", stockFundamentalAnalysis.getAlCostOfDebt());
				update.set("qrEBIDTAMargin", stockFundamentalAnalysis.getQrEBIDTAMargin());
				update.set("qrOperatingMargin", stockFundamentalAnalysis.getQrOperatingMargin());
				update.set("qrNetMargin", stockFundamentalAnalysis.getQrNetMargin());
				update.set("qrDividendPercent", stockFundamentalAnalysis.getQrDividendPercent());
				update.set("summary", stockFundamentalAnalysis.getSummary());
				update.set("todayDate", dateUtil.getDateFromLocalDate(stockFundamentalAnalysis.getTodayDate()));
				update.set("updateDate", dateUtil.getDateFromLocalDateTime(stockFundamentalAnalysis.getUpdateDate()));
				query.addCriteria(Criteria.where("todayDate").is(dateUtil.getDateFromLocalDate(stockFundamentalAnalysis.getTodayDate()))
						.and("securityId").is(stockFundamentalAnalysis.getSecurityId()));
				operation.upsert(query, update);
			}
			operation.execute();
		}
		updateCache(stockFundamentalAnalysisList);
	}

	public void updateCache(List<StockFundamentalAnalysis> stockFundamentalAnalysisList) {
		// update stockFundamentalAnalysisCache with latest stockFundamentalAnalysis
		for (StockFundamentalAnalysis stockFundamentalAnalysis : stockFundamentalAnalysisList) {
			StockFundamentalAnalysis cacheValue = stockFundamentalAnalysisCache.get(stockFundamentalAnalysis.getSecurityId());
			if(cacheValue!=null){
				if(cacheValue.getTodayDate().isBefore(stockFundamentalAnalysis.getTodayDate())){
					stockFundamentalAnalysisCache.put(stockFundamentalAnalysis.getSecurityId(), stockFundamentalAnalysis);
				}
			}else {
				stockFundamentalAnalysisCache.put(stockFundamentalAnalysis.getSecurityId(), stockFundamentalAnalysis);
			}
		}
	}

	@ManagedOperation(description = "getStockFundamentalAnalysisFromCache")
	public String getStockFundamentalAnalysisFromCache(String name) {
		try {
			StockFundamentalAnalysis stockFundamentalAnalysis = getLatestStockFundamentalAnalysis(name);
			if (stockFundamentalAnalysis != null) {
				return stockFundamentalAnalysis.toString();
			} else {
				return "StockFundamentalAnalysis not found";
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@ManagedOperation(description = "dumpStockFundamentalAnalysisCache")
	public String dumpEODPriceCache() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, StockFundamentalAnalysis> entry : stockFundamentalAnalysisCache.entrySet()) {
			builder.append(entry.getKey().toString());
			builder.append("=");
			builder.append(entry.getValue().toString());
			builder.append("\n");
		}
		return builder.toString();
	}
}