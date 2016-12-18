package com.intelliinvest.data.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.StockFundamentalAnalysis;
import com.intelliinvest.util.Helper;

@ManagedResource(objectName = "bean:name=IndustryFundamentalsRepository", description = "IndustryFundamentalsRepository")
public class IndustryFundamentalsRepository {
	private static Logger logger = Logger.getLogger(IndustryFundamentalsRepository.class);
	private static final String COLLECTION_INDUSTRY_FUNDAMENTALS = "INDUSTRY_FUNDAMENTALS";
	@Autowired
	private MongoTemplate mongoTemplate;
	private Map<String, IndustryFundamentals> industryFundamentalsCache = new ConcurrentHashMap<String, IndustryFundamentals>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<IndustryFundamentals> industryFundamentals = getLatestIndustryFundamentalsFromDB();
		if (Helper.isNotNullAndNonEmpty(industryFundamentals)) {
			for (IndustryFundamentals industryFundamental : industryFundamentals) {
				industryFundamentalsCache.put(industryFundamental.getName(), industryFundamental);
			}
			logger.info("Initialised industryFundamentalsCache from DB in IndustryFundamentalsRepository with size "
					+ industryFundamentalsCache.size());
		} else {
			logger.error(
					"Could not initialise industryFundamentalsCache from DB in IndustryFundamentalsRepository. INDUSTRY_FUNDAMENTALS is empty.");
		}
	}

	public IndustryFundamentals getLatestIndustryFundamentals(String name) {
		IndustryFundamentals industryFundamental = industryFundamentalsCache.get(name);
		if (industryFundamental == null) {
			logger.error("Inside getIndustryFundamentals() IndustryFundamentals not found in cache for " + name);
			return null;
		}
		return industryFundamental.clone();
	}

	public Map<String, IndustryFundamentals> getLatestIndustryFundamentals() throws Exception {
		Map<String, IndustryFundamentals> retVal = new HashMap<String, IndustryFundamentals>();
		for (Map.Entry<String, IndustryFundamentals> entry : industryFundamentalsCache.entrySet()) {
			retVal.put(entry.getKey(), entry.getValue().clone());
		}
		return retVal;
	}

	public List<IndustryFundamentals> getLatestIndustryFundamentalsFromDB() throws DataAccessException {
		logger.info("Inside getLatestIndustryFundamentalsFromDB()...");
		// retrieve record having max yearQuarter for each industry
		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "todayDate"),
				group("name").first("name").as("name").first("yearQuarter").as("yearQuarter").first("alMarketCap")
						.as("alMarketCap").first("alBookValuePerShare").as("alBookValuePerShare").first("alEarningPerShare").as("alEarningPerShare").first("alEPSPct")
						.as("alEPSPct").first("alPriceToEarning").as("alPriceToEarning").first("alCashToDebtRatio").as("alCashToDebtRatio").first("alCurrentRatio")
						.as("alCurrentRatio").first("alEquityToAssetRatio").as("alEquityToAssetRatio")
						.first("alDebtToCapitalRatio").as("alDebtToCapitalRatio").first("alLeveredBeta")
						.as("alLeveredBeta").first("alReturnOnEquity").as("alReturnOnEquity").first("alSolvencyRatio")
						.as("alSolvencyRatio").first("alCostOfEquity").as("alCostOfEquity").first("alCostOfDebt")
						.as("alCostOfDebt").first("qrEBIDTAMargin").as("qrEBIDTAMargin").first("qrOperatingMargin")
						.as("qrOperatingMargin").first("qrNetMargin").as("qrNetMargin").first("qrDividendPercent")
						.as("qrDividendPercent").first("todayDate").as("todayDate").first("updateDate").as("updateDate"))
								.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<IndustryFundamentals> results = mongoTemplate.aggregate(aggregation,
				COLLECTION_INDUSTRY_FUNDAMENTALS, IndustryFundamentals.class);
		List<IndustryFundamentals> retVal = new ArrayList<IndustryFundamentals>();

		for (IndustryFundamentals industryFundamental : results.getMappedResults()) {
			retVal.add(industryFundamental);
		}
		return retVal;
	}

	public List<IndustryFundamentals> getIndustryFundamentalsFromDB(String name) throws DataAccessException {
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "todayDate"));
		query.addCriteria(Criteria.where("name").is(name));
		return mongoTemplate.find(query, IndustryFundamentals.class, COLLECTION_INDUSTRY_FUNDAMENTALS);
	}
	
	public List<IndustryFundamentals> getIndustryFundamentalsFromDBAfterDate(String name, LocalDate date) throws DataAccessException {
		logger.info("Fetching industry fundamentals data for: "+name+" and after date: "+date);
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "todayDate"));
		query.addCriteria(Criteria.where("todayDate").gte(date).and("name").is(name));
		return mongoTemplate.find(query, IndustryFundamentals.class, COLLECTION_INDUSTRY_FUNDAMENTALS);
	}

	public IndustryFundamentals getIndustryFundamentalsFromDB(String name, LocalDate date) throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("todayDate").is(date).and("name").is(name));
		return mongoTemplate.findOne(query, IndustryFundamentals.class, COLLECTION_INDUSTRY_FUNDAMENTALS);
	}

	public void updateIndustryFundamentals(IndustryFundamentals industryFundamentals) {
		logger.debug("Inside updateIndustryFundamentals()...");
		Query query = new Query();
		Update update = new Update();
		update.set("name", industryFundamentals.getName());
		update.set("yearQuarter", industryFundamentals.getYearQuarter());
		update.set("alMarketCap", industryFundamentals.getAlMarketCap());
		update.set("alBookValuePerShare", industryFundamentals.getAlBookValuePerShare());
		update.set("alEarningPerShare", industryFundamentals.getAlEarningPerShare());
		update.set("alEPSPct", industryFundamentals.getAlEPSPct());
		update.set("alPriceToEarning", industryFundamentals.getAlPriceToEarning());
		update.set("alCashToDebtRatio", industryFundamentals.getAlCashToDebtRatio());
		update.set("alCurrentRatio", industryFundamentals.getAlCurrentRatio());
		update.set("alEquityToAssetRatio", industryFundamentals.getAlEquityToAssetRatio());
		update.set("alDebtToCapitalRatio", industryFundamentals.getAlDebtToCapitalRatio());
		update.set("alLeveredBeta", industryFundamentals.getAlLeveredBeta());
		update.set("alReturnOnEquity", industryFundamentals.getAlReturnOnEquity());
		update.set("alSolvencyRatio", industryFundamentals.getAlSolvencyRatio());
		update.set("alCostOfEquity", industryFundamentals.getAlCostOfEquity());
		update.set("alCostOfDebt", industryFundamentals.getAlCostOfDebt());
		update.set("qrEBIDTAMargin", industryFundamentals.getQrEBIDTAMargin());
		update.set("qrOperatingMargin", industryFundamentals.getQrOperatingMargin());
		update.set("qrNetMargin", industryFundamentals.getQrNetMargin());
		update.set("qrDividendPercent", industryFundamentals.getQrDividendPercent());
		update.set("todayDate", industryFundamentals.getTodayDate());
		update.set("updateDate", industryFundamentals.getUpdateDate());
		query.addCriteria(Criteria.where("todayDate").is(industryFundamentals.getTodayDate()).and("name")
				.is(industryFundamentals.getName()));
		mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true).upsert(true),
				IndustryFundamentals.class, COLLECTION_INDUSTRY_FUNDAMENTALS);
		
		IndustryFundamentals cacheValue = industryFundamentalsCache.get(industryFundamentals.getName());
		if(cacheValue!=null){
			if(cacheValue.getTodayDate().isBefore(industryFundamentals.getTodayDate())){
				industryFundamentalsCache.put(industryFundamentals.getName(), industryFundamentals);
			}
		}else {
			industryFundamentalsCache.put(industryFundamentals.getName(), industryFundamentals);
		}
	}

	@ManagedOperation(description = "getIndustryFundamentalsFromCache")
	public String getIndustryFundamentalsFromCache(String name) {
		try {
			IndustryFundamentals industryFundamental = getLatestIndustryFundamentals(name);
			if (industryFundamental != null) {
				return industryFundamental.toString();
			} else {
				return "IndustryFundamentals not found";
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@ManagedOperation(description = "dumpIndustryFundamentalsCache")
	public String dumpEODPriceCache() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, IndustryFundamentals> entry : industryFundamentalsCache.entrySet()) {
			builder.append(entry.getKey().toString());
			builder.append("=");
			builder.append(entry.getValue().toString());
			builder.append("\n");
		}
		return builder.toString();
	}
}