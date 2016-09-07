package com.intelliinvest.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelliinvest.common.exception.IntelliInvestException;
import com.intelliinvest.common.exception.IntelliInvestPropertyHoler;
import com.intelliinvest.data.dao.UserRepository;
import com.intelliinvest.data.model.User;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.EncryptUtil;
import com.intelliinvest.util.Helper;
import com.intelliinvest.util.MailUtil;

@Component
public class UserService {
	private static Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	private final UserRepository userRepository;
	private final MailUtil mailUtil;

	@Autowired
	public UserService(UserRepository userRepository, MailUtil mailUtil) {
		this.userRepository = userRepository;
		this.mailUtil = mailUtil;
	}
					
	public List<User> getAllUsers() {
		return userRepository.getAllUsers();
	}
	
	public void registerUser(User user){
		LOGGER.info("Inside register()...");
		if (userRepository.isUserExists(user.getUserId())) {
			throw new IntelliInvestException("User " + user.getUserId() + " already exists");
		}
		LocalDateTime currentDateTime = DateUtil.getLocalDateTime();
		user.setRenewalDate(currentDateTime.toLocalDate());
		user.setCreateDate(currentDateTime.toLocalDate());
		user.setUpdateDate(currentDateTime);
		
		user.setPassword(EncryptUtil.encrypt(user.getPassword()));
		user.setPlan("DEFAULT_10");
		user.setUserType("User");
		user.setActive("N");
		
		String activationCode = generateActivationCode();
		user.setActivationCode(activationCode);
		LocalDate expiryDate = currentDateTime.toLocalDate().plusDays(new Long(IntelliInvestPropertyHoler.getProperty("trail.period")));

		user.setExpiryDate(expiryDate);
		user.setLoggedIn(false);

		userRepository.insertUser(user);
		
		LOGGER.info("Registration for user " + user.getUsername() + " with mail id " + user.getUserId() + " successful");
		sendActivationMail(user);
		
	}

	private String generateActivationCode() {
		Long time = System.currentTimeMillis();
		String randomText = "ACT" + time.toString().substring(time.toString().length() - 5);
		return randomText;
	}
	
	public void sendActivationMail(User user){
		LOGGER.info("Sending activation mail for user " + user.getUserId());
		boolean mail_send_success = mailUtil.sendMail(new String[] { user.getUserId() },
				"Activation of IntelliInvest Account",
				"Hi " + user.getUsername() + ",<br>To activate your account please click below link<br>http://"
						+ IntelliInvestPropertyHoler.getProperty("context.url") + "/user/activate?userId=" + user.getUserId()
						+ "&activationCode=" + user.getActivationCode() + "<br>Regards,<br>IntelliInvest Team.");
		if (!mail_send_success) {
			throw new IntelliInvestException("Exception occured while sending mail to User " + user.getUserId());
		}
		LOGGER.info(" Activation mail sent successfully to " + user.getUserId());
	}

	public void activateUser(String userId, String activationCode){
		User user = userRepository.getUser(userId);
		if ((user.getActivationCode() == null)
				|| (user.getActivationCode() != null && !activationCode.equals(user.getActivationCode()))) {
			throw new IntelliInvestException("Incorrect activation code");
		}
		user.setActive("Y");
		userRepository.updateUser(user);
	}

	public User getUser(String userId) {
		return userRepository.getUser(userId);
	}
	
	public void login(String userId, String password){
		User user = userRepository.getUser(userId);
		if(null==user || !EncryptUtil.encrypt(password).equals(user.getPassword())){
			LOGGER.error("Login for user " + userId + " failed");
			throw new IntelliInvestException("Login for user " + userId + " failed."
					+ " Please check user name and/or password entered");
		}else if (!"Y".equals(user.getActive())) {
			LOGGER.error("Login for user " + userId + " failed. User is not active.");
			throw new IntelliInvestException("Login for user " + userId+ " failed."
					+ " Please activate account before logging in. Check activation mail for more details.");
		}else if(user.getLoggedIn()) {
			LOGGER.error("User " + userId + " is already logged in");
			throw new IntelliInvestException("User " + userId + " is already logged in");
		}
		LOGGER.error("User " + userId + " is successfully logged in");
		user.setLoggedIn(true);
		LocalDateTime lastLoginTime = user.getLastLoginDate();
		user.setLastLoginDate(DateUtil.getLocalDateTime());
		userRepository.updateUser(user);
		user.setLastLoginDate(lastLoginTime);
	}

	public void logout(String userId){
		LOGGER.debug("Inside logout()...");
		User user = userRepository.getUser(userId);
		user.setLoggedIn(false);
		userRepository.updateUser(user);
	}

	public void forgotPassword(String userId){
		LOGGER.debug("Inside forgotPassword()...");
		User user = userRepository.getUser(userId);
		if (user == null) {
			LOGGER.error("User " + userId + " does not exists");
			throw new IntelliInvestException("User " + userId + " does not exists");
		}
		String password = generatePassword();
		String encryptedNewPassword = EncryptUtil.encrypt(password);
		user.setPassword(encryptedNewPassword);
		user.setLoggedIn(false);
		user.setUpdateDate(DateUtil.getLocalDateTime());
		userRepository.updateUser(user);
		LOGGER.info("Reset password successful for user {}", userId);
		sendResetPasswordMail(user);
	}
	
	public void sendResetPasswordMail(User user){
		if (mailUtil.sendMail(new String[] { user.getUserId() },
				"Password reset from IntelliInvest", "Hi,\n Your password has been reset to " + EncryptUtil.decrypt(user.getPassword()))) {
			LOGGER.info("Mail sent to user for forgot password with mail id " + user.getUserId() + "");
		} else {
			LOGGER.error("Error sending mail to user for forgot password with userId id " + user.getUserId() + "");
			throw new IntelliInvestException(
					"Problem sending Reset password mail. Please contact admin for further support");
		}
		
	}

	private String generatePassword() {
		Long time = System.currentTimeMillis();
		String randomText = "INI" + time.toString().substring(time.toString().length() - 5);
		return randomText;
	}

	
	public void updateUser(String userId, String userName, String phone, String sendNotification, String oldPassword, String newPassword){
		LOGGER.debug("Inside updateUser()...");
		User user = userRepository.getUser(userId);
		if (!user.getLoggedIn()) {
			throw new IntelliInvestException("User " + userId + " is not logged in");
		}
		if (Helper.isNotNullAndNonEmpty(sendNotification)) {
			user.setSendNotification(Boolean.parseBoolean(sendNotification));
		}
		if (Helper.isNotNullAndNonEmpty(phone)) {
			user.setPhone(phone);
		}
		user.setUpdateDate(DateUtil.getLocalDateTime());
		if (Helper.isNotNullAndNonEmpty(oldPassword) && Helper.isNotNullAndNonEmpty(newPassword)) {
			if (user.getPassword() == null
					|| (user.getPassword() != null && !oldPassword.equals(EncryptUtil.decrypt(user.getPassword())))) {
				throw new IntelliInvestException("Incorrect old password.");
			}
			user.setPassword(EncryptUtil.encrypt(newPassword));
		} 
		userRepository.updateUser(user);
	}

	public void removeUser(String userId) {
		userRepository.deleteUser(userId);
	}

}
