package com.intelliinvest.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.dao.WatchListRepository;
import com.intelliinvest.web.bo.response.StatusResponse;
import com.intelliinvest.web.bo.response.WatchListResponse;

@Controller
public class WatchListController {

	private static Logger logger = Logger.getLogger(WatchListController.class);

	private static final String APPLICATION_JSON = "application/json";

	@Autowired
	WatchListRepository watchListRepository;

	@RequestMapping(value = "/watchList/get", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody WatchListResponse getWatchListData(@RequestParam("userId") String userId) {
		logger.debug("in getWatchListData method...");
		return watchListRepository.getTradingAccountData(userId);
	}

	@RequestMapping(value = "/watchList/add", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody WatchListResponse addWatchListData(@RequestParam("userId") String userId,
			@RequestParam("stockCode") String stockCode) {
		return watchListRepository.addTradingAccountData(userId, stockCode);

	}

	@RequestMapping(value = "/watchList/remove", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody WatchListResponse removeWatchListData(@RequestParam("userId") String userId,
			@RequestParam("stockCode") String stockCode) {
		return watchListRepository.removeTradingAccountData(userId, stockCode);

	}

	@RequestMapping(value = "/watchList/sendDailyMails", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody StatusResponse sendDailyMails() {
		boolean b = watchListRepository.sendDailyTradingAccountUpdateMail();
		if (b)
			return new StatusResponse(StatusResponse.SUCCESS, "Mails has been sent successfully");
		else
			return new StatusResponse(StatusResponse.FAILED, "Mails has not been sent successfully, some internal error there.");
	}

}
