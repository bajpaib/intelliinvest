package com.intelliinvest.data.bubbleData;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.dao.StockSignalsRepository;
import com.intelliinvest.data.model.BubbleData;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.web.bo.response.BubbleDataResponse;
import com.intelliinvest.web.controllers.BubbleDataController;

@ManagedResource(objectName = "bean:name=BubbleDataFetcher", description = "BubbleDataFetcher")
public class BubbleDataFetcher {

	private static Logger logger = Logger.getLogger(BubbleDataFetcher.class);

	private static final String COLLECTION_STOCK_SIGNALS = "STOCK_SIGNALS";

	@Autowired
	DateUtil dateUtil;

	@Autowired
	private MongoTemplate mongoTemplate;

	private Map<Integer, List<BubbleData>> cache = new ConcurrentHashMap<Integer, List<BubbleData>>();
	private static int DEFAULT_TIME_PERIOD = 5;

	@PostConstruct
	public void init() {
		refreshCache();
	}

	@ManagedOperation(description = "refreshCache")
	public void refreshCache() {
		logger.info("Inside refresh cache method....");
		cache.put(DEFAULT_TIME_PERIOD, getTechnicalDataFromDB(DEFAULT_TIME_PERIOD));
		logger.info("Bubble Data Cache has been refreshed...");
	}

	List<BubbleData> getTechnicalDataFromDB(int timePeriod) {
		Date date = dateUtil.getDateFromLocalDate(dateUtil.substractDays(dateUtil.getLocalDate(), 365 * timePeriod));
		logger.info("Date start from :: " + date);
		Aggregation aggregation = newAggregation(Aggregation.match(Criteria.where("signalDate").gte(date)),
				/* group("signalDate","aggSignal"), */
				group("signalDate", "aggSignal").count().as("count"), project("signalDate", "count").and("aggSignal"),
				sort(Sort.Direction.ASC, "signalDate"));
		AggregationResults<BubbleData> results = mongoTemplate.aggregate(aggregation, COLLECTION_STOCK_SIGNALS,
				BubbleData.class);
		List<BubbleData> bubbleDatas = results.getMappedResults();
		return bubbleDatas;
	}

	public List<BubbleDataResponse> getTechnicalBubbleData(int timePeriod) {
		List<BubbleData> bubbleDatas = null;
		if (timePeriod > DEFAULT_TIME_PERIOD) {
			logger.info("greater than default size, so returing from the db....");
			bubbleDatas = getTechnicalDataFromDB(timePeriod);
			return getBubbleDataResponse(bubbleDatas);
		} else {
			logger.info("less than default size, so returing from the cache....");
			LocalDate fromDate = dateUtil.substractDays(dateUtil.getLocalDate(), 365 * timePeriod);
			bubbleDatas = cache.get(DEFAULT_TIME_PERIOD);
			return getBubbleDataResponse(bubbleDatas, fromDate);
		}
	}

	private List<BubbleDataResponse> getBubbleDataResponse(List<BubbleData> bubbleDatas, LocalDate fromDate) {
		logger.info("Bubble Data Size is : " + bubbleDatas.size()+" and from date is:"+fromDate);
		Map<LocalDate, BubbleDataResponse> bubbleDatasMap = new LinkedHashMap<>();
		BubbleDataResponse bubbleDataResponse = null;

		for (BubbleData bubbleData : bubbleDatas) {
			LocalDate date = bubbleData.getSignalDate();
			if (date.compareTo(fromDate) > 0) {
				if (bubbleDatasMap.get(date) != null) {
					bubbleDataResponse = bubbleDatasMap.get(date);
				} else {
					bubbleDataResponse = new BubbleDataResponse();
					bubbleDataResponse.setDate(date);
					bubbleDatasMap.put(date, bubbleDataResponse);
				}
				if (bubbleData.getAggSignal() != null && (bubbleData.getAggSignal().equals(IntelliinvestConstants.BUY)
						|| bubbleData.getAggSignal().equals(IntelliinvestConstants.HOLD))) {
					bubbleDataResponse.setNoOfBuyHold(bubbleDataResponse.getNoOfBuyHold() + bubbleData.getCount());
				} else {
					bubbleDataResponse.setNoOfSellWait(bubbleDataResponse.getNoOfSellWait() + bubbleData.getCount());
				}
				bubbleDataResponse.setTotalNo(bubbleDataResponse.getTotalNo() + bubbleData.getCount());

				double percentageNoOfBuyHold = 0d;
				double percentageNoOfSellWait = 0d;

				percentageNoOfBuyHold = (bubbleDataResponse.getNoOfBuyHold() / bubbleDataResponse.getTotalNo()) * 100;
				percentageNoOfSellWait = (bubbleDataResponse.getNoOfSellWait() / bubbleDataResponse.getTotalNo()) * 100;

				bubbleDataResponse.setPercentageBuyHold(percentageNoOfBuyHold);
				bubbleDataResponse.setPercentageSellWait(percentageNoOfSellWait);
			}
		}
		logger.info("response size is:" + bubbleDatasMap.size());
		return new LinkedList<BubbleDataResponse>(bubbleDatasMap.values());
	}

	private List<BubbleDataResponse> getBubbleDataResponse(List<BubbleData> bubbleDatas) {
		logger.info("Bubble Data Size is : " + bubbleDatas.size());
		Map<LocalDate, BubbleDataResponse> bubbleDatasMap = new LinkedHashMap<>();
		BubbleDataResponse bubbleDataResponse = null;
		// List<BubbleDataResponse> bubbleDataResponses = new ArrayList<>();

		for (BubbleData bubbleData : bubbleDatas) {
			LocalDate date = bubbleData.getSignalDate();
			if (bubbleDatasMap.get(date) != null) {
				bubbleDataResponse = bubbleDatasMap.get(date);
			} else {
				bubbleDataResponse = new BubbleDataResponse();
				bubbleDataResponse.setDate(date);
				bubbleDatasMap.put(date, bubbleDataResponse);
			}
			if (bubbleData.getAggSignal() != null && (bubbleData.getAggSignal().equals(IntelliinvestConstants.BUY)
					|| bubbleData.getAggSignal().equals(IntelliinvestConstants.HOLD))) {
				bubbleDataResponse.setNoOfBuyHold(bubbleDataResponse.getNoOfBuyHold() + bubbleData.getCount());
			} else {
				bubbleDataResponse.setNoOfSellWait(bubbleDataResponse.getNoOfSellWait() + bubbleData.getCount());
			}
			bubbleDataResponse.setTotalNo(bubbleDataResponse.getTotalNo() + bubbleData.getCount());

			double percentageNoOfBuyHold = 0d;
			double percentageNoOfSellWait = 0d;

			percentageNoOfBuyHold = (bubbleDataResponse.getNoOfBuyHold() / bubbleDataResponse.getTotalNo()) * 100;
			percentageNoOfSellWait = (bubbleDataResponse.getNoOfSellWait() / bubbleDataResponse.getTotalNo()) * 100;

			bubbleDataResponse.setPercentageBuyHold(percentageNoOfBuyHold);
			bubbleDataResponse.setPercentageSellWait(percentageNoOfSellWait);
		}
		logger.info("response size is:" + bubbleDatasMap.size());
		return new LinkedList<BubbleDataResponse>(bubbleDatasMap.values());
	}

}
