package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;


@Document(collection=User.COLLECTION_NAME)
public class User implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final String COLLECTION_NAME = "USER";
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
	@JsonSerialize(using=JsonDateSerializer.class)
	private LocalDate renewalDate;
	@JsonSerialize(using=JsonDateSerializer.class)
	private LocalDate expiryDate;
	@JsonSerialize(using=JsonDateTimeSerializer.class)
	private LocalDateTime lastLoginDate;
	@JsonSerialize(using=JsonDateSerializer.class)
	private LocalDate createDate;
	@JsonSerialize(using=JsonDateTimeSerializer.class)
	private LocalDateTime updateDate;
	
	public User() {
		super();
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

	public LocalDate getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDate createDate) {
		this.createDate = createDate;
	}

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}
	
	public LocalDate getRenewalDate() {
		return renewalDate;
	}

	public void setRenewalDate(LocalDate renewalDate) {
		this.renewalDate = renewalDate;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDate expiryDate) {
		this.expiryDate = expiryDate;
	}

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
