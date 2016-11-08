package com.intelliinvest.web.bo.response;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

@JsonAutoDetect
public class UserResponse implements Serializable {
	
	private String userId;
	private String username;
	private String phone;
	private String password;
	private String plan;
	private String userType;
	private String active;
	private String activationCode;
	private LocalDateTime createDate;
	private LocalDateTime updateDate;
	private LocalDate renewalDate;
	private LocalDate expiryDate;
	private LocalDateTime lastLoginDate;
	private boolean loggedIn;
	private boolean sendNotification;
	private boolean success;
	private String message;
	private String deviceId;
	
	public UserResponse() {
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

	@JsonSerialize(using = JsonDateTimeSerializer.class)
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

	@Override
	public String toString() {
		return "UserResponse [userId=" + userId + ", username=" + username + ", phone=" + phone + ", password="
				+ password + ", plan=" + plan + ", userType=" + userType + ", active=" + active + ", activationCode="
				+ activationCode + ", createDate=" + createDate + ", updateDate=" + updateDate + ", renewalDate="
				+ renewalDate + ", expiryDate=" + expiryDate + ", lastLoginDate=" + lastLoginDate + ", loggedIn="
				+ loggedIn + ", sendNotification=" + sendNotification + ", success=" + success + ", message=" + message
				+ "]";
	}

}
