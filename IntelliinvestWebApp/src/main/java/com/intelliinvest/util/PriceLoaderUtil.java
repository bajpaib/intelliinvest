package com.intelliinvest.util;

import java.util.List;

import com.intelliinvest.data.model.EODStockPrice;

public class PriceLoaderUtil {
	public static void enrichQuandlStockPrices(List<EODStockPrice> quandlStockPrices, String code) {
		for(EODStockPrice quandlStockPrice : quandlStockPrices){
			quandlStockPrice.setExchange("NSE");
			quandlStockPrice.setSeries("");
			quandlStockPrice.setSymbol(code);
		}
	}
}
