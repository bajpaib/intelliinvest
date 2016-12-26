package com.intelliinvest.data.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.time.LocalDate;
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
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.model.IndustryFundamentalAnalysis;
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.web.bo.response.IndustriesAnalysisResponse;

@ManagedResource(objectName = "bean:name=IndustryFundamentalAnalysisRepository", description = "IndustryFundamentalAnalysisRepository")

public class IndustryFundamentalAnalysisRepository {

	private static Logger logger = Logger.getLogger(IndustryFundamentalAnalysisRepository.class);
	private static final String COLLECTION_INDUSTRY_FUNDAMENTAL_ANALYSIS = "INDUSTRY_FUNDAMENTAL_ANALYSIS";
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	StockRepository stockRepository;

	@Autowired
	IndustryFundamentalsRepository industryFundamentalsRepository;

	@Autowired
	DateUtil dateUtil;

	private Map<String, IndustryFundamentalAnalysis> industryFundamentalAnalysisCache = new ConcurrentHashMap<String, IndustryFundamentalAnalysis>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<IndustryFundamentalAnalysis> industryFundamentalAnalysisList = getLatestIndustryFundamentalAnalysisFromDB();
		if (Helper.isNotNullAndNonEmpty(industryFundamentalAnalysisList)) {
			for (IndustryFundamentalAnalysis industryFundamentalAnalysis : industryFundamentalAnalysisList) {
				industryFundamentalAnalysisCache.put(industryFundamentalAnalysis.getName(),
						industryFundamentalAnalysis);
			}
			logger.info(
					"Initialised industryFundamentalAnalysisCache from DB in IndustryFundamentalAnalysisRepository with size "
							+ industryFundamentalAnalysisCache.size());
		} else {
			logger.error(
					"Could not initialise industryFundamentalsCache from DB in IndustryFundamentalAnalysisRepository.Either INDUSTRY_FUNDAMENTALS is empty or indutries are not there.");
		}
	}

	public IndustryFundamentalAnalysis getLatestIndustryFundamentalAnalysis(String industry_name) {
		IndustryFundamentalAnalysis industryFundamentalAnalysis = industryFundamentalAnalysisCache.get(industry_name);
		if (industryFundamentalAnalysis == null) {
			logger.error("Inside getIndustryFundamentalAnalysis() IndustryFundamentalAnalysis not found in cache for "
					+ industry_name);
			return null;
		}
		return industryFundamentalAnalysis;
	}

	public IndustryFundamentalAnalysis getIndustryFundamentalAnalysisFromDB(String industry_name, LocalDate date)
			throws DataAccessException {
		Query query = new Query();
		query.addCriteria(Criteria.where("todayDate").is(date).and("name").is(industry_name));
		return mongoTemplate.findOne(query, IndustryFundamentalAnalysis.class,
				COLLECTION_INDUSTRY_FUNDAMENTAL_ANALYSIS);
	}

	public void updateIndustryFundamentalsAnalysis(IndustryFundamentalAnalysis industryFundamentalAnalysis) {
		logger.debug("Inside updateIndustryFundamentalsAnalysis()...");
		Query query = new Query();
		Update update = new Update();
		update.set("name", industryFundamentalAnalysis.getName());
		update.set("alEPSPct", industryFundamentalAnalysis.getAlEPSPct());
		update.set("alCashToDebtRatio", industryFundamentalAnalysis.getAlCashToDebtRatio());
		update.set("alLeveredBeta", industryFundamentalAnalysis.getAlLeveredBeta());
		update.set("qrOperatingMargin", industryFundamentalAnalysis.getQrOperatingMargin());
		update.set("alReturnOnEquity", industryFundamentalAnalysis.getAlReturnOnEquity());
		update.set("alEPSPct_signal", industryFundamentalAnalysis.getAlEPSPct_signal());
		update.set("alCashToDebtRatio_signal", industryFundamentalAnalysis.getAlCashToDebtRatio_signal());
		update.set("alLeveredBeta_signal", industryFundamentalAnalysis.getAlLeveredBeta_signal());
		update.set("qrOperatingMargin_signal", industryFundamentalAnalysis.getQrOperatingMargin_signal());
		update.set("alReturnOnEquity_signal", industryFundamentalAnalysis.getAlReturnOnEquity_signal());
		update.set("agg_signal", industryFundamentalAnalysis.getAggSignal());
		update.set("todayDate", industryFundamentalAnalysis.getTodayDate());
		update.set("t_minus_1", industryFundamentalAnalysis.getT_minus_1());
		update.set("t_minus_2", industryFundamentalAnalysis.getT_minus_2());
		update.set("updateDate", industryFundamentalAnalysis.getUpdateDate());

		query.addCriteria(Criteria.where("todayDate").is(industryFundamentalAnalysis.getTodayDate()).and("name")
				.is(industryFundamentalAnalysis.getName()));
		mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true).upsert(true),
				IndustryFundamentals.class, COLLECTION_INDUSTRY_FUNDAMENTAL_ANALYSIS);
		refreshCache(industryFundamentalAnalysis);
	}

	public List<IndustryFundamentalAnalysis> getLatestIndustryFundamentalAnalysisFromDB() throws DataAccessException {
		logger.info("Inside getLatestIndustryFundamentalAnalysisFromDB()...");
		// retrieve record having max yearQuarter for each industry
		final Aggregation aggregation = newAggregation(sort(Sort.Direction.DESC, "todayDate"),
				group("name").first("name").as("name").first("alEPSPct").as("alEPSPct").first("alCashToDebtRatio")
						.as("alCashToDebtRatio").first("alLeveredBeta").as("alLeveredBeta").first("alReturnOnEquity")
						.as("alReturnOnEquity").first("qrOperatingMargin").as("qrOperatingMargin")
						.first("alEPSPct_signal").as("alEPSPct_signal").first("alCashToDebtRatio_signal")
						.as("alCashToDebtRatio_signal").first("alLeveredBeta_signal").as("alLeveredBeta_signal")
						.first("alReturnOnEquity_signal").as("alReturnOnEquity_signal")
						.first("qrOperatingMargin_signal").as("qrOperatingMargin_signal").first("todayDate")
						.as("todayDate").first("updateDate").as("updateDate"))
								.withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
		AggregationResults<IndustryFundamentalAnalysis> results = mongoTemplate.aggregate(aggregation,
				COLLECTION_INDUSTRY_FUNDAMENTAL_ANALYSIS, IndustryFundamentalAnalysis.class);
		return results.getMappedResults();
	}

	private void refreshCache(IndustryFundamentalAnalysis industryFundamentalAnalysis) {
		industryFundamentalAnalysisCache.put(industryFundamentalAnalysis.getName(), industryFundamentalAnalysis);
	}

	public boolean refreshAllIndustriesFundamentalAnalysis() {
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean retFlag = true;
		List<String> industries_name = stockRepository.getIndustriesName();
		logger.info("Industries are: " + industries_name);
		for (String industry_name : industries_name) {
			if (Helper.isNotNullAndNonEmpty(industry_name)) {
				try {
					IndustryFundamentalAnalysis industryFundamentalAnalysis = generateIndustryFundamentalAnalysis(
							industry_name);
					updateIndustryFundamentalsAnalysis(industryFundamentalAnalysis);
				} catch (Exception e) {
					logger.error(
							"Some exception occurred there while getting industries analysis response for industry: "
									+ industry_name + " error is:" + e.getMessage());
					retFlag = false;
				}
			} else {
				logger.error("Following Industry has not a valid value: " + industry_name);
				retFlag = false;
			}
		}
		logger.info("refresh all industries analysis has been completed...");
		return retFlag;
	}

	public IndustriesAnalysisResponse getAllIndutryFundamentalAnalysis() {
		IndustriesAnalysisResponse response = new IndustriesAnalysisResponse();
		boolean error = true;
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;

		List<String> industries_name = stockRepository.getIndustriesName();
		logger.info("Industries are: " + industries_name);
		for (String industry_name : industries_name) {
			if (Helper.isNotNullAndNonEmpty(industry_name)) {
				try {
					IndustryFundamentalAnalysis industryFundamentalAnalysis = getLatestIndustryFundamentalAnalysis(
							industry_name);
					if (industryFundamentalAnalysis != null) {
						response.getIndustriesFundamentalAnalysis().add(industryFundamentalAnalysis);

					} else {
						error = true;
						errorMsg="no data found for industries";
						logger.info("no data found for industry: " + industry_name);
					}
				} catch (Exception e) {
					logger.error(
							"Some exception occurred there while getting industries analysis response for industry: "
									+ industry_name + " error is:" + e.getMessage());
				}
			} else {
				logger.error("Following Industry has not a valid value: " + industry_name);
			}
		}

		if (response != null && !error) {
			response.setSuccess(true);
			response.setMessage("Data has been returned successfully.");
		} else {
			response.setSuccess(false);
			response.setMessage(errorMsg);
		}

		return response;
	}

	public IndustryFundamentalAnalysis getIndutryFundamentalAnalysisFromSecurityId(String securityId) {
		IndustryFundamentalAnalysis response = new IndustryFundamentalAnalysis();
		if (Helper.isNotNullAndNonEmpty(securityId)) {
			try {
				Stock stock = stockRepository.getStockById(securityId);
				if (stock == null) {
					throw new IntelliinvestException("Stock not found for id:" + securityId);
				}
				String industry_name = stock.getIndustry();
				if (!Helper.isNotNullAndNonEmpty(industry_name)) {
					throw new IntelliinvestException("Industry not present for security:" + securityId);
				}
				response = getLatestIndustryFundamentalAnalysis(industry_name);

			} catch (Exception e) {
				logger.error("Exception inside getFundamentalAnalysisTimeSeries() " + e.getMessage());
			}
		} else {
			logger.error("security id has not a valid value....");
		}

		return response;
	}

	private IndustryFundamentalAnalysis generateIndustryFundamentalAnalysis(String industry_name) {
		IndustryFundamentalAnalysis industryFundamentalAnalysisResponse = new IndustryFundamentalAnalysis();
		IndustryFundamentals industryFundamentals_t = industryFundamentalsRepository
				.getLatestIndustryFundamentals(industry_name);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		List<IndustryFundamentals> industries = null;

		IndustryFundamentals industryFundamentals_t_1 = null;
		IndustryFundamentals industryFundamentals_t_2 = null;
		String yearQuarter = industryFundamentals_t.getYearQuarter();
		int year = Integer.parseInt(yearQuarter.substring(0, 4));
		String quarter = yearQuarter.substring(4, yearQuarter.length());
		String date = "";
		String date_1 = "";
		String date_2 = "";
		String date_3 = "";
		switch (quarter) {
		case "Q1":
			date = year + "-" + "03" + "-" + "31";
			date_1 = (year - 1) + "-" + "03" + "-" + "31";
			date_2 = (year - 2) + "-" + "03" + "-" + "31";
			date_3 = (year - 3) + "-" + "03" + "-" + "31";
			break;
		case "Q2":
			date = year + "-" + "06" + "-" + "30";
			date_1 = (year - 1) + "-" + "06" + "-" + "30";
			date_2 = (year - 2) + "-" + "06" + "-" + "30";
			date_3 = (year - 3) + "-" + "06" + "-" + "30";
			break;
		case "Q3":
			date = year + "-" + "09" + "-" + "30";
			date_1 = (year - 1) + "-" + "09" + "-" + "30";
			date_2 = (year - 2) + "-" + "09" + "-" + "30";
			date_3 = (year - 3) + "-" + "09" + "-" + "30";
			break;
		case "Q4":
			date = year + "-" + "12" + "-" + "31";
			date_1 = (year - 1) + "-" + "12" + "-" + "31";
			date_2 = (year - 2) + "-" + "12" + "-" + "31";
			date_3 = (year - 3) + "-" + "12" + "-" + "31";
			break;
		}

		LocalDate localDate_1 = LocalDate.parse(date_1, dateFormat);
		LocalDate localDate_2 = LocalDate.parse(date_2, dateFormat);
		LocalDate localDate_3 = LocalDate.parse(date_3, dateFormat);
		industries = industryFundamentalsRepository.getIndustryFundamentalsFromDBAfterDate(industry_name, localDate_3);
		logger.info("industries size:" + industries.size());
		logger.info("T-1:" + localDate_1);
		logger.info("T-2:" + localDate_2);
		for (int i = industries.size() - 1; i >= 0; i--) {
			IndustryFundamentals tempIndustryFundamentals = industries.get(i);

			if (tempIndustryFundamentals.getTodayDate().equals(localDate_1)) {
				industryFundamentals_t_1 = tempIndustryFundamentals;
			} else if (tempIndustryFundamentals.getTodayDate().equals(localDate_2)) {
				industryFundamentals_t_2 = tempIndustryFundamentals;
				break;
			} else if (tempIndustryFundamentals.getTodayDate().compareTo(localDate_1) < 0
					&& industryFundamentals_t_1 == null) {
				industryFundamentals_t_1 = tempIndustryFundamentals;
			} else if (tempIndustryFundamentals.getTodayDate().compareTo(localDate_2) < 0
					&& industryFundamentals_t_2 == null) {
				industryFundamentals_t_2 = tempIndustryFundamentals;
				break;
			}
		}

		logger.info("Current Industry Analysis Data:\n" + industryFundamentals_t.toString());
		logger.info("T-1 Industry Analysis Data:\n" + industryFundamentals_t_1.toString());
		logger.info("T-2 Industry Analysis Data\n:" + industryFundamentals_t_2.toString());

		industryFundamentalAnalysisResponse.setName(industry_name);
		industryFundamentalAnalysisResponse.setAlReturnOnEquity(industryFundamentals_t.getAlReturnOnEquity());
		industryFundamentalAnalysisResponse.setAlCashToDebtRatio(industryFundamentals_t.getAlCashToDebtRatio());
		industryFundamentalAnalysisResponse.setAlEPSPct(industryFundamentals_t.getAlEPSPct());
		industryFundamentalAnalysisResponse.setAlLeveredBeta(industryFundamentals_t.getAlLeveredBeta());
		industryFundamentalAnalysisResponse.setQrOperatingMargin(industryFundamentals_t.getQrOperatingMargin());

		setSignalsData(industryFundamentalAnalysisResponse, industryFundamentals_t, industryFundamentals_t_1,
				industryFundamentals_t_2);

		industryFundamentalAnalysisResponse.setUpdateDate(dateUtil.getLocalDateTime());
		return industryFundamentalAnalysisResponse;
	}

	private void setSignalsData(IndustryFundamentalAnalysis response, IndustryFundamentals industryFundamentals_t,
			IndustryFundamentals industryFundamentals_t_1, IndustryFundamentals industryFundamentals_t_2) {

		final Map<Integer, Integer> signalsCounter = new HashMap<Integer, Integer>() {
			{
				put(1, 0);
				put(0, 0);
				put(-1, 0);
			}
		};

		response.setAlReturnOnEquity_signal(getSignalData(industryFundamentals_t.getAlReturnOnEquity(),
				industryFundamentals_t_1.getAlReturnOnEquity(), industryFundamentals_t_2.getAlReturnOnEquity(),
				signalsCounter));

		response.setAlEPSPct_signal(getSignalData(industryFundamentals_t.getAlEPSPct(),
				industryFundamentals_t_1.getAlEPSPct(), industryFundamentals_t_2.getAlEPSPct(), signalsCounter));

		response.setAlCashToDebtRatio_signal(getSignalData(industryFundamentals_t.getAlCashToDebtRatio(),
				industryFundamentals_t_1.getAlCashToDebtRatio(), industryFundamentals_t_2.getAlCashToDebtRatio(),
				signalsCounter));

		response.setAlLeveredBeta_signal(
				getSignalData(industryFundamentals_t.getAlLeveredBeta(), industryFundamentals_t_1.getAlLeveredBeta(),
						industryFundamentals_t_2.getAlLeveredBeta(), signalsCounter));

		response.setQrOperatingMargin_signal(getSignalData(industryFundamentals_t.getQrOperatingMargin(),
				industryFundamentals_t_1.getQrOperatingMargin(), industryFundamentals_t_2.getQrOperatingMargin(),
				signalsCounter));

		logger.info(signalsCounter.toString());
		if (signalsCounter.get(1) >= 3)
			response.setAggSignal(1);
		else if (signalsCounter.get(-1) >= 3) {
			response.setAggSignal(-1);
		} else {
			response.setAggSignal(0);
		}

	}

	private int getSignalData(double data, double data_1, double data_2, Map<Integer, Integer> signalsCounter) {
		if (data > data_1 && data > data_2) {
			signalsCounter.put(1, signalsCounter.get(1) + 1);
			return 1;
		} else if (data > data_1 && data < data_2) {
			signalsCounter.put(0, signalsCounter.get(0) + 1);
			return 0;
		} else {
			signalsCounter.put(-1, signalsCounter.get(-1) + 1);
			return -1;
		}
	}

}
