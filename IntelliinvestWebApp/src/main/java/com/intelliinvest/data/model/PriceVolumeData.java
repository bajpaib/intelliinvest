package com.intelliinvest.data.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class PriceVolumeData {
	@DateTimeFormat(iso = ISO.DATE)
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	LocalDateTime date;
	Double price;
	Long volume;
	
	public PriceVolumeData(LocalDateTime date, Double price, Long volume) {
		super();
		this.date = date;
		this.price = price;
		this.volume = volume;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}
	
	
}
