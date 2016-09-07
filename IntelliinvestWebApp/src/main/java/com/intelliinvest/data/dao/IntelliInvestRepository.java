package com.intelliinvest.data.dao;

public interface IntelliInvestRepository {

	String getBSECode(String nseCode);

	String getNSECode(String bseCode);

}