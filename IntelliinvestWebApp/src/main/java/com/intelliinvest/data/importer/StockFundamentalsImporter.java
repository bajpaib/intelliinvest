package com.intelliinvest.data.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.dao.StockFundamentalsRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockFundamentals;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.HttpUtil;
import com.intelliinvest.util.ScheduledThreadPoolHelper;
import com.intelliinvest.util.ZipFilteredReader;

@ManagedResource(objectName = "bean:name=StockFundamentalsImporter", description = "StockFundamentalsImporter")
public class StockFundamentalsImporter {
	private static Logger logger = Logger.getLogger(StockFundamentalsImporter.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private StockFundamentalsRepository stockFundamentalsRepository;
	@Autowired
	private DateUtil dateUtil;
	private final static String STOCK_FUDAMENTALS_URL = "https://www.quandl.com/api/v3/databases/DEB/data?api_key=yhwhU_RHkVxbTtFTff9t";

	private String stockFundamentalsDataDir;

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
		stockFundamentalsDataDir = IntelliInvestStore.properties.getProperty("stock.fundamentals.data.dir");
	}

	private void initializeScheduledTasks() {
		Runnable refreshStockFundamentalsTask = new Runnable() {
			public void run() {
				if (!dateUtil.isBankHoliday(dateUtil.getLocalDate())) {
					try {
						bulkUploadStockFundamentals();
					} catch (Exception e) {
						logger.error("Error while refreshing stock fundamentals " + e.getMessage());
					}
				}
			}
		};
		LocalDateTime timeNow = dateUtil.getLocalDateTime();
		int refreshStockFundamentalsStartMonth = new Integer(
				IntelliInvestStore.properties.getProperty("stock.fundamentals.refresh.start.month"));
		int refreshStockFundamentalsStartDay = new Integer(
				IntelliInvestStore.properties.getProperty("stock.fundamentals.refresh.start.day"));
		int refreshStockFundamentalsStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("stock.fundamentals.refresh.start.hr"));
		int refreshStockFundamentalsMin = new Integer(
				IntelliInvestStore.properties.getProperty("stock.fundamentals.refresh.start.min"));

		LocalDateTime timeNext = timeNow.withMonth(refreshStockFundamentalsStartMonth)
				.withDayOfMonth(refreshStockFundamentalsStartDay).withHour(refreshStockFundamentalsStartHour)
				.withMinute(refreshStockFundamentalsMin).withSecond(0);

		if (timeNow.compareTo(timeNext) > 0) {
			timeNext = timeNext.plusMonths(3);
		}
		Duration duration = Duration.between(timeNow, timeNext);
		long initialDelay = duration.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(refreshStockFundamentalsTask,
				initialDelay, 90 * 24 * 60 * 60, TimeUnit.SECONDS);

		logger.info("Scheduled refreshStockFundamentalsTask for periodic stock fundamentals refresh. Next refresh scheduled at " + timeNext);
	}

	public void bulkUploadStockFundamentals() throws Exception {
		List<Stock> stockDetails = stockRepository.getStocks();
		List<Stock> nonWorldStocks = new ArrayList<Stock>();
		for (Stock stock : stockDetails) {
			if (!stock.isWorldStock()) {
				nonWorldStocks.add(stock);
			}
		}
		List<StockFundamentals> nonWorldStockFundamentals = fetchStockFundamentalsFromQuandl(nonWorldStocks);
		logger.info("Inside updateStockFundamentals, # of StockFundamentals downloaded are "
				+ nonWorldStockFundamentals.size());
		if (Helper.isNotNullAndNonEmpty(nonWorldStockFundamentals)) {
			stockFundamentalsRepository.bulkUploadStockFundamentals(nonWorldStockFundamentals);
		}
	}

	private List<StockFundamentals> fetchStockFundamentalsFromQuandl(List<Stock> stocks) throws Exception {
		try {
			Map<String, StockFundamentals> stockFundamentals = getDataFromQuandl(stocks);
			logger.info("No of distinct entries for quarterYear and Stocks loaded are " + stockFundamentals.size());
			List<StockFundamentals> retVal = new ArrayList<StockFundamentals>();
			for(Map.Entry<String, StockFundamentals> entry: stockFundamentals.entrySet()){
				retVal.add(entry.getValue());
			}
			return retVal;
		} catch (Exception e) {
			throw new IntelliinvestException("Error while fetching Stock Fundamentals from Quandl " + e.getMessage());
		}
	}

	private Map<String, StockFundamentals> getDataFromQuandl(List<Stock> stocks) throws Exception {
		Map<String, StockFundamentals> stockFundamentals = new HashMap<String, StockFundamentals>();

		Map<String, String> fundamentalCodeToSecurityIdMap = new HashMap<String, String>();
		for (Stock stock : stocks) {
			if (Helper.isNotNullAndNonEmpty(stock.getFundamentalCode())) {
				fundamentalCodeToSecurityIdMap.put(stock.getFundamentalCode(), stock.getSecurityId());
			}
		}

		BufferedReader reader = null;
		try {
			// download zip file as download.zip
			String zipName = "download.zip";
			HttpUtil.downloadZipFile(STOCK_FUDAMENTALS_URL, stockFundamentalsDataDir, zipName);
			// extract the csv file from zip
			ZipFilteredReader zipReader = new ZipFilteredReader(stockFundamentalsDataDir + "/" + zipName,
					stockFundamentalsDataDir);
			String fileName = zipReader.filteredExpandZipFile(zipEntry -> zipEntry.getName().endsWith(".csv"));

			if (!Helper.isNotNullAndNonEmpty(fileName)) {
				throw new IntelliinvestException("No Stock Fundamentals data retrieved from Quandl");
			}

			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			reader = new BufferedReader(new FileReader(stockFundamentalsDataDir + "/" + fileName));
			LocalDateTime updateDateTime = dateUtil.getLocalDateTime();
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					String[] stockFundamentalsArray = line.split(",");
					if (stockFundamentalsArray.length < 3) {
						throw new IntelliinvestException("Error while fetching Stock Fundamentals data");
					}

					String code = null;
					String attrName = null;
					String key = stockFundamentalsArray[0];
					int index = key.indexOf("_A_") != -1 ? key.indexOf("_A_") : key.indexOf("_Q_");
					if (index != -1) {
						code = key.substring(0, index);
						attrName = key.substring(index, key.length());
					} else {
						// desired attribute is not present, continue
						continue;
					}

					// filter for desired attribute
					if (!IntelliinvestConstants.stockFundamentalAttrList.contains(attrName)) {
						// Attribute not desired, continue
						continue;
					}

					// filter for desired stock
					String securityId = fundamentalCodeToSecurityIdMap.get(code);
					if (!Helper.isNotNullAndNonEmpty(securityId)) {
						// stock not desired, continue
						continue;
					}

					LocalDate date = LocalDate.parse(stockFundamentalsArray[1], dateFormat);
					double attrValue = new Double(stockFundamentalsArray[2]);

					int month = date.getMonthValue();
					int year = date.getYear();

					String quarter;

					switch (month) {
					case 3:
					case 4:
					case 5:
						quarter = IntelliinvestConstants.Quarter.Q1.name();
						break;
					case 6:
					case 7:
					case 8:
						quarter = IntelliinvestConstants.Quarter.Q2.name();
						break;
					case 9:
					case 10:
					case 11:
						quarter = IntelliinvestConstants.Quarter.Q3.name();
						break;
					case 12:
					case 1:
					case 2:
						quarter = IntelliinvestConstants.Quarter.Q4.name();
						break;
					default:
						quarter = IntelliinvestConstants.Quarter.Q1.name();
					}

					String mapKey = quarter + year + securityId;
					StockFundamentals stock = stockFundamentals.get(mapKey);

					if (stock == null) {
						stock = new StockFundamentals();
						stock.setSecurityId(securityId);
						stock.setQuarterYear(quarter + year);
						stock.setUpdateDate(updateDateTime);
						stockFundamentals.put(mapKey, stock);
					}

					switch (attrName) {
					case IntelliinvestConstants.ANNUAL_MARKETCAP:
						stock.setAnnualMarketCap(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_EARNING_PER_SHARE:
						stock.setAnnualEarningPerShare(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_PRICE_TO_EARNING:
						stock.setAnnualPriceToEarning(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_CASH_TO_DEBT_RATIO:
						stock.setAnnualCashToDebtRatio(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_CURRENT_RATIO:
						stock.setAnnualCurrentRatio(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_EQUITY_TO_ASSET_RATIO:
						stock.setAnnualEquityToAssetRatio(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_DEBT_TO_CAPITAL_RATIO:
						stock.setAnnualDebtToCapitalRatio(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_LEVERED_BETA:
						stock.setAnnualLeveredBeta(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_RETURN_ON_EQUITY:
						stock.setAnnualReturnOnEquity(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_SOLVENCY_RATIO:
						stock.setAnnualSolvencyRatio(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_COST_OF_EQUITY:
						stock.setAnnualCostOfEquity(attrValue);
						break;
					case IntelliinvestConstants.ANNUAL_COST_OF_DEBT:
						stock.setAnnualCostOfDebt(attrValue);
						break;
					case IntelliinvestConstants.QUARTERLY_EBIDTA_MARGIN:
						stock.setQuarterlyEBIDTAMargin(attrValue);
						break;
					case IntelliinvestConstants.QUARTERLY_OPERATING_MARGIN:
						stock.setQuarterlyOperatingMargin(attrValue);
						break;
					case IntelliinvestConstants.QUARTERLY_NET_MARGIN:
						stock.setQuarterlyNetMargin(attrValue);
						break;
					case IntelliinvestConstants.QUARTERLY_DIVIDEND_PERCENT:
						stock.setQuarterlyDividendPercent(attrValue);
						break;
					case IntelliinvestConstants.QUARTERLY_UNADJ_BSE__CLOSE_PRICE:
						stock.setQuarterlyUnadjBseClosePrice(attrValue);
						break;
					default:
						break;
					}

				} catch (Exception e) {
					logger.error("Error while fetching Stock Fundamentals data: " + line + "Error: " + e.getMessage());
					logger.error(line);
				}
			}
		} catch (Exception e) {
			throw new IntelliinvestException("Error while fetching Stock Fundamentals from Quandl " + e.getMessage());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return stockFundamentals;
	}

}