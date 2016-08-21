package com.intelliinvest.data.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository.QuandlStockPriceKey;
import com.intelliinvest.data.model.ForecastedStockPrice;
import com.intelliinvest.data.model.QuandlStockPrice;

@ManagedResource(objectName = "bean:name=ForecastedStockPriceRepository", description = "ForecastedStockPriceRepository")
public class ForecastedStockPriceRepository {
	private static Logger logger = Logger.getLogger(ForecastedStockPriceRepository.class);
	private static final String COLLECTION_STOCK_PRICE_DAILY_FORECAST = "STOCK_PRICE_DAILY_FORECAST";
	@Autowired
	private MongoTemplate mongoTemplate;

	public List<ForecastedStockPrice> getDailyForecastStockPricesFromDB(String code, Date startDate, Date endDate)
			throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("code").is(code).and("forecastDate").gte(startDate).lte(endDate));
		return mongoTemplate.find(query, ForecastedStockPrice.class, COLLECTION_STOCK_PRICE_DAILY_FORECAST);
	}

	public List<ForecastedStockPrice> getLatestDailyForecastStockPricesFromDB() throws DataAccessException {
		logger.info("Inside getLatestDailyForecastStockPricesFromDB()...");
		// retrieve record having max forecastDate for each stock
		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "forecastDate"),
				group("code").first("forecastDate").as("forecastDate"))
						.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

		AggregationResults<ForecastedStockPrice> results = mongoTemplate.aggregate(aggregation,
				COLLECTION_STOCK_PRICE_DAILY_FORECAST, ForecastedStockPrice.class);

		List<ForecastedStockPrice> retVal = new ArrayList<ForecastedStockPrice>();

		for (ForecastedStockPrice price : results.getMappedResults()) {
			retVal.add(getDailyForecastStockPriceFromDB(price.getCode(), price.getForecastDate()));
		}
		return retVal;
	}
	
	public ForecastedStockPrice getDailyForecastStockPriceFromDB(String code, Date forecastDate)
			throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("code").is(code).and("forecastDate").is(forecastDate));
		return mongoTemplate.findOne(query, ForecastedStockPrice.class, COLLECTION_STOCK_PRICE_DAILY_FORECAST);
	}
	
	public List<ForecastedStockPrice> getDailyForecastStockPricesFromDB(Date forecastDate)
			throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("forecastDate").is(forecastDate));
		return mongoTemplate.find(query, ForecastedStockPrice.class, COLLECTION_STOCK_PRICE_DAILY_FORECAST);
	}
	
	public  Map<String, ForecastedStockPrice> getDailyForecastStockPricesMapFromDB(Date forecastDate) throws DataAccessException{
		List<ForecastedStockPrice> prices = getDailyForecastStockPricesFromDB(forecastDate);
		Map<String, ForecastedStockPrice> retVal = new HashMap<String, ForecastedStockPrice>();		
		for(ForecastedStockPrice price: prices){
			retVal.put(price.getCode(), price);
		}
		return retVal;
	}

	public void updateDailyForecastStockPrices(List<ForecastedStockPrice> forecastPrices)
			throws IntelliinvestException {
		logger.info("Inside updateDailyForecastStockPrices()...");
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, ForecastedStockPrice.class,
				COLLECTION_STOCK_PRICE_DAILY_FORECAST);
		for (ForecastedStockPrice price : forecastPrices) {
			Query query = new Query();
			query.addCriteria(
					Criteria.where("code").is(price.getCode()).and("forecastDate").is(price.getForecastDate()));
			Update update = new Update();
			update.set("code", price.getCode());
			update.set("forecastPrice", price.getForecastPrice());
			update.set("forecastDate", price.getForecastDate());
			update.set("updateDate", price.getUpdateDate());
			operation.upsert(query, update);
		}
		operation.execute();
	}

	@ManagedOperation(description = "getDailyForecastStockPrice")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "Stock Code", description = "Stock Code"),
			@ManagedOperationParameter(name = "Forecast Date (yyyy-MM-dd)", description = "Forecast Date (yyyy-MM-dd)") })
	public String getDailyForecastStockPrice(String code, String forecastDateStr) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date forecastDate = format.parse(forecastDateStr);
			ForecastedStockPrice price = getDailyForecastStockPriceFromDB(code, forecastDate);
			if (price != null) {
				return price.toString();
			} else {
				return "Price not found";
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	@ManagedOperation(description = "getDailyForecastStockPrices")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "Forecast Date (yyyy-MM-dd)", description = "Forecast Date (yyyy-MM-dd)") })
	public String getDailyForecastStockPrices(String forecastDateStr) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date forecastDate = format.parse(forecastDateStr);
			List<ForecastedStockPrice> prices = getDailyForecastStockPricesFromDB(forecastDate);
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