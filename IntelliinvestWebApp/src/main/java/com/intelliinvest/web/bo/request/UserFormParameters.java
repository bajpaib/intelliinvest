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
	String deviceId;

	String reTakeQuestionaire;
	String pushNotification;
	String showZeroPortfolio;

	
	public UserFormParameters() {
		super();
	}
	
	public String getReTakeQuestionaire() {
		return reTakeQuestionaire;
	}

	public void setReTakeQuestionaire(String reTakeQuestionaire) {
		this.reTakeQuestionaire = reTakeQuestionaire;
	}

	public String getPushNotification() {
		return pushNotification;
	}

	public void setPushNotification(String pushNotification) {
		this.pushNotification = pushNotification;
	}

	public String getShowZeroPortfolio() {
		return showZeroPortfolio;
	}

	public void setShowZeroPortfolio(String showZeroPortfolio) {
		this.showZeroPortfolio = showZeroPortfolio;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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
