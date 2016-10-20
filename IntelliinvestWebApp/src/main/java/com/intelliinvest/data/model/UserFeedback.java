package com.intelliinvest.data.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.intelliinvest.data.model.RiskProfileResult.RiskInvestmentProfile;

/**
 * todayDate (T) dailyForecastDate (T+1) weeklyForecastDate (T+5)
 * monthlyForecastDate (T+20)
 *
 */
@Document(collection = "USER_FEEDBACK")
public class UserFeedback {
	@Id 
	private String userId;
	private String feedback;
	private String feedbackOptionId;
	private RiskInvestmentProfile riskInvestmentProfile;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;
	
	public UserFeedback(String userId, String feedback, String feedbackOptionId, RiskInvestmentProfile riskInvestmentProfile, LocalDateTime updateDate) {
		super();
		this.userId = userId;
		this.feedback = feedback;
		this.feedbackOptionId = feedbackOptionId;
		this.riskInvestmentProfile = riskInvestmentProfile;
		this.updateDate = updateDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public String getFeedbackOptionId() {
		return feedbackOptionId;
	}

	public void setFeedbackOptionId(String feedbackOptionId) {
		this.feedbackOptionId = feedbackOptionId;
	}
	
	public RiskInvestmentProfile getRiskInvestmentProfile() {
		return riskInvestmentProfile;
	}
	
	public void setRiskInvestmentProfile(RiskInvestmentProfile riskInvestmentProfile) {
		this.riskInvestmentProfile = riskInvestmentProfile;
	}

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}


}