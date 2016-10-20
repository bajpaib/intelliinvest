package com.intelliinvest.data.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
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
	
	private String downloadQuandlDataFile() throws Exception {
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
		return stockFundamentalsDataDir + "/" + fileName;
	}
	
	
	public void bulkUploadStockFundamentals() throws Exception {
		String filePath= downloadQuandlDataFile();
		uploadStockFundamentals(filePath);
	}

	@ManagedOperation(description = "uploadStockFundamentals")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "filePath", description = "filePath")})
	public void uploadStockFundamentals(String filePath) throws Exception {
		List<Stock> stockDetails = stockRepository.getStocks();
		List<Stock> stocks = new ArrayList<Stock>();
		for (Stock stock : stockDetails) {
			if (!stock.isWorldStock()) {
				stocks.add(stock);
			}
		}
		
		Map<String, String> fundamentalCodeToSecurityIdMap = new HashMap<String, String>();
		Map<String, String> fundamentalCodeToIndustryMap = new HashMap<String, String>();
		for (Stock stock : stocks) {
			if (Helper.isNotNullAndNonEmpty(stock.getFundamentalCode())) {
				fundamentalCodeToSecurityIdMap.put(stock.getFundamentalCode(), stock.getSecurityId());
				if(Helper.isNotNullAndNonEmpty(stock.getIndustry())) {
					fundamentalCodeToIndustryMap.put(stock.getFundamentalCode(), stock.getIndustry());
				}
			}
		}
		
		BufferedReader reader = null;
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			reader = new BufferedReader(new FileReader(filePath));
			LocalDateTime updateDateTime = dateUtil.getLocalDateTime();
			List<StockFundamentals> dataList = new ArrayList<StockFundamentals>();
			String line;
			int size = 0;
			boolean firstDBCall = true;
			Set<String> industryNotFound = new HashSet<String>(); 
			String key = null;
			String prevKey = "";
			StockFundamentals stock = null;
			while ((line = reader.readLine()) != null) {
				try {
					String[] stockFundamentalsArray = line.split(",");
					if (stockFundamentalsArray.length < 3) {
						throw new IntelliinvestException("Error while fetching Stock Fundamentals data");
					}

					String code = null;
					String attrName = null;
					key = stockFundamentalsArray[0];
					int index = key.indexOf("_A_") != -1 ? key.indexOf("_A_") : key.indexOf("_Q_");
					if (index != -1) {
						code = key.substring(0, index);
						attrName = key.substring(index+1, key.length());
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
					String attrValue = stockFundamentalsArray[2];

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
						quarter = IntelliinvestConstants.Quarter.Q4.name();
						break;
					case 1:
					case 2:
						quarter = IntelliinvestConstants.Quarter.Q4.name();
						year = year - 1;
						break;
					default:
						quarter = IntelliinvestConstants.Quarter.Q1.name();
					}

					// check for industry
					String industry = fundamentalCodeToIndustryMap.get(code);
					if (!Helper.isNotNullAndNonEmpty(industry)) {
						// industry not found, continue
//						logger.error("Ignoring Fundamental data. Industry not found for securityId=" + securityId);
						industryNotFound.add(securityId);
						continue;
					}
					
					if(!prevKey.equals(key)){
						stock = new StockFundamentals();
						stock.setSecurityId(securityId);
						stock.setAttrName(attrName);
						stock.addYearQuarterAttrVal(year + quarter, attrValue);
						stock.setUpdateDate(updateDateTime);	
						dataList.add(stock);
					}else{
						stock.addYearQuarterAttrVal(year + quarter, attrValue);
					}
					
					++size;
					prevKey = key;
					if(dataList.size() > 2999){
						stockFundamentalsRepository.bulkUploadStockFundamentals(dataList, firstDBCall);
						firstDBCall = false;
						dataList.clear();
					}

				} catch (Exception e) {
					logger.error("Error while uploadStockFundamentals: " + line + "Error: " + e.getMessage());
				}
			}
			//now update the remaining rows
			if(dataList.size() > 0){
				stockFundamentalsRepository.bulkUploadStockFundamentals(dataList, firstDBCall);
				logger.info("Number of rows uploaded are: "+size);
				dataList.clear();
			}		
			
			if(!industryNotFound.isEmpty()){
				logger.error("The stocks not having industry mapping are: "+industryNotFound);
			}
		} catch (Exception e) {
			throw new IntelliinvestException("Error while uploadStockFundamentals: " + e.getMessage());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

}