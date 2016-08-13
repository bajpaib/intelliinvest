package com.intelliinvest.web.util;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import com.intelliinvest.web.common.IntelliInvestStore;

public class DateUtil {

	public static final ZoneId ZONE_ID = ZoneId.of(IntelliInvestStore.properties.getProperty("default.timezone"));

	public static ZonedDateTime getZonedDateTime() {
		return ZonedDateTime.now(ZONE_ID);
	}

	public static Date getCurrentDate() {
		ZonedDateTime zonedNow = getZonedDateTime();
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

	public static Date getLastBusinessDate() {
		ZonedDateTime zonedNow = getZonedDateTime();
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

	public static Date addDaysToDate(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}
}