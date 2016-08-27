package com.intelliinvest.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.intelliinvest.common.IntelliInvestStore;

public class DateUtil {

	public static final ZoneId ZONE_ID = ZoneId.of(IntelliInvestStore.properties.getProperty("default.timezone"));
	
	public static LocalDate getLocalDate() {
		return LocalDate.now(ZONE_ID);
	}

	public static Date getDateFromLocalDate() {
		return getDateFromLocalDate(getLocalDate());
	}

	public static LocalDateTime getLocalDateTime() {
		return LocalDateTime.now(ZONE_ID);
	}

	public static Date getDateFromLocalDateTime() {
		return getDateFromLocalDateTime(getLocalDateTime());
	}

	public static DayOfWeek getDayOfWeek() {
		return getLocalDate().getDayOfWeek();
	}

	public static LocalDate getNextBusinessDate() {
		LocalDate localDate = getLocalDate();
		LocalDate nextBusinessDate = addBusinessDays(localDate, 1);
		return nextBusinessDate;
	}

	public static LocalDate getLastBusinessDate() {
		LocalDate localDate = getLocalDate();
		LocalDate nextBusinessDate = substractBusinessDays(localDate, 1);
		return nextBusinessDate;
	}

	public static LocalDate getNextBusinessDate(LocalDate date) {
		LocalDate nextBusinessDate = addBusinessDays(date, 1);
		return nextBusinessDate;
	}

	public static LocalDate getLastBusinessDate(LocalDate date) {
		LocalDate nextBusinessDate = substractBusinessDays(date, 1);
		return nextBusinessDate;
	}

	// Conversion from java.time.LocalDate to java.util.Date
	public static Date getDateFromLocalDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay().atZone(ZONE_ID).toInstant());
	}

	// Conversion from java.util.Date to java.time.LocalDate
	public static LocalDate getLocalDateFromDate(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZONE_ID).toLocalDate();
	}

	// Conversion from java.time.LocalDateTime to java.util.Date
	public static Date getDateFromLocalDateTime(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
	}

	// Conversion from java.util.Date to java.time.LocalDateTime
	public static LocalDateTime getLocalDateTimeFromDate(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZONE_ID);
	}

	public static LocalDate addBusinessDays(LocalDate date, int workdays) {
		if (workdays < 1) {
			return date;
		}
		LocalDate retVal = date.plusDays(workdays);	
		while (isBankHoliday(retVal)){
			retVal = retVal.plusDays(1);
		}
		return retVal;
	}
	
	public static LocalDate substractBusinessDays(LocalDate date, int workdays) {
		if (workdays < 1) {
			return date;
		}
		LocalDate retVal = date.minusDays(workdays);	
		while (isBankHoliday(retVal)){
			retVal = retVal.minusDays(1);
		}
		return retVal;
	}

	public static boolean isBankHoliday(LocalDate date) {
		// add holiday calendar later
		if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return true;
		}
		return false;
	}

	/* Formatted Date: ddmmyyyy */
	public static int convertToJulian(LocalDate date) {
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

	/*
	 * public static void main(String[] args) { LocalDateTime localDateTime =
	 * getLocalDateTime(); LocalDate localDate = getLocalDate();
	 * System.out.println("localDateTime:" + localDateTime);
	 * System.out.println("localDate:" + localDate);
	 * System.out.println("getDateFromLocalDateTime:" +
	 * getDateFromLocalDateTime(localDateTime));
	 * System.out.println("getDateFromLocalDate:" +
	 * getDateFromLocalDate(localDate)); }
	 */
}
