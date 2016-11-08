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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.dao.StockSignalsRepository;
import com.intelliinvest.data.model.BubbleData;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.web.bo.response.BubbleDataResponse;
import com.intelliinvest.web.controllers.BubbleDataController;

public class BubbleDataFetcher {

	private static Logger logger = Logger.getLogger(BubbleDataFetcher.class);

	private static final String COLLECTION_STOCK_SIGNALS = "STOCK_SIGNALS";

	@Autowired
	DateUtil dateUtil;

	@Autowired
	private MongoTemplate mongoTemplate;

	public List<BubbleDataResponse> getTechnicalBubbleData(int timePeriod) {
		logger.info("Inside getTechnicalBubbleData()...");
		// LocalDate date =dateUtil.substractDays(dateUtil.getLocalDate(), 365 *
		// timePeriod);
		Date date = dateUtil.getDateFromLocalDate(dateUtil.substractDays(dateUtil.getLocalDate(), 365 * timePeriod));
		logger.info("Date start from :: " + date);
		Aggregation aggregation = newAggregation(Aggregation.match(Criteria.where("signalDate").gte(date)),
				/* group("signalDate","aggSignal"), */
				group("signalDate", "aggSignal").count().as("count"), project("signalDate", "count").and("aggSignal"),
				sort(Sort.Direction.ASC, "signalDate"));
		AggregationResults<BubbleData> results = mongoTemplate.aggregate(aggregation, COLLECTION_STOCK_SIGNALS,
				BubbleData.class);
		List<BubbleData> bubbleDatas = results.getMappedResults();

		return getBubbleDataResponse(bubbleDatas);
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
