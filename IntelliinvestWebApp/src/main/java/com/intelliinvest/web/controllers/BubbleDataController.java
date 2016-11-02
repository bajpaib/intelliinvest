package com.intelliinvest.web.controllers;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.bubbleData.BubbleDataFetcher;
import com.intelliinvest.data.dao.StockSignalsRepository;
import com.intelliinvest.data.model.BubbleData;
import com.intelliinvest.data.model.StockSignals;
import com.intelliinvest.data.signals.StockSignalsGenerator;
import com.intelliinvest.web.bo.response.BubbleDataResponse;

@Controller
public class BubbleDataController {

	private static Logger logger = Logger.getLogger(BubbleDataController.class);

	private static final String APPLICATION_JSON = "application/json";

	@Autowired
	private BubbleDataFetcher bubbleDataFetcher;

	@RequestMapping(value = "/bubbleData/technical", method = RequestMethod.GET, produces = APPLICATION_JSON)
	public @ResponseBody
	List<BubbleDataResponse> getTechnicalBubbleData() {
		return bubbleDataFetcher.getTechnicalBubbleData();
	}
	
}
