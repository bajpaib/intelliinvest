package com.intelliinvest.data.dao.persistent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.intelliinvest.data.dao.UserRepository;
import com.intelliinvest.data.model.User;

@Component("userRepository")
class UserRepositoryDB implements UserRepository {
	private static Logger LOGGER = LoggerFactory.getLogger(UserRepositoryDB.class);
	private final MongoTemplate mongoTemplate;

	@Autowired
	public UserRepositoryDB(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.persistent.UserRepository#getAllUsers()
	 */
	public List<User> getAllUsers(){
		LOGGER.debug("Inside getAllUsers()");
		return mongoTemplate.findAll(User.class, User.COLLECTION_NAME);
	}
	
	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.persistent.UserRepository#getUser(java.lang.String)
	 */
	public User getUser(String userId){
		LOGGER.debug("Inside getUser()");
		return mongoTemplate.findOne(Query.query(Criteria.where("userId").is(userId)), User.class, User.COLLECTION_NAME);
	}
	
	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.persistent.UserRepository#isUserExists(java.lang.String)
	 */
	public Boolean isUserExists(String userId){
		LOGGER.info("Checking user exists with user id {}", userId);
		return null!=getUser(userId);
	}
	
	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.persistent.UserRepository#insertUser(com.intelliinvest.data.model.User)
	 */
	public void insertUser(User user){
		LOGGER.info("Inserting user with id {}", user.getUserId());
		mongoTemplate.insert(user, User.COLLECTION_NAME);
	}
	
	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.persistent.UserRepository#updateUser(com.intelliinvest.data.model.User)
	 */
	public void updateUser(User user){
		LOGGER.info("Updating user with id {}", user.getUserId());
		mongoTemplate.save(user, User.COLLECTION_NAME);
	}
	
	/* (non-Javadoc)
	 * @see com.intelliinvest.data.dao.persistent.UserRepository#deleteUser(java.lang.String)
	 */
	public void deleteUser(String userId){
		LOGGER.info("Inside removeUser()...");
		User user = mongoTemplate.findAndRemove(Query.query(Criteria.where("userId").is(userId)), User.class, User.COLLECTION_NAME);
	}
	
}
