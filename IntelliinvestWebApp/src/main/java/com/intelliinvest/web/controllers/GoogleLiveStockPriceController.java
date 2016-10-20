package com.intelliinvest.web.controllers;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.data.importer.GoogleLiveStockPriceImporter;
import com.intelliinvest.data.model.PriceVolumeData;
import com.intelliinvest.web.bo.response.IntradayPriceVolumeDataResponse;

@Controller
public class GoogleLiveStockPriceController {
	private static Logger logger = Logger.getLogger(GoogleLiveStockPriceController.class);
	@Autowired
	private GoogleLiveStockPriceImporter googleLiveStockPriceImporter;

	@RequestMapping(value = "/google/backLoadLivePrices", method = RequestMethod.GET)
	public @ResponseBody String backLoadLivePrices() {
		googleLiveStockPriceImporter.updateCurrentPrices();
		return "Success";
	}
	
	@RequestMapping(value = "/google/intraday/volumeprice", method = RequestMethod.GET)
	public @ResponseBody IntradayPriceVolumeDataResponse backLoadLivePrices(@RequestParam("exchange") String exchange, @RequestParam("securityId") String securityId) {
		IntradayPriceVolumeDataResponse intradayPriceVolumeDataResponse = new IntradayPriceVolumeDataResponse(exchange, securityId);
		List<PriceVolumeData> priceVolumeDatas = googleLiveStockPriceImporter.getIntraDayPriceVolumeData(exchange, securityId);
		intradayPriceVolumeDataResponse.setPriceVolumeDatas(priceVolumeDatas);
		if(priceVolumeDatas.isEmpty()){
			intradayPriceVolumeDataResponse.setSuccess(false);
			intradayPriceVolumeDataResponse.setMessage("Error fetching Intra Day Price Volume Data");
		}
		return intradayPriceVolumeDataResponse;
	}

}