package com.intelliinvest.data.dao;

import java.time.LocalDate;
import java.util.List;

import com.intelliinvest.common.exception.IntelliInvestException;
import com.intelliinvest.data.model.EODStockPrice;

public interface EODHistoryPriceRepository {

	List<EODStockPrice> getHistoryStockPrices(String exchange, LocalDate eodDate);

	List<EODStockPrice> getHistoryStockPrices(String exchange, LocalDate startDate, LocalDate endDate);

	EODStockPrice getHistoryStockPricesForCode(String exchange, String symbol, LocalDate date);

	List<EODStockPrice> getHistoryStockPricesForCode(String exchange, String symbol, LocalDate startDate, LocalDate endDate);

	void updateHistoryPrices(String exchange, List<EODStockPrice> eodStockPrices) throws IntelliInvestException;

}