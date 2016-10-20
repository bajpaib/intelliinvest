package com.intelliinvest.web.bo.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class FundamentalAnalysisTimeSeriesResponse {
	private String id;
	private String attrName;
	private List<String> attrDateSeries;
	private List<Double> attrValSeries;
	private List<Double> indValSeries;
	private List<String> indDateSeries;
	private boolean success;
	private String message;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAttrName() {
		return attrName;
	}
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}
	public List<String> getAttrDateSeries() {
		return attrDateSeries;
	}
	public void setAttrDateSeries(List<String> attrDateSeries) {
		this.attrDateSeries = attrDateSeries;
	}
	public List<Double> getAttrValSeries() {
		return attrValSeries;
	}
	public void setAttrValSeries(List<Double> attrValSeries) {
		this.attrValSeries = attrValSeries;
	}
	public List<Double> getIndValSeries() {
		return indValSeries;
	}
	public void setIndValSeries(List<Double> indValSeries) {
		this.indValSeries = indValSeries;
	}
	public List<String> getIndDateSeries() {
		return indDateSeries;
	}
	public void setIndDateSeries(List<String> indDateSeries) {
		this.indDateSeries = indDateSeries;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
