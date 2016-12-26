package com.intelliinvest.data.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.model.User;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.EncryptUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.MailUtil;

public class UserRepository {
	private static Logger logger = Logger.getLogger(UserRepository.class);
	private static final String COLLECTION_USER = "USER";
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private MailUtil mailUtil;
	@Autowired
	private DateUtil dateUtil;

	private Map<String, Boolean> userLoginCache = new ConcurrentHashMap<String, Boolean>();

	@PostConstruct
	public void init() {
		initialiseCacheFromDB();
	}

	public void initialiseCacheFromDB() {
		List<User> users = getAllUsers();
		if (Helper.isNotNullAndNonEmpty(users)) {
			for (User user : users) {
				userLoginCache.put(user.getUserId(), user.getLoggedIn());
			}
			logger.info("Initialised userLoginCache from DB in UserRepository with size " + userLoginCache.size());
		} else {
			logger.error("Could not initialise userLoginCache from DB in UserRepository. USER is empty.");
		}
	}

	public User registerUser(String userName, String userId, String phone, String password, boolean sendNotification,
			String deviceId, Boolean pushNotification, Boolean showZeroPortf, Boolean reTakeQuestionaire)
			throws Exception {
		logger.info("Inside register()...");
		User user = getUserByUserId(userId);
		if (user != null) {
			throw new IntelliinvestException("User " + userId + " already exists");
		}
		user = new User();
		user.setPassword(EncryptUtil.encrypt(password));
		user.setPhone(phone);
		user.setUsername(userName);
		user.setSendNotification(sendNotification);

		Long time = System.currentTimeMillis();
		String randomText = "ACT" + time.toString().substring(time.toString().length() - 5);

		LocalDate currentDate = dateUtil.getLocalDate();
		LocalDate expiryDate = dateUtil.addBusinessDays(currentDate,
				new Integer(IntelliInvestStore.properties.get("trail.period").toString()));

		LocalDateTime currentDateTime = dateUtil.getLocalDateTime();
		user.setUserId(userId);
		user.setPlan("DEFAULT_10");
		user.setUserType("User");
		user.setActive("N");
		user.setActivationCode(randomText);
		user.setRenewalDate(currentDate);
		user.setExpiryDate(expiryDate);
		user.setCreateDate(currentDateTime);
		user.setUpdateDate(currentDateTime);
		user.setLoggedIn(false);
		user.setDeviceId(deviceId);
		user.setPushNotification(pushNotification);
		user.setShowZeroPortfolio(showZeroPortf);
		user.setReTakeQuestionaire(reTakeQuestionaire);
		mongoTemplate.insert(user, COLLECTION_USER);
		logger.info("Registration for user " + userName + " with mail id " + userId + " successful");
		logger.info("Sending activation mail for user " + userId);
		boolean mail_send_success = mailUtil.sendMail(new String[] { userId }, "Activation of IntelliInvest Account",
				"Hi " + userName + ",<br>To activate your account please click below link<br>http://"
						+ IntelliInvestStore.properties.getProperty("context.url") + "/user/activate?userId=" + userId
						+ "&activationCode=" + randomText + "<br>Regards,<br>IntelliInvest Team.");
		if (!mail_send_success) {
			throw new IntelliinvestException("Exception occured while sending mail to User " + userId);
		}
		logger.info(" Activation mail sent successfully to " + userId);
		return user;
	}

	public User activateUser(String userId, String activationCode) throws Exception {
		logger.info("Inside activateUser()...");
		if (!Helper.isNotNullAndNonEmpty(userId) || !Helper.isNotNullAndNonEmpty(activationCode)) {
			throw new IntelliinvestException("Invalid UserId or Activation Code.");
		}
		User user = getUserByUserId(userId);
		if (user == null) {
			throw new IntelliinvestException("User " + userId + " does not exists");
		}
		if ((user.getActivationCode() == null)
				|| (user.getActivationCode() != null && !activationCode.equals(user.getActivationCode()))) {
			throw new IntelliinvestException("Incorrect activation code");
		}
		Query query = new Query();
		query.addCriteria(Criteria.where("userId").is(userId).and("activationCode").is(activationCode));
		Update update = new Update();
		update.set("active", "Y");
		update.set("updateDate", dateUtil.getLocalDateTime());
		return mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), User.class,
				COLLECTION_USER);
	}

	public User login(String userId, String password) throws Exception {
		logger.debug("Inside login()...");
		User retVal = null;
		boolean user_not_active = false;
		boolean user_not_exists = false;
		boolean user_logged_in = false;
		try {
			User user = mongoTemplate.findOne(
					Query.query(Criteria.where("userId").is(userId).and("password").is(EncryptUtil.encrypt(password))),
					User.class, COLLECTION_USER);

			if (user != null) {
				if (user.getActive().equals("Y")) {

					if (user.getLoggedIn()) {
						user_logged_in = true;
						throw new IntelliinvestException("User " + userId + " is already logged in");
					}
					Query query = new Query();
					query.addCriteria(Criteria.where("userId").is(userId));
					LocalDateTime currentDateTime = dateUtil.getLocalDateTime();
					Update update = new Update();
					update.set("loggedIn", true);
					update.set("updateDate", currentDateTime);
					update.set("lastLoginDate", currentDateTime);
					retVal = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true),
							User.class, COLLECTION_USER);
					// update cache
					userLoginCache.put(user.getUserId(), true);
					return retVal;
				} else {
					logger.error("Login for user " + userId + " failed. User is not active.");
					user_not_active = true;
					throw new IntelliinvestException("Login for user " + userId
							+ " failed. Please activate account before logging in. Check activation mail for more details.");
				}
			} else {
				logger.error("Login for user " + userId + " failed. User does not exists.");
				user_not_exists = true;
				throw new IntelliinvestException(
						"Login for user " + userId + " failed. Please check user name and/or password entered");
			}
		} catch (Exception e) {
			if (user_not_active || user_not_exists || user_logged_in)
				throw e;
			else {
				throw new IntelliinvestException("Login for user " + userId
						+ " failed due to system failure. It has been notified to Admin. Please try after some time. Exception: "
						+ e.getMessage());
			}
		}
	}

	public User logout(String userId) throws Exception {
		logger.debug("Inside logout()...");
		User user = getUserByUserId(userId);
		if (user == null) {
			throw new IntelliinvestException("User " + userId + " does not exists");
		}

		if (!user.getLoggedIn()) {
			throw new IntelliinvestException("User " + userId + " is not logged in");
		}
		Query query = new Query();
		query.addCriteria(Criteria.where("userId").is(userId));
		Update update = new Update();
		update.set("loggedIn", false);
		update.set("updateDate", dateUtil.getLocalDateTime());

		user = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), User.class,
				COLLECTION_USER);
		// update cache
		userLoginCache.put(user.getUserId(), false);
		return user;
	}

	public User forgotPassword(String userId) throws Exception {
		logger.debug("Inside forgotPassword()...");
		boolean password_send_failure = false;
		try {
			User user = getUserByUserId(userId);
			if (user == null) {
				logger.error("Password reset mail not sent to user with userId id " + userId
						+ " because Username does not exists");
				password_send_failure = true;
				throw new IntelliinvestException("User " + userId + " does not exists");
			}
			Long time = System.currentTimeMillis();
			String randomText = "INI" + time.toString().substring(time.toString().length() - 5);
			String encryptedNewPassword = EncryptUtil.encrypt(randomText);
			Query query = new Query();
			query.addCriteria(Criteria.where("userId").is(userId));
			Update update = new Update();
			update.set("password", encryptedNewPassword);
			update.set("loggedIn", false);
			update.set("updateDate", dateUtil.getLocalDateTime());
			user = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), User.class,
					COLLECTION_USER);
			// update cache
			userLoginCache.put(user.getUserId(), false);
			if (mailUtil.sendMail(new String[] { userId }, "Password reset from IntelliInvest",
					"Hi,\n Your password has been reset to " + randomText)) {
				logger.info("Mail sent to user with mail id " + userId + "");
			} else {
				logger.error("Error sending mail to user with userId id " + userId + "");
				password_send_failure = true;
				throw new IntelliinvestException(
						"Problem sending activation userId to your account. Please contact admin for further support");
			}
			return user;
		} catch (Exception e) {
			if (password_send_failure)
				throw e;
			throw new IntelliinvestException("Error while password reset " + e.getMessage());
		}
	}

	public User updateUser(String userName, String userId, String phone, String oldPassword, String newPassword,
			String sendNotification, String pushNotification, String showZeroPortfolio, String reTakeQuestionarie)
			throws Exception {
		logger.debug("Inside updateUser()...");
		User user = getUserByUserId(userId);
		if (user == null) {
			throw new IntelliinvestException("User " + userId + " does not exists");
		}
		if (!user.getLoggedIn()) {
			throw new IntelliinvestException("User " + userId + " is not logged in");
		}
		Query query = new Query();
		Update update = new Update();

		if (Helper.isNotNullAndNonEmpty(sendNotification)) {
			update.set("sendNotification", Boolean.parseBoolean(sendNotification));
		}
		if (Helper.isNotNullAndNonEmpty(pushNotification)) {
			update.set("pushNotification", Boolean.parseBoolean(pushNotification));
		}
		if (Helper.isNotNullAndNonEmpty(showZeroPortfolio)) {
			update.set("showZeroPortfolio", Boolean.parseBoolean(showZeroPortfolio));
		}
		if (Helper.isNotNullAndNonEmpty(reTakeQuestionarie)) {
			update.set("reTakeQuestionaire", Boolean.parseBoolean(reTakeQuestionarie));
		}
		if (Helper.isNotNullAndNonEmpty(phone)) {
			update.set("phone", phone);
		}
		update.set("updateDate", dateUtil.getLocalDateTime());
		if (Helper.isNotNullAndNonEmpty(oldPassword) && Helper.isNotNullAndNonEmpty(newPassword)) {
			if (user.getPassword() == null
					|| (user.getPassword() != null && !oldPassword.equals(EncryptUtil.decrypt(user.getPassword())))) {
				throw new IntelliinvestException("Incorrect old password.");
			}

			update.set("password", EncryptUtil.encrypt(newPassword));
			query.addCriteria(Criteria.where("userId").is(userId).and("password").is(EncryptUtil.encrypt(oldPassword)));

		} else {
			query.addCriteria(Criteria.where("userId").is(userId));
		}
		return mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), User.class,
				COLLECTION_USER);
	}

	public List<User> getAllUsers() throws DataAccessException {
		logger.debug("Inside getAllUsers()...");
		return mongoTemplate.findAll(User.class, COLLECTION_USER);
	}

	public User getUserByUserId(String userId) throws DataAccessException {
		logger.debug("Inside getUserByUserId()...");
		return mongoTemplate.findOne(Query.query(Criteria.where("userId").is(userId)), User.class, COLLECTION_USER);
	}

	public User removeUser(String userId) throws DataAccessException {
		logger.info("Inside removeUser()...");
		// update cache
		userLoginCache.remove(userId);
		return mongoTemplate.findAndRemove(Query.query(Criteria.where("userId").is(userId)), User.class,
				COLLECTION_USER);
	}

	public User updateDeviceID(String userId, String deviceId) throws Exception {
		logger.debug("Inside updateDeviceID()...");
		try {
			User user = getUserByUserId(userId);
			if (user == null) {
				logger.error(userId + " Username does not exists");
				throw new IntelliinvestException("User " + userId + " does not exists");
			}
			if (!user.getLoggedIn()) {
				throw new IntelliinvestException("User " + userId + " is not logged in");
			}
			Query query = new Query();
			query.addCriteria(Criteria.where("userId").is(userId));
			Update update = new Update();
			update.set("deviceId", deviceId);
			user = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), User.class,
					COLLECTION_USER);
			return user;
		} catch (Exception e) {

			throw new IntelliinvestException("Error while update device id... " + e.getMessage());
		}
	}

	public User updateReTakeQuestionaire(String userId, String reTakeQuestionaire) throws Exception {
		logger.debug("Inside updateReTakeQuestionaire()...");
		try {
			User user = getUserByUserId(userId);
			if (user == null) {
				logger.error(userId + " Username does not exists");
				throw new IntelliinvestException("User " + userId + " does not exists");
			}
			if (!user.getLoggedIn()) {
				throw new IntelliinvestException("User " + userId + " is not logged in");
			}
			Query query = new Query();
			query.addCriteria(Criteria.where("userId").is(userId));
			Update update = new Update();
			logger.info("userid:"+userId);
			logger.info("retake questionaire: "+reTakeQuestionaire);
			update.set("reTakeQuestionaire", Boolean.parseBoolean(reTakeQuestionaire));
			user = mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), User.class,
					COLLECTION_USER);
			return user;

		} catch (Exception e) {
			throw new IntelliinvestException("Error while update retakequestionaire parameter... " + e.getMessage());
		}
	}
}