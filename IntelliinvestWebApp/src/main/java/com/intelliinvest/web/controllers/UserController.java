package com.intelliinvest.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.model.User;
import com.intelliinvest.web.bo.UserFormParameters;
import com.intelliinvest.web.bo.UserResponse;
import com.intelliinvest.web.common.CommonConstParams;
import com.intelliinvest.web.dao.UserRepository;
import com.intelliinvest.web.util.Converter;
import com.intelliinvest.web.util.Helper;

@Controller
public class UserController {

	private static Logger logger = Logger.getLogger(UserController.class);

	private static final String APPLICATION_JSON = "application/json";

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(value = "/user/register", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserResponse registerUser(@RequestBody UserFormParameters userFormParameters) {

		UserResponse userResponse = new UserResponse();
		String userId = userFormParameters.getUserId();
		String username = userFormParameters.getUsername();
		String password = userFormParameters.getPassword();
		String phone = userFormParameters.getPhone();
		String sendNotification = userFormParameters.getSendNotification();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;

		User user = null;
		boolean error = false;

		try {
			if (Helper.isNotNullAndNonEmpty(username) && Helper.isNotNullAndNonEmpty(password)
					&& Helper.isNotNullAndNonEmpty(userId) && Helper.isNotNullAndNonEmpty(phone)
					&& Helper.isNotNullAndNonEmpty(sendNotification + "")) {
				user = userRepository.register(username, userId, phone, password, Boolean.valueOf(sendNotification));
			}
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside registerUser() " + errorMsg);
			error = true;
		}

		if (user != null && !error) {
			userResponse = Converter.getUserResponse(user);
			userResponse.setSuccess(true);
			userResponse.setMessage("Registration for user " + username + " with userId id " + userId
					+ " is successful. Please activate your account by clicking link in your activation userId.");
		} else {
			userResponse.setUserId(userId);
			userResponse.setSuccess(false);
			userResponse.setMessage(errorMsg);
		}

		return userResponse;
	}

	@RequestMapping(value = "/user/activate", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody UserResponse activateUser(@RequestParam("userId") String userId,
			@RequestParam("activationCode") String activationCode) {

		UserResponse userResponse = new UserResponse();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		User user = null;
		boolean error = false;

		try {
			user = userRepository.activateUser(userId, activationCode);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside activateUser() " + errorMsg);
			error = true;
		}

		if (user != null && !error) {
			userResponse.setUserId(userId);
			userResponse.setSuccess(true);
			userResponse.setMessage("Account has been activated successfully.");
		} else {
			userResponse.setUserId(userId);
			userResponse.setSuccess(false);
			userResponse.setMessage(errorMsg);
		}

		return userResponse;
	}

	@RequestMapping(value = "/user/login", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserResponse login(@RequestBody UserFormParameters userFormParameters) {

		UserResponse userResponse = new UserResponse();
		String userId = userFormParameters.getUserId();
		String password = userFormParameters.getPassword();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		User user = null;
		boolean error = false;

		if (Helper.isNotNullAndNonEmpty(userId) && Helper.isNotNullAndNonEmpty(password)) {
			try {
				user = userRepository.login(userId, password);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside login() " + errorMsg);
				error = true;
			}
		}

		if (user != null && !error) {
			userResponse = Converter.getUserResponse(user);
			userResponse.setSuccess(true);
			userResponse.setMessage("User has successfully logged in.");
		} else {
			userResponse.setUserId(userId);
			userResponse.setSuccess(false);
			userResponse.setMessage(errorMsg);
		}

		return userResponse;
	}

	@RequestMapping(value = "/user/logout", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserResponse logout(@RequestBody UserFormParameters userFormParameters) {

		UserResponse userResponse = new UserResponse();
		String userId = userFormParameters.getUserId();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		User user = null;
		boolean error = false;

		if (Helper.isNotNullAndNonEmpty(userId)) {
			try {
				user = userRepository.logout(userId);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside logout() " + errorMsg);
				error = true;
			}
		}

		if (user != null && !error) {
			userResponse = Converter.getUserResponse(user);
			userResponse.setSuccess(true);
			userResponse.setMessage("User has logged out successfully...");
		} else {
			userResponse.setUserId(userId);
			userResponse.setSuccess(false);
			userResponse.setMessage(errorMsg);
		}

		return userResponse;
	}

	@RequestMapping(value = "/user/forgotPassword", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserResponse forgotPassword(@RequestBody UserFormParameters userFormParameters) {

		UserResponse userResponse = new UserResponse();
		String userId = userFormParameters.getUserId();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		User user = null;
		boolean error = false;

		if (Helper.isNotNullAndNonEmpty(userId)) {
			try {
				user = userRepository.forgotPassword(userId);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside forgotPassword() " + errorMsg);
				error = true;
			}
		}

		if (user != null && !error) {
			userResponse = Converter.getUserResponse(user);
			userResponse.setSuccess(true);
			userResponse.setMessage("New password has been sent to your registered mail id.");
		} else {
			userResponse.setUserId(userId);
			userResponse.setSuccess(false);
			userResponse.setMessage(errorMsg);
		}

		return userResponse;
	}

	@RequestMapping(value = "/user/update", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserResponse updateUser(@RequestBody UserFormParameters userFormParameters) {

		UserResponse userResponse = new UserResponse();
		String userName = userFormParameters.getUsername();
		String userId = userFormParameters.getUserId();
		String oldPassword = userFormParameters.getOldPassword();
		String password = userFormParameters.getPassword();
		String phone = userFormParameters.getPhone();
		String sendNotification = userFormParameters.getSendNotification();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		User user = null;
		boolean error = false;

		try {
			user = userRepository.updateUser(userName, userId, phone, oldPassword, password, sendNotification);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside updateUser() " + errorMsg);
			error = true;
		}

		if (user != null && !error) {
			userResponse = Converter.getUserResponse(user);
			userResponse.setSuccess(true);
			userResponse.setMessage("User details have been updated successfully.");
		} else {
			userResponse.setUserId(userId);
			userResponse.setSuccess(false);
			userResponse.setMessage(errorMsg);
		}

		return userResponse;
	}

	@RequestMapping(value = "/user/getUserByUserId", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserResponse getUserByUserId(@RequestBody UserFormParameters userFormParameters) {

		UserResponse userResponse = new UserResponse();
		String userId = userFormParameters.getUserId();
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		User user = null;
		boolean error = false;

		if (Helper.isNotNullAndNonEmpty(userId)) {
			try {
				user = userRepository.getUserByUserId(userId);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside getUserByUserId() " + errorMsg);
				error = true;
			}

		} else {
			errorMsg = "UserId is null or empty";
			logger.error("Exception inside updateUser() " + errorMsg);
			error = true;
		}

		if (user == null) {
			errorMsg = "User does not exists.";
			logger.error("Exception inside updateUser() " + errorMsg);
			error = true;
		}

		if (user != null && !error) {
			userResponse = Converter.getUserResponse(user);
			userResponse.setSuccess(true);
			userResponse.setMessage("User details have been returned successfully.");
		} else {
			userResponse.setUserId(userId);
			userResponse.setSuccess(false);
			userResponse.setMessage(errorMsg);
		}

		return userResponse;
	}

	@RequestMapping(value = "/user/getUsers", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody List<UserResponse> getAllUsers() {
		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		List<User> userDetails = null;
		boolean error = false;

		try {
			userDetails = userRepository.getAllUsers();
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getAllUsers() " + errorMsg);
			error = true;
		}
		if (userDetails != null && !error) {
			return Converter.convertUsersList(userDetails);
		} else {
			List<UserResponse> list = new ArrayList<>();
			UserResponse userResponse = new UserResponse();
			userResponse.setSuccess(false);
			userResponse.setMessage(errorMsg);
			list.add(userResponse);
			return list;
		}

	}

	@RequestMapping(value = "/user/remove", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserResponse removeUser(@RequestBody UserFormParameters userFormParameters) {

		String errorMsg = CommonConstParams.ERROR_MSG_DEFAULT;
		String userId = userFormParameters.getUserId();
		UserResponse userResponse = new UserResponse();
		User user = null;
		boolean error = false;

		if (Helper.isNotNullAndNonEmpty(userId)) {
			try {
				user = userRepository.removeUser(userId);
			} catch (Exception e) {
				errorMsg = e.getMessage();
				logger.error("Exception inside removeUser() " + errorMsg);
				error = true;
			}
		}

		if (user != null && !error) {
			userResponse = Converter.getUserResponse(user);
			userResponse.setSuccess(true);
			userResponse.setMessage("User has been removed successfully.");
		} else {
			userResponse.setUserId(userId);
			userResponse.setSuccess(false);
			userResponse.setMessage(errorMsg);
		}
		return userResponse;

	}

}
