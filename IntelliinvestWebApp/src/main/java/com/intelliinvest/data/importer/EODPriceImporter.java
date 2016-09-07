package com.intelliinvest.data.importer;

public interface EODPriceImporter {

	String importData(String code, String startDate, String endDate) throws Exception;

}