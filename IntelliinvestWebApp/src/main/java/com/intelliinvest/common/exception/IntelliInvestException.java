package com.intelliinvest.common.exception;

public class IntelliInvestException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public IntelliInvestException(String errorMessage) {
		super(errorMessage);
	}
	
	public IntelliInvestException(String errorMessage, Throwable e) {
		super(errorMessage, e);
	}
}
