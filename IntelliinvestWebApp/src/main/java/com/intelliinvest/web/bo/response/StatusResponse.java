package com.intelliinvest.web.bo.response;

public class StatusResponse {
	String status = "SUCCESS";
	String message = "";

	public static final String SUCCESS = "SUCCESS";
	public static final String FAILED = "FAILED";

	public static final StatusResponse STATUS_SUCCESS = new StatusResponse(SUCCESS, "");
	public static final StatusResponse STATUS_FAILURE = new StatusResponse(FAILED, "");

	public StatusResponse(String status, String message) {
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