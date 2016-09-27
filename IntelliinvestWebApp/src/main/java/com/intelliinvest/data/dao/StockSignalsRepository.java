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
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.data.model.StockSignalsComponents;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.util.Converter;
import com.intelliinvest.util.Helper;

@ManagedResource(objectName = "bean:name=StockSignalsRepository", description = "StockSignalsRepository")
public class StockSignalsRepository {

	private static Logger logger = Logger.getLogger(StockSignalsRepository.class);
	private static final String COLLECTION_STOCK_SIGNALS_COMPONENTS = "STOCK_SIGNALS_COMPONENTS_#MN#";
	private static final String COLLECTION_STOCK_SIGNALS = "STOCK_SIGNALS";
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	WatchListRepository watchListRepository;
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
				if (stockSignal != null) {
					// logger.debug("stcok signals : " + stockSignal);
					signalCache.put(stockSignal.getSymbol(), stockSignal);
				} else {
					logger.debug("null stock signals....");
				}
			}
			logger.debug("refreshing signalCache from DB in StockSignalRepository with size " + signalCache.size());
			watchListRepository.refreshCache();
		} else {
			logger.error("Could not refresh signalCache from DB in StockSignalRepository. STOCK_SIGNALS is empty.");
		}
	}

	public List<StockSignals> getLatestStockSignalFromDB() {
		logger.info("Inside getLatestStockSignalFromDB()...");

		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "signalDate"),
				group("symbol").first("symbol").as("symbol").first("signalDate").as("signalDate").first("signalType")
						.as("signalType").first("previousSignalType").as("previousSignalType").first("open").as("open")
						.first("signalPresent").as("signalPresent").first("oscillatorSignal").as("oscillatorSignal")
						.first("previousOscillatorSignal").as("previousOscillatorSignal")
						.first("signalPresentOscillator").as("signalPresentOscillator").first("bollingerSignal")
						.as("bollingerSignal").first("previousBollingerSignal").as("previousBollingerSignal")
						.first("signalPresentBollinger").as("signalPresentBollinger"))
								.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<StockSignals> results = mongoTemplate.aggregate(aggregation, COLLECTION_STOCK_SIGNALS,
				StockSignals.class);
		List<StockSignals> retVal = new ArrayList<StockSignals>();

		for (StockSignals stockSignal : results.getMappedResults()) {
			// logger.info("Stock Signals details are: " +
			// stockSignal.toString());
			retVal.add(stockSignal);
		}
		return retVal;
	}

	/*
	 * public static void main(String[] args) { FileSystemXmlApplicationContext
	 * context=new FileSystemXmlApplicationContext(
	 * "src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml");
	 * 
	 * StockSignalsRepository repository = new StockSignalsRepository();
	 * System.out.println(repository.getLatestStockSignalFromDB()); }
	 */
	public List<StockSignals> getStockSignals(String symbol) {
		logger.debug("Inside getStockSignalDetails...");
		return mongoTemplate.find(Query.query(Criteria.where("symbol").is(symbol)), StockSignals.class,
				COLLECTION_STOCK_SIGNALS);
	}

	public StockSignals getStockSignalsFromCache(String symbol) {
		// logger.debug("Inside getStockSignalDetails from cache...");
		return signalCache.get(symbol);
	}

	public StockSignals getStockSignals(String symbol, LocalDate signalDate) {
		logger.debug("Inside getStockSignalDetails, date & symbol..." + symbol + " and date: " + signalDate);
		return mongoTemplate.findOne(Query.query(Criteria.where("symbol").is(symbol).and("signalDate").is(signalDate)),
				StockSignals.class, COLLECTION_STOCK_SIGNALS);
	}

	public List<StockSignalsDTO> getStockSignalsComplete(String symbol, int magicnumber) throws IntelliinvestException {
		logger.debug("Inside getStockSignalComplete...");
		Query query = Query.query(Criteria.where("symbol").is(symbol));
		query.with(new Sort(Sort.Direction.DESC, "signalDate"));

		List<StockSignals> stockSignalsList = mongoTemplate.find(query, StockSignals.class, COLLECTION_STOCK_SIGNALS);
		List<StockSignalsComponents> stockSignalsComponentsList = mongoTemplate.find(
				Query.query(Criteria.where("symbol").is(symbol)), StockSignalsComponents.class,
				COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, magicnumber + ""));

		logger.debug("Stock Signals list size is: " + stockSignalsList.size()
				+ " Stock Signals Components list size is :" + stockSignalsComponentsList.size());
		return Converter.convertBO2DTO(stockSignalsComponentsList, stockSignalsList);
	}

	/*
	 * public Map<String, StockSignalsDTO> getStockSignalsComplete(Date date,
	 * int ma) { logger.debug("getting complete stock Signal for date " + date +
	 * " and ma " + ma); Map<String, StockSignalsDTO> stockSignalsDTOMap = new
	 * HashMap<String, StockSignalsDTO>();
	 * 
	 * List<StockSignals> stockSignalsList = mongoTemplate.find(
	 * Query.query(Criteria.where("signalDate").is(date)), StockSignals.class,
	 * COLLECTION_STOCK_SIGNALS);
	 * 
	 * List<StockSignalsComponents> stockSignalsComponentsList = mongoTemplate
	 * .find(Query.query(Criteria.where("signalDate").is(date)),
	 * StockSignalsComponents.class,
	 * COLLECTION_STOCK_SIGNALS_COMPONENTS.replace( MAGIC_NUMBER_STR, ma + ""));
	 * 
	 * List<StockSignalsDTO> stockSignalsDTO = Converter.convertBO2DTO(
	 * stockSignalsComponentsList, stockSignalsList);
	 * 
	 * for (StockSignalsDTO signalComponent : stockSignalsDTO) { String symbol =
	 * signalComponent.getSymbol(); if (!stockSignalsDTOMap.containsKey(symbol))
	 * { stockSignalsDTOMap.put(symbol, signalComponent); } } return
	 * stockSignalsDTOMap; }
	 */
	public Map<String, StockSignalsDTO> getStockSignalsComplete(LocalDate date, int ma) {
		logger.debug("getting complete stock Signal for date " + date + " and ma " + ma);
		Map<String, StockSignalsDTO> stockSignalsDTOMap = new HashMap<String, StockSignalsDTO>();

		List<StockSignals> stockSignalsList = mongoTemplate.find(Query.query(Criteria.where("signalDate").is(date)),
				StockSignals.class, COLLECTION_STOCK_SIGNALS);

		List<StockSignalsComponents> stockSignalsComponentsList = mongoTemplate.find(
				Query.query(Criteria.where("signalDate").is(date)), StockSignalsComponents.class,
				COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, ma + ""));

		List<StockSignalsDTO> stockSignalsDTO = Converter.convertBO2DTO(stockSignalsComponentsList, stockSignalsList);

		for (StockSignalsDTO signalComponent : stockSignalsDTO) {
			String symbol = signalComponent.getSymbol();
			if (!stockSignalsDTOMap.containsKey(symbol)) {
				stockSignalsDTOMap.put(symbol, signalComponent);
			}
		}
		return stockSignalsDTOMap;
	}

	public StockSignalsDTO getStockSignalsComplete(LocalDate date, String symbol, int ma) {
		logger.debug("getting complete stock Signal for date " + date + " and ma " + ma + " symbol:" + symbol);
		StockSignalsDTO stockSignalsDTO = null;

		StockSignals stockSignals = mongoTemplate.findOne(
				Query.query(Criteria.where("signalDate").is(date).and("symbol").is(symbol)), StockSignals.class);

		StockSignalsComponents stockSignalsComponents = mongoTemplate.findOne(
				Query.query(Criteria.where("signalDate").is(date).and("symbol").is(symbol)),
				StockSignalsComponents.class);

		if (stockSignals != null && stockSignalsComponents != null)
			stockSignalsDTO = Converter.convertBO2DTO(stockSignalsComponents, stockSignals);
		return stockSignalsDTO;
	}

	public List<StockSignalsDTO> getEODStockPriceddFromStartDate(LocalDate startDate, String symbol, int ma) {
		logger.info("Inside getEODStockPricesFromStartDate()..." + startDate);
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "eodDate"));
		query.addCriteria(Criteria.where("signalDate").gte(startDate).and("symbol").is(symbol));
		List<StockSignals> stockSignalsList = mongoTemplate.find(query, StockSignals.class, COLLECTION_STOCK_SIGNALS);

		List<StockSignalsComponents> stockSignalsComponentsList = mongoTemplate.find(
				Query.query(Criteria.where("signalDate").gte(startDate).and("symbol").is(symbol)),
				StockSignalsComponents.class, COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, ma + ""));
		if (stockSignalsList != null && stockSignalsComponentsList != null
				&& stockSignalsList.size() == stockSignalsComponentsList.size())
			return Converter.convertBO2DTO(stockSignalsComponentsList, stockSignalsList);
		else
			return null;
	}

	public void updateStockSignals(int magicNumber, List<StockSignalsDTO> stockSignalsDTOList)
			throws IntelliinvestException {
		logger.debug("Inside updateStockSignals..." + stockSignalsDTOList.size());

		List<StockSignalsComponents> stockSignalsComponentsList = new ArrayList<StockSignalsComponents>();
		List<StockSignals> stockSignalsList = new ArrayList<StockSignals>();

		Converter.convertDTO2BO(stockSignalsDTOList, stockSignalsComponentsList, stockSignalsList);
		logger.debug("SignalComponentsList size is: " + stockSignalsComponentsList.size()
				+ " and stocksignals list size is: " + stockSignalsList.size());
		if (stockSignalsComponentsList.size() == stockSignalsList.size()) {
			for (int i = 0; i < stockSignalsList.size(); i++) {
				StockSignals stockSignals = stockSignalsList.get(i);
				StockSignalsComponents stockSignalsComponents = stockSignalsComponentsList.get(i);

				// logger.debug("Stock Signals Object being iserted: "
				// + stockSignals.toString());
				//
				mongoTemplate.remove(Query.query(Criteria.where("symbol").is(stockSignals.getSymbol()).and("signalDate")
						.is(stockSignals.getSignalDate())), StockSignals.class);
				mongoTemplate.save(stockSignals, COLLECTION_STOCK_SIGNALS);

				mongoTemplate.remove(Query.query(Criteria.where("symbol").is(stockSignals.getSymbol()).and("signalDate")
						.is(stockSignals.getSignalDate())), StockSignalsComponents.class);
				mongoTemplate.save(stockSignalsComponents,
						COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, magicNumber + ""));
			}
			// refreshCache();
			// watchListRepository.refreshCache();

		} else {
			throw new IntelliinvestException("invalid input data....");
		}
	}

	public void deleteStockSignals(int magicNumber, String symbol) throws IntelliinvestException {
		logger.debug("Inside deleteStockSignals()...");
		mongoTemplate.remove(Query.query(Criteria.where("symbol").is(symbol)), COLLECTION_STOCK_SIGNALS);
		mongoTemplate.remove(Query.query(Criteria.where("symbol").is(symbol)),
				COLLECTION_STOCK_SIGNALS_COMPONENTS.replace(MAGIC_NUMBER_STR, magicNumber + ""));

	}

}