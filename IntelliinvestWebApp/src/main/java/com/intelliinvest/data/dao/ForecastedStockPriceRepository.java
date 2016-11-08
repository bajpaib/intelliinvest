package com.intelliinvest.data.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliinvestConstants.ForecastType;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.model.ForecastedStockPrice;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.ForecastedStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;

@ManagedResource(objectName = "bean:name=ForecastedStockPriceRepository", description = "ForecastedStockPriceRepository")
public class ForecastedStockPriceRepository {
	private static Logger logger = Logger.getLogger(ForecastedStockPriceRepository.class);
	private static final String COLLECTION_STOCK_PRICE_FORECAST = "STOCK_PRICE_FORECAST";
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private DateUtil dateUtil;
	private Map<String, ForecastedStockPrice> priceCache = new ConcurrentHashMap<String, ForecastedStockPrice>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<ForecastedStockPrice> prices = getLatestForecastStockPricesFromDB();
		if (Helper.isNotNullAndNonEmpty(prices)) {
			for (ForecastedStockPrice price : prices) {
				priceCache.put(price.getSecurityId(), price);
			}
			logger.info("Initialised priceCache from DB in ForecastedStockPriceRepository with size " + priceCache.size());
		} else {
			logger.error(
					"Could not initialise priceCache from DB in ForecastedStockPriceRepository. STOCK_PRICE_FORECAST is empty.");
		}
	}
	
	public List<ForecastedStockPrice> getLatestForecastStockPricesFromDB() throws DataAccessException {
		logger.info("Inside getLatestStockPrices()...");
		// retrieve record having max eodDate for each stock
		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "todayDate"),
				group("securityId").first("securityId").as("securityId").first("todayDate").as("todayDate")
						.first("tomorrowForecastPrice").as("tomorrowForecastPrice").first("weeklyForecastPrice").as("weeklyForecastPrice")
						.first("monthlyForecastPrice").as("monthlyForecastPrice").first("tomorrowForecastDate").as("tomorrowForecastDate")
						.first("weeklyForecastDate").as("weeklyForecastDate").first("monthlyForecastDate").as("monthlyForecastDate")
						.first("updateDate").as("updateDate"))
								.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<ForecastedStockPrice> results = mongoTemplate.aggregate(aggregation,
				COLLECTION_STOCK_PRICE_FORECAST, ForecastedStockPrice.class);
		List<ForecastedStockPrice> retVal = new ArrayList<ForecastedStockPrice>();

		for (ForecastedStockPrice price : results.getMappedResults()) {
			retVal.add(price);
		}
		return retVal;
	}
	
	public ForecastedStockPrice getLatestForecastStockPrice(String id) {
		ForecastedStockPrice price = priceCache.get(id);
		if (price == null) {
			logger.error("Inside getLatestForecastStockPrice() QuandlStockPrice not found in cache for " + id);
			return null;
		}
		return price.clone();
	}
	
	public ForecastedStockPrice getForecastStockPriceFromDB(String id, LocalDate todayDate)
			throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("todayDate").is(todayDate).and("securityId").is(id));
		return mongoTemplate.findOne(query, ForecastedStockPrice.class, COLLECTION_STOCK_PRICE_FORECAST);
	}
	
	public List<ForecastedStockPrice> getForecastStockPricesForDateRangeFromDB(String id, List<LocalDate> dates)
			throws DataAccessException {
		Query query = new Query();
		query.with(new Sort(Sort.Direction.DESC, "todayDate"));
		query.addCriteria(Criteria.where("todayDate").in(dates).and("securityId").is(id));
		return mongoTemplate.find(query, ForecastedStockPrice.class, COLLECTION_STOCK_PRICE_FORECAST);
	}

	public List<ForecastedStockPrice> getForecastStockPricesFromDB(LocalDate todayDate) throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("todayDate").is(todayDate));
		return mongoTemplate.find(query, ForecastedStockPrice.class, COLLECTION_STOCK_PRICE_FORECAST);
	}

	public Map<String, ForecastedStockPrice> getForecastStockPricesMapFromDB(LocalDate todayDate)
			throws DataAccessException {
		List<ForecastedStockPrice> prices = getForecastStockPricesFromDB(todayDate);
		Map<String, ForecastedStockPrice> retVal = new HashMap<String, ForecastedStockPrice>();
		for (ForecastedStockPrice price : prices) {
			retVal.put(price.getSecurityId(), price);
		}
		return retVal;
	}

	public void updateForecastStockPrices(List<ForecastedStockPrice> forecastPrices, ForecastType forecastType)
			throws IntelliinvestException {
		logger.info("Inside updateForecastStockPrices()...");
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, ForecastedStockPrice.class,
				COLLECTION_STOCK_PRICE_FORECAST);
		for (ForecastedStockPrice price : forecastPrices) {
			Query query = new Query();
			query.addCriteria(Criteria.where("todayDate")
					.is(dateUtil.getDateFromLocalDate(price.getTodayDate())).and("securityId").is(price.getSecurityId()));
			Update update = new Update();
			update.set("securityId", price.getSecurityId());
			switch (forecastType) {
			case DAILY:
				update.set("tomorrowForecastDate", dateUtil.getDateFromLocalDate(price.getTomorrowForecastDate()));
				update.set("tomorrowForecastPrice", price.getTomorrowForecastPrice() !=null ? price.getTomorrowForecastPrice() : 0);
				break;
			case WEEKLY:
				update.set("weeklyForecastDate", dateUtil.getDateFromLocalDate(price.getWeeklyForecastDate()));
				update.set("weeklyForecastPrice", price.getWeeklyForecastPrice() !=null ? price.getWeeklyForecastPrice() : 0);
				break;
			case MONTHLY:
				update.set("monthlyForecastDate", dateUtil.getDateFromLocalDate(price.getMonthlyForecastDate()));
				update.set("monthlyForecastPrice", price.getMonthlyForecastPrice() !=null ? price.getMonthlyForecastPrice() : 0);
				break;
			default:
				logger.error("Defaulting to daily forecastType. Incorrect forecastType " + forecastType.name());
				update.set("tomorrowForecastDate", dateUtil.getDateFromLocalDate(price.getTomorrowForecastDate()));
				update.set("tomorrowForecastPrice", price.getTomorrowForecastPrice() !=null ? price.getTomorrowForecastPrice() : 0);
				break;
			}
			update.set("todayDate", dateUtil.getDateFromLocalDate(price.getTodayDate()));
			update.set("updateDate", dateUtil.getDateFromLocalDateTime());
			operation.upsert(query, update);
		}
		operation.execute();
	}

	@ManagedOperation(description = "getForecastStockPrice")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "Stock Id", description = "Stock Id"),
			@ManagedOperationParameter(name = "Today Date(yyyy-MM-dd)", description = "Today Date") })
	public String getForecastStockPrice(String securityId, String todayDateStr) {
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate todayDate = LocalDate.parse(todayDateStr, dateFormat);
			ForecastedStockPrice price = getForecastStockPriceFromDB(securityId, todayDate);
			if (price != null) {
				return price.toString();
			} else {
				return "Price not found";
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@ManagedOperation(description = "getForecastStockPrices")
	@ManagedOperationParameters({
			@ManagedOperationParameter(name = "Today Date(yyyy-MM-dd)", description = "Today Date") })
	public String getForecastStockPrices(String todayDateStr) {
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate todayDate = LocalDate.parse(todayDateStr, dateFormat);
			List<ForecastedStockPrice> prices = getForecastStockPricesFromDB(todayDate);
			StringBuilder builder = new StringBuilder();
			for (ForecastedStockPrice price : prices) {
				builder.append(price.toString());
				builder.append("\n");
			}
			return builder.toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
