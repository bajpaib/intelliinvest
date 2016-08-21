package com.intelliinvest.common;

public class IntelliinvestException extends Exception {

	private String message;
	
	public IntelliinvestException(String errorMessage) {
		this.message = errorMessage;
	}
	
	@Override
	public String getMessage() {
	
		return message;
	}

	
	
}
