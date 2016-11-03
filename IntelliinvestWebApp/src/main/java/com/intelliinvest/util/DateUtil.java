package com.intelliinvest.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.HolidayRepository;

public class DateUtil {

	@Autowired
	private HolidayRepository holidayRepository;

	public final ZoneId ZONE_ID = ZoneId.of(IntelliInvestStore.properties.getProperty("default.timezone"));

	public LocalDate getLocalDate() {
		return LocalDate.now(ZONE_ID);
	}

	public Date getDateFromLocalDate() {
		return getDateFromLocalDate(getLocalDate());
	}

	public LocalDateTime getLocalDateTime() {
		return LocalDateTime.now(ZONE_ID);
	}

	public Date getDateFromLocalDateTime() {
		return getDateFromLocalDateTime(getLocalDateTime());
	}

	public DayOfWeek getDayOfWeek() {
		return getLocalDate().getDayOfWeek();
	}

	public LocalDate getNextBusinessDate() {
		LocalDate localDate = getLocalDate();
		LocalDate nextBusinessDate = addBusinessDays(localDate, 1);
		return nextBusinessDate;
	}

	public LocalDate getLastBusinessDate() {
		LocalDate localDate = getLocalDate();
		LocalDate nextBusinessDate = substractBusinessDays(localDate, 1);
		return nextBusinessDate;
	}

	public LocalDate getNextBusinessDate(LocalDate date) {
		LocalDate nextBusinessDate = addBusinessDays(date, 1);
		return nextBusinessDate;
	}

	public LocalDate getLastBusinessDate(LocalDate date) {
		LocalDate nextBusinessDate = substractBusinessDays(date, 1);
		return nextBusinessDate;
	}

	// Conversion from java.time.LocalDate to java.util.Date
	public Date getDateFromLocalDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay().atZone(ZONE_ID).toInstant());
	}

	// Conversion from java.util.Date to java.time.LocalDate
	public LocalDate getLocalDateFromDate(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZONE_ID).toLocalDate();
	}

	// Conversion from java.time.LocalDateTime to java.util.Date
	public Date getDateFromLocalDateTime(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
	}

	// Conversion from java.util.Date to java.time.LocalDateTime
	public LocalDateTime getLocalDateTimeFromDate(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZONE_ID);
	}

	public LocalDate addBusinessDays(LocalDate date, int workdays) {
		if (workdays < 1) {
			return date;
		}
		LocalDate retVal = date;
		for (int i = 0; i < workdays; ++i) {
			retVal = retVal.plusDays(1);
			while (isBankHoliday(retVal)) {
				retVal = retVal.plusDays(1);
			}
		}
		return retVal;
	}

	public LocalDate substractBusinessDays(LocalDate date, int workdays) {
		if (workdays < 1) {
			return date;
		}
		LocalDate retVal = date;
		for (int i = 0; i < workdays; ++i) {
			retVal = retVal.minusDays(1);
			while (isBankHoliday(retVal)) {
				retVal = retVal.minusDays(1);
			}
		}
		return retVal;
	}
	
	public LocalDate substractDays(LocalDate date, int workdays) {
		if (workdays < 1) {
			return date;
		}
		LocalDate retVal = date;
		retVal = retVal.minusDays(workdays);
		return retVal;
	}


	public boolean isBankHoliday(LocalDate date) {
		// add holiday calendar later
		if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
				|| holidayRepository.isHoliday(date)) {
			return true;
		}
		return false;
	}

	/* Formatted Date: ddmmyyyy */
	public int convertToJulian(LocalDate date) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy");
		String formattedDate = format.format(date);

		int resultJulian = 0;
		if (formattedDate.length() > 0) {
			/* Days of month */
			int[] monthValues = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

			String dayS, monthS, yearS;
			dayS = formattedDate.substring(0, 2);
			monthS = formattedDate.substring(2, 4);
			yearS = formattedDate.substring(4, 8);

			/* Convert to Integer */
			int day = Integer.valueOf(dayS);
			int month = Integer.valueOf(monthS);
			int year = Integer.valueOf(yearS);

			// Leap year check
			if (year % 4 == 0) {
				monthValues[1] = 29;
			}
			// Start building Julian date
			String julianDate = "1";
			// last two digit of year: 2012 ==> 12
			julianDate += yearS.substring(2, 4);
			int julianDays = 0;
			for (int i = 0; i < month - 1; i++) {
				julianDays += monthValues[i];
			}
			julianDays += day;
			if (String.valueOf(julianDays).length() < 2) {
				julianDate += "00";
			}
			if (String.valueOf(julianDays).length() < 3) {
				julianDate += "0";
			}
			julianDate += String.valueOf(julianDays);
			resultJulian = Integer.valueOf(julianDate);
		}
		return resultJulian;
	}

	public LocalDate addBusinessDays(int days) {
		LocalDate localDate = getLocalDate();
		return addBusinessDays(localDate, days);
	}

	/*
	 * public void main(String[] args) { LocalDateTime localDateTime =
	 * getLocalDateTime(); LocalDate localDate = getLocalDate();
	 * System.out.println("localDateTime:" + localDateTime);
	 * System.out.println("localDate:" + localDate);
	 * System.out.println("getDateFromLocalDateTime:" +
	 * getDateFromLocalDateTime(localDateTime));
	 * System.out.println("getDateFromLocalDate:" +
	 * getDateFromLocalDate(localDate)); }
	 */

	public static void main(String[] args) {

		List<String> quarterYear = new ArrayList<String>();
		quarterYear.add("2016Q4");
		quarterYear.add("2015Q3");
		quarterYear.add("2016Q2");
		quarterYear.add("2015Q1");
		quarterYear.add("2015Q4");
		quarterYear.add("2016Q3");
		quarterYear.add("2015Q2");
		quarterYear.add("2016Q1");

		String minYearQuarter = "2014Q1";
		String yearQuarter = "2014Q1";

		if (minYearQuarter.compareTo(yearQuarter) > 0) {
			System.out.println(minYearQuarter + "is greater than" + yearQuarter);
		} else {
			System.out.println(minYearQuarter + "is less than" + yearQuarter);
		}
	}
}