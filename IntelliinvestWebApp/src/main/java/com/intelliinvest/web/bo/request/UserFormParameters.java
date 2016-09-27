package com.intelliinvest.web.bo.request;

import java.io.Serializable;

public class UserFormParameters implements Serializable {
	String userId;
	String username;
	String phone;
	String password;
	String sendNotification;
	String activationCode;
	String oldPassword;

	public UserFormParameters() {
		super();
	}
	
	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSendNotification() {
		return sendNotification;
	}

	public void setSendNotification(String sendNotification) {
		this.sendNotification = sendNotification;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}
}
