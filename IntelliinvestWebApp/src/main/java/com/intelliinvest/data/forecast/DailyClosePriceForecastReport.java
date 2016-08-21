package com.intelliinvest.data.forecast;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.ForecastedStockPriceRepository;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.ForecastedStockPrice;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.MailUtil;
import com.intelliinvest.util.MathUtil;
import com.intelliinvest.util.ScheduledThreadPoolHelper;

@ManagedResource(objectName = "bean:name=DailyClosePriceForecastReport", description = "DailyClosePriceForecastReport")
public class DailyClosePriceForecastReport {

	private static Logger logger = Logger.getLogger(DailyClosePriceForecaster.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private ForecastedStockPriceRepository forecastedStockPriceRepository;
	@Autowired
	private MailUtil mailUtil;

	private static final String DAILY_CLOSE_PRICE_FORECAST_REPORT = "DailyClosePriceForecastReport";
	private String reportDataDir;

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
		reportDataDir = IntelliInvestStore.properties.getProperty("daily.close.price.forecast.report.data.dir");
	}

	private void initializeScheduledTasks() {
		Runnable dailyClosePriceForecastReportTask = new Runnable() {
			public void run() {
				if (!DateUtil.isBankHoliday(DateUtil.getCurrentDate())) {
					try {
						// We need to generate forecast report for today
						generateAndEmailForecastReport(DateUtil.getCurrentDateWithNoTime());
					} catch (Exception e) {
						logger.error("Error while running dailyClosePriceForecastReportTask for NSE stocks "
								+ e.getMessage());
					}
				}
			}
		};
		ZonedDateTime zonedNow = DateUtil.getZonedDateTime();
		int dailyClosePricePredictStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("daily.close.price.forecast.report.start.hr"));
		int dailyClosePricePredictStartMin = new Integer(
				IntelliInvestStore.properties.getProperty("daily.close.price.forecast.report.start.min"));
		ZonedDateTime zonedNext = zonedNow.withHour(dailyClosePricePredictStartHour)
				.withMinute(dailyClosePricePredictStartMin).withSecond(0);
		if (zonedNow.compareTo(zonedNext) > 0) {
			zonedNext = zonedNext.plusDays(1);
		}
		Duration duration = Duration.between(zonedNow, zonedNext);
		long initialDelay = duration.getSeconds();
		ScheduledThreadPoolHelper.getScheduledExecutorService().scheduleAtFixedRate(dailyClosePriceForecastReportTask,
				initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);

		logger.info("Scheduled dailyClosePriceForecastReportTask");
	}

	private void generateAndEmailForecastReport(Date today) {
		try {
			// We need to compare today's close with the close prices forecasted
			// on the last business day
			Map<String, ForecastedStockPrice> forecastedPrices = forecastedStockPriceRepository
					.getDailyForecastStockPricesMapFromDB(today);
			Map<String, QuandlStockPrice> eodPrices = quandlEODStockPriceRepository.getEODStockPrices(today);

			List<Stock> stockDetails = stockRepository.getStocks();
			List<Stock> nonWorldStocks = new ArrayList<Stock>();
			for (Stock stock : stockDetails) {
				if (!stock.isWorldStock()) {
					nonWorldStocks.add(stock);
				}
			}
			System.out.println("nonWorldStocks:" + nonWorldStocks.size());
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String date = format.format(today);

			BufferedWriter writer = new BufferedWriter(
					new FileWriter(reportDataDir + "/" + DAILY_CLOSE_PRICE_FORECAST_REPORT + date + ".csv"));
			writeHeader(writer);
			int i = 1;

			try {
				for (Stock stock : nonWorldStocks) {
					try {
						double actualClose = 0.0;
						double forecastedClose = 0.0;
						double difference = 0.0;
						double percentDifference = 0.0;

						QuandlStockPrice eodPrice = eodPrices.get(stock.getCode());
						if (eodPrice != null) {
							actualClose = eodPrice.getClose();
						}
						ForecastedStockPrice forecastPrice = forecastedPrices.get(stock.getCode());
						if (forecastPrice != null) {
							forecastedClose = forecastPrice.getForecastPrice();
						}

						if (!(MathUtil.isNearZero(actualClose) || MathUtil.isNearZero(forecastedClose))) {
							difference = eodPrice.getClose() - forecastPrice.getForecastPrice();
							percentDifference = (difference * 100) / eodPrice.getClose();
						}

						LinkedList<String> valuesQueue = new LinkedList<String>();
						valuesQueue.add(date);
						valuesQueue.add(stock.getCode());
						valuesQueue.add(new Double(MathUtil.round(actualClose)).toString());
						valuesQueue.add(new Double(MathUtil.round(forecastedClose)).toString());
						valuesQueue.add(new Double(MathUtil.round(difference)).toString());
						valuesQueue.add(new Double(MathUtil.round(percentDifference)).toString());
						String valueLine = valuesQueue.toString().replaceAll("\\[|\\]", "");
						writer.write(valueLine);
						System.out.println("Writing Stock Number:" + i + " valueLine:" + valueLine);
						++i;
						writer.newLine();
						valuesQueue.clear();
					} catch (Exception e) {
						logger.error("Exception while writing DailyClosePriceForecastReportTask for stock:"
								+ stock.getCode());
					}
				}
			} finally {
				writer.flush();
				writer.close();
			}
			String[] recipients = {
					IntelliInvestStore.properties.getProperty("daily.close.price.forecast.report.recepients") };
			String subject = "Daily Close Price Forecast Report for " + format.format(today);
			String message = subject;
			String[] attachment = {
					reportDataDir + "/" + DAILY_CLOSE_PRICE_FORECAST_REPORT + format.format(today) + ".csv" };
			mailUtil.sendMail(recipients, subject, message, attachment);

		} catch (Exception e) {
			logger.error("Exception while writing DailyClosePriceForecastReportTask for " + today);
		}
	}

	private void writeHeader(BufferedWriter writer) throws Exception {
		LinkedList<String> valuesQueue = new LinkedList<String>();
		valuesQueue.add("CloseDate");
		valuesQueue.add("StockCode");
		valuesQueue.add("ActualClose");
		valuesQueue.add("ForecastedClose");
		valuesQueue.add("Difference");
		valuesQueue.add("%Difference");
		String valueLine = valuesQueue.toString().replaceAll("\\[|\\]", "");
		writer.write(valueLine);
		writer.newLine();
	}

	@ManagedOperation(description = "generateAndEmailForecastReport")
	@ManagedOperationParameters({
			@ManagedOperationParameter(name = "Forecast Date (yyyy-MM-dd)", description = "Forecast Date") })
	public String generateAndEmailForecastReport(String forecastDate) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = format.parse(forecastDate);
			generateAndEmailForecastReport(date);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Success";
	}
}
