package com.intelliinvest.data.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Document(collection = "QUANDL_STOCK_PRICE")
@CompoundIndexes({
		@CompoundIndex(name = "QUANDL_STOCK_PRICE_IDX", def = "{'exchange': 1, 'symbol': 1, 'eodDate': -1}") })
public class QuandlStockPrice {

	private String exchange;
	private String symbol;
	private String series;
	private double open;
	private double high;
	private double low;
	private double close;
	private double last;
	private int tottrdqty;
	private double tottrdval;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate eodDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;

	public QuandlStockPrice() {
		super();
	}

	public QuandlStockPrice(String exchange, String symbol, String series, double open, double high, double low,
			double close, double last, int tottrdqty, double tottrdval, LocalDate eodDate, LocalDateTime updateDate) {
		super();
		this.exchange = exchange;
		this.symbol = symbol;
		this.series = series;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.last = last;
		this.tottrdqty = tottrdqty;
		this.tottrdval = tottrdval;
		this.eodDate = eodDate;
		this.updateDate = updateDate;
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

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getLast() {
		return last;
	}

	public void setLast(double last) {
		this.last = last;
	}

	public int getTottrdqty() {
		return tottrdqty;
	}

	public void setTottrdqty(int tottrdqty) {
		this.tottrdqty = tottrdqty;
	}

	public double getTottrdval() {
		return tottrdval;
	}

	public void setTottrdval(double tottrdval) {
		this.tottrdval = tottrdval;
	}

	public LocalDate getEodDate() {
		return eodDate;
	}

	public void setEodDate(LocalDate eodDate) {
		this.eodDate = eodDate;
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
	public QuandlStockPrice clone() throws CloneNotSupportedException {
		return new QuandlStockPrice(exchange, symbol, series, open, high, low, close, last, tottrdqty, tottrdval,
				eodDate, updateDate);
	}

}