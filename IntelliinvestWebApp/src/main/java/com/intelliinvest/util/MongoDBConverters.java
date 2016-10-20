package com.intelliinvest.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

import com.intelliinvest.data.model.RiskProfileResult.Answer;
import com.intelliinvest.data.model.RiskProfileResult.RiskInvestmentProfileKey;

public class MongoDBConverters {

	public static final ZoneId ZONE_ID = ZoneId.of("Asia/Calcutta");
	public static final String SEPERATOR = "::";

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

	public static class StringFromAnswer implements Converter<Answer, String> {
		@Override
		public String convert(Answer answer) {
			return answer.getQuestionGroupId() + SEPERATOR + answer.getQuestionId() + SEPERATOR + answer.getOptionId();
		}
	}

	public static class AnswerFromString implements Converter<String, Answer> {
		@Override
		public Answer convert(String value) {
			String[] values = value.split(SEPERATOR);
			return new Answer(values[0], values[1], values[2]);
		}
	}

	public static class StringFromRiskInvestmentProfileKey implements Converter<RiskInvestmentProfileKey, String> {
		@Override
		public String convert(RiskInvestmentProfileKey riskInvestmentProfileKey) {
			return riskInvestmentProfileKey.getTimeHorizonOptionId() + SEPERATOR
					+ riskInvestmentProfileKey.getRiskType();
		}
	}

	public static class RiskInvestmentProfileKeyFromString implements Converter<String, RiskInvestmentProfileKey> {
		@Override
		public RiskInvestmentProfileKey convert(String value) {
			String[] values = value.split(SEPERATOR);
			return new RiskInvestmentProfileKey(values[0], values[1]);
		}
	}

}
