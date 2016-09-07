package com.intelliinvest.data.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = EODStockPrice.COLLECTION_NAME)
@CompoundIndexes({
		@CompoundIndex(name = "EOD_STOCK_PRICE_IDX", def = "{'exchange': 1, 'symbol': 1, 'eodDate': -1}") })
@CsvRecord( separator = "," , quote = "\"", skipFirstLine = true)
public class EODStockPrice {

	public static final String COLLECTION_NAME = "EOD_STOCK_PRICE";
	public static final String DEFAULT_EXCHANGE = "NSE";
	
	private String exchange = "NSE";
	private String symbol = "";
	private String series;
	@DataField(pos=1, pattern = "yyyy-MM-dd")
	@JsonSerialize(using=JsonDateSerializer.class)
	private LocalDate eodDate;
	@DataField(pos=2)
	private Double open;
	@DataField(pos=3)
	private Double high;
	@DataField(pos=4)
	private Double low;
	@DataField(pos=5)
	private Double last;
	@DataField(pos=6)
	private Double close;
	@DataField(pos=7)
	private Double tottrdqty;
	@DataField(pos=8)
	private Double tottrdval;
	@JsonSerialize(using=JsonDateTimeSerializer.class)
	private LocalDateTime updateDate = DateUtil.getLocalDateTime();

	
	public LocalDate getEodDate() {
		return eodDate;
	}



	public void setEodDate(LocalDate eodDate) {
		this.eodDate = eodDate;
	}



	public String getExchange() {
		return exchange;
	}



	public void setExchange(String exchange) {
		this.exchange = exchange;
	}



	public String getSymbol() {
		return symbol;
	}



	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}



	public String getSeries() {
		return series;
	}



	public void setSeries(String series) {
		this.series = series;
	}



	public Double getOpen() {
		return open;
	}



	public void setOpen(Double open) {
		this.open = open;
	}



	public Double getHigh() {
		return high;
	}



	public void setHigh(Double high) {
		this.high = high;
	}



	public Double getLow() {
		return low;
	}



	public void setLow(Double low) {
		this.low = low;
	}



	public Double getLast() {
		return last;
	}



	public void setLast(Double last) {
		this.last = last;
	}



	public Double getClose() {
		return close;
	}



	public void setClose(Double close) {
		this.close = close;
	}



	public Double getTottrdqty() {
		return tottrdqty;
	}



	public void setTottrdqty(Double tottrdqty) {
		this.tottrdqty = tottrdqty;
	}



	public Double getTottrdval() {
		return tottrdval;
	}



	public void setTottrdval(Double tottrdval) {
		this.tottrdval = tottrdval;
	}



	public LocalDateTime getUpdateDate() {
		return updateDate;
	}



	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}


	@Override
	public String toString() {
		return "QuandlStockPrice [exchange=" + exchange + ", symbol=" + symbol + ", series=" + series + ", open=" + open
				+ ", high=" + high + ", low=" + low + ", close=" + close + ", last=" + last + ", tottrdqty=" + tottrdqty
				+ ", tottrdval=" + tottrdval + ", eodDate=" + eodDate + ", updateDate=" + updateDate + "]";
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}