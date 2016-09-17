package com.intelliinvest.data.forecast;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

@ManagedResource(objectName = "bean:name=ClosePriceForecastReport", description = "ClosePriceForecastReport")
public class ClosePriceForecastReport {

	private static Logger logger = Logger.getLogger(ClosePriceForecastReport.class);
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;
	@Autowired
	private ForecastedStockPriceRepository forecastedStockPriceRepository;
	@Autowired
	private MailUtil mailUtil;
	@Autowired
	private DateUtil dateUtil;

	private static final String CLOSE_PRICE_FORECAST_REPORT = "ClosePriceForecastReport";
	private String reportDataDir;

	@PostConstruct
	public void init() {
		initializeScheduledTasks();
		reportDataDir = IntelliInvestStore.properties.getProperty("close.price.forecast.report.data.dir");
	}

	private void initializeScheduledTasks() {
		Runnable dailyClosePriceForecastReportTask = new Runnable() {
			public void run() {
				if (!dateUtil.isBankHoliday(dateUtil.getLocalDate())) {
					try {
						// We need to generate forecast report for today
						generateAndEmailForecastReport(dateUtil.getLocalDate());
					} catch (Exception e) {
						logger.error("Error while running dailyClosePriceForecastReportTask for NSE stocks "
								+ e.getMessage());
					}
				}
			}
		};
		LocalDateTime zonedNow = dateUtil.getLocalDateTime();
		int dailyClosePricePredictStartHour = new Integer(
				IntelliInvestStore.properties.getProperty("close.price.forecast.report.start.hr"));
		int dailyClosePricePredictStartMin = new Integer(
				IntelliInvestStore.properties.getProperty("close.price.forecast.report.start.min"));
		LocalDateTime zonedNext = zonedNow.withHour(dailyClosePricePredictStartHour)
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

	private void generateAndEmailForecastReport(LocalDate today) {
		try {
			// Get Today's closing prices
			Map<String, QuandlStockPrice> eodPrices = quandlEODStockPriceRepository.getEODStockPrices(today);
			// We need to compare today's close with tomorrowForecastPrice
			// forecasted a day before (on T-1)
			LocalDate lastBusinessDate = dateUtil.getLastBusinessDate(today);
			Map<String, ForecastedStockPrice> dailyForecastedPrices = forecastedStockPriceRepository
					.getForecastStockPricesMapFromDB(lastBusinessDate);

			// We need to compare today's close with weeklyForecastPrice
			// forecasted a day before (on T-5)
			LocalDate weeklyBusinessDate = dateUtil.substractBusinessDays(today, 5);
			Map<String, ForecastedStockPrice> weeklyForecastedPrices = forecastedStockPriceRepository
					.getForecastStockPricesMapFromDB(weeklyBusinessDate);

			// We need to compare today's close with weeklyForecastPrice
			// forecasted a day before (on T-20)
			LocalDate monthlyBusinessDate = dateUtil.substractBusinessDays(today, 20);
			Map<String, ForecastedStockPrice> monthlyForecastedPrices = forecastedStockPriceRepository
					.getForecastStockPricesMapFromDB(monthlyBusinessDate);

			List<Stock> stockDetails = stockRepository.getStocks();
			List<Stock> nonWorldStocks = new ArrayList<Stock>();
			for (Stock stock : stockDetails) {
				if (!stock.isWorldStock()) {
					nonWorldStocks.add(stock);
				}
			}
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String date = dateFormat.format(today);

			BufferedWriter writer = new BufferedWriter(
					new FileWriter(reportDataDir + "/" + CLOSE_PRICE_FORECAST_REPORT + date + ".csv"));
			writeHeader(writer);
			try {
				for (Stock stock : nonWorldStocks) {
					try {
						double actualClose = 0.0;
						double dailyForecastedClose = 0.0;
						double dailyDifference = 0.0;
						double percentDailyDifference = 0.0;

						QuandlStockPrice eodPrice = eodPrices.get(stock.getSecurityId());
						if (eodPrice != null) {
							actualClose = eodPrice.getClose();
						}

						ForecastedStockPrice dailyForecastPrice = dailyForecastedPrices.get(stock.getSecurityId());
						if (dailyForecastPrice != null && dailyForecastPrice.getTomorrowForecastDate().equals(today)) {
							dailyForecastedClose = dailyForecastPrice.getTomorrowForecastPrice();
							if (!(MathUtil.isNearZero(actualClose) || MathUtil.isNearZero(dailyForecastedClose))) {
								dailyDifference = eodPrice.getClose() - dailyForecastPrice.getTomorrowForecastPrice();
								percentDailyDifference = (dailyDifference * 100) / eodPrice.getClose();
							}
						}

						double weeklyForecastedClose = 0.0;
						double weeklyDifference = 0.0;
						double percentWeeklyDifference = 0.0;

						ForecastedStockPrice weeklyForecastPrice = weeklyForecastedPrices.get(stock.getSecurityId());
						if (weeklyForecastPrice != null && weeklyForecastPrice.getWeeklyForecastDate().equals(today)) {
							weeklyForecastedClose = weeklyForecastPrice.getWeeklyForecastPrice();
							if (!(MathUtil.isNearZero(actualClose) || MathUtil.isNearZero(weeklyForecastedClose))) {
								weeklyDifference = eodPrice.getClose() - weeklyForecastPrice.getWeeklyForecastPrice();
								percentWeeklyDifference = (weeklyDifference * 100) / eodPrice.getClose();
							}
						}

						double monthlyForecastedClose = 0.0;
						double monthlyDifference = 0.0;
						double percentMonthlyDifference = 0.0;
						ForecastedStockPrice monthlyForecastPrice = monthlyForecastedPrices.get(stock.getSecurityId());
						if (monthlyForecastPrice != null
								&& monthlyForecastPrice.getMonthlyForecastDate().equals(today)) {
							monthlyForecastedClose = monthlyForecastPrice.getMonthlyForecastPrice();
							if (!(MathUtil.isNearZero(actualClose) || MathUtil.isNearZero(monthlyForecastedClose))) {
								monthlyDifference = eodPrice.getClose()
										- monthlyForecastPrice.getMonthlyForecastPrice();
								percentMonthlyDifference = (monthlyDifference * 100) / eodPrice.getClose();
							}
						}

						LinkedList<String> valuesQueue = new LinkedList<String>();
						valuesQueue.add(date);
						valuesQueue.add(stock.getSecurityId());
						valuesQueue.add(new Double(MathUtil.round(actualClose)).toString());
						valuesQueue.add(new Double(MathUtil.round(dailyForecastedClose)).toString());
						valuesQueue.add(new Double(MathUtil.round(dailyDifference)).toString());
						valuesQueue.add(new Double(MathUtil.round(percentDailyDifference)).toString());
						valuesQueue.add(new Double(MathUtil.round(weeklyForecastedClose)).toString());
						valuesQueue.add(new Double(MathUtil.round(weeklyDifference)).toString());
						valuesQueue.add(new Double(MathUtil.round(percentWeeklyDifference)).toString());
						valuesQueue.add(new Double(MathUtil.round(monthlyForecastedClose)).toString());
						valuesQueue.add(new Double(MathUtil.round(monthlyDifference)).toString());
						valuesQueue.add(new Double(MathUtil.round(percentMonthlyDifference)).toString());
						String valueLine = valuesQueue.toString().replaceAll("\\[|\\]", "");
						writer.write(valueLine);
						// System.out.println("Writing Stock Code valueLine:" +
						// valueLine);
						writer.newLine();
						valuesQueue.clear();
					} catch (Exception e) {
						logger.error(
								"Exception while writing ClosePriceForecastReportTask for stock:" + stock.getSecurityId());
					}
				}
			} finally {
				writer.flush();
				writer.close();
			}
			String[] recipients = {
					IntelliInvestStore.properties.getProperty("close.price.forecast.report.recepients") };
			String subject = "Close Price Forecast Report for " + dateFormat.format(today);
			String message = subject;
			String[] attachment = {
					reportDataDir + "/" + CLOSE_PRICE_FORECAST_REPORT + dateFormat.format(today) + ".csv" };
			mailUtil.sendMail(recipients, subject, message, attachment);

		} catch (Exception e) {
			logger.error("Exception while writing ClosePriceForecastReportTask for " + today);
		}
	}

	private void writeHeader(BufferedWriter writer) throws Exception {
		LinkedList<String> valuesQueue = new LinkedList<String>();
		valuesQueue.add("CloseDate");
		valuesQueue.add("SecurityId");
		valuesQueue.add("ActualCls");
		valuesQueue.add("PredDailyCls");
		valuesQueue.add("PredDailyClsDiff");
		valuesQueue.add("%DailyClsDiff");
		valuesQueue.add("PredWeeklyCls");
		valuesQueue.add("PredWeeklyClsDiff");
		valuesQueue.add("%WeeklyClsDiff");
		valuesQueue.add("PredMonthlyCls");
		valuesQueue.add("PredMonthlyClsDiff");
		valuesQueue.add("%MonthlyClsDiff");
		String valueLine = valuesQueue.toString().replaceAll("\\[|\\]", "");
		writer.write(valueLine);
		writer.newLine();
	}

	@ManagedOperation(description = "generateAndEmailClosePriceForecastReport")
	@ManagedOperationParameters({
			@ManagedOperationParameter(name = "Today Date (yyyy-MM-dd)", description = "Today Date") })
	public String generateAndEmailClosePriceForecastReport(String today) {
		try {
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(today, dateFormat);
			generateAndEmailForecastReport(date);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Success";
	}
}