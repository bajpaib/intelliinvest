package com.intelliinvest.data.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "RISK_PROFILE_QUESTIONNAIRE")
public class RiskProfileQuestionnaire {
	
	public static String TIME_HORIZON_QUESTION_ID = "TH";
	public static String FEED_BACK_QUESTION_ID = "FB";
	
	@Id
	String questionnaireId = "1";
	List<QuestionGroup> questionGroups;
	
	public String getQuestionnaireId() {
		return questionnaireId;
	}

	public void setQuestionnaireId(String questionnaireId) {
		this.questionnaireId = questionnaireId;
	}

	public List<QuestionGroup> getQuestionGroups() {
		return questionGroups;
	}

	public void setQuestionGroups(List<QuestionGroup> questionGroups) {
		this.questionGroups = questionGroups;
	}

	@Override
	public String toString() {
		return "RsikProfileQuestionnaire [questionnaireId=" + questionnaireId + ", questionGroups=" + questionGroups + "]";
	}
	
	public static class QuestionGroup{
		String groupId;
		String groupName;
		List<Question> questions;
		
		public QuestionGroup() {
			// TODO Auto-generated constructor stub
		}
		
		public QuestionGroup(String groupId, String groupName, List<Question> questions) {
			super();
			this.groupId = groupId;
			this.groupName = groupName;
			this.questions = questions;
		}

		public String getGroupId() {
			return groupId;
		}
		
		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}
		
		public String getGroupName() {
			return groupName;
		}

		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}

		public List<Question> getQuestions() {
			return questions;
		}

		public void setQuestions(List<Question> questions) {
			this.questions = questions;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof QuestionGroup){
				QuestionGroup questionGroup = (QuestionGroup)obj;
				return this.groupId.equals(questionGroup.groupId);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return (this.groupId).hashCode();
		}
		
		@Override
		public String toString() {
			return "QuestionGroup [groupId=" + groupId + ", groupName=" + groupName + ", questions=" + questions+ "]";
		}
		
	}
	
	public static class Question{
		String groupId;
		String questionId;
		String question;
		List<Option> options;
		
		public Question() {
			// TODO Auto-generated constructor stub
		}
		
		public Question(String groupId, String questionId, String question, List<Option> options) {
			super();
			this.groupId = groupId;
			this.questionId = questionId;
			this.question = question;
			this.options = options;
		}
		
		public String getGroupId() {
			return groupId;
		}
		
		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}
		
		public String getQuestionId() {
			return questionId;
		}
		
		public void setQuestionId(String questionId) {
			this.questionId = questionId;
		}
		
		public String getQuestion() {
			return question;
		}
		public void setQuestion(String question) {
			this.question = question;
		}
		public List<Option> getOptions() {
			return options;
		}
		public void setOptions(List<Option> options) {
			this.options = options;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Question){
				Question question = (Question)obj;
				return this.groupId.equals(question.groupId)
					&& this.questionId.equals(question.questionId);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return (this.groupId + this.questionId).hashCode();
		}
		
		@Override
		public String toString() {
			return "Question [groupId=" + groupId + ", questionId=" + questionId + ", question=" + question + questionId + ", options = " + options +"] ";
		}
		
	}
	
	public static class Option{
		String optionId;
		String option;
		
		public Option() {
			// TODO Auto-generated constructor stub
		}
		
		public Option(String optionId, String option) {
			super();
			this.optionId = optionId;
			this.option = option;
		}

		public String getOptionId() {
			return optionId;
		}
		
		public void setOptionId(String optionId) {
			this.optionId = optionId;
		}
		
		public String getOption() {
			return option;
		}

		public void setOption(String option) {
			this.option = option;
		}
		
		@Override
		public String toString() {
			return "Option [optionId=" + optionId + ", option=" + option+ "]";
		}

	}
}

