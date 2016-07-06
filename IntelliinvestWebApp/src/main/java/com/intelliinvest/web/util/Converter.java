package com.intelliinvest.web.util;

import java.util.ArrayList;
import java.util.List;

import com.intelliinvest.data.model.User;
import com.intelliinvest.web.bo.UserResponse;

public class Converter {

	public static List<UserResponse> convertUsersList(List<User> userDetails) {
		List<com.intelliinvest.web.bo.UserResponse> userDetailsRes = new ArrayList<>();
		if (userDetails != null) {
			for (User user : userDetails) {
				userDetailsRes.add(getUserResponse(user));
			}
		}
		return userDetailsRes;
	}

	public static UserResponse getUserResponse(User user) {
		UserResponse userResponse = new UserResponse();

		userResponse.setUserId(user.getUserId());
		userResponse.setUsername(user.getUsername());
		userResponse.setPhone(user.getPhone());
		userResponse.setPlan(user.getPlan());
		userResponse.setUserType(user.getUserType());
		userResponse.setActive(user.getActive());
		userResponse.setActivationCode(user.getActivationCode());
		userResponse.setCreateDate(user.getCreateDate());
		userResponse.setUpdateDate(user.getUpdateDate());
		userResponse.setRenewalDate(user.getRenewalDate());
		userResponse.setExpiryDate(user.getExpiryDate());
		userResponse.setLoggedIn(user.getLoggedIn());
		userResponse.setLastLoginDate(user.getLastLoginDate());
		userResponse.setSendNotification(user.getSendNotification());

		return userResponse;
	}

}
