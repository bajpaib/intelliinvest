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
	private static final String DEFAULT_EXCHANGE = "NSE";

	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private DateUtil dateUtil;
	private Map<QuandlStockPriceKey, QuandlStockPrice> priceCache = new ConcurrentHashMap<QuandlStockPriceKey, QuandlStockPrice>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	public class QuandlStockPriceKey {
		private String exchange;
		private String symbol;

		QuandlStockPriceKey(String exchange, String symbol) {
			this.exchange = exchange;
			this.symbol = symbol;
		}

		public String getExchange() {
			return exchange;
		}

		public String getSymbol() {
			return symbol;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
			result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			QuandlStockPriceKey other = (QuandlStockPriceKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (exchange == null) {
				if (other.exchange != null)
					return false;
			} else if (!exchange.equals(other.exchange))
				return false;
			if (symbol == null) {
				if (other.symbol != null)
					return false;
			} else if (!symbol.equals(other.symbol))
				return false;
			return true;
		}

		private QuandlEODStockPriceRepository getOuterType() {
			return QuandlEODStockPriceRepository.this;
		}

		@Override
		public String toString() {
			return "QuandlStockPriceKey [exchange=" + exchange + ", symbol=" + symbol + "]";
		}
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<QuandlStockPrice> prices = getLatestStockPricesFromDB();
		if (Helper.isNotNullAndNonEmpty(prices)) {
			for (QuandlStockPrice price : prices) {
				QuandlStockPriceKey key = new QuandlStockPriceKey(price.getExchange(), price.getSymbol());
				priceCache.put(key, price);
			}
			logger.info("Initialised priceCache from DB in QuandlStockPriceRepository with size " + priceCache.size());
		} else {
			logger.error(
					"Could not initialise priceCache from DB in QuandlStockPriceRepository. QUANDL_STOCK_PRICE is empty.");
		}
	}

	public QuandlStockPrice getEODStockPrice(String symbol) throws Exception {
		return getEODStockPrice(symbol, DEFAULT_EXCHANGE);
	}

	private QuandlStockPrice getEODStockPrice(String symbol, String exchange) throws Exception {
		QuandlStockPrice price = priceCache.get(new QuandlStockPriceKey(exchange, symbol));
		if (price == null) {
			logger.error("Inside getEODStockPrice() QuandlStockPrice not found in cache for " + symbol);
		}
		return price.clone();
	}

	public QuandlStockPrice getStockPriceFromDB(String symbol, LocalDate eodDate) throws DataAccessException {
		return getStockPriceFromDB(DEFAULT_EXCHANGE, symbol, eodDate);
	}

	private QuandlStockPrice getStockPriceFromDB(String exchange, String symbol, LocalDate eodDate)
			throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("exchange").is(exchange).and("symbol").is(symbol).and("eodDate").is(eodDate));
		return mongoTemplate.findOne(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
	}

	public List<QuandlStockPrice> getStockPricesFromDB(String symbol, LocalDate startDate, LocalDate endDate) {
		return getStockPricesFromDB(DEFAULT_EXCHANGE, symbol, startDate, endDate);
	}

	public List<QuandlStockPrice> getStockPricesFromDB(String exchange, String symbol, LocalDate startDate,
			LocalDate endDate) throws DataAccessException {
		if (!Helper.isNotNullAndNonEmpty(exchange)) {
			exchange = DEFAULT_EXCHANGE;
		}
		Query query = new Query();
		query.addCriteria(Criteria.where("exchange").is(exchange).and("symbol").is(symbol).and("eodDate").gte(startDate)
				.lte(endDate));
		return mongoTemplate.find(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
	}

	public List<QuandlStockPrice> getStockPricesFromDB(LocalDate eodDate) {
		return getStockPricesFromDB(DEFAULT_EXCHANGE, eodDate);
	}

	public List<QuandlStockPrice> getStockPricesFromDB(String exchange, LocalDate eodDate) throws DataAccessException {
		if (!Helper.isNotNullAndNonEmpty(exchange)) {
			exchange = DEFAULT_EXCHANGE;
		}
		Query query = new Query();
		query.addCriteria(Criteria.where("exchange").is(exchange).and("eodDate").is(eodDate));
		return mongoTemplate.find(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
	}

	public Map<String, QuandlStockPrice> getEODStockPrices(LocalDate date) throws Exception {
		Map<String, QuandlStockPrice> retVal = new HashMap<String, QuandlStockPrice>();
		List<QuandlStockPrice> prices = getStockPricesFromDB(date);
		for (QuandlStockPrice price : prices) {
			retVal.put(price.getSymbol(), price.clone());
		}
		return retVal;
	}

	private List<QuandlStockPrice> getLatestStockPricesFromDB() throws DataAccessException {
		logger.info("Inside getLatestStockPrices()...");
		// retrieve record having max eodDate for each stock
		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "eodDate"),
				group("exchange", "symbol").first("eodDate").as("eodDate"))
						.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<QuandlStockPrice> results = mongoTemplate.aggregate(aggregation,
				COLLECTION_QUANDL_STOCK_PRICE, QuandlStockPrice.class);
		List<QuandlStockPrice> retVal = new ArrayList<QuandlStockPrice>();

		for (QuandlStockPrice price : results.getMappedResults()) {
			retVal.add(getStockPriceFromDB(price.getExchange(), price.getSymbol(), price.getEodDate()));
		}
		return retVal;
	}

	public void updateEODStockPrices(List<QuandlStockPrice> quandlPrices) throws IntelliinvestException {
		logger.info("Inside updateQuandlStockPrices()...");
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, QuandlStockPrice.class);
		for (QuandlStockPrice price : quandlPrices) {
			Query query = new Query();
			query.addCriteria(Criteria.where("exchange").is(price.getExchange()).and("symbol").is(price.getSymbol())
					.and("eodDate").is(dateUtil.getDateFromLocalDate(price.getEodDate())));
			Update update = new Update();
			update.set("exchange", price.getExchange());
			update.set("symbol", price.getSymbol());
			update.set("series", price.getSeries());
			update.set("open", price.getOpen());
			update.set("high", price.getHigh());
			update.set("low", price.getLow());
			update.set("close", price.getClose());
			update.set("last", price.getLast());
			update.set("tottrdqty", price.getTottrdqty());
			update.set("tottrdval", price.getTottrdval());
			update.set("eodDate", dateUtil.getDateFromLocalDate(price.getEodDate()));
			update.set("updateDate", dateUtil.getDateFromLocalDateTime(dateUtil.getLocalDateTime()));
			operation.upsert(query, update);
		}
		operation.execute();
	}

	public void updateCache(List<QuandlStockPrice> quandlPrices) {
		// update priceCache with latest price
		for (QuandlStockPrice price : quandlPrices) {
			QuandlStockPriceKey key = new QuandlStockPriceKey(price.getExchange(), price.getSymbol());
			priceCache.put(key, price);
		}
	}

	@ManagedOperation(description = "getEODStockPriceFromCache")
	public String getEODStockPriceFromCache(String symbol) {
		try {
			QuandlStockPrice price = getEODStockPrice(symbol);
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
		for (Map.Entry<QuandlStockPriceKey, QuandlStockPrice> entry : priceCache.entrySet()) {
			builder.append(entry.getKey().toString());
			builder.append("=");
			builder.append(entry.getValue().toString());
			builder.append("\n");
		}
		return builder.toString();
	}
}