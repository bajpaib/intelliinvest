package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = "USER")
public class User implements Serializable {

	@Id
	private String userId;
	private String username;
	private String phone;
	private String password;
	private String plan;
	private String userType;
	private String active;
	private String activationCode;
	private boolean loggedIn;
	private boolean sendNotification;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate renewalDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDate expiryDate;
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private LocalDateTime lastLoginDate;
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private LocalDateTime createDate;
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private LocalDateTime updateDate;
	private String deviceId;

	public User() {
		super();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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

	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public boolean getLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public boolean getSendNotification() {
		return sendNotification;
	}

	public void setSendNotification(boolean sendNotification) {
		this.sendNotification = sendNotification;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getRenewalDate() {
		return renewalDate;
	}

	public void setRenewalDate(LocalDate renewalDate) {
		this.renewalDate = renewalDate;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDate expiryDate) {
		this.expiryDate = expiryDate;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(LocalDateTime lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", username=" + username + ", phone=" + phone + ", password=" + password
				+ ", plan=" + plan + ", userType=" + userType + ", active=" + active + ", activationCode="
				+ activationCode + ", loggedIn=" + loggedIn + ", sendNotification=" + sendNotification
				+ ", renewalDate=" + renewalDate + ", expiryDate=" + expiryDate + ", lastLoginDate=" + lastLoginDate
				+ ", createDate=" + createDate + ", updateDate=" + updateDate + "]";
	}
}
