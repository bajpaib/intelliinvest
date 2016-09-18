package com.intelliinvest.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockPrice;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.data.model.StockSignalsComponents;
import com.intelliinvest.data.model.User;
import com.intelliinvest.web.bo.StockPriceResponse;
import com.intelliinvest.web.bo.StockResponse;
import com.intelliinvest.web.bo.UserResponse;
import com.intelliinvest.web.dto.StockSignalsDTO;

public class Converter {

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
				return item1.getCode().compareTo(item2.getCode());
			}
		});
		return stockResponseList;
	}

	public static StockResponse getStockResponse(Stock stock) {
		StockResponse stockResponse = new StockResponse();
		stockResponse.setCode(stock.getCode());
		stockResponse.setName(stock.getName());
		stockResponse.setNiftyStock(stock.isNiftyStock());
		stockResponse.setWorldStock(stock.isWorldStock());
		stockResponse.setUpdateDate(stock.getUpdateDate());
		stockResponse.setSuccess(true);
		return stockResponse;
	}

	public static List<StockPriceResponse> convertStockPriceList(List<StockPrice> prices) {
		List<StockPriceResponse> stockResponseList = new ArrayList<StockPriceResponse>();
		if (prices != null) {
			for (StockPrice price : prices) {
				stockResponseList.add(getStockPriceResponse(price));
			}
		}

		stockResponseList.sort(new Comparator<StockPriceResponse>() {
			public int compare(StockPriceResponse item1, StockPriceResponse item2) {
				return item1.getCode().compareTo(item2.getCode());
			}
		});

		return stockResponseList;
	}

	public static StockPriceResponse getStockPriceResponse(StockPrice price) {
		StockPriceResponse stockPriceResponse = new StockPriceResponse();
		stockPriceResponse.setCode(price.getCode());
		stockPriceResponse.setCp(price.getCp());
		stockPriceResponse.setCurrentPrice(price.getCurrentPrice());
		stockPriceResponse.setEodDate(price.getEodDate());
		stockPriceResponse.setEodPrice(price.getEodPrice());
		stockPriceResponse.setUpdateDate(price.getUpdateDate());
		stockPriceResponse.setSuccess(true);
		return stockPriceResponse;
	}


	public static void convertDTO2BO(List<StockSignalsDTO> stockSignalsDTOList,
			List<StockSignalsComponents> stockSignalsComponentsList,
			List<StockSignals> stockSignalsList) {
		for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOList) {
			StockSignalsComponents stockSignalsComponents = new StockSignalsComponents(
					stockSignalsDTO.getSymbol(), stockSignalsDTO.getTR(),
					stockSignalsDTO.getPlusDM1(),
					stockSignalsDTO.getMinusDM1(), stockSignalsDTO.getTRn(),
					stockSignalsDTO.getPlusDMn(),
					stockSignalsDTO.getMinusDMn(),
					stockSignalsDTO.getPlusDIn(),
					stockSignalsDTO.getMinusDIn(),
					stockSignalsDTO.getDiffDIn(), stockSignalsDTO.getSumDIn(),
					stockSignalsDTO.getDX(), stockSignalsDTO.getADXn(),
					stockSignalsDTO.getSplitMultiplier(),
					stockSignalsDTO.getSignalDate(),
					stockSignalsDTO.getHigh10Day(),
					stockSignalsDTO.getLow10Day(),
					stockSignalsDTO.getRange10Day(),
					stockSignalsDTO.getStochastic10Day(),
					stockSignalsDTO.getPercentKFlow(),
					stockSignalsDTO.getPercentDFlow(),
					stockSignalsDTO.getSma(), stockSignalsDTO.getUpperBound(),
					stockSignalsDTO.getLowerBound(),
					stockSignalsDTO.getBandwidth());
			StockSignals stockSignals = new StockSignals(
					stockSignalsDTO.getSymbol(),
					stockSignalsDTO.getPreviousSignalType(),
					stockSignalsDTO.getSignalType(),
					stockSignalsDTO.getSignalDate(),
					stockSignalsDTO.getSignalPresent(),
					stockSignalsDTO.getOscillatorSignal(),
					stockSignalsDTO.getPreviousOscillatorSignal(),
					stockSignalsDTO.getSignalPresentOscillator(),
					stockSignalsDTO.getBollingerSignal(),
					stockSignalsDTO.getPreviousBollingerSignal(),
					stockSignalsDTO.getSignalPresentBollinger());

			stockSignalsComponentsList.add(stockSignalsComponents);
			stockSignalsList.add(stockSignals);
		}
	}

	public static List<StockSignalsDTO> convertBO2DTO(
			List<StockSignalsComponents> stockSignalsComponentsList,
			List<StockSignals> stockSignalsList) {
		List<StockSignalsDTO> stockSignalsDTOList = new ArrayList<StockSignalsDTO>();
		for (int i = 0; i < stockSignalsList.size(); i++) {
			StockSignals stockSignals = stockSignalsList.get(i);
			StockSignalsComponents stockSignalsComponents = stockSignalsComponentsList
					.get(i);

			StockSignalsDTO stockSignalsDTO = new StockSignalsDTO(
					stockSignals.getSymbol(),
					stockSignals.getPreviousSignalType(),
					stockSignals.getSignalType(), stockSignals.getSignalDate(),
					stockSignals.getSignalPresent(),
					stockSignals.getOscillatorSignal(),
					stockSignals.getPreviousOscillatorSignal(),
					stockSignals.getSignalPresentOscillator(),
					stockSignals.getBollingerSignal(),
					stockSignals.getPreviousBollingerSignal(),
					stockSignals.getSignalPresentBollinger(),
					stockSignalsComponents.getTR(),
					stockSignalsComponents.getPlusDM1(),
					stockSignalsComponents.getMinusDM1(),
					stockSignalsComponents.getTRn(),
					stockSignalsComponents.getPlusDMn(),
					stockSignalsComponents.getMinusDMn(),
					stockSignalsComponents.getPlusDIn(),
					stockSignalsComponents.getMinusDIn(),
					stockSignalsComponents.getDiffDIn(),
					stockSignalsComponents.getSumDIn(),
					stockSignalsComponents.getDX(),
					stockSignalsComponents.getADXn(),
					stockSignalsComponents.getSplitMultiplier(),
					stockSignalsComponents.getHigh10Day(),
					stockSignalsComponents.getLow10Day(),
					stockSignalsComponents.getRange10Day(),
					stockSignalsComponents.getStochastic10Day(),
					stockSignalsComponents.getPercentKFlow(),
					stockSignalsComponents.getPercentDFlow(),
					stockSignalsComponents.getSma(),
					stockSignalsComponents.getUpperBound(),
					stockSignalsComponents.getLowerBound(),
					stockSignalsComponents.getBandwidth());
		}
		return null;
	}
}
