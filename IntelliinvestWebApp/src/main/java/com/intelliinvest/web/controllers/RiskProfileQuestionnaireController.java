package com.intelliinvest.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.dao.RiskProfileQuestionnaireRepository;
import com.intelliinvest.data.model.RiskProfileQuestionnaire;
import com.intelliinvest.data.model.RiskProfileQuestionnaire.Question;
import com.intelliinvest.data.model.RiskProfileQuestionnaire.QuestionGroup;
import com.intelliinvest.data.model.RiskProfileResult.Answer;
import com.intelliinvest.data.model.RiskProfileResult.RiskInvestmentProfile;

@Controller
public class RiskProfileQuestionnaireController {

	private static Logger logger = Logger.getLogger(RiskProfileQuestionnaireController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private RiskProfileQuestionnaireRepository riskProfileQuestionnaireRepository;
	
	@RequestMapping(value = "/risk/profile/questionnaire", method = RequestMethod.POST, consumes=MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String saveQuestionanaire(@RequestBody String content) {
		logger.info("saveQuestionanaire");
		try{
			riskProfileQuestionnaireRepository.saveQuestionanaire(content);
		}catch (Exception e) {
			logger.info(e);
		}
		return "SUCCESS";
	}
	
	@RequestMapping(value = "/risk/profile/questions", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody List<Question> getRiskProfileQuestions(@RequestParam("userId") String userId) {
		logger.info("getting RiskProfileQuestions for user " + userId);
		return getQuestions(riskProfileQuestionnaireRepository.getRiskProfileQuestionnaire());
	}
	
	List<Question> getQuestions(RiskProfileQuestionnaire riskProfileQuestionnaire){
		List<Question> questions = new ArrayList<Question>();
		Question timeHorizonQuestion = null;
		Question feedbackQuestion = null;
		for(QuestionGroup questionGroup : riskProfileQuestionnaire.getQuestionGroups()){
			if(questionGroup.getQuestions().size()>2){
				Integer randomNumber1 = new Random().nextInt(questionGroup.getQuestions().size());
				Integer randomNumber2 = new Random().nextInt(questionGroup.getQuestions().size());
				while(randomNumber1==randomNumber2){
					randomNumber2 = new Random().nextInt(questionGroup.getQuestions().size());
				}
				questions.add(questionGroup.getQuestions().get(randomNumber1));
				questions.add(questionGroup.getQuestions().get(randomNumber2));
				
			}else if(questionGroup.getQuestions().size()==2){
				questions.add(questionGroup.getQuestions().get(0));
				questions.add(questionGroup.getQuestions().get(1));
			}else if(questionGroup.getQuestions().size()==1){
				if(questionGroup.getQuestions().get(0).getQuestionId().equals(RiskProfileQuestionnaire.TIME_HORIZON_QUESTION_ID)){
					timeHorizonQuestion = questionGroup.getQuestions().get(0);
				}else if(questionGroup.getQuestions().get(0).getQuestionId().equals(RiskProfileQuestionnaire.FEED_BACK_QUESTION_ID)){
					feedbackQuestion = questionGroup.getQuestions().get(0);
				}
			}
		}
		questions.add(0, timeHorizonQuestion);
		questions.add(feedbackQuestion);
		return questions;
	}
	
	@RequestMapping(value = "/risk/investment/profile", method = RequestMethod.POST, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
	public @ResponseBody RiskInvestmentProfile getRiskToInvestmentProfile(@RequestParam("userId") String userId, @RequestBody List<Answer> answers) {
		logger.info("getting Risk to Investment Profile for user " + userId);
		Integer totalScore = 0;
		Answer feedbackAnswer = null;
		Answer timeHorizonAnswer = null;
		for(Answer answer : answers){
			if(answer.getQuestionId().equals(RiskProfileQuestionnaire.TIME_HORIZON_QUESTION_ID)){
				timeHorizonAnswer = answer;
			}else if(answer.getQuestionId().equals(RiskProfileQuestionnaire.FEED_BACK_QUESTION_ID)){
				feedbackAnswer = answer;
			}else{
				totalScore += riskProfileQuestionnaireRepository.getScore(answer);
			}
		}
		
		String riskType = riskProfileQuestionnaireRepository.getRiskType(totalScore);
		String timeHorizonOptionId = timeHorizonAnswer.getOptionId();
		RiskInvestmentProfile riskInvestmentProfile = riskProfileQuestionnaireRepository.getRiskInvestmentProfiles(timeHorizonOptionId, riskType);
		riskProfileQuestionnaireRepository.saveFeedBack(userId, feedbackAnswer, riskInvestmentProfile);
		logger.info("Retreived RiskInvestmentProfile() for user " + userId + " is " + riskInvestmentProfile);
		return riskInvestmentProfile;
	}
	
	@RequestMapping(value = "/risk/investment/profile", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody RiskInvestmentProfile getRiskToInvestmentProfile(@RequestParam("userId") String userId) {
		return riskProfileQuestionnaireRepository.getRiskInvestmentProfiles(userId);
	}

}