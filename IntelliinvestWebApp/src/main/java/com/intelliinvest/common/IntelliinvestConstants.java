package com.intelliinvest.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	// Stock Fundamentals Attributes names present in download file from Quandl
	public final String ANNUAL_INTEREST_COVERAGE = "A_IC";
	public final String ANNUAL_ASSET_TURNOVER = "A_ASETTO";
	public final String ANNUAL_BOOK_VALUE_SHARE_ONEYEAR = "A_BVSH1";
	public final String ANNUAL_ENTERPRISE_VALUE_EBIT = "A_EVEBIT";
	public final String ANNUAL_BOOK_VALUE_SHARE_FIVEYEAR = "A_BVSH5";
	public final String ANNUAL_CURRENT_LIABILITIES = "A_CL";
	public final String ANNUAL_OPERATING_EXPENSE = "A_OEXPNS";
	public final String ANNUAL_REVENUE_VALUE_SHARE_FIVEYEAR = "A_REVSH5";
	public final String ANNUAL_CASH_TO_DEBT_RATIO = "A_CASHDEBT";
	public final String ANNUAL_EVEBIDTA = "A_EVEBIDTA";
	public final String ANNUAL_EV_SALES = "A_EVREV";
	public final String ANNUAL_CASH_RATIO = "A_CASHRATIO";
	public final String ANNUAL_EV_CFO = "A_EV_CFO";
	public final String ANNUAL_CASH_TURNOVER = "A_CASHTO";
	public final String ANNUAL_WORKING_CAPITAL_TURNOVER = "A_WCTO";
	public final String ANNUAL_TOTAL_ASSET = "A_TA";
	public final String ANNUAL_CURRENT_RATIO = "A_CRATIO";
	public final String ANNUAL_CFO_SALES = "A_CFO_SALES";
	public final String ANNUAL_RETURN_OF_INVESTED_CAPITAL = "A_ROIC";
	public final String ANNUAL_EQUITY_TO_ASSET_RATIO = "A_EQASSET";
	public final String ANNUAL_ENTERPRISE_VALUE = "A_EV";
	public final String ANNUAL_DEBT_TO_CAPITAL_RATIO = "A_DEBT_CE";
	public final String ANNUAL_ASSET_TO_SHAREHOLDER_EQUITY = "A_AE";
	public final String ANNUAL_DIVIDEND_COVER = "A_DIVCVR";
	public final String ANNUAL_EV_TO_ASSET_RATIO = "A_EV_ASSETS";
	public final String ANNUAL_FACE_VALUE = "A_FV";
	public final String ANNUAL_CASH_AND_BANK_BALANCE = "A_CASH";
	public final String ANNUAL_TOTAL_EQUITY_TURNOVER = "A_ETO";
	public final String ANNUAL_CFO_TO_DEBT = "A_CFO_DEBT";
	public final String ANNUAL_DEBT_TO_ASSET_RATIO = "A_DEBT_ASSETS";
	public final String ANNUAL_TOTAL_DEBT = "A_DEBT";
	public final String ANNUAL_TOTAL_INCOME = "A_TI";
	public final String ANNUAL_PRICE_TO_CASHFLOW_OPERATIONS = "A_PCFO";
	public final String ANNUAL_EV_TO_EARNING = "A_EV_NP";
	public final String ANNUAL_ACCRUALS = "A_ACCRUALS";
	public final String ANNUAL_RETURN_ON_ASSETS = "A_ROA";
	public final String ANNUAL_FIXED_ASSET_TURN_OVER = "A_NBTO";
	public final String ANNUAL_PROFIT_BEFORE_DEPRICIATION_AND_TAXES = "A_PBDT";
	public final String ANNUAL_TOTAL_LIABILITIES = "A_TL";
	public final String ANNUAL_CURRENT_ASSETS = "A_CA";
	public final String ANNUAL_LEVERED_BBETA = "A_BBETA";
	public final String ANNUAL_CASH_FROM_FINANCING_ACTIVITY = "A_CFA";
	public final String ANNUAL_CAPITALIZATION_RATIO = "A_CAPRATIO";
	public final String ANNUAL_CAPITAL_EMPLOYED = "A_CE";
	public final String ANNUAL_CASH_EARNING_PER_SHARE = "A_CEPS";
	public final String ANNUAL_CREDIT_DEFAULT_SPREAD = "A_CREDIT";
	public final String ANNUAL_DEGREE_OF_CUMULATIVE_LEVERAGE = "A_DCL";
	public final String ANNUAL_BOOK_VALUE_PER_SHARE = "A_BVSH";
	public final String ANNUAL_CASH_FROM_INVESTING_ACTIVITIES = "A_CFI";
	public final String ANNUAL_DEBT_TO_EBIDT_RATIO = "A_DEBTEBIDT";
	public final String ANNUAL_CASHFLOW_FROM_OPERATING_ACTIVITY = "A_CFO";
	public final String ANNUAL_DIVIDEND = "A_DIV";
	public final String ANNUAL_DAYS_IN_WORKING_CAPITAL = "A_DAYWC";
	public final String ANNUAL_DEPRICIATION_EXPENSE = "A_DEP";
	public final String ANNUAL_DIVIDEND_PER_SHARE = "A_DIVSH";
	public final String ANNUAL_DEGREE_OF_FINANCIAL_LEVERAGE = "A_DFL";
	public final String ANNUAL_DIVIDEND_PAYOUT = "A_DIVPAY";
	public final String ANNUAL_CAPITAL_WORK_IN_PROGRESS = "A_CWIP";
	public final String ANNUAL_CASH_RETURN_ON_CAPITAL_INVESTED = "A_CROCI";
	public final String ANNUAL_DEGREE_OF_OPERATING_LEVERAGE = "A_DOL";
	public final String ANNUAL_DIVIDENDYIELD = "A_DIVYLD";
	public final String ANNUAL_EARNING_PER_SHARE = "A_EPS";
	public final String ANNUAL_EARNING_PER_SHARE_PCT = "A_EPS_PCT";
	public final String ANNUAL_ACCOUNTING_COST_OF_DEBT = "A_FKD";
	public final String ANNUAL_SHAREHOLDER_EQUITY = "A_EQCAP";
	public final String ANNUAL_CORPORATE_TAX_RATE = "A_ETR";
	public final String ANNUAL_INCOME_BEFORE_TAX = "A_IBTPCT";
	public final String ANNUAL_COST_OF_EQUITY = "A_KE";
	public final String ANNUAL_MARKET_CAPITALIZATION = "A_MCAP";
	public final String ANNUAL_LONG_TERM_INVESTMENT = "A_INV";
	public final String ANNUAL_COST_OF_DEBT = "A_KD";
	public final String ANNUAL_LEVERED_BETA = "A_LBETA";
	public final String ANNUAL_EARNING_YIELD = "A_EYIELD";
	public final String ANNUAL_INTEREST = "A_INT";
	public final String ANNUAL_NET_BLOCK = "A_NBLOCK";
	public final String ANNUAL_NET_CASHFLOW = "A_NCF";
	public final String ANNUAL_OPERATING_INCOME = "A_OI";
	public final String ANNUAL_NET_PROFIT = "A_NP";
	public final String ANNUAL_LONG_TERM_DEBT_TO_EQUITY = "A_LTDE";
	public final String ANNUAL_OPERATING_PROFIT = "A_OP";
	public final String ANNUAL_OPERATING_CASHFLOW_TO_REVENUE = "A_OPCASHPCT";
	public final String ANNUAL_OPERATING_PROFIT_PER_SHARE = "A_OPMSH";
	public final String ANNUAL_PROFIT_BEFORE_TAX = "A_PBT";
	public final String ANNUAL_PRICE_BY_EARNING_TO_GROWTH = "A_PEG";
	public final String ANNUAL_PRICE_TO_EARNING = "A_PE";
	public final String ANNUAL_OPERATING_MARGIN = "A_OPMPCT";
	public final String ANNUAL_PRICE_TO_BOOK_VALUE = "A_PBV";
	public final String ANNUAL_REVENUE_PER_SHARE = "A_REVSH";
	public final String ANNUAL_RETENTION_RATIO = "A_RETRATIO";
	public final String ANNUAL_RETURN_ON_EQUITY = "A_ROE";
	public final String ANNUAL_PRICE_TO_SALES = "A_PS";
	public final String ANNUAL_LONG_TERM_DEBT = "A_SDEBT";
	public final String ANNUAL_SOLVENCY_RATIO = "A_SOLRATIO";
	public final String ANNUAL_RESERVES = "A_RSRV";
	public final String ANNUAL_OUTSTANDING_SHARES = "A_SHARE";
	public final String ANNUAL_LONG_TERM_UNSECURED_DEBT = "A_UDEBT";
	public final String ANNUAL_REVENUE = "A_SR";
	public final String ANNUAL_INCOME_TAX_EXPENSE = "A_TAX";
	public final String ANNUAL_WEIGHTED_AVERAGE_COST_OF_CAPITAL = "A_WACC";
	public final String ANNUAL_WORKING_CAPITAL = "A_WC";
	public final String ANNUAL_WEIGHT_OF_DEBT = "A_WD";
	public final String ANNUAL_TIMES_INTEREST_EARNED = "A_TIMINT";
	public final String ANNUAL_WEIGHT_OF_EQUITY = "A_WE";

	public final String QUARTER_SHARE_HOLDER_EQUITY = "Q_EQCAP";
	public final String QUARTER_PROFIT_BEFORE_DEPRICIATION_ANDTAX = "Q_PBDT";
	public final String QUARTER_PROFIT_BEFORE_TAX = "Q_PBT";
	public final String QUARTER_TOTAL_INCOME = "Q_TI";
	public final String QUARTER_UNADJUSTED_BSE_CLOSE_PRICE = "Q_BSEC";
	public final String QUARTER_BSE_TRADE_VOLUME = "Q_BSEVOL";
	public final String QUARTER_UNADJUSTED_BSE_HIGH_PRICE = "Q_BSEH";
	public final String QUARTER_EBIDTA_MARGIN = "Q_EBIDTPCT";
	public final String QUARTER_EARNING_PER_SHARE = "Q_EPS";
	public final String QUARTER_NET_PROFIT = "Q_NP";
	public final String QUARTER_EBIDTA_PER_SHARE = "Q_EBIDTSH";
	public final String QUARTER_OPERATING_MARGIN = "Q_OPMPCT";
	public final String QUARTER_OPERATING_PROFIT = "Q_OP";
	public final String QUARTER_OPERATING_PROFIT_PER_SHARE = "Q_OPMSH";
	public final String QUARTER_REVENUE_PER_SHARE = "Q_REVSH";
	public final String QUARTER_REVENUE = "Q_SR";
	public final String QUARTER_NET_MARGIN = "Q_NETPCT";
	public final String QUARTER_OUTSTANDING_SHARES = "Q_SHARE";
	public final String QUARTER_DIVIDEND_PER_SHARE = "Q_DIVSH";
	public final String QUARTER_REVENUE_ONE_QUARTER = "Q_REV1Q";
	public final String QUARTER_OPERATING_EXPENSE = "Q_OEXPNS";
	public final String QUARTER_OPERATING_EXPENSE_PER_SHARE = "Q_OEXPNS";
	public final String QUARTER_SHAREHOLDER_EQUITY = "Q_EQCAP";
	public final String QUARTER_DIVIDEND_PERCENT = "Q_DIV_PCT";
	public final String QUARTER_FACE_VALUE = "Q_FV";

	public final List<String> stockFundamentalAttrList = new ArrayList<>(Arrays.asList(ANNUAL_MARKET_CAPITALIZATION,
			ANNUAL_BOOK_VALUE_PER_SHARE, ANNUAL_EARNING_PER_SHARE, ANNUAL_EARNING_PER_SHARE_PCT,
			ANNUAL_PRICE_TO_EARNING, ANNUAL_CASH_TO_DEBT_RATIO, ANNUAL_CURRENT_RATIO, ANNUAL_EQUITY_TO_ASSET_RATIO,
			ANNUAL_DEBT_TO_CAPITAL_RATIO, ANNUAL_LEVERED_BBETA, ANNUAL_RETURN_ON_EQUITY, ANNUAL_SOLVENCY_RATIO,
			ANNUAL_COST_OF_EQUITY, ANNUAL_COST_OF_DEBT, ANNUAL_TOTAL_INCOME, ANNUAL_ASSET_TURNOVER, ANNUAL_TOTAL_ASSET,
			ANNUAL_SHAREHOLDER_EQUITY, ANNUAL_RETURN_ON_ASSETS, ANNUAL_DEGREE_OF_FINANCIAL_LEVERAGE,
			QUARTER_EBIDTA_MARGIN, QUARTER_OPERATING_MARGIN, QUARTER_NET_MARGIN, QUARTER_OUTSTANDING_SHARES,
			QUARTER_DIVIDEND_PERCENT, QUARTER_UNADJUSTED_BSE_CLOSE_PRICE));

	// Java to DB Stock Fundamentals attribute mapping
	public final Map<String, String> stockFundamentalDBAttrMap = new HashMap<String, String>() {
		{
			put("alMarketCap", ANNUAL_MARKET_CAPITALIZATION);
			put("alBookValuePerShare", ANNUAL_BOOK_VALUE_PER_SHARE);
			put("alEarningPerShare", ANNUAL_EARNING_PER_SHARE);
			put("alEPSPct", ANNUAL_EARNING_PER_SHARE_PCT);
			put("alPriceToEarning", ANNUAL_PRICE_TO_EARNING);
			put("alCashToDebtRatio", ANNUAL_CASH_TO_DEBT_RATIO);
			put("alCurrentRatio", ANNUAL_CURRENT_RATIO);
			put("alEquityToAssetRatio", ANNUAL_EQUITY_TO_ASSET_RATIO);
			put("alDebtToCapitalRatio", ANNUAL_DEBT_TO_CAPITAL_RATIO);
			put("alLeveredBeta", ANNUAL_LEVERED_BBETA);
			put("alReturnOnEquity", ANNUAL_RETURN_ON_EQUITY);
			put("alSolvencyRatio", ANNUAL_SOLVENCY_RATIO);
			put("alCostOfEquity", ANNUAL_COST_OF_EQUITY);
			put("alCostOfDebt", ANNUAL_COST_OF_DEBT);
			put("qrEBIDTAMargin", QUARTER_EBIDTA_MARGIN);
			put("qrOperatingMargin", QUARTER_OPERATING_MARGIN);
			put("qrNetMargin", QUARTER_NET_MARGIN);
			put("qrDividendPercent", QUARTER_DIVIDEND_PERCENT);
		}
	};

	//Signals String
	public final String WAIT = "Wait";
	public final String HOLD = "Hold";
	public final String BUY = "Buy";
	public final String SELL = "Sell";
	
	public final String HOLD_ID = "0";
	public final String BUY_ID = "1";
	public final String SELL_ID = "-1";
	
	
	public final String SIGNAL_PRESENT="Y";
	public final String SIGNAL_NOT_PRESENT="N";

	// Industry Names present in STOCK table
	public final String INDUSTRY_AGRICULTURE = "AGRICULTURE";
	public final String INDUSTRY_AUTOMOBILES = "AUTOMOBILES";
	public final String INDUSTRY_AUTO_COMPONENTS = "AUTO_COMPONENTS";
	public final String INDUSTRY_AVIATION = "AVIATION";
	public final String INDUSTRY_BANKING = "BANKING";
	public final String INDUSTRY_BIOTECHNOLOGY = "BIOTECHNOLOGY";
	public final String INDUSTRY_CEMENT = "CEMENT";
	public final String INDUSTRY_CONSUMER_MARKETS = "CONSUMERMARKETS";
	public final String INDUSTRY_EDUCATION_AND_TRAINING = "EDUCATION_AND_TRAINING";
	public final String INDUSTRY_ENGINEERING = "ENGINEERING";
	public final String INDUSTRY_FINANCIAL_SERVICES = "FINANCIAL_SERVICES";
	public final String INDUSTRY_FOOD_INDUSTRY = "FOOD_INDUSTRY";
	public final String INDUSTRY_GEMS_AND_JEWELLERY = "GEMS_AND_JEWELLERY";
	public final String INDUSTRY_HEALTHCARE = "HEALTHCARE";
	public final String INDUSTRY_INFRASTRUCTURE = "INFRASTRUCTURE";
	public final String INDUSTRY_INSURANCE = "INSURANCE";
	public final String INDUSTRY_IT_AND_ITES = "IT_AND_ITES";
	public final String INDUSTRY_MANUFACTURING = "MANUFACTURING";
	public final String INDUSTRY_MEDIA_AND_ENTERTAINMENT = "MEDIA_AND_ENTERTAINMENT";
	public final String INDUSTRY_OIL_AND_GAS = "OIL_AND_GAS";
	public final String INDUSTRY_PHARMACEUTICALS = "PHARMACEUTICALS";
	public final String INDUSTRY_REAL_ESTATE = "REAL_ESTATE";
	public final String INDUSTRY_RESEARCH_AND_DEVELOPMENT = "RESEARCH_AND_DEVELOPMENT";
	public final String INDUSTRY_RETAIL = "RETAIL";
	public final String INDUSTRY_SCIENCE_AND_TECHNOLOGY = "SCIENCE_AND_TECHNOLOGY";
	public final String INDUSTRY_SEMICONDUCTOR = "SEMICONDUCTOR";
	public final String INDUSTRY_SERVICES = "SERVICES";
	public final String INDUSTRY_STEEL = "STEEL";
	public final String INDUSTRY_TELECOMMUNICATIONS = "TELECOMMUNICATIONS";
	public final String INDUSTRY_TEXTILES = "TEXTILES";
	public final String INDUSTRY_TOURISM_AND_HOSPITALITY = "TOURISM_AND_HOSPITALITY";
	public final String INDUSTRY_URBAN_MARKET = "URBAN_MARKET";

	public final List<String> stockIndustryList = new ArrayList<>(Arrays.asList(INDUSTRY_AGRICULTURE,
			INDUSTRY_AUTOMOBILES, INDUSTRY_AUTO_COMPONENTS, INDUSTRY_AVIATION, INDUSTRY_BANKING, INDUSTRY_BIOTECHNOLOGY,
			INDUSTRY_CEMENT, INDUSTRY_CONSUMER_MARKETS, INDUSTRY_EDUCATION_AND_TRAINING, INDUSTRY_ENGINEERING,
			INDUSTRY_FINANCIAL_SERVICES, INDUSTRY_FOOD_INDUSTRY, INDUSTRY_GEMS_AND_JEWELLERY, INDUSTRY_HEALTHCARE,
			INDUSTRY_INFRASTRUCTURE, INDUSTRY_INSURANCE, INDUSTRY_IT_AND_ITES, INDUSTRY_MANUFACTURING,
			INDUSTRY_MEDIA_AND_ENTERTAINMENT, INDUSTRY_OIL_AND_GAS, INDUSTRY_PHARMACEUTICALS, INDUSTRY_REAL_ESTATE,
			INDUSTRY_RESEARCH_AND_DEVELOPMENT, INDUSTRY_RETAIL, INDUSTRY_SCIENCE_AND_TECHNOLOGY, INDUSTRY_SEMICONDUCTOR,
			INDUSTRY_SERVICES, INDUSTRY_STEEL, INDUSTRY_TELECOMMUNICATIONS, INDUSTRY_TEXTILES,
			INDUSTRY_TOURISM_AND_HOSPITALITY, INDUSTRY_URBAN_MARKET));
}