package com.intelliinvest.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.dao.NewsFetcherRepository;
import com.intelliinvest.util.Helper;
import com.intelliinvest.web.bo.response.NewsResponse;


@Controller
public class NewsController {

	private static Logger logger = Logger.getLogger(NewsController.class);

	private static final String APPLICATION_JSON = "application/json";

	@Autowired
	private NewsFetcherRepository newsFetcherRepository;

	@RequestMapping(value = "/news", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody
	NewsResponse getNews(@RequestParam("stockCode") String stockCode) {
		NewsResponse newsResponse = new NewsResponse();
		newsResponse.setStockCode(stockCode);
		if (Helper.isNotNullAndNonEmpty(stockCode)) {
			try {
				newsResponse.setDescriptions(newsFetcherRepository.getNewsFromGoogle(stockCode));
			} catch (Exception e) {
				logger.error("Error fetching news for stock " + stockCode + "from google");
				newsResponse.setSuccess(false);
				newsResponse.setMessage("Error fetching news for stock " + stockCode);
			}
		}else{
			newsResponse.setSuccess(false);
			newsResponse.setMessage("Stock can not be empty");
		}
		return newsResponse;
	}

	@RequestMapping(value = "/news/topstories", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody
	NewsResponse getTopStories() {
		NewsResponse newsResponse = new NewsResponse();
		newsResponse.setStockCode("TOP_STORIES");
		try {
			newsResponse.setDescriptions(newsFetcherRepository.getTopStories());
		} catch (Exception e) {
			logger.error("Error fetching top stories");
			newsResponse.setSuccess(false);
			newsResponse.setMessage("Error fetching top stories");
		}
		return newsResponse;
	}

}
