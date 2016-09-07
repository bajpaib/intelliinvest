package com.intelliinvest.response;

public class Status {
	String status =  "SUCCESS";
	String message = "";
	
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILED = "FAILED";
	
	public static final Status STATUS_SUCCESS = new Status(SUCCESS, "");
	public static final Status STATUS_FAILURE = new Status(FAILED, "");
	
	public Status(String status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
