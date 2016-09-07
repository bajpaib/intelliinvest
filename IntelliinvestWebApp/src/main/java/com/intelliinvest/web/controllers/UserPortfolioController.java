package com.intelliinvest.web.controllers;

import java.util.Collection;
import java.util.Map;

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

import com.intelliinvest.data.model.Portfolio;
import com.intelliinvest.data.model.PortfolioItem;
import com.intelliinvest.data.model.UserPortfolio;
import com.intelliinvest.response.Status;
import com.intelliinvest.service.UserPortfolioService;

@Controller
public class UserPortfolioController {

	private static Logger LOGGER = LoggerFactory.getLogger(UserPortfolioController.class);
	
	private final UserPortfolioService userPortfolioService;

	@Autowired
	public UserPortfolioController(UserPortfolioService userPortfolioService) {
		this.userPortfolioService = userPortfolioService;
	}

	@RequestMapping(value = "/userPortfolio", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody UserPortfolio getUserPortfolio(@RequestParam("userId") String userId) {
		return userPortfolioService.getUserPortfolio(userId);
	}
	
	@RequestMapping(value = "/portfolio", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Portfolio getPortfolio(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName) {
		return userPortfolioService.getPortfolio(userId, portfolioName);
	}
	
	@RequestMapping(value = "/portfolioNames", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<String> getPortfolioNames(@RequestParam("userId") String userId) {
		return userPortfolioService.getPortfolioNames(userId);
	}
	
	@RequestMapping(value = "/portfolioItems", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> getPortfolioItems(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName) {
		return userPortfolioService.getPortfolioItems(userId, portfolioName);
	}
	
	@RequestMapping(value = "/userPortfolio", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody UserPortfolio addUserPortfolio(@RequestParam("userId") String userId) {
		return userPortfolioService.addUserPortfolio(userId);
	}
	
	@RequestMapping(value = "/portfolio", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Portfolio addPortfolio(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName) {
		return userPortfolioService.addPortfolio(userId, portfolioName);
	}
	
	@RequestMapping(value = "/portfolioItem", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> addPortfolioItem(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName, @RequestBody PortfolioItem portfolioItem) {
		return userPortfolioService.addPortfolioItem(userId, portfolioName, portfolioItem);
	}
	
	@RequestMapping(value = "/portfolioItems", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> addPortfolioItems(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName, @RequestBody Collection<PortfolioItem> portfolioItems) {
		return userPortfolioService.addPortfolioItems(userId, portfolioName, portfolioItems);
	}
	
	@RequestMapping(value = "/portfolioName", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Portfolio updatePortfolioName(@RequestParam("userId") String userId, @RequestParam("oldPortfolioName") String oldPortfolioName, @RequestParam("newPortfolioName") String newPortfolioName) {
		return userPortfolioService.updatePortfolioName(userId, oldPortfolioName, newPortfolioName);
	}
	
	@RequestMapping(value = "/portfolio", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Portfolio updatePortfolio(@RequestParam("userId") String userId, @RequestBody Portfolio portfolio) {
		return userPortfolioService.updatePortfolio(userId, portfolio);
	}
	
	@RequestMapping(value = "/portfolioItem", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> updatePortfolioItem(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName, @RequestBody PortfolioItem portfolioItem) {
		return userPortfolioService.updatePortfolioItem(userId, portfolioName, portfolioItem);
	}
	
	@RequestMapping(value = "/portfolioItems", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> updatePortfolioItems(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName, @RequestBody Collection<PortfolioItem> portfolioItems) {
		return userPortfolioService.updatePortfolioItems(userId, portfolioName, portfolioItems);
	}
	
	@RequestMapping(value = "/userPortfolio", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status deleteUserPortfolio(@RequestParam("userId") String userId) {
		userPortfolioService.deleteUserPortfolio(userId);
		return Status.STATUS_SUCCESS;
	}
	
	@RequestMapping(value = "/portfolio", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Status deletePortfolio(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName) {
		userPortfolioService.deletePortfolio(userId, portfolioName);
		return Status.STATUS_SUCCESS;
	}
	
	@RequestMapping(value = "/portfolioItemById", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> deletePortfolioItemById(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName, @RequestParam("portfolioItemId") String portfolioItemId) {
		return userPortfolioService.deletePortfolioItem(userId, portfolioName, portfolioItemId);
	}
	
	@RequestMapping(value = "/portfolioItem", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> deletePortfolioItem(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName, @RequestBody PortfolioItem portfolioItem) {
		return userPortfolioService.deletePortfolioItem(userId, portfolioName, portfolioItem);
	}
	
	@RequestMapping(value = "/portfolioItemsByCode", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> deletePortfolioItemByCode(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName, @RequestParam("code") String code) {
		return userPortfolioService.deletePortfolioItemByCode(userId, portfolioName, code);
	}
	
	@RequestMapping(value = "/portfolioItemsInCode", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> deletePortfolioItemsInCode(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName, @RequestParam("code") String code, @RequestBody Collection<PortfolioItem> portfolioItems) {
		return userPortfolioService.deletePortfolioItemsInCode(userId, portfolioName, code, portfolioItems);
	}
	
	@RequestMapping(value = "/userPortfolio/summary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Collection<PortfolioItem>> getUserPortfolioSummary(@RequestParam("userId") String userId) {
		return userPortfolioService.getUserPortfolioSummary(userId);
	}
	
	@RequestMapping(value = "/portfolio/summary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Collection<PortfolioItem> getPortfolioSummary(@RequestParam("userId") String userId, @RequestParam("portfolioName") String portfolioName) {
		return userPortfolioService.getPortfolioSummary(userId, portfolioName);
	}
}
