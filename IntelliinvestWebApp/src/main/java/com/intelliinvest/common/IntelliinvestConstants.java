package com.intelliinvest.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface IntelliinvestConstants {

	public final String ERROR_MSG_DEFAULT = "Invalid Input Value, Please check...";
	public final String EXCHANGE_NSE = "NSE";
	public final String EXCHANGE_BSE = "BSE";
	public final String EXCHANGE_BOM = "BOM";

	// Forecast Reports
	public enum ForecastType {
		DAILY, WEEKLY, MONTHLY
	}

	public enum Quarter {
		Q1, Q2, Q3, Q4
	}

	// Stock Fundamentals
	public final String ANNUAL_MARKETCAP = "_A_MCAP";
	public final String ANNUAL_EARNING_PER_SHARE = "_A_EPS";
	public final String ANNUAL_PRICE_TO_EARNING = "_A_PE";
	public final String ANNUAL_CASH_TO_DEBT_RATIO = "_A_CASHDEBT";
	public final String ANNUAL_CURRENT_RATIO = "_A_CRATIO";
	public final String ANNUAL_EQUITY_TO_ASSET_RATIO = "_A_EQASSET";
	public final String ANNUAL_DEBT_TO_CAPITAL_RATIO = "_A_DEBT_CE";
	public final String ANNUAL_LEVERED_BETA = "_A_BBETA";
	public final String ANNUAL_RETURN_ON_EQUITY = "_A_ROE";
	public final String ANNUAL_SOLVENCY_RATIO = "_A_SOLRATIO";
	public final String ANNUAL_COST_OF_EQUITY = "_A_KE";
	public final String ANNUAL_COST_OF_DEBT = "_A_KD";
	public final String QUARTERLY_EBIDTA_MARGIN = "_Q_EBIDTPCT";
	public final String QUARTERLY_OPERATING_MARGIN = "_Q_OPMPCT";
	public final String QUARTERLY_NET_MARGIN = "_Q_NETPCT";
	public final String QUARTERLY_DIVIDEND_PERCENT = "_Q_DIV_PCT";
	public final String QUARTERLY_UNADJ_BSE__CLOSE_PRICE = "_Q_BSEC";

	public final List<String> stockFundamentalAttrList = new ArrayList<>(Arrays.asList(ANNUAL_MARKETCAP, ANNUAL_EARNING_PER_SHARE,
			ANNUAL_PRICE_TO_EARNING, ANNUAL_CASH_TO_DEBT_RATIO, ANNUAL_CURRENT_RATIO, ANNUAL_EQUITY_TO_ASSET_RATIO,
			ANNUAL_DEBT_TO_CAPITAL_RATIO, ANNUAL_LEVERED_BETA, ANNUAL_RETURN_ON_EQUITY, ANNUAL_SOLVENCY_RATIO,
			ANNUAL_COST_OF_EQUITY, ANNUAL_COST_OF_DEBT, QUARTERLY_EBIDTA_MARGIN, QUARTERLY_OPERATING_MARGIN,
			QUARTERLY_NET_MARGIN, QUARTERLY_DIVIDEND_PERCENT, QUARTERLY_UNADJ_BSE__CLOSE_PRICE));

	
}