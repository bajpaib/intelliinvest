package com.intelliinvest.data.dao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.data.model.Holiday;
import com.intelliinvest.util.Helper;

@ManagedResource(objectName = "bean:name=HolidayRepository", description = "HolidayRepository")
public class HolidayRepository {
	private static Logger logger = Logger.getLogger(HolidayRepository.class);
	private static final String COLLECTION_HOLIDAY_CALENDAR = "HOLIDAY_CALENDAR";
	@Autowired
	private MongoTemplate mongoTemplate;
	private Map<String, Holiday> holidayCache = new ConcurrentHashMap<String, Holiday>();

	DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		List<Holiday> holidays = getHolidaysFromDB();
		if (Helper.isNotNullAndNonEmpty(holidays)) {
			for (Holiday holiday : holidays) {
				holidayCache.put(dateFormat.format(holiday.getDate()), holiday);
			}
			logger.info("Initialised holidayCache in HolidayRepository from DB with size " + holidayCache.size());
		} else {
			logger.error("Could not initialise holidayCache from DB in HolidayRepository. HOLIDAY_CALENDAR is empty.");
		}
	}

	public boolean isHoliday(LocalDate date) throws DataAccessException {
		if (holidayCache.containsKey(dateFormat.format(date))) {
			return true;
		}
		return false;
	}

	public List<Holiday> getHolidays() {
		logger.debug("Inside getHolidays()...");
		List<Holiday> retVal = new ArrayList<Holiday>();
		if (holidayCache.size() == 0) {
			logger.error("Inside getHolidays() holidayCache is empty");
		}
		for (Holiday holiday : holidayCache.values()) {
			retVal.add(holiday);
		}
		return retVal;
	}

	public List<Holiday> getHolidaysFromDB() throws DataAccessException {
		logger.debug("Inside getHolidaysFromDB()...");
		return mongoTemplate.findAll(Holiday.class, COLLECTION_HOLIDAY_CALENDAR);
	}

	@ManagedOperation(description = "isHoliday")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "date(yyyy-MM-dd)", description = "Holiday Date") })
	public String isHoliday(String date) throws DataAccessException {
		if (holidayCache.get(date) != null) {
			return holidayCache.get(date).toString();
		}
		return "Not a holiday";
	}
}