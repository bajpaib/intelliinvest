package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

@Document(collection = "HOLIDAY_CALENDAR")
public class Holiday implements Serializable {

	@Id
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate date;
	private String desc;

	public Holiday() {
		super();
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return "Holiday [date=" + date + ", desc=" + desc + "]";
	}

}