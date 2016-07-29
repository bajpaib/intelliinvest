package com.intelliinvest.web.util;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import com.intelliinvest.web.common.IntelliInvestStore;

public class DateUtil {

	public static final ZoneId ZONE_ID = ZoneId.of(IntelliInvestStore.properties.getProperty("default.timezone"));

	public static Date getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date addDaysToDate(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

}
