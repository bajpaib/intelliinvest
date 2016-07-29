package com.intelliinvest.web.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class MathUtil {

	private static final int DEFAULT_ROUND_PLACES = 2;

	public static double round(double value) {
		return round(value, DEFAULT_ROUND_PLACES);
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	public static void main(String[] args){
		ZoneId zoneId = ZoneId.of("Asia/Calcutta");
		ZonedDateTime zonedNow = ZonedDateTime.now(zoneId);	
		DayOfWeek dayOfWeek = zonedNow.getDayOfWeek();
		int hour = zonedNow.getHour();		
		System.out.println(zonedNow);
		System.out.println(dayOfWeek);
		System.out.println(hour);		
	}
}
