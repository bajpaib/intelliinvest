package com.intelliinvest.data.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StockDetailStaticHolder {

	public static Map<String, Stock> nonWorldStockDetailsMap = new ConcurrentHashMap<String, Stock>();
	public static Map<String, StockPrice> nonWorldStockPriceMap = new ConcurrentHashMap<String, StockPrice>();
	public static Map<String, Stock> worldStockDetailsMap = new ConcurrentHashMap<String, Stock>();
	public static Map<String, StockPrice> worldStockPriceMap = new ConcurrentHashMap<String, StockPrice>();
	public static Map<String, String> NSEToBSEMap = new ConcurrentHashMap<String, String>();
	public static Map<String, String> BSEToNSEMap = new ConcurrentHashMap<String, String>();
	public static HashSet<String> BOMStocksSet = new HashSet<String>();
	public static HashSet<String> BOMStocksSetTemp = new HashSet<String>();
	public static Map<String, StockPrice> NIFTYStockPriceMap = new ConcurrentHashMap<String, StockPrice>();
	public static Map<String, String> QUANDL_STOCK_CODES_MAPPING = new ConcurrentHashMap<String, String>();

	public static double getEODPrice(String code) {
		if (nonWorldStockPriceMap.containsKey(code)) {
			return nonWorldStockPriceMap.get(code).getEodPrice();
		} else if (worldStockPriceMap.containsKey(code)) {
			return worldStockPriceMap.get(code).getEodPrice();
		} else {
			return 0d;
		}
	}

	public static double getCurrentPrice(String code) {
		if (nonWorldStockPriceMap.containsKey(code)) {
			return nonWorldStockPriceMap.get(code).getCurrentPrice();
		} else if (worldStockPriceMap.containsKey(code)) {
			return worldStockPriceMap.get(code).getCurrentPrice();
		} else {
			return 0d;
		}
	}

	public static double getCP(String code) {
		if (nonWorldStockPriceMap.containsKey(code)) {
			return nonWorldStockPriceMap.get(code).getCp();
		} else if (worldStockPriceMap.containsKey(code)) {
			return worldStockPriceMap.get(code).getCp();
		} else {
			return 0d;
		}
	}

	public static Stock getStock(String code) {
		Stock retVal = null;
		if (nonWorldStockDetailsMap.containsKey(code)) {
			retVal = nonWorldStockDetailsMap.get(code);
		} else if (worldStockDetailsMap.containsKey(code)) {
			retVal = worldStockDetailsMap.get(code);
		}
		return retVal;
	}

	public static StockPrice getStockPrice(String code) {
		StockPrice retVal = null;
		if (nonWorldStockPriceMap.containsKey(code)) {
			retVal = nonWorldStockPriceMap.get(code);
		} else if (worldStockPriceMap.containsKey(code)) {
			retVal = worldStockPriceMap.get(code);
		}
		return retVal;
	}
	
	public static List<Stock> getStocks() {
		List<Stock> retVal = new ArrayList<Stock>();
		retVal.addAll(nonWorldStockDetailsMap.values());
		retVal.addAll(worldStockDetailsMap.values());
		return retVal;
	}

	public static List<StockPrice> getStockPrices() {
		List<StockPrice> retVal = new ArrayList<StockPrice>();
		retVal.addAll(nonWorldStockPriceMap.values());
		retVal.addAll(worldStockPriceMap.values());
		return retVal;
	}
}
