package com.intelliinvest.data.convertor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

import com.intelliinvest.util.DateUtil;

public class MongoDBConverters {

	public static class DateFromLocalDate implements Converter<LocalDate, Date> {
		// Conversion from java.time.LocalDate to java.util.Date
		public Date convert(LocalDate localDate) {
			return DateUtil.getDateFromLocalDate(localDate);
		}
	}

	public static class LocalDateFromDate implements Converter<Date, LocalDate> {
		// Conversion from java.util.Date to java.time.LocalDate
		public LocalDate convert(Date date) {
			return DateUtil.getLocalDateFromDate(date);
		}
	}

	public static class DateFromLocalDateTime implements Converter<LocalDateTime, Date> {
		// Conversion from java.time.LocalDateTime to java.util.Date
		public Date convert(LocalDateTime localDateTime) {
			return DateUtil.getDateFromLocalDateTime(localDateTime);
		}
	}

	public static class LocalDateTimeFromDate implements Converter<Date, LocalDateTime> {
		// Conversion from java.util.Date to java.time.LocalDateTime
		public LocalDateTime convert(Date date) {
			return DateUtil.getLocalDateTimeFromDate(date);
		}
	}
}