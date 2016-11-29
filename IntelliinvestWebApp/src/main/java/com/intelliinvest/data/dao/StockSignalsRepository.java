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
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.data.model.StockSignalsComponents;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.data.signals.MagicNumberGenerator;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.IntelliinvestConverter;
import com.intelliinvest.web.bo.response.StockPnlData;
import com.intelliinvest.web.bo.response.StockSignalsArchiveResponse;
import com.intelliinvest.web.bo.response.StockSignalsResponse;

@ManagedResource(objectName = "bean:name=StockSignalsRepository", description = "StockSignalsRepository")
public class StockSignalsRepository {

	private static Logger logger = Logger.getLogger(StockSignalsRepository.class);
	private static final String COLLECTION_STOCK_SIGNALS_COMPONENTS = "STOCK_SIGNALS_COMPONENTS_#MN#";
	private static final String COLLECTION_STOCK_SIGNALS = "STOCK_SIGNALS";
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	DateUtil dateUtil;

	@Autowired
	QuandlEODStockPriceRepository quandlEODStockPriceRepository;

	@Autowired
	StockRepository stockRepository;

	@Autowired
	MagicNumberGenerator magicNumberGenerator;

	private Map<String, StockSignals> signalCache = new ConcurrentHashMap<String, StockSignals>();
	private static String MAGIC_NUMBER_STR = "#MN#";

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		logger.debug("Initializing cache.......");
		refreshCache();
		logger.debug("Initializing cache done.......");

	}

	public void refreshCache() {
		logger.debug("refreshing cache.....");
		List<StockSignals> stockSignals = getLatestStockSignalFromDB();
		logger.debug("list size is: " + stockSignals.size());
		if (Helper.isNotNullAndNonEmpty(stockSignals)) {
			for (StockSignals stockSignal : stockSignals) {
				if (stockSignal != null && stockSignal.getSecurityId()!=null) {
					// logger.debug("stcok signals : " + stockSignal);
					signalCache.put(stockSignal.getSecurityId(), stockSignal);
				} else {
					logger.debug("null stock signals....");
				}
			}
			logger.debug("refreshing signalCache from DB in StockSignalRepository with size " + signalCache.size());
			// watchListRepository.refreshCache();
		} else {
			logger.error("Could not refresh signalCache from DB in StockSignalRepository. STOCK_SIGNALS is empty.");
		}
	}

	public void refreshCachefromTodaySignals(List<StockSignals> stockSignals) {
		logger.debug("refreshing cache for today signals.....");
		// List<StockSignals> stockSignals = getLatestStockSignalFromDB();
		logger.debug("list size is: " + stockSignals.size());
		if (Helper.isNotNullAndNonEmpty(stockSignals)) {
			for (StockSignals stockSignal : stockSignals) {
				if (stockSignal != null) {
					// logger.debug("stcok signals : " + stockSignal);
					signalCache.put(stockSignal.getSecurityId(), stockSignal);
				} else {
					logger.debug("null stock signals....");
				}
			}
			logger.debug("refreshing signalCache for today signals is done with size " + stockSignals.size());
			// watchListRepository.refreshCache();
		} else {
			logger.error(
					"Could not refresh signalCache from todays signals in StockSignalRepository. STOCK_SIGNALS is empty.");
		}
	}

	public StockSignalsArchiveResponse getStockSignalsArchive(int ma, String securityId, int timePeriod) {
		logger.debug("Inside getStockSignalDetails...from time:::" + timePeriod);
		LocalDate date = getLastDate(timePeriod);

		logger.debug("from date:" + date);
		List<StockSignals> stockSignals = mongoTemplate.find(
				Query.query(Criteria.where("securityId").is(securityId).and("signalDate").gte(date)),
				StockSignals.class, COLLECTION_STOCK_SIGNALS);

		List<StockSignalsComponents> stockSignalsComponents = mongoTemplate.find(
				Query.query(Criteria.where("securityId").is(securityId).and("signalDate").gte(date)),
				StockSignalsComponents.class, COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, ma + ""));

		if (stockSignals != null && stockSignalsComponents != null
				&& stockSignals.size() == stockSignalsComponents.size())
			return getArchiveResponse(date, securityId, stockSignals, stockSignalsComponents);
		else {
			StockSignalsArchiveResponse response = new StockSignalsArchiveResponse();
			response.setMessage("Some internal data error...");
			response.setSuccess(false);
			return response;
		}

	}

	public List<StockSignals> getLatestStockSignalFromDB() {
		logger.info("Inside getLatestStockSignalFromDB()...");

		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "signalDate"), getGroupObj())
				.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<StockSignals> results = mongoTemplate.aggregate(aggregation, COLLECTION_STOCK_SIGNALS,
				StockSignals.class);
		logger.info("return signals list size is:" + results.getMappedResults().size());
		List<StockSignals> retVal = new ArrayList<StockSignals>();

		for (StockSignals stockSignal : results.getMappedResults()) {
			// logger.info("Stock Signals details are: " +
			// stockSignal.toString());
			retVal.add(stockSignal);
		}
		return retVal;
	}

	private AggregationOperation getGroupObj() {
		return group("securityId").first("securityId").as("securityId").first("signalDate").as("signalDate")
				.first("adxSignal").as("adxSignal").first("adxSignalPresent").as("adxSignalPresent")
				.first("oscillatorSignal").as("oscillatorSignal").first("signalPresentOscillator")
				.as("signalPresentOscillator").first("bollingerSignal").as("bollingerSignal")
				.first("signalPresentBollinger").as("signalPresentBollinger").first("movingAverageSignal_SmallTerm")
				.as("movingAverageSignal_SmallTerm").first("movingAverageSignal_Main").as("movingAverageSignal_Main")
				.first("movingAverageSignal_MidTerm").as("movingAverageSignal_MidTerm")
				.first("movingAverageSignal_LongTerm").as("movingAverageSignal_LongTerm")
				.first("movingAverageSignal_SmallTerm_present").as("movingAverageSignal_SmallTerm_present")
				.first("movingAverageSignal_Main_present").as("movingAverageSignal_Main_present")
				.first("movingAverageSignal_MidTerm_present").as("movingAverageSignal_MidTerm_present")
				.first("movingAverageSignal_LongTerm_present").as("movingAverageSignal_LongTerm_present")
				.first("aggSignal").as("aggSignal").first("aggSignal_present").as("aggSignal_present")
				.first("aggSignal_previous").as("aggSignal_previous");
	}

	/*
	 * public static void main(String[] args) { FileSystemXmlApplicationContext
	 * context = new FileSystemXmlApplicationContext(
	 * "src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml");
	 * 
	 * StockSignalsRepository repository = new StockSignalsRepository();
	 * System.out.println(repository.getStockSignalsFromStartDateUsingJoin(
	 * "ABAN", 10)); }
	 */

	public StockSignalsArchiveResponse getStockSignalsDetails(int ma, String securityId, int timePeriod,
			String signalPresentStr) {
		logger.debug("Inside getStockSignalDetails...from time:::" + timePeriod);
		LocalDate date = getLastDate(timePeriod);

		logger.debug("from date:" + date);
		List<StockSignals> stockSignals = mongoTemplate.find(
				Query.query(Criteria.where("securityId").is(securityId).and("signalDate").gte(date)
						.and(signalPresentStr).is(IntelliinvestConstants.SIGNAL_PRESENT)),
				StockSignals.class, COLLECTION_STOCK_SIGNALS);

		if (stockSignals != null && stockSignals.size() > 0) {
			List<LocalDate> dates = stockSignals.parallelStream().map(StockSignals::getSignalDate)
					.collect(Collectors.toList());

			List<StockSignalsComponents> stockSignalsComponents = mongoTemplate.find(
					Query.query(Criteria.where("securityId").is(securityId).and("signalDate").in(dates)),
					StockSignalsComponents.class,
					COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, ma + ""));

			logger.info("Stock Signals size :" + stockSignals.size() + " Component List size is : "
					+ stockSignalsComponents.size());
			if (stockSignals != null && stockSignalsComponents != null
					&& stockSignals.size() == stockSignalsComponents.size())
				return getArchiveResponse(date, securityId, stockSignals, stockSignalsComponents);

		} else {
			if (stockRepository.getStockById(securityId) != null)
				return getArchiveResponse(date, securityId, null, null);

		}
		StockSignalsArchiveResponse response = new StockSignalsArchiveResponse();
		response.setMessage("Some internal data error...");
		response.setSuccess(false);
		return response;

	}

	public StockSignalsResponse getStockSignalsBySecurityId(String securityId) {
		StockPrice stockPrice = stockRepository.getStockPriceById(securityId);
		StockSignals stockSignals = signalCache.get(securityId);
		QuandlStockPrice quandlStockPrice = quandlEODStockPriceRepository.getLatestEODStockPrice(securityId);
		return IntelliinvestConverter.convertToStockSignalReponse(stockPrice, quandlStockPrice, stockSignals);

	}

	public StockSignals getStockSignalsFromCache(String securityId) {
		return signalCache.get(securityId);
	}

	public StockSignals getStockSignals(String securityId, LocalDate signalDate) {
		logger.debug("Inside getStockSignalDetails, date & securityId..." + securityId + " and date: " + signalDate);
		return mongoTemplate.findOne(
				Query.query(Criteria.where("securityId").is(securityId).and("signalDate").is(signalDate)),
				StockSignals.class, COLLECTION_STOCK_SIGNALS);
	}

	public void updateStockSignals(int ma, List<StockSignalsDTO> stockSignalsDTOList, boolean isTodaySignalsUpdate)
			throws IntelliinvestException {
		// logger.info("Inside updateStockSignals()...");
		List<StockSignalsComponents> stockSignalsComponentsList = new ArrayList<StockSignalsComponents>();
		List<StockSignals> stockSignalsList = new ArrayList<StockSignals>();

		IntelliinvestConverter.convertDTO2BO(stockSignalsDTOList, stockSignalsComponentsList, stockSignalsList);
		logger.debug("SignalComponentsList size is: " + stockSignalsComponentsList.size()
				+ " and stocksignals list size is: " + stockSignalsList.size());
		BulkOperations operation = mongoTemplate.bulkOps(BulkMode.UNORDERED, StockSignals.class);
		BulkOperations operation1 = mongoTemplate.bulkOps(BulkMode.UNORDERED, StockSignalsComponents.class,
				COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, ma + ""));
		if (stockSignalsList.size() > 0 && stockSignalsComponentsList.size() == stockSignalsList.size()) {
			// Batch inserts
			int start = -1000;
			int end = 0;
			while (end < stockSignalsDTOList.size()) {
				start = start + 1000;
				end = end + 1000;
				if (end > stockSignalsDTOList.size()) {
					end = stockSignalsDTOList.size();
				}
				List<StockSignals> stockSignals = stockSignalsList.subList(start, end);
				List<StockSignalsComponents> stockSignalsComponents = stockSignalsComponentsList.subList(start, end);

				for (int i = 0; i < stockSignals.size(); i++) {
					StockSignals stockSignal = stockSignals.get(i);
					StockSignalsComponents stockSignalsComponent = stockSignalsComponents.get(i);
					Query query = new Query();
					query.addCriteria(
							Criteria.where("signalDate").is(dateUtil.getDateFromLocalDate(stockSignal.getSignalDate()))
									.and("securityId").is(stockSignal.getSecurityId()));
					Update update = new Update();
					update.set("securityId", stockSignal.getSecurityId());
					update.set("adxSignal", stockSignal.getAdxSignal());
					update.set("signalDate", dateUtil.getDateFromLocalDate(stockSignal.getSignalDate()));
					update.set("adxSignalPresent", stockSignal.getAdxSignalPresent());
					update.set("oscillatorSignal", stockSignal.getOscillatorSignal());
					update.set("signalPresentOscillator", stockSignal.getSignalPresentOscillator());
					update.set("bollingerSignal", stockSignal.getBollingerSignal());
					update.set("signalPresentBollinger", stockSignal.getSignalPresentBollinger());
					update.set("movingAverageSignal_SmallTerm", stockSignal.getMovingAverageSignal_SmallTerm());
					update.set("movingAverageSignal_Main", stockSignal.getMovingAverageSignal_Main());
					update.set("movingAverageSignal_MidTerm", stockSignal.getMovingAverageSignal_MidTerm());
					update.set("movingAverageSignal_LongTerm", stockSignal.getMovingAverageSignal_LongTerm());
					update.set("movingAverageSignal_SmallTerm_present",
							stockSignal.getMovingAverageSignal_SmallTerm_present());
					update.set("movingAverageSignal_Main_present", stockSignal.getMovingAverageSignal_Main_present());
					update.set("movingAverageSignal_MidTerm_present",
							stockSignal.getMovingAverageSignal_MidTerm_present());
					update.set("movingAverageSignal_LongTerm_present",
							stockSignal.getMovingAverageSignal_LongTerm_present());
					update.set("aggSignal", stockSignal.getAggSignal());
					update.set("aggSignal_present", stockSignal.getAggSignal_present());
					update.set("aggSignal_previous", stockSignal.getAggSignal_previous());

					operation.upsert(query, update);

					Query query1 = new Query();
					query1.addCriteria(Criteria.where("signalDate")
							.is(dateUtil.getDateFromLocalDate(stockSignalsComponent.getSignalDate())).and("securityId")
							.is(stockSignalsComponent.getSecurityId()));
					Update update1 = new Update();
					update1.set("securityId", stockSignalsComponent.getSecurityId());
					update1.set("signalDate", dateUtil.getDateFromLocalDate(stockSignalsComponent.getSignalDate()));
					update1.set("TR", stockSignalsComponent.getTR());
					update1.set("plusDM1", stockSignalsComponent.getPlusDM1());
					update1.set("minusDM1", stockSignalsComponent.getMinusDM1());
					update1.set("TRn", stockSignalsComponent.getTRn());
					update1.set("plusDMn", stockSignalsComponent.getPlusDMn());
					update1.set("minusDMn", stockSignalsComponent.getMinusDMn());
					update1.set("plusDIn", stockSignalsComponent.getPlusDIn());
					update1.set("minusDIn", stockSignalsComponent.getMinusDIn());
					update1.set("diffDIn", stockSignalsComponent.getDiffDIn());
					update1.set("sumDIn", stockSignalsComponent.getSumDIn());
					update1.set("DX", stockSignalsComponent.getDX());
					update1.set("ADXn", stockSignalsComponent.getADXn());
					update1.set("splitMultiplier", stockSignalsComponent.getSplitMultiplier());
					update1.set("signalDate", dateUtil.getDateFromLocalDate(stockSignalsComponent.getSignalDate()));
					update1.set("high10Day", stockSignalsComponent.getHigh10Day());
					update1.set("low10Day", stockSignalsComponent.getLow10Day());
					update1.set("range10Day", stockSignalsComponent.getRange10Day());
					update1.set("stochastic10Day", stockSignalsComponent.getStochastic10Day());
					update1.set("percentKFlow", stockSignalsComponent.getPercentKFlow());
					update1.set("percentDFlow", stockSignalsComponent.getPercentDFlow());
					update1.set("sma", stockSignalsComponent.getSma());
					update1.set("upperBound", stockSignalsComponent.getUpperBound());
					update1.set("lowerBound", stockSignalsComponent.getLowerBound());
					update1.set("bandwidth", stockSignalsComponent.getBandwidth());
					update1.set("movingAverage_5", stockSignalsComponent.getMovingAverage_5());
					update1.set("movingAverage_10", stockSignalsComponent.getMovingAverage_10());
					update1.set("movingAverage_15", stockSignalsComponent.getMovingAverage_15());
					update1.set("movingAverage_25", stockSignalsComponent.getMovingAverage_25());
					update1.set("movingAverage_50", stockSignalsComponent.getMovingAverage_50());

					operation1.upsert(query1, update1);

				}
				com.mongodb.BulkWriteResult result = operation.execute();
				com.mongodb.BulkWriteResult result1 = operation1.execute();

				// logger.debug("Update count:" + result.getModifiedCount() + "
				// Inserted Count:"
				// + result.getInsertedCount() + " removed count:" +
				// result.getRemovedCount() + " matched count:"
				// + result.getMatchedCount());
				// logger.debug("Update count:" + result1.getModifiedCount() + "
				// Inserted Count:"
				// + result1.getInsertedCount() + " removed count:" +
				// result1.getRemovedCount() + " matched count:"
				// + result1.getMatchedCount());
			}
			if (isTodaySignalsUpdate) {
				refreshCachefromTodaySignals(stockSignalsList);
			}
		} else {
			throw new IntelliinvestException("invalid input data....");
		}
	}

	public Map<String, List<StockSignalsDTO>> getStockSignalsFromStartDate(LocalDate date, int ma) {
		// logger.debug("getting complete stock Signal from start date " + date
		// + " and ma " + ma);
		Map<String, List<StockSignalsDTO>> stockSignalsDTOMap = null;
		try {
			stockSignalsDTOMap = new HashMap<String, List<StockSignalsDTO>>();
			Query query = new Query();
			query.with(new Sort(Sort.Direction.ASC, "signalDate"));
			query.addCriteria(Criteria.where("signalDate").gte(date));

			List<StockSignals> stockSignalsList = mongoTemplate.find(query, StockSignals.class,
					COLLECTION_STOCK_SIGNALS);

			List<StockSignalsComponents> stockSignalsComponentsList = mongoTemplate.find(query,
					StockSignalsComponents.class,
					COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, ma + ""));

			List<StockSignalsDTO> stockSignalsDTO = IntelliinvestConverter.convertBO2DTO(stockSignalsComponentsList,
					stockSignalsList);

			for (StockSignalsDTO signalComponent : stockSignalsDTO) {
				String securityId = signalComponent.getSecurityId();
				List<StockSignalsDTO> stockSignalsDTOs = stockSignalsDTOMap.get(securityId);
				if (stockSignalsDTOs == null) {
					stockSignalsDTOs = new ArrayList<StockSignalsDTO>();
					stockSignalsDTOMap.put(securityId, stockSignalsDTOs);
				}
				stockSignalsDTOs.add(signalComponent);
			}
		} catch (Exception e) {
			logger.info("Exception while getting stock signals for all stocks...");
		}
		return stockSignalsDTOMap;
	}

	public List<StockSignalsDTO> getStockSignalsFromStartDate(LocalDate startDate, String securityId, int ma) {
		// logger.info("Inside getStockSignalsFromStartDate()..." + startDate);
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "signalDate"));
		query.addCriteria(Criteria.where("signalDate").gte(startDate).and("securityId").is(securityId));
		List<StockSignals> stockSignalsList = mongoTemplate.find(query, StockSignals.class, COLLECTION_STOCK_SIGNALS);

		List<StockSignalsComponents> stockSignalsComponentsList = mongoTemplate.find(query,
				StockSignalsComponents.class, COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, ma + ""));

		return IntelliinvestConverter.convertBO2DTO(stockSignalsComponentsList, stockSignalsList);

	}

	public List<StockSignals> getTechnicalAnalysisData() {
		List<StockSignals> signalList = new ArrayList<StockSignals>(signalCache.values());
		List<StockSignals> retList = signalList.parallelStream().filter(s -> ((s.getAdxSignalPresent() != null
				&& s.getAdxSignalPresent().equals(IntelliinvestConstants.SIGNAL_PRESENT))
				|| (s.getSignalPresentBollinger() != null
						&& s.getSignalPresentBollinger().equals(IntelliinvestConstants.SIGNAL_PRESENT))
				|| (s.getSignalPresentOscillator() != null
						&& s.getSignalPresentOscillator().equals(IntelliinvestConstants.SIGNAL_PRESENT))
				|| (s.getMovingAverageSignal_Main_present() != null
						&& s.getMovingAverageSignal_Main_present().equals(IntelliinvestConstants.SIGNAL_PRESENT))))
				.collect(Collectors.toList());

		List<String> nullList = signalList.stream()
				.filter(s -> (s.getAdxSignalPresent() == null || s.getSignalPresentBollinger() == null
						|| s.getSignalPresentOscillator() == null || s.getMovingAverageSignal_Main_present() == null))
				.map(StockSignals::getSecurityId).collect(Collectors.toList());

		logger.info("Null Signals list size is :" + nullList.size());
		logger.info("Signals with some null signals are: " + nullList.toString());
		logger.debug("Stock-Signals list size is: " + retList.size());
		return retList;

	}

	private LocalDate getLastDate(int timePeriod) {
		return dateUtil.substractDays(dateUtil.getLocalDate(), 365 * timePeriod);
	}

	private StockSignalsArchiveResponse getArchiveResponse(LocalDate date, String securityId,
			List<StockSignals> stockSignalsList, List<StockSignalsComponents> stockSignalsComponentsList) {

		StockSignalsArchiveResponse stockSignalsArchiveResponse = new StockSignalsArchiveResponse();
		stockSignalsArchiveResponse.setStockSignalsList(stockSignalsList);
		stockSignalsArchiveResponse.setSecurityId(securityId);

		List<StockSignalsDTO> stockSignalsDTOs = IntelliinvestConverter.convertBO2DTO(stockSignalsComponentsList,
				stockSignalsList);

		List<QuandlStockPrice> quandlStockPrices = quandlEODStockPriceRepository.getStockPricesFromStartDate(securityId,
				date);

		Map<LocalDate, Double> priceMap = new HashMap<LocalDate, Double>();
		QuandlStockPrice lastQuandlStockPrice = null;
		QuandlStockPrice firstQuandlStockPrice = quandlStockPrices.get(0);
		for (QuandlStockPrice quandlStockPrice : quandlStockPrices) {
			priceMap.put(quandlStockPrice.getEodDate(), quandlStockPrice.getClose());
			lastQuandlStockPrice = quandlStockPrice;
		}

		// logger.info(priceMap.toString());
		List<StockSignalsDTO> stockSignalsDTOsWithADXSignalPresnt = new ArrayList<StockSignalsDTO>();
		List<StockSignalsDTO> stockSignalsDTOsWithOscSignalPresnt = new ArrayList<StockSignalsDTO>();
		List<StockSignalsDTO> stockSignalsDTOsWithBollSignalPresnt = new ArrayList<StockSignalsDTO>();
		List<StockSignalsDTO> stockSignalsDTOsWithMovingAveragePresnt = new ArrayList<StockSignalsDTO>();
		List<StockSignalsDTO> stockSignalsDTOsWithMovingAverageLongTermPresnt = new ArrayList<StockSignalsDTO>();
		
		logger.info("Stock Signals list size is :" + stockSignalsDTOs.size());
		StockSignalsDTO lastStockSignalsDTO = null;
		for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOs) {
			if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getAdxSignalPresent())) {
				stockSignalsDTOsWithADXSignalPresnt.add(stockSignalsDTO);
			}

			if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getSignalPresentBollinger())) {
				stockSignalsDTOsWithBollSignalPresnt.add(stockSignalsDTO);
			}

			if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getSignalPresentOscillator())) {
				stockSignalsDTOsWithOscSignalPresnt.add(stockSignalsDTO);
			}
			if (IntelliinvestConstants.SIGNAL_PRESENT
					.equals(stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main_present())) {
				stockSignalsDTOsWithMovingAveragePresnt.add(stockSignalsDTO);
			}
			if (IntelliinvestConstants.SIGNAL_PRESENT
					.equals(stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_LongTerm_present())) {
				stockSignalsDTOsWithMovingAverageLongTermPresnt.add(stockSignalsDTO);
			}
			// logger.debug("Stock Signals object: " +
			// stockSignalsDTO.toString());
			lastStockSignalsDTO = stockSignalsDTO;
		}

		stockSignalsArchiveResponse.setAdxPnl(Helper.formatDecimalNumber(getPercentagePnlData(magicNumberGenerator.getPnlADX(priceMap,
				stockSignalsDTOsWithADXSignalPresnt, lastQuandlStockPrice, lastStockSignalsDTO),firstQuandlStockPrice)));

		stockSignalsArchiveResponse
				.setOscillatorPnl(Helper.formatDecimalNumber(getPercentagePnlData(magicNumberGenerator.getPnlOscillator(priceMap,
						stockSignalsDTOsWithOscSignalPresnt, lastQuandlStockPrice, lastStockSignalsDTO),firstQuandlStockPrice)));

		stockSignalsArchiveResponse
				.setBollingerPnl(Helper.formatDecimalNumber(getPercentagePnlData(magicNumberGenerator.getPnlBollinger(priceMap,
						stockSignalsDTOsWithBollSignalPresnt, lastQuandlStockPrice, lastStockSignalsDTO),firstQuandlStockPrice)));

		stockSignalsArchiveResponse
				.setMovingAveragePnl(Helper.formatDecimalNumber(getPercentagePnlData(magicNumberGenerator.getPnlMovingAverage(priceMap,
						stockSignalsDTOsWithMovingAveragePresnt, lastQuandlStockPrice, lastStockSignalsDTO),firstQuandlStockPrice)));

		stockSignalsArchiveResponse
		.setMovingAvgLongTermPnl(Helper.formatDecimalNumber(getPercentagePnlData(magicNumberGenerator.getPnlMovingAverageLongTerm(priceMap,
				stockSignalsDTOsWithMovingAverageLongTermPresnt, lastQuandlStockPrice, lastStockSignalsDTO),firstQuandlStockPrice)));

		
		QuandlStockPrice stockPrice_first = quandlStockPrices.get(0);
		QuandlStockPrice stockPrice_last = quandlStockPrices.get(quandlStockPrices.size() - 1);

		double holdBuyPnl = ((stockPrice_last.getClose() - stockPrice_first.getClose()) / stockPrice_first.getClose())
				* 100;

		stockSignalsArchiveResponse.setHoldBuyPnl(Helper.formatDecimalNumber(holdBuyPnl));
		stockSignalsArchiveResponse.setSuccess(true);
		stockSignalsArchiveResponse.setMessage("Data has been returned successfully...");

		QuandlStockPrice quandlStockPrice = quandlEODStockPriceRepository.getLatestEODStockPrice(securityId);
		StockPrice stockPrice = stockRepository.getStockPriceById(securityId);

		setPriceInformation(stockSignalsArchiveResponse, quandlStockPrice, stockPrice);

		return stockSignalsArchiveResponse;
	}

	private Double getPercentagePnlData(Double pnlVal, QuandlStockPrice firstQuandlStockPrice) {
		return (pnlVal/firstQuandlStockPrice.getClose())*100;
	}

	private void setPriceInformation(StockSignalsArchiveResponse stockSignalsArchiveResponse,
			QuandlStockPrice quandlStockPrice, StockPrice stockPrice) {
		if (stockPrice != null) {
			stockSignalsArchiveResponse.setCurrentPrice(stockPrice.getCurrentPrice());
			stockSignalsArchiveResponse.setCp(stockPrice.getCp());
			stockSignalsArchiveResponse.setCurrentPriceExchange(stockPrice.getExchange());
			stockSignalsArchiveResponse.setCurrentPriceUpdateDate(stockPrice.getUpdateDate());
		}
		if (quandlStockPrice != null) {
			stockSignalsArchiveResponse.setEodDate(quandlStockPrice.getEodDate());
			stockSignalsArchiveResponse.setEodPrice(quandlStockPrice.getClose());
			stockSignalsArchiveResponse.setEodPriceExchange(quandlStockPrice.getExchange());
			stockSignalsArchiveResponse.setEodPriceUpdateDate(quandlStockPrice.getUpdateDate());
		}
	}

	public StockSignals getStockSignalsForWatchList(String securityId) {
		final Aggregation aggregation = newAggregation(
				Aggregation.match(Criteria.where("securityId").is(securityId).and("aggSignal_present").is("Y")),
				sort(Sort.Direction.DESC, "signalDate"), getGroupObj())
						.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<StockSignals> results = mongoTemplate.aggregate(aggregation, COLLECTION_STOCK_SIGNALS,
				StockSignals.class);
		logger.info("return result size is::" + results.getMappedResults().size());
		if (results.getMappedResults().size() > 0)
			return results.getMappedResults().get(0);
		else
			return null;
	}
	
	public StockPnlData getStockPnlData(Integer ma, String securityId, int timePeriod) {
		StockPnlData stockPnlData = new StockPnlData();
		stockPnlData.setSecurityId(securityId);
		LocalDate date = getLastDate(timePeriod);

		logger.debug("from date:" + date);
		List<StockSignals> stockSignals = mongoTemplate.find(
				Query.query(Criteria.where("securityId").is(securityId).and("signalDate").gte(date)),
				StockSignals.class, COLLECTION_STOCK_SIGNALS);

		List<StockSignalsComponents> stockSignalsComponents = mongoTemplate.find(
				Query.query(Criteria.where("securityId").is(securityId).and("signalDate").gte(date)),
				StockSignalsComponents.class, COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, ma + ""));

		List<StockSignalsDTO> stockSignalsDTOs = IntelliinvestConverter.convertBO2DTO(stockSignalsComponents,
				stockSignals);

		logger.info("DTOs list size is : " + stockSignalsDTOs.size());
		if (stockSignalsDTOs != null && !stockSignalsDTOs.isEmpty()) {

			List<QuandlStockPrice> quandlStockPrices = quandlEODStockPriceRepository
					.getStockPricesFromStartDate(securityId, date);

			Map<LocalDate, Double> priceMap = new HashMap<LocalDate, Double>();
			QuandlStockPrice lastQuandlStockPrice = null;
			for (QuandlStockPrice quandlStockPrice : quandlStockPrices) {
				priceMap.put(quandlStockPrice.getEodDate(), quandlStockPrice.getClose());
				lastQuandlStockPrice = quandlStockPrice;
			}

//			logger.info(priceMap.toString());
			List<StockSignalsDTO> stockSignalsDTOsWithADXSignalPresnt = new ArrayList<StockSignalsDTO>();
			List<StockSignalsDTO> stockSignalsDTOsWithOscSignalPresnt = new ArrayList<StockSignalsDTO>();
			List<StockSignalsDTO> stockSignalsDTOsWithBollSignalPresnt = new ArrayList<StockSignalsDTO>();
			List<StockSignalsDTO> stockSignalsDTOsWithMovingAveragePresnt = new ArrayList<StockSignalsDTO>();
			List<StockSignalsDTO> stockSignalsDTOsWithAggSignalPresnt = new ArrayList<StockSignalsDTO>();

			StockSignalsDTO lastStockSignalsDTO = null;
			for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOs) {
				if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getAdxSignalPresent())) {
					stockSignalsDTOsWithADXSignalPresnt.add(stockSignalsDTO);
				}

				if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getSignalPresentBollinger())) {
					stockSignalsDTOsWithBollSignalPresnt.add(stockSignalsDTO);
				}

				if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getSignalPresentOscillator())) {
					stockSignalsDTOsWithOscSignalPresnt.add(stockSignalsDTO);
				}
				if (IntelliinvestConstants.SIGNAL_PRESENT
						.equals(stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main_present())) {
					stockSignalsDTOsWithMovingAveragePresnt.add(stockSignalsDTO);
				}
				if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getAggSignal_present())) {
					stockSignalsDTOsWithAggSignalPresnt.add(stockSignalsDTO);
				}

				lastStockSignalsDTO = stockSignalsDTO;
			}

			stockPnlData.setAdxPnl(Helper.formatDecimalNumber(magicNumberGenerator.getPnlADX(priceMap,
					stockSignalsDTOsWithADXSignalPresnt, lastQuandlStockPrice, lastStockSignalsDTO)));

			stockPnlData.setOscillatorPnl(Helper.formatDecimalNumber(magicNumberGenerator.getPnlOscillator(priceMap,
					stockSignalsDTOsWithOscSignalPresnt, lastQuandlStockPrice, lastStockSignalsDTO)));

			stockPnlData.setBollingerPnl(Helper.formatDecimalNumber(magicNumberGenerator.getPnlBollinger(priceMap,
					stockSignalsDTOsWithBollSignalPresnt, lastQuandlStockPrice, lastStockSignalsDTO)));

			stockPnlData
					.setMovingAveragePnl(Helper.formatDecimalNumber(magicNumberGenerator.getPnlMovingAverage(priceMap,
							stockSignalsDTOsWithMovingAveragePresnt, lastQuandlStockPrice, lastStockSignalsDTO)));

			stockPnlData.setAggPnl(Helper.formatDecimalNumber(magicNumberGenerator.getPnlAgg(priceMap,
					stockSignalsDTOsWithAggSignalPresnt, lastQuandlStockPrice, lastStockSignalsDTO)));

			stockPnlData.setSuccess(true);
			stockPnlData.setMsg("Data has been returned successfully.");
		} else {
			stockPnlData.setSuccess(false);
			stockPnlData.setMsg("Some Internal error occurred...");
		}

		return stockPnlData;
	}
}