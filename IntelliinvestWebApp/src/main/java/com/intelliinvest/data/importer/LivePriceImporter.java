package com.intelliinvest.data.importer;

import java.util.List;
import java.util.Set;

import org.apache.camel.Body;

import com.intelliinvest.data.model.LiveStockPrice;

public interface LivePriceImporter{
	List<LiveStockPrice> importData(@Body Set<String> stocks) throws Exception;

}