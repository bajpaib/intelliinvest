package com.intelliinvest.data.forecast;

public class TrainingData {
	double date;
	double open;
	double high;
	double low;
	double last;
	double close;
	int tottrdqty;
	double outputClose;

	public TrainingData(double date, double open, double high, double low, double last, double close, int tottrdqty,
			double outputClose) {
		super();
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.last = last;
		this.close = close;
		this.tottrdqty = tottrdqty;
		this.outputClose = outputClose;
	}

	public double getDate() {
		return date;
	}

	public void setDate(double date) {
		this.date = date;
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

	public double getLast() {
		return last;
	}

	public void setLast(double last) {
		this.last = last;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public int getTottrdqty() {
		return tottrdqty;
	}

	public void setTottrdqty(int tottrdqty) {
		this.tottrdqty = tottrdqty;
	}

	public double getOutputClose() {
		return outputClose;
	}

	public void setOutputClose(double outputClose) {
		this.outputClose = outputClose;
	}

	@Override
	public String toString() {
		return "TrainingData [date=" + date + ", open=" + open + ", high=" + high + ", low=" + low + ", last=" + last
				+ ", close=" + close + ", tottrdqty=" + tottrdqty + ", outputClose=" + outputClose + "]";
	}
}