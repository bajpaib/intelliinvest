package com.intelliinvest.web.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.util.Date;
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

import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.web.common.IntelliinvestException;
import com.intelliinvest.web.util.Helper;

public class QuandlEODStockPriceRepository {
	private static Logger logger = Logger.getLogger(QuandlEODStockPriceRepository.class);
	private static final String COLLECTION_QUANDL_STOCK_PRICE = "QUANDL_STOCK_PRICE";
	private static final String DEFAULT_EXCHANGE = "NSE";
	@Autowired
	private MongoTemplate mongoTemplate;
	private Map<QuandlStockPriceKey, QuandlStockPrice> eodPriceCache = new ConcurrentHashMap<QuandlStockPriceKey, QuandlStockPrice>();

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
	}

	public void initialiseCacheFromDB() {
		List<QuandlStockPrice> prices = getLatestEODStockPricesFromDB();
		if (Helper.isNotNullAndNonEmpty(prices)) {
			for (QuandlStockPrice price : prices) {
				QuandlStockPriceKey key = new QuandlStockPriceKey(price.getExchange(), price.getSymbol());
				eodPriceCache.put(key, price);
			}
			logger.info("Initialised eodPriceCache in QuandlStockPriceRepository from DB with size " + eodPriceCache.size());
		} else {
			logger.error(
					"Could not initialise eodPriceCache from DB in QuandlStockPriceRepository. QUANDL_STOCK_PRICE is empty.");
		}
	}

	public QuandlStockPrice getEODStockPrice(String symbol) {
		return getEODStockPrice(symbol, DEFAULT_EXCHANGE);
	}

	public QuandlStockPrice getEODStockPrice(String symbol, String exchange) {
		logger.debug("Inside getEODStockPrice()...");
		QuandlStockPrice price = eodPriceCache.get(new QuandlStockPriceKey(symbol, exchange));
		if (price == null) {
			logger.error("Inside getEODStockPrice(). QuandlStockPrice not found in cache for " + symbol);
		}
		return price;
	}

	public QuandlStockPrice getEODStockPriceFromDB(String exchange, String symbol, Date eodDate)
			throws DataAccessException {
		logger.debug("Inside getEODStockPriceFromDB()...");
		Query query = new Query();
		query.addCriteria(Criteria.where("exchange").is(exchange).and("symbol").is(symbol).and("eodDate").is(eodDate));
		return mongoTemplate.findOne(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
	}

	public List<QuandlStockPrice> getLatestEODStockPricesFromDB() throws DataAccessException {
		logger.info("Inside getLatestEODStockPricesFromDB()...");
		// retrieve record having max eodDate for each stock
		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "eodDate"),
				group("exchange", "symbol").first("eodDate").as("eodDate"));
		AggregationResults<QuandlStockPrice> results = mongoTemplate.aggregate(aggregation,
				COLLECTION_QUANDL_STOCK_PRICE, QuandlStockPrice.class);
		return results.getMappedResults();
	}

	public void updateEODStockPrices(List<QuandlStockPrice> quandlPrices) throws IntelliinvestException {
		logger.info("Inside updateEODStockPrices()...");
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, QuandlStockPrice.class);
		Date currentDate = new Date();
		for (QuandlStockPrice price : quandlPrices) {
			Query query = new Query();
			query.addCriteria(Criteria.where("exchange").is(price.getExchange()).and("symbol").is(price.getSymbol())
					.and("eodDate").is(price.getEodDate()));
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
			update.set("eodDate", price.getEodDate());
			update.set("updateDate", currentDate);
			operation.upsert(query, update);
		}
		operation.execute();
		// update eodPriceCache with latest price
		for (QuandlStockPrice price : quandlPrices) {
			QuandlStockPriceKey key = new QuandlStockPriceKey(price.getExchange(), price.getSymbol());
			eodPriceCache.put(key, price);
		}
	}
}
