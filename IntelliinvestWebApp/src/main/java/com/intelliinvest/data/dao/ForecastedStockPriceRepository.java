package com.intelliinvest.data.dao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.CommonConstParams.ForecastType;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.model.ForecastedStockPrice;
import com.intelliinvest.util.DateUtil;

@ManagedResource(objectName = "bean:name=ForecastedStockPriceRepository", description = "ForecastedStockPriceRepository")
public class ForecastedStockPriceRepository {
	private static Logger logger = Logger.getLogger(ForecastedStockPriceRepository.class);
	private static final String COLLECTION_STOCK_PRICE_FORECAST = "STOCK_PRICE_FORECAST";
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private DateUtil dateUtil;

	public ForecastedStockPrice getForecastStockPriceFromDB(String code, LocalDate todayDate)
			throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("code").is(code).and("todayDate").is(todayDate));
		return mongoTemplate.findOne(query, ForecastedStockPrice.class, COLLECTION_STOCK_PRICE_FORECAST);
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
			retVal.put(price.getCode(), price);
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
			query.addCriteria(Criteria.where("code").is(price.getCode()).and("todayDate")
					.is(dateUtil.getDateFromLocalDate(price.getTodayDate())));
			Update update = new Update();
			update.set("code", price.getCode());
			switch (forecastType) {
			case DAILY:
				update.set("tomorrowForecastDate", dateUtil.getDateFromLocalDate(price.getTomorrowForecastDate()));
				update.set("tomorrowForecastPrice", price.getTomorrowForecastPrice());
				break;
			case WEEKLY:
				update.set("weeklyForecastDate", dateUtil.getDateFromLocalDate(price.getWeeklyForecastDate()));
				update.set("weeklyForecastPrice", price.getWeeklyForecastPrice());
				break;
			case MONTHLY:
				update.set("monthlyForecastDate", dateUtil.getDateFromLocalDate(price.getMonthlyForecastDate()));
				update.set("monthlyForecastPrice", price.getMonthlyForecastPrice());
				break;
			default:
				logger.error("Defaulting to daily forecastType. Incorrect forecastType " + forecastType.name());
				update.set("tomorrowForecastDate", dateUtil.getDateFromLocalDate(price.getTomorrowForecastDate()));
				update.set("tomorrowForecastPrice", price.getTomorrowForecastPrice());
				break;
			}
			update.set("todayDate", dateUtil.getDateFromLocalDate(price.getTodayDate()));
			update.set("updateDate", dateUtil.getDateFromLocalDateTime());
			operation.upsert(query, update);
		}
		operation.execute();
	}

	@ManagedOperation(description = "getForecastStockPrice")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "Stock Code", description = "Stock Code"),
			@ManagedOperationParameter(name = "Today Date(yyyy-MM-dd)", description = "Today Date") })
	public String getForecastStockPrice(String code, String todayDateStr) {
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate todayDate = LocalDate.parse(todayDateStr, dateFormat);
			ForecastedStockPrice price = getForecastStockPriceFromDB(code, todayDate);
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
