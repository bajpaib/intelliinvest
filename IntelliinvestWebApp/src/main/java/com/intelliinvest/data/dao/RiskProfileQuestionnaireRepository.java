package com.intelliinvest.data.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.intelliinvest.data.model.RiskProfileQuestionnaire;
import com.intelliinvest.data.model.RiskProfileQuestionnaire.Option;
import com.intelliinvest.data.model.RiskProfileQuestionnaire.Question;
import com.intelliinvest.data.model.RiskProfileQuestionnaire.QuestionGroup;
import com.intelliinvest.data.model.RiskProfileResult;
import com.intelliinvest.data.model.RiskProfileResult.Answer;
import com.intelliinvest.data.model.RiskProfileResult.RiskInvestmentProfile;
import com.intelliinvest.data.model.RiskProfileResult.RiskInvestmentProfileKey;
import com.intelliinvest.data.model.UserFeedback;
import com.intelliinvest.util.DateUtil;

@ManagedResource(objectName = "bean:name=RiskProfileQuestionnaireRepository", description = "RiskProfileQuestionnaireRepository")
public class RiskProfileQuestionnaireRepository {
	private static Logger logger = Logger.getLogger(RiskProfileQuestionnaireRepository.class);
	private static final String COLLECTION_RISK_PROFILE_QUESTIONNAIRE = "RISK_PROFILE_QUESTIONNAIRE";
	private static final String COLLECTION_RISK_PROFILE_RESULT = "RISK_PROFILE_RESULT";
	private static final String COLLECTION_USER_FEEDBACK = "USER_FEEDBACK";
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private DateUtil dateUtil;

	private RiskProfileQuestionnaire riskProfileQuestionnaire;
	private RiskProfileResult riskProfileResult;

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	@ManagedOperation(description = "initialiseCacheFromDB")
	public void initialiseCacheFromDB() {
		riskProfileQuestionnaire = getRiskProfileQuestionnaireFromDB();
		riskProfileResult = getRiskProfileResultFromDB();
	}

	// public static void main(String[] args) throws Exception{
	// NavigableMap<Integer, String> scoreToRiskMap = new TreeMap<Integer,
	// String>();
	// scoreToRiskMap.put(119, "Low");
	// scoreToRiskMap.put(139, "Mod");
	// scoreToRiskMap.put(200, "High");
	// System.out.println(scoreToRiskMap.ceilingKey(100));
	// System.out.println(scoreToRiskMap.ceilingKey(119));
	// System.out.println(scoreToRiskMap.ceilingKey(120));
	// System.out.println(scoreToRiskMap.ceilingKey(121));
	// System.out.println(scoreToRiskMap.ceilingKey(130));
	//
	//
	// System.out.println(scoreToRiskMap.floorKey(100));
	// System.out.println(scoreToRiskMap.floorKey(119));
	// System.out.println(scoreToRiskMap.floorKey(120));
	// System.out.println(scoreToRiskMap.floorKey(121));
	// System.out.println(scoreToRiskMap.floorKey(130));
	//
	// String content = FileUtils.readFileToString(new
	// File("C:\\Users\\raja\\Desktop\\questionnaire.txt"), "UTF-8");
	// RiskProfileQuestionnaireRepository riskProfileQuestionnaireRepository =
	// new RiskProfileQuestionnaireRepository();
	// riskProfileQuestionnaireRepository.saveQuestionanaire(content);
	// }

	public void saveQuestionanaire(String content) {
		RiskProfileQuestionnaire riskProfileQuestionnaire = new RiskProfileQuestionnaire();
		String[] lines = content.split("\n");
		Integer questionGroupIndex = -1;
		Integer questionIndex = -1;
		QuestionGroup questionGroup = null;
		Question question = null;
		Map<Answer, Integer> scoreMap = new HashMap<Answer, Integer>();
		NavigableMap<Integer, String> scoreToRiskMap = new TreeMap<Integer, String>();
		String[] investmentTypes = null;
		Map<RiskInvestmentProfileKey, RiskInvestmentProfile> riskInvestmentProfiles = new HashMap<RiskInvestmentProfileKey, RiskInvestmentProfile>();
		List<QuestionGroup> questionGroups = new ArrayList<QuestionGroup>();
		int i = 0;
		while (i < lines.length) {
			if (StringUtils.isBlank(lines[i].trim())) {
				// do nothing
			} else if ("--QuestionGroup".equals(lines[i].trim())) {
				questionGroupIndex++;
				questionIndex = 0;
				questionGroup = new QuestionGroup(questionGroupIndex.toString(), lines[i + 1].trim(),
						new ArrayList<Question>());
				questionGroups.add(questionGroup);
				i++;
			} else if ("--Question".equals(lines[i].trim())) {
				String questionId = questionIndex.toString();
				if (questionGroup.getGroupName().equals("TimeHorizon")) {
					questionId = RiskProfileQuestionnaire.TIME_HORIZON_QUESTION_ID;
				} else if (questionGroup.getGroupName().equals("Feedback")) {
					questionId = RiskProfileQuestionnaire.FEED_BACK_QUESTION_ID;
				}
				question = new Question(questionGroupIndex.toString(), questionId, lines[i + 1].trim(),
						new ArrayList<Option>());
				i++;

				String[] scores = new String[0];
				if (!StringUtils.isBlank(lines[i + 6].trim())) {
					scores = lines[i + 6].trim().split("\\|");
				}
				for (Integer j = 0; j < 5; j++) {
					if (!StringUtils.isBlank(lines[i + 1])) {
						question.getOptions().add(new Option(j.toString(), lines[i + 1].trim()));
						if (!questionGroup.getGroupName().equals("Feedback")
								&& !questionGroup.getGroupName().equals("TimeHorizon")) {
							scoreMap.put(new Answer(questionGroupIndex.toString(), questionId, j.toString()),
									Integer.valueOf(scores[j]));
						}
					}
					i++;
				}
				questionGroup.getQuestions().add(question);
				i = i++;
				questionIndex++;
			} else if ("--ScoreToRiskMap".equals(lines[i].trim())) {
				while (!StringUtils.isBlank(lines[i + 1].trim())) {
					String[] values = lines[i + 1].trim().split("\\|");
					scoreToRiskMap.put(Integer.valueOf(values[0]), values[1]);
					i++;
				}
			} else if ("--InvestmentTypes".equals(lines[i].trim())) {
				investmentTypes = lines[i + 1].trim().split("\\|");
				i++;
			} else if ("--RiskToInvestment".equals(lines[i].trim())) {
				while (!StringUtils.isBlank(lines[i + 1].trim())) {
					String[] values = lines[i + 1].trim().split("\\|");
					Map<String, Integer> investmentDetails = new HashMap<String, Integer>();
					for (int k = 0; k < investmentTypes.length; k++) {
						investmentDetails.put(investmentTypes[k], Integer.valueOf(values[3 + k]));
					}
					riskInvestmentProfiles.put(new RiskInvestmentProfileKey(values[0], values[1]),
							new RiskInvestmentProfile(values[2], investmentDetails));
					i++;
				}
			}
			i++;
		}

		riskProfileQuestionnaire.setQuestionnaireId("1");
		riskProfileQuestionnaire.setQuestionGroups(questionGroups);
		mongoTemplate.dropCollection(COLLECTION_RISK_PROFILE_QUESTIONNAIRE);
		mongoTemplate.insert(riskProfileQuestionnaire, COLLECTION_RISK_PROFILE_QUESTIONNAIRE);
		logger.info("Saved RiskProfileQuestionnaire" + getRiskProfileQuestionnaireFromDB());

		RiskProfileResult riskProfileResult = new RiskProfileResult();
		riskProfileResult.setResultId("1");
		riskProfileResult.setScoreMap(scoreMap);
		riskProfileResult.setScoreToRiskMap(scoreToRiskMap);
		riskProfileResult.setRiskInvestmentProfiles(riskInvestmentProfiles);

		mongoTemplate.dropCollection(COLLECTION_RISK_PROFILE_RESULT);
		mongoTemplate.insert(riskProfileResult, COLLECTION_RISK_PROFILE_RESULT);
		logger.info("Saved RiskProfileResult" + getRiskProfileResultFromDB());

		initialiseCacheFromDB();
	}

	public RiskProfileQuestionnaire getRiskProfileQuestionnaireFromDB() {
		logger.info("Inside getRiskProfileQuestionnaireFromDB()...");
		Query query = new Query();
		query.addCriteria(Criteria.where("questionnaireId").is("1"));
		return mongoTemplate.findOne(query, RiskProfileQuestionnaire.class, COLLECTION_RISK_PROFILE_QUESTIONNAIRE);
	}

	public RiskProfileResult getRiskProfileResultFromDB() {
		logger.info("Inside getRiskProfileResultFromDB()...");
		Query query = new Query();
		query.addCriteria(Criteria.where("resultId").is("1"));
		return mongoTemplate.findOne(query, RiskProfileResult.class, COLLECTION_RISK_PROFILE_RESULT);
	}

	public RiskProfileQuestionnaire getRiskProfileQuestionnaire() {
		logger.info("Inside getRiskProfileQuestionnaire()...");
		return riskProfileQuestionnaire;
	}

	public void saveFeedBack(String userId, Answer feedbackAnswer, RiskInvestmentProfile riskInvestmentProfile) {
		logger.info("Inside saveFeedBack() for user " + userId);
		UserFeedback userFeedback = new UserFeedback(userId, feedbackAnswer.getComments(), feedbackAnswer.getOptionId(),
				riskInvestmentProfile, dateUtil.getLocalDateTime());
		mongoTemplate.save(userFeedback, COLLECTION_USER_FEEDBACK);
	}

	public Integer getScore(Answer answer) {
		logger.info("Inside getScore() for  answer " + answer);
		return riskProfileResult.getScoreMap().get(answer);
	}

	public String getRiskType(Integer totalScore) {
		logger.info("Inside getRiskType() for  totalScore " + totalScore);
		return riskProfileResult.getScoreToRiskMap().ceilingEntry(totalScore).getValue();
	}

	public RiskInvestmentProfile getRiskInvestmentProfiles(String timeHorizonOptionId, String riskType) {
		logger.info("Inside getRiskInvestmentProfiles() for  timeHorizonOptionId " + timeHorizonOptionId
				+ " and riskType " + riskType);
		return riskProfileResult.getRiskInvestmentProfiles()
				.get(new RiskInvestmentProfileKey(timeHorizonOptionId, riskType));
	}

	public RiskInvestmentProfile getRiskInvestmentProfiles(String userId) {
		RiskInvestmentProfile riskInvestmentProfile = null;
		logger.info("Inside getRiskInvestmentProfiles() for  userId " + userId);
		Query query = new Query();
		query.addCriteria(Criteria.where("userId").is(userId));
		UserFeedback userFeedback = mongoTemplate.findOne(query, UserFeedback.class);
		if (null != userFeedback) {
			riskInvestmentProfile = userFeedback.getRiskInvestmentProfile();
		}

		if (null == riskInvestmentProfile) {
			riskInvestmentProfile = new RiskInvestmentProfile();
			riskInvestmentProfile.setDescription("No risk investment profile preesent");
		}

		return riskInvestmentProfile;
	}

}