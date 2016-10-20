package com.intelliinvest.data.model;

import java.util.Map;
import java.util.NavigableMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "RISK_PROFILE_RESULT")
public class RiskProfileResult {
	
	public static String TIME_HORIZON_QUESTION_ID = "TH";
	public static String FEED_BACK_QUESTION_ID = "FB";
	
	@Id
	String resultId = "1";
	
	Map<Answer, Integer> scoreMap;
	
	NavigableMap<Integer, String> scoreToRiskMap;
	
	Map<RiskInvestmentProfileKey, RiskInvestmentProfile> riskInvestmentProfiles;
	
	public String getResultId() {
		return resultId;
	}
	
	public void setResultId(String resultId) {
		this.resultId = resultId;
	}
	
	public Map<Answer, Integer> getScoreMap() {
		return scoreMap;
	}

	public void setScoreMap(Map<Answer, Integer> scoreMap) {
		this.scoreMap = scoreMap;
	}

	public NavigableMap<Integer, String> getScoreToRiskMap() {
		return scoreToRiskMap;
	}

	public void setScoreToRiskMap(NavigableMap<Integer, String> scoreToRiskMap) {
		this.scoreToRiskMap = scoreToRiskMap;
	}

	public Map<RiskInvestmentProfileKey, RiskInvestmentProfile> getRiskInvestmentProfiles() {
		return riskInvestmentProfiles;
	}

	public void setRiskInvestmentProfiles(Map<RiskInvestmentProfileKey, RiskInvestmentProfile> riskInvestmentProfiles) {
		this.riskInvestmentProfiles = riskInvestmentProfiles;
	}

	@Override
	public String toString() {
		return "RsikProfileQuestionnaire [resultId=" + resultId + ", scoreMap=" + scoreMap 
										+ ", scoreToRiskMap=" + scoreToRiskMap 
										+ ", riskInvestmentProfiles=" + riskInvestmentProfiles + "]";
	}
	
	public static class RiskInvestmentProfile {
		String description;
		Map<String, Integer> investmentDetails;
		
		public RiskInvestmentProfile() {
			// TODO Auto-generated constructor stub
		}
		
		public RiskInvestmentProfile(String description, Map<String, Integer> investmentDetails) {
			super();
			this.description = description;
			this.investmentDetails = investmentDetails;
		}

		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}

		public Map<String, Integer> getInvestmentDetails() {
			return investmentDetails;
		}


		public void setInvestmentDetails(Map<String, Integer> investmentDetails) {
			this.investmentDetails = investmentDetails;
		}
		
		@Override
		public String toString() {
			return "RiskInvestmentProfile [description=" + description + ", investmentDetails=" + investmentDetails+ "]";
		}
		
	}
	
	public static class Answer{
		String questionGroupId;
		String questionId;
		String optionId;
		String comments;
		
		public Answer() {
			// TODO Auto-generated constructor stub
		}
		
		public Answer(String questionGroupId, String questionId, String optionId) {
			super();
			this.questionGroupId = questionGroupId;
			this.questionId = questionId;
			this.optionId = optionId;
		}
		
		public String getQuestionGroupId() {
			return questionGroupId;
		}
		
		public void setQuestionGroupId(String questionGroupId) {
			this.questionGroupId = questionGroupId;
		}
		
		public String getQuestionId() {
			return questionId;
		}
		public void setQuestionId(String questionId) {
			this.questionId = questionId;
		}
		public String getOptionId() {
			return optionId;
		}
		
		public void setOptionId(String optionId) {
			this.optionId = optionId;
		}
		
		public String getComments() {
			return comments;
		}
		
		public void setComments(String comments) {
			this.comments = comments;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Answer){
				Answer answer = (Answer)obj;
				return this.questionGroupId.equals(answer.questionGroupId)
					&& this.questionId.equals(answer.questionId) && this.optionId.equals(answer.optionId);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return (this.questionGroupId + this.questionId + this.optionId).hashCode();
		}
		
		@Override
		public String toString() {
			return "Answer [questionGroupId=" + questionGroupId + ", questionId=" + questionId + ", optionId=" + optionId + ", comments=" + comments + "]";
		}
	}
	
	public static class RiskInvestmentProfileKey{
		String timeHorizonOptionId;
		String riskType;
		
		public RiskInvestmentProfileKey() {
			// TODO Auto-generated constructor stub
		}
		
		public RiskInvestmentProfileKey(String timeHorizonOptionId, String riskType) {
			super();
			this.timeHorizonOptionId = timeHorizonOptionId;
			this.riskType = riskType;
		}

		public String getTimeHorizonOptionId() {
			return timeHorizonOptionId;
		}

		public void setTimeHorizonOptionId(String timeHorizonOptionId) {
			this.timeHorizonOptionId = timeHorizonOptionId;
		}

		public String getRiskType() {
			return riskType;
		}

		public void setRiskType(String riskType) {
			this.riskType = riskType;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof RiskInvestmentProfileKey){
				RiskInvestmentProfileKey riskInvestmentProfileKey = (RiskInvestmentProfileKey)obj;
				return this.timeHorizonOptionId.equals(riskInvestmentProfileKey.timeHorizonOptionId)
					&& this.riskType.equals(riskInvestmentProfileKey.riskType);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return (this.timeHorizonOptionId + this.riskType).hashCode();
		}
		
		@Override
		public String toString() {
			return "RiskInvestmentProfileKey [timeHorizonOptionId=" + timeHorizonOptionId + ", riskType=" + riskType + "]";
		}
		
	}
	
}

