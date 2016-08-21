package com.intelliinvest.util;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import com.intelliinvest.common.IntelliInvestStore;

public class DateUtil {

	public static final ZoneId ZONE_ID = ZoneId.of(IntelliInvestStore.properties.getProperty("default.timezone"));

	public static ZonedDateTime getZonedDateTime() {
		return ZonedDateTime.now(ZONE_ID);
	}

	public static ZonedDateTime getZonedDateTimeWithNoTime() {
		ZonedDateTime zonedNow = getZonedDateTime();		
		return ZonedDateTime.of(zonedNow.getYear(),zonedNow.getMonthValue(),zonedNow.getDayOfMonth(),0,0,0,0,ZONE_ID);
	}
	
	public static Date getCurrentDate() {
		ZonedDateTime zonedNow = getZonedDateTime();
		return Date.from(zonedNow.toInstant());
	}
	
	public static Date getCurrentDateWithNoTime() {
		ZonedDateTime zonedNow = getZonedDateTimeWithNoTime();		
		return Date.from(zonedNow.toInstant());
	}

	public static DayOfWeek getDayOfWeek() {
		return getZonedDateTime().getDayOfWeek();
	}

	public static Date getNextBusinessDate() {
		ZonedDateTime zonedNow = getZonedDateTime();
		ZonedDateTime nextBusinessDate = addWorkingDays(zonedNow, 1);
		return Date.from(nextBusinessDate.toInstant());
	}

	public static Date getNextBusinessDate(Date date) {
		ZonedDateTime zonedNow = getZonedDateTime();
		ZonedDateTime nextBusinessDate = addWorkingDays(zonedNow, 1);
		return Date.from(nextBusinessDate.toInstant());
	}

	public static Date getLastBusinessDate() {
		ZonedDateTime zonedNow = getZonedDateTimeWithNoTime();
		ZonedDateTime nextBusinessDate = substractWorkingDays(zonedNow, 1);
		return Date.from(nextBusinessDate.toInstant());
	}

	public static ZonedDateTime addWorkingDays(ZonedDateTime date, int workdays) {
		if (workdays < 1) {
			return date;
		}
		ZonedDateTime result = date;
		int addedDays = 0;
		while (addedDays < workdays) {
			result = result.plusDays(1);
			if (!(result.getDayOfWeek() == DayOfWeek.SATURDAY || result.getDayOfWeek() == DayOfWeek.SUNDAY)) {
				++addedDays;
			}
		}
		return result;
	}

	public static ZonedDateTime substractWorkingDays(ZonedDateTime date, int workdays) {
		if (workdays < 1) {
			return date;
		}
		ZonedDateTime result = date;
		int substractedDays = 0;
		while (substractedDays < workdays) {
			result = result.plusDays(-1);
			if (!(result.getDayOfWeek() == DayOfWeek.SATURDAY || result.getDayOfWeek() == DayOfWeek.SUNDAY)) {
				++substractedDays;
			}
		}
		return result;
	}

	public static Date addBusinessDaysToDate(Date date, int days) {
		date = addDaysToDate(date, 1);
		while (isBankHoliday(date)) {
			date = addDaysToDate(date, 1);
		}
		return date;
	}

	public static Date addDaysToDate(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

	public static boolean isBankHoliday(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		if ((Calendar.SATURDAY == c.get(c.DAY_OF_WEEK)) || (Calendar.SUNDAY == c.get(c.DAY_OF_WEEK))) {
			return (true);
		} else {
			return false;
		}
	}

	/* Unformatted Date: ddmmyyyy */
	public static int convertToJulian(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
		String unformattedDate = format.format(date);

		int resultJulian = 0;
		if (unformattedDate.length() > 0) {
			/* Days of month */
			int[] monthValues = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

			String dayS, monthS, yearS;
			dayS = unformattedDate.substring(0, 2);
			monthS = unformattedDate.substring(2, 4);
			yearS = unformattedDate.substring(4, 8);

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
	
/*	public static void main(String[] args){
		System.out.println(getZonedDateTime());
		System.out.println(getCurrentDate());
		System.out.println(getCurrentDateWithNoTime());
	}*/
}