package com.intelliinvest.web.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.dao.UserPortfolioRepository;
import com.intelliinvest.data.model.Portfolio;
import com.intelliinvest.data.model.PortfolioItem;
import com.intelliinvest.util.Helper;
import com.intelliinvest.web.bo.PortfolioItemRequest;
import com.intelliinvest.web.bo.UserPortfolioFormParameters;
import com.intelliinvest.web.bo.UserPortfolioResponse;

@Controller
public class UserPortfolioController {

	private static Logger logger = Logger.getLogger(UserPortfolioController.class);
	private static final String APPLICATION_JSON = "application/json";
	@Autowired
	private UserPortfolioRepository userPortfolioRepository;

	@RequestMapping(value = "/portfolio/getPortfolioNames", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserPortfolioResponse getPortfolioNames(
			@RequestBody UserPortfolioFormParameters portfolioFormParameters) {
		UserPortfolioResponse portfolioResponse = new UserPortfolioResponse();
		String userId = portfolioFormParameters.getUserId();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean error = false;
		try {
			if (!Helper.isNotNullAndNonEmpty(userId)) {
				throw new IntelliinvestException("Invalid input. Please check userId.");
			}
			String portfolioNames = userPortfolioRepository.getPortfolioNames(userId);
			portfolioResponse.setPortfolioName(portfolioNames);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getPortfolioNames() " + errorMsg);
			error = true;
		}
		portfolioResponse.setUserId(userId);
		if (!error) {
			portfolioResponse.setSuccess(true);
			portfolioResponse.setMessage("Portfolio Names have been retrieved successfully");
		} else {
			portfolioResponse.setSuccess(false);
			portfolioResponse.setMessage(errorMsg);
		}
		return portfolioResponse;
	}

	@RequestMapping(value = "/portfolio/getPortfolioSummary", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserPortfolioResponse getPortfolioSummary(
			@RequestBody UserPortfolioFormParameters portfolioFormParameters) {

		UserPortfolioResponse portfolioResponse = new UserPortfolioResponse();
		String userId = portfolioFormParameters.getUserId();
		String portfolioName = portfolioFormParameters.getPortfolioName();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean error = false;

		try {
			if (!(Helper.isNotNullAndNonEmpty(userId) && Helper.isNotNullAndNonEmpty(portfolioName))) {
				throw new IntelliinvestException("Invalid input. Please check userId and portfolio name.");
			}
			Portfolio portfolio = userPortfolioRepository.getPortfolio(userId, portfolioName);
			userPortfolioRepository.populatePnlForPortfolioItems(portfolio.getPortfolioItems());
			List<PortfolioItem> portfolioSummary = userPortfolioRepository
					.getPortfolioSummary(portfolio.getPortfolioItems());
			portfolioResponse.setPortfolioSummaryItems(portfolioSummary);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getPortfolioSummary() " + errorMsg);
			error = true;
		}
		portfolioResponse.setUserId(userId);
		portfolioResponse.setPortfolioName(portfolioName);
		if (!error) {
			portfolioResponse.setSuccess(true);
			portfolioResponse.setMessage("Portfolio Summary has been retrieved successfully");
		} else {
			portfolioResponse.setSuccess(false);
			portfolioResponse.setMessage(errorMsg);
		}
		return portfolioResponse;
	}

	@RequestMapping(value = "/portfolio/addPortfolioItemsForCode", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserPortfolioResponse addPortfolioItemsForCode(
			@RequestBody UserPortfolioFormParameters portfolioFormParameters) {

		UserPortfolioResponse portfolioResponse = new UserPortfolioResponse();
		String userId = portfolioFormParameters.getUserId();
		String portfolioName = portfolioFormParameters.getPortfolioName();
		String portfolioItemCode = portfolioFormParameters.getPortfolioItemCode();
		List<PortfolioItemRequest> requests = portfolioFormParameters.getPortfolioItems();

		List<PortfolioItem> portfolioItems = new ArrayList<PortfolioItem>();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		for (PortfolioItemRequest request : requests) {
			PortfolioItem item = new PortfolioItem();
			item.setPortfolioItemId(request.getPortfolioItemId());
			item.setPrice(request.getPrice());
			item.setCode(request.getCode());
			item.setDirection(request.getDirection());
			item.setQuantity(request.getQuantity());
			item.setTradeDate(LocalDate.parse(request.getTradeDate(), dateFormat));
			portfolioItems.add(item);
		}

		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean error = false;
		try {
			if (!(Helper.isNotNullAndNonEmpty(userId) && Helper.isNotNullAndNonEmpty(portfolioName)
					&& Helper.isNotNullAndNonEmpty(portfolioItemCode) && Helper.isNotNullAndNonEmpty(portfolioItems))) {
				throw new IntelliinvestException(
						"Invalid input. Please check userId, portfolio name, portfolio item code and portfolio items.");
			}
			Portfolio portfolio = userPortfolioRepository.addPortfolioItems(userId, portfolioName, portfolioItems);
			List<PortfolioItem> portfolioItemsForCode = userPortfolioRepository
					.getPortfolioItemsForCode(portfolioItemCode, portfolio.getPortfolioItems());
			userPortfolioRepository.populatePnlForPortfolioItems(portfolioItemsForCode);
			List<PortfolioItem> portfolioSummary = userPortfolioRepository.getPortfolioSummary(portfolioItemsForCode);
			portfolioResponse.setPortfolioSummaryItems(portfolioSummary);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside addPortfolioItemsForCode() " + errorMsg);
			error = true;
		}
		portfolioResponse.setUserId(userId);
		portfolioResponse.setPortfolioName(portfolioName);
		if (!error) {
			portfolioResponse.setSuccess(true);
			portfolioResponse.setMessage("Portfolio Items have been added successfully");
		} else {
			portfolioResponse.setSuccess(false);
			portfolioResponse.setMessage(errorMsg);
		}
		return portfolioResponse;
	}

	@RequestMapping(value = "/portfolio/getPortfolioItemsByCode", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserPortfolioResponse getPortfolioItemsByCode(
			@RequestBody UserPortfolioFormParameters portfolioFormParameters) {

		UserPortfolioResponse portfolioResponse = new UserPortfolioResponse();
		String userId = portfolioFormParameters.getUserId();
		String portfolioName = portfolioFormParameters.getPortfolioName();
		String portfolioItemCode = portfolioFormParameters.getPortfolioItemCode();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean error = false;
		try {
			if (!(Helper.isNotNullAndNonEmpty(userId) && Helper.isNotNullAndNonEmpty(portfolioName)
					&& Helper.isNotNullAndNonEmpty(portfolioItemCode))) {
				throw new IntelliinvestException(
						"Invalid input. Please check userId, portfolio name and portfolio item code.");
			}
			Portfolio portfolio = userPortfolioRepository.getPortfolio(userId, portfolioName);
			List<PortfolioItem> portfolioItemsForCode = userPortfolioRepository
					.getPortfolioItemsForCode(portfolioItemCode, portfolio.getPortfolioItems());
			if (!Helper.isNotNullAndNonEmpty(portfolioItemsForCode)) {
				throw new IntelliinvestException("User does not have portfolio items for portfolio " + portfolioName
						+ " and code " + portfolioItemCode);
			}
			userPortfolioRepository.populatePnlForPortfolioItems(portfolioItemsForCode);
			portfolioResponse.setPortfolioItems(portfolioItemsForCode);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside getPortfolioItemsByCode() " + errorMsg);
			error = true;
		}
		portfolioResponse.setUserId(userId);
		portfolioResponse.setPortfolioName(portfolioName);
		if (!error) {
			portfolioResponse.setSuccess(true);
			portfolioResponse.setMessage("Portfolio Items have been retrieved successfully for portfolio "
					+ portfolioName + " and code " + portfolioItemCode);
		} else {
			portfolioResponse.setSuccess(false);
			portfolioResponse.setMessage(errorMsg);
		}
		return portfolioResponse;
	}

	@RequestMapping(value = "/portfolio/updatePortfolioItemsForCode", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserPortfolioResponse updatePortfolioItemsForCode(
			@RequestBody UserPortfolioFormParameters portfolioFormParameters) {
		UserPortfolioResponse portfolioResponse = new UserPortfolioResponse();
		String userId = portfolioFormParameters.getUserId();
		String portfolioName = portfolioFormParameters.getPortfolioName();
		String portfolioItemCode = portfolioFormParameters.getPortfolioItemCode();
		List<PortfolioItemRequest> requests = portfolioFormParameters.getPortfolioItems();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean error = false;
		List<PortfolioItem> portfolioItems = new ArrayList<PortfolioItem>();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		for (PortfolioItemRequest request : requests) {
			PortfolioItem item = new PortfolioItem();
			item.setPortfolioItemId(request.getPortfolioItemId());
			item.setPrice(request.getPrice());
			item.setCode(request.getCode());
			item.setDirection(request.getDirection());
			item.setQuantity(request.getQuantity());
			item.setTradeDate(LocalDate.parse(request.getTradeDate(), dateFormat));
			portfolioItems.add(item);
		}

		try {
			if (!(Helper.isNotNullAndNonEmpty(userId) && Helper.isNotNullAndNonEmpty(portfolioName)
					&& Helper.isNotNullAndNonEmpty(portfolioItemCode) && Helper.isNotNullAndNonEmpty(portfolioItems))) {
				throw new IntelliinvestException(
						"Invalid input. Please check userId, portfolio name, portfolio item code and portfolio items.");
			}
			Portfolio portfolio = userPortfolioRepository.updatePortfolioItems(userId, portfolioName, portfolioItems);
			List<PortfolioItem> portfolioItemsForCode = userPortfolioRepository
					.getPortfolioItemsForCode(portfolioItemCode, portfolio.getPortfolioItems());
			userPortfolioRepository.populatePnlForPortfolioItems(portfolioItemsForCode);
			portfolioResponse.setPortfolioItems(portfolioItemsForCode);
			List<PortfolioItem> portfolioSummary = userPortfolioRepository.getPortfolioSummary(portfolioItemsForCode);
			portfolioResponse.setPortfolioSummaryItems(portfolioSummary);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside updatePortfolioItemsForCode() " + errorMsg);
			error = true;
		}
		portfolioResponse.setUserId(userId);
		portfolioResponse.setPortfolioName(portfolioName);
		if (!error) {
			portfolioResponse.setSuccess(true);
			portfolioResponse.setMessage("Portfolio Items have been updated successfully");
		} else {
			portfolioResponse.setSuccess(false);
			portfolioResponse.setMessage(errorMsg);
		}
		return portfolioResponse;
	}

	@RequestMapping(value = "/portfolio/deletePortfolio", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserPortfolioResponse deletePortfolio(
			@RequestBody UserPortfolioFormParameters portfolioFormParameters) {
		UserPortfolioResponse portfolioResponse = new UserPortfolioResponse();
		String userId = portfolioFormParameters.getUserId();
		String portfolioName = portfolioFormParameters.getPortfolioName();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean error = false;

		try {
			if (!(Helper.isNotNullAndNonEmpty(userId) && Helper.isNotNullAndNonEmpty(portfolioName))) {
				throw new IntelliinvestException("Invalid input. Please check userId and portfolio name.");
			}
			userPortfolioRepository.deletePortfolio(userId, portfolioName);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside deletePortfolio() " + errorMsg);
			error = true;
		}
		portfolioResponse.setUserId(userId);
		portfolioResponse.setPortfolioName(portfolioName);
		if (!error) {
			portfolioResponse.setSuccess(true);
			portfolioResponse.setMessage("Portfolio has been deleted successfully");
		} else {
			portfolioResponse.setSuccess(false);
			portfolioResponse.setMessage(errorMsg);
		}
		return portfolioResponse;
	}

	@RequestMapping(value = "/portfolio/deletePortfolioItemsForCode", method = RequestMethod.POST, produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
	public @ResponseBody UserPortfolioResponse deletePortfolioItemsForCode(
			@RequestBody UserPortfolioFormParameters portfolioFormParameters) {
		UserPortfolioResponse portfolioResponse = new UserPortfolioResponse();
		String userId = portfolioFormParameters.getUserId();
		String portfolioName = portfolioFormParameters.getPortfolioName();
		String portfolioItemCode = portfolioFormParameters.getPortfolioItemCode();
		List<PortfolioItemRequest> requests = portfolioFormParameters.getPortfolioItems();
		String errorMsg = IntelliinvestConstants.ERROR_MSG_DEFAULT;
		boolean error = false;
		List<PortfolioItem> portfolioItems = new ArrayList<PortfolioItem>();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		for (PortfolioItemRequest request : requests) {
			PortfolioItem item = new PortfolioItem();
			item.setPortfolioItemId(request.getPortfolioItemId());
			item.setPrice(request.getPrice());
			item.setCode(request.getCode());
			item.setDirection(request.getDirection());
			item.setQuantity(request.getQuantity());
			if (request.getTradeDate() != null) {
				item.setTradeDate(LocalDate.parse(request.getTradeDate(), dateFormat));
			}
			portfolioItems.add(item);
		}

		try {
			if (!(Helper.isNotNullAndNonEmpty(userId) && Helper.isNotNullAndNonEmpty(portfolioName)
					&& Helper.isNotNullAndNonEmpty(portfolioItemCode) && Helper.isNotNullAndNonEmpty(portfolioItems))) {
				throw new IntelliinvestException(
						"Invalid input. Please check userId, portfolio name, portfolio item code and portfolio items.");
			}
			Portfolio portfolio = userPortfolioRepository.deletePortfolioItems(userId, portfolioName, portfolioItems);
			List<PortfolioItem> portfolioItemsForCode = userPortfolioRepository
					.getPortfolioItemsForCode(portfolioItemCode, portfolio.getPortfolioItems());
			userPortfolioRepository.populatePnlForPortfolioItems(portfolioItemsForCode);
			portfolioResponse.setPortfolioItems(portfolioItemsForCode);
			List<PortfolioItem> portfolioSummary = userPortfolioRepository.getPortfolioSummary(portfolioItemsForCode);
			portfolioResponse.setPortfolioSummaryItems(portfolioSummary);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			logger.error("Exception inside deletePortfolioItemsForCode() " + errorMsg);
			error = true;
		}
		portfolioResponse.setUserId(userId);
		portfolioResponse.setPortfolioName(portfolioName);
		if (!error) {
			portfolioResponse.setSuccess(true);
			portfolioResponse.setMessage("Portfolio Items have been deleted successfully");
		} else {
			portfolioResponse.setSuccess(false);
			portfolioResponse.setMessage(errorMsg);
		}
		return portfolioResponse;
	}
}
