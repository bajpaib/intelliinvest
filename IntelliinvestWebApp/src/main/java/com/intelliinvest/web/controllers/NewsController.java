package com.intelliinvest.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.dao.NewsFetcherRepository;


@Controller
public class NewsController {

	private static final String APPLICATION_XML = "application/xml";

	@Autowired
	private NewsFetcherRepository newsFetcherRepository;

	@RequestMapping(value = "/news", method = RequestMethod.GET, produces = APPLICATION_XML)
	public @ResponseBody String getNews(@RequestParam("stockCode") String stockCode, @RequestParam(required=false, name="count", defaultValue="0") Integer count) {
			return newsFetcherRepository.getNews(stockCode, count);
	}

	@RequestMapping(value = "/news/topstories", method = RequestMethod.GET, produces = APPLICATION_XML)
	public @ResponseBody String getTopStories() {
		return newsFetcherRepository.getTopStories();
	}

}
