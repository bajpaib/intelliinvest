package com.intelliinvest.web.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.model.User;
import com.intelliinvest.response.Status;
import com.intelliinvest.service.UserService;

@Controller
public class UserController {

	private static Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@RequestMapping(value = "/user/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status registerUser(@RequestBody User user) {
		userService.registerUser(user);
		return new Status(Status.SUCCESS, "User with userId " + user.getUserId() + " registered successfully. Please check activation mail before login.");
	}

	@RequestMapping(value = "/user/activate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status activateUser(@RequestParam("userId") String userId, @RequestParam("activationCode") String activationCode) {
		userService.activateUser(userId, activationCode);
		return new Status(Status.SUCCESS, "Activated account for userId " + userId + " successfully");
	}
	@RequestMapping(value = "/user/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status login(@RequestParam("userId") String userId, @RequestParam("password") String password) {
		userService.login(userId, password);
		return new Status(Status.SUCCESS, "Login for user " + userId + " successful");
	}

	@RequestMapping(value = "/user/logout", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status logout(@RequestParam("userId") String userId) {
		userService.logout(userId);
		return new Status(Status.SUCCESS, "Logout for user " + userId + " successful");
	}

	@RequestMapping(value = "/user/forgot/password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status forgotPassword(@RequestParam("userId") String userId) {
		userService.forgotPassword(userId);
		return new Status(Status.SUCCESS, "Reset password for user " + userId + " successful. Please chack mail for new password.");
	}

	@RequestMapping(value = "/user/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status updateUser(@RequestParam("userId") String userId,
												@RequestParam(required=false, value="userName") String userName,
												@RequestParam(required=false, value="phone") String phone,
												@RequestParam(required=false, value="sendNotification") String sendNotification,
												@RequestParam(required=false, value="oldPassword") String oldPassword,
												@RequestParam(required=false, value="password") String password
												) {
			userService.updateUser(userId, userName, phone, sendNotification, oldPassword, password);
			return new Status(Status.SUCCESS, "User details updated successfully");
	}

	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody User getUserByUserId(@RequestParam("userId") String userId) {
		return userService.getUser(userId);
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<User> getAllUsers() {
		return userService.getAllUsers();
	}

	@RequestMapping(value = "/user/remove", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status removeUser(@RequestParam("userId") String userId) {
		userService.removeUser(userId);
		return new Status(Status.SUCCESS, "User " + userId + " removed successfully");
	}
}
