package com.intelliinvest.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.intelliinvest.data.model.ForecastedStockPrice;
import com.intelliinvest.data.model.IndustryFundamentals;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockFundamentalAnalysis;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.data.model.StockSignalsComponents;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.data.model.User;
import com.intelliinvest.web.bo.response.ForecastedStockPriceResponse;
import com.intelliinvest.web.bo.response.IndustryFundamentalsResponse;
import com.intelliinvest.web.bo.response.StockFundamentalAnalysisResponse;
import com.intelliinvest.web.bo.response.StockPriceResponse;
import com.intelliinvest.web.bo.response.StockResponse;
import com.intelliinvest.web.bo.response.UserResponse;

public class IntelliinvestConverter {

	public static List<UserResponse> convertUsersList(List<User> userDetails) {
		List<UserResponse> userResponseList = new ArrayList<UserResponse>();
		if (userDetails != null) {
			for (User user : userDetails) {
				userResponseList.add(getUserResponse(user));
			}
		}
		return userResponseList;
	}

	public static UserResponse getUserResponse(User user) {
		UserResponse userResponse = new UserResponse();
		userResponse.setUserId(user.getUserId());
		userResponse.setUsername(user.getUsername());
		userResponse.setPhone(user.getPhone());
		userResponse.setPlan(user.getPlan());
		userResponse.setUserType(user.getUserType());
		userResponse.setActive(user.getActive());
		userResponse.setActivationCode(user.getActivationCode());
		userResponse.setCreateDate(user.getCreateDate());
		userResponse.setUpdateDate(user.getUpdateDate());
		userResponse.setRenewalDate(user.getRenewalDate());
		userResponse.setExpiryDate(user.getExpiryDate());
		userResponse.setLoggedIn(user.getLoggedIn());
		userResponse.setLastLoginDate(user.getLastLoginDate());
		userResponse.setSendNotification(user.getSendNotification());
		return userResponse;
	}

	public static List<StockResponse> convertStockList(List<Stock> stocks) {
		List<StockResponse> stockResponseList = new ArrayList<StockResponse>();
		if (stocks != null) {
			for (Stock stock : stocks) {
				stockResponseList.add(getStockResponse(stock));
			}
		}

		stockResponseList.sort(new Comparator<StockResponse>() {
			public int compare(StockResponse item1, StockResponse item2) {
				return item1.getSecurityId().compareTo(item2.getSecurityId());
			}
		});
		return stockResponseList;
	}

	public static StockResponse getStockResponse(Stock stock) {
		StockResponse stockResponse = new StockResponse();
		stockResponse.setSecurityId(stock.getSecurityId());
		stockResponse.setBseCode(stock.getBseCode());
		stockResponse.setNseCode(stock.getNseCode());
		stockResponse.setFundamentalCode(stock.getFundamentalCode());
		stockResponse.setName(stock.getName());
		stockResponse.setIsin(stock.getIsin());
		stockResponse.setIndustry(stock.getIndustry());
		stockResponse.setWorldStock(stock.isWorldStock());
		stockResponse.setNiftyStock(stock.isNiftyStock());
		stockResponse.setNseStock(stock.isNseStock());
		stockResponse.setUpdateDate(stock.getUpdateDate());
		stockResponse.setSuccess(true);
		return stockResponse;
	}

	public static List<StockPriceResponse> convertStockPriceList(List<StockPrice> prices,
			Map<String, QuandlStockPrice> quandlStockPrices) {
		List<StockPriceResponse> stockResponseList = new ArrayList<StockPriceResponse>();
		if (prices != null) {
			for (StockPrice price : prices) {
				stockResponseList.add(getStockPriceResponse(price, quandlStockPrices.get(price.getSecurityId())));
			}
		}

		stockResponseList.sort(new Comparator<StockPriceResponse>() {
			public int compare(StockPriceResponse item1, StockPriceResponse item2) {
				return item1.getSecurityId().compareTo(item2.getSecurityId());
			}
		});

		return stockResponseList;
	}

	public static StockPriceResponse getStockPriceResponse(StockPrice price, QuandlStockPrice quandlStockPrice) {
		StockPriceResponse stockPriceResponse = new StockPriceResponse();
		stockPriceResponse.setSecurityId(price.getSecurityId());
		stockPriceResponse.setCp(MathUtil.round(price.getCp()));
		stockPriceResponse.setCurrentPrice(MathUtil.round(price.getCurrentPrice()));
		stockPriceResponse.setCurrentPriceExchange(price.getExchange());
		stockPriceResponse.setCurrentPriceUpdateDate(price.getUpdateDate());
		if (quandlStockPrice != null) {
			stockPriceResponse.setEodPrice(MathUtil.round(quandlStockPrice.getClose()));
			stockPriceResponse.setEodDate(quandlStockPrice.getEodDate());
			stockPriceResponse.setEodPriceExchange(quandlStockPrice.getExchange());
			stockPriceResponse.setEodPriceUpdateDate(quandlStockPrice.getUpdateDate());
		}
		stockPriceResponse.setSuccess(true);
		return stockPriceResponse;
	}

	public static List<ForecastedStockPriceResponse> convertForecastedStockPriceList(
			List<ForecastedStockPrice> prices) {
		List<ForecastedStockPriceResponse> responseList = new ArrayList<ForecastedStockPriceResponse>();
		if (prices != null) {
			for (ForecastedStockPrice price : prices) {
				responseList.add(getForecastedStockPriceResponse(price));
			}
		}
		responseList.sort(new Comparator<ForecastedStockPriceResponse>() {
			public int compare(ForecastedStockPriceResponse item1, ForecastedStockPriceResponse item2) {
				return item1.getSecurityId().compareTo(item2.getSecurityId());
			}
		});
		return responseList;
	}

	public static ForecastedStockPriceResponse getForecastedStockPriceResponse(ForecastedStockPrice price) {
		ForecastedStockPriceResponse response = new ForecastedStockPriceResponse();
		response.setMonthlyForecastDate(price.getMonthlyForecastDate());
		response.setMonthlyForecastPrice(MathUtil.round(price.getMonthlyForecastPrice()));
		response.setSecurityId(price.getSecurityId());
		response.setTodayDate(price.getTodayDate());
		response.setTomorrowForecastDate(price.getTomorrowForecastDate());
		response.setTomorrowForecastPrice(MathUtil.round(price.getTomorrowForecastPrice()));
		response.setUpdateDate(price.getUpdateDate());
		response.setWeeklyForecastDate(price.getWeeklyForecastDate());
		response.setWeeklyForecastPrice(MathUtil.round(price.getWeeklyForecastPrice()));
		response.setSuccess(true);
		return response;
	}

	public static StockFundamentalAnalysisResponse getStockFundamentalAnalysisResponse(StockFundamentalAnalysis stock) {
		StockFundamentalAnalysisResponse response = new StockFundamentalAnalysisResponse();
		response.setSecurityId(stock.getSecurityId());
		response.setYearQuarter(stock.getYearQuarter());
		response.setAlEPSPct(MathUtil.round(stock.getAlEPSPct()));
		response.setAlCashToDebtRatio(MathUtil.round(stock.getAlCashToDebtRatio()));
		response.setAlCurrentRatio(MathUtil.round(stock.getAlCurrentRatio()));
		response.setAlEquityToAssetRatio(MathUtil.round(stock.getAlEquityToAssetRatio()));
		response.setAlDebtToCapitalRatio(MathUtil.round(stock.getAlDebtToCapitalRatio()));
		response.setAlLeveredBeta(MathUtil.round(stock.getAlLeveredBeta()));
		response.setAlReturnOnEquity(MathUtil.round(stock.getAlReturnOnEquity()));
		response.setAlSolvencyRatio(MathUtil.round(stock.getAlSolvencyRatio()));
		response.setAlCostOfEquity(MathUtil.round(stock.getAlCostOfEquity()));
		response.setAlCostOfDebt(MathUtil.round(stock.getAlCostOfDebt()));
		response.setQrEBIDTAMargin(MathUtil.round(stock.getQrEBIDTAMargin()));
		response.setQrOperatingMargin(MathUtil.round(stock.getQrOperatingMargin()));
		response.setQrNetMargin(MathUtil.round(stock.getQrNetMargin()));
		response.setQrDividendPercent(MathUtil.round(stock.getQrDividendPercent()));
		response.setSummary(stock.getSummary());
		response.setTodayDate(stock.getTodayDate());
		response.setUpdateDate(stock.getUpdateDate());
		response.setSuccess(true);
		return response;
	}

	public static IndustryFundamentalsResponse getIndustryFundamentalsResponse(IndustryFundamentals industry) {
		IndustryFundamentalsResponse response = new IndustryFundamentalsResponse();
		response.setName(industry.getName());
		response.setYearQuarter(industry.getYearQuarter());
		response.setAlMarketCap(MathUtil.round(industry.getAlMarketCap()));
		response.setAlBookValuePerShare(MathUtil.round(industry.getAlBookValuePerShare()));
		response.setAlEarningPerShare(MathUtil.round(industry.getAlEarningPerShare()));
		response.setAlEPSPct(MathUtil.round(industry.getAlEPSPct()));
		response.setAlPriceToEarning(MathUtil.round(industry.getAlPriceToEarning()));
		response.setAlCashToDebtRatio(MathUtil.round(industry.getAlCashToDebtRatio()));
		response.setAlCurrentRatio(MathUtil.round(industry.getAlCurrentRatio()));
		response.setAlEquityToAssetRatio(MathUtil.round(industry.getAlEquityToAssetRatio()));
		response.setAlDebtToCapitalRatio(MathUtil.round(industry.getAlDebtToCapitalRatio()));
		response.setAlLeveredBeta(MathUtil.round(industry.getAlLeveredBeta()));
		response.setAlReturnOnEquity(MathUtil.round(industry.getAlReturnOnEquity()));
		response.setAlSolvencyRatio(MathUtil.round(industry.getAlSolvencyRatio()));
		response.setAlCostOfEquity(MathUtil.round(industry.getAlCostOfEquity()));
		response.setAlCostOfDebt(MathUtil.round(industry.getAlCostOfDebt()));
		response.setQrEBIDTAMargin(MathUtil.round(industry.getQrEBIDTAMargin()));
		response.setQrOperatingMargin(MathUtil.round(industry.getQrOperatingMargin()));
		response.setQrNetMargin(MathUtil.round(industry.getQrNetMargin()));
		response.setQrDividendPercent(MathUtil.round(industry.getQrDividendPercent()));
		response.setTodayDate(industry.getTodayDate());
		response.setUpdateDate(industry.getUpdateDate());
		response.setSuccess(true);
		return response;
	}

	public static void convertDTO2BO(List<StockSignalsDTO> stockSignalsDTOList,
			List<StockSignalsComponents> stockSignalsComponentsList, List<StockSignals> stockSignalsList) {
		for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOList) {
			StockSignalsComponents stockSignalsComponents = new StockSignalsComponents(stockSignalsDTO.getSymbol(),
					stockSignalsDTO.getTR(), stockSignalsDTO.getPlusDM1(), stockSignalsDTO.getMinusDM1(),
					stockSignalsDTO.getTRn(), stockSignalsDTO.getPlusDMn(), stockSignalsDTO.getMinusDMn(),
					stockSignalsDTO.getPlusDIn(), stockSignalsDTO.getMinusDIn(), stockSignalsDTO.getDiffDIn(),
					stockSignalsDTO.getSumDIn(), stockSignalsDTO.getDX(), stockSignalsDTO.getADXn(),
					stockSignalsDTO.getSplitMultiplier(), stockSignalsDTO.getSignalDate(),
					stockSignalsDTO.getHigh10Day(), stockSignalsDTO.getLow10Day(), stockSignalsDTO.getRange10Day(),
					stockSignalsDTO.getStochastic10Day(), stockSignalsDTO.getPercentKFlow(),
					stockSignalsDTO.getPercentDFlow(), stockSignalsDTO.getSma(), stockSignalsDTO.getUpperBound(),
					stockSignalsDTO.getLowerBound(), stockSignalsDTO.getBandwidth(),
					stockSignalsDTO.getMovingAverageComponents().getMovingAverage_5(),
					stockSignalsDTO.getMovingAverageComponents().getMovingAverage_10(),
					stockSignalsDTO.getMovingAverageComponents().getMovingAverage_15(),
					stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25(),
					stockSignalsDTO.getMovingAverageComponents().getMovingAverage_50());
			StockSignals stockSignals = new StockSignals(stockSignalsDTO.getSymbol(),
					/* stockSignalsDTO.getPreviousSignalType(), */stockSignalsDTO.getSignalType(),
					stockSignalsDTO.getSignalDate(), stockSignalsDTO.getSignalPresent(),
					stockSignalsDTO.getOscillatorSignal(), /*
															 * stockSignalsDTO.
															 * getPreviousOscillatorSignal
															 * (),
															 */
					stockSignalsDTO.getSignalPresentOscillator(), stockSignalsDTO.getBollingerSignal(),
					/* stockSignalsDTO.getPreviousBollingerSignal(), */stockSignalsDTO.getSignalPresentBollinger(),
					stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_SmallTerm(),
					stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main(),
					stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_MidTerm(),
					stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_LongTerm(),
					stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_SmallTerm_present(),
					stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main_present(),
					stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_MidTerm_present(),
					stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_LongTerm_present(),
					stockSignalsDTO.getAggSignal(), stockSignalsDTO.getAggSignal_present(),
					stockSignalsDTO.getAggSignal_previous());

			stockSignalsComponentsList.add(stockSignalsComponents);
			stockSignalsList.add(stockSignals);
		}
	}

	public static List<StockSignalsDTO> convertBO2DTO(List<StockSignalsComponents> stockSignalsComponentsList,
			List<StockSignals> stockSignalsList) {
		List<StockSignalsDTO> stockSignalsDTOList = new ArrayList<StockSignalsDTO>();
		for (int i = 0; i < stockSignalsList.size(); i++) {
			StockSignals stockSignals = stockSignalsList.get(i);
			StockSignalsComponents stockSignalsComponents = stockSignalsComponentsList.get(i);
			StockSignalsDTO stockSignalsDTO = new StockSignalsDTO();
			StockSignalsDTO.MovingAverageSignals movingAverageSignals = stockSignalsDTO.new MovingAverageSignals(
					stockSignals.getMovingAverageSignal_SmallTerm(), stockSignals.getMovingAverageSignal_Main(),
					stockSignals.getMovingAverageSignal_MidTerm(), stockSignals.getMovingAverageSignal_LongTerm(),
					stockSignals.getMovingAverageSignal_SmallTerm_present(),
					stockSignals.getMovingAverageSignal_Main_present(),
					stockSignals.getMovingAverageSignal_MidTerm_present(),
					stockSignals.getMovingAverageSignal_LongTerm_present());

			StockSignalsDTO.MovingAverageComponents movingAverageComponents = stockSignalsDTO.new MovingAverageComponents(
					stockSignalsComponents.getMovingAverage_5(), stockSignalsComponents.getMovingAverage_10(),
					stockSignalsComponents.getMovingAverage_15(), stockSignalsComponents.getMovingAverage_25(),
					stockSignalsComponents.getMovingAverage_50());

			stockSignalsDTO = new StockSignalsDTO(stockSignals.getSymbol(),
					/* stockSignals.getPreviousSignalType(), */stockSignals.getSignalType(),
					stockSignals.getSignalDate(), stockSignals.getSignalPresent(), stockSignals.getOscillatorSignal(),
					/* stockSignals.getPreviousOscillatorSignal(), */stockSignals.getSignalPresentOscillator(),
					stockSignals.getBollingerSignal(), /*
														 * stockSignals.
														 * getPreviousBollingerSignal
														 * (),
														 */
					stockSignals.getSignalPresentBollinger(), stockSignalsComponents.getTR(),
					stockSignalsComponents.getPlusDM1(), stockSignalsComponents.getMinusDM1(),
					stockSignalsComponents.getTRn(), stockSignalsComponents.getPlusDMn(),
					stockSignalsComponents.getMinusDMn(), stockSignalsComponents.getPlusDIn(),
					stockSignalsComponents.getMinusDIn(), stockSignalsComponents.getDiffDIn(),
					stockSignalsComponents.getSumDIn(), stockSignalsComponents.getDX(),
					stockSignalsComponents.getADXn(), stockSignalsComponents.getSplitMultiplier(),
					stockSignalsComponents.getHigh10Day(), stockSignalsComponents.getLow10Day(),
					stockSignalsComponents.getRange10Day(), stockSignalsComponents.getStochastic10Day(),
					stockSignalsComponents.getPercentKFlow(), stockSignalsComponents.getPercentDFlow(),
					stockSignalsComponents.getSma(), stockSignalsComponents.getUpperBound(),
					stockSignalsComponents.getLowerBound(), stockSignalsComponents.getBandwidth(),
					movingAverageComponents, movingAverageSignals, stockSignals.getAggSignal(),
					stockSignals.getAggSignal_present(), stockSignals.getAggSignal_previous());

			stockSignalsDTOList.add(stockSignalsDTO);
		}
		return stockSignalsDTOList;
	}

	public static StockSignalsDTO convertBO2DTO(StockSignalsComponents stockSignalsComponents,
			StockSignals stockSignals) {
		StockSignalsDTO stockSignalsDTO = new StockSignalsDTO();

		StockSignalsDTO.MovingAverageSignals movingAverageSignals = stockSignalsDTO.new MovingAverageSignals(
				stockSignals.getMovingAverageSignal_SmallTerm(), stockSignals.getMovingAverageSignal_Main(),
				stockSignals.getMovingAverageSignal_MidTerm(), stockSignals.getMovingAverageSignal_LongTerm(),
				stockSignals.getMovingAverageSignal_SmallTerm_present(),
				stockSignals.getMovingAverageSignal_Main_present(),
				stockSignals.getMovingAverageSignal_MidTerm_present(),
				stockSignals.getMovingAverageSignal_LongTerm_present());

		StockSignalsDTO.MovingAverageComponents movingAverageComponents = stockSignalsDTO.new MovingAverageComponents(
				stockSignalsComponents.getMovingAverage_5(), stockSignalsComponents.getMovingAverage_10(),
				stockSignalsComponents.getMovingAverage_15(), stockSignalsComponents.getMovingAverage_25(),
				stockSignalsComponents.getMovingAverage_50());

		stockSignalsDTO = new StockSignalsDTO(stockSignals.getSymbol(),
				/* stockSignals.getPreviousSignalType(), */stockSignals.getSignalType(), stockSignals.getSignalDate(),
				stockSignals.getSignalPresent(), stockSignals.getOscillatorSignal(),
				/* stockSignals.getPreviousOscillatorSignal(), */stockSignals.getSignalPresentOscillator(),
				stockSignals
						.getBollingerSignal(), /*
												 * stockSignals.
												 * getPreviousBollingerSignal(),
												 */
				stockSignals.getSignalPresentBollinger(), stockSignalsComponents.getTR(),
				stockSignalsComponents.getPlusDM1(), stockSignalsComponents.getMinusDM1(),
				stockSignalsComponents.getTRn(), stockSignalsComponents.getPlusDMn(),
				stockSignalsComponents.getMinusDMn(), stockSignalsComponents.getPlusDIn(),
				stockSignalsComponents.getMinusDIn(), stockSignalsComponents.getDiffDIn(),
				stockSignalsComponents.getSumDIn(), stockSignalsComponents.getDX(), stockSignalsComponents.getADXn(),
				stockSignalsComponents.getSplitMultiplier(), stockSignalsComponents.getHigh10Day(),
				stockSignalsComponents.getLow10Day(), stockSignalsComponents.getRange10Day(),
				stockSignalsComponents.getStochastic10Day(), stockSignalsComponents.getPercentKFlow(),
				stockSignalsComponents.getPercentDFlow(), stockSignalsComponents.getSma(),
				stockSignalsComponents.getUpperBound(), stockSignalsComponents.getLowerBound(),
				stockSignalsComponents.getBandwidth(), movingAverageComponents, movingAverageSignals,
				stockSignals.getAggSignal(), stockSignals.getAggSignal_present(), stockSignals.getAggSignal_previous());
		return stockSignalsDTO;
	}

}
