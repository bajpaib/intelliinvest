package com.intelliinvest.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class MongoDBConverters {

	public static final ZoneId ZONE_ID = ZoneId.of("Asia/Calcutta");

	public static class DateFromLocalDate implements Converter<LocalDate, Date> {
		// Conversion from java.time.LocalDate to java.util.Date
		public Date convert(LocalDate localDate) {
			return Date.from(localDate.atStartOfDay().atZone(ZONE_ID).toInstant());
		}
	}

	public static class LocalDateFromDate implements Converter<Date, LocalDate> {
		// Conversion from java.util.Date to java.time.LocalDate
		public LocalDate convert(Date date) {
			return LocalDateTime.ofInstant(date.toInstant(), ZONE_ID).toLocalDate();
		}
	}

	public static class DateFromLocalDateTime implements Converter<LocalDateTime, Date> {
		// Conversion from java.time.LocalDateTime to java.util.Date
		public Date convert(LocalDateTime localDateTime) {
			return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
		}
	}

	public static class LocalDateTimeFromDate implements Converter<Date, LocalDateTime> {
		// Conversion from java.util.Date to java.time.LocalDateTime
		public LocalDateTime convert(Date date) {
			return LocalDateTime.ofInstant(date.toInstant(), ZONE_ID);
		}
	}
}