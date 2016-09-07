package com.intelliinvest.util;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.TypeConverters;

import com.intelliinvest.data.model.EODStockPrice;

public class Converter implements TypeConverters {

	@org.apache.camel.Converter
	public List<EODStockPrice> toEODStockPriceList(EODStockPrice eodStockPrice){
		return Arrays.asList(eodStockPrice);
	}
	
}
