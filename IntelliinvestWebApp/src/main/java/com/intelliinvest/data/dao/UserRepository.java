package com.intelliinvest.data.dao;

import java.util.List;

import com.intelliinvest.data.model.User;

public interface UserRepository {

	List<User> getAllUsers();

	User getUser(String userId);

	Boolean isUserExists(String userId);

	void insertUser(User user);

	void updateUser(User user);

	void deleteUser(String userId);

}