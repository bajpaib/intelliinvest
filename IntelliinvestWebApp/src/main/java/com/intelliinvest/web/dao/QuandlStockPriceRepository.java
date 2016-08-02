package com.intelliinvest.web.dao;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.web.common.IntelliinvestException;

public class QuandlStockPriceRepository {
  private static Logger logger = Logger.getLogger(UserRepository.class);
  private static final String COLLECTION_QUANDL_STOCK_PRICE = "QUANDL_STOCK_PRICE";
  @Autowired
  private MongoTemplate mongoTemplate;

  public QuandlStockPrice getStockPrice(String exchange, String symbol, Date eodDate) throws DataAccessException {
    logger.info("Inside getStockPrice()...");
    Query query = new Query();
    query.addCriteria(Criteria.where("exchange").is(exchange).and("symbol").is(symbol).and("eodDate").is(eodDate));

    return mongoTemplate.findOne(query, QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
  }

  public List<QuandlStockPrice> getStockPrices() throws DataAccessException {
    logger.info("Inside getStockPrices()...");
    return mongoTemplate.findAll(QuandlStockPrice.class, COLLECTION_QUANDL_STOCK_PRICE);
  }

  public void updateQuandlStockPrices(List<QuandlStockPrice> quandlPrices) throws IntelliinvestException {
    logger.info("Inside updateQuandlStockPrices()...");
    BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, QuandlStockPrice.class);
    Date currentDate = new Date();
    for (QuandlStockPrice price : quandlPrices) {
      Query query = new Query();
      query.addCriteria(Criteria.where("exchange").is(price.getExchange()).and("symbol").is(price.getSymbol()).and("eodDate").is(price.getEodDate()));
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
        } 
} 
