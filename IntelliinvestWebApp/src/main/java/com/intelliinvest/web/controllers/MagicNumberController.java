package com.intelliinvest.web.controllers;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.dao.MagicNumberRepository;
import com.intelliinvest.data.model.MagicNumberData;
import com.intelliinvest.data.signals.MagicNumberGenerator;

@Controller
public class MagicNumberController {

	private static Logger logger = Logger.getLogger(MagicNumberController.class);

	@Autowired
	MagicNumberRepository magicNumberRepository;
	
	@Autowired
	MagicNumberGenerator magicNumberGenerator;
	
	private static Integer MOVING_AVERAGE = new Integer(IntelliInvestStore.properties.get("ma").toString());;

	@RequestMapping(value = "/magicNumbers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<MagicNumberData> getMagicNumberDatas(@RequestParam("userId") String userId) {
		logger.info("in getMagicNumberDatas method...");
		return magicNumberRepository.getMagicNumbers();
	}

	@RequestMapping(value = "/magicNumber/{securityId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody MagicNumberData getMagicNumberData(@RequestParam("userId") String userId, @PathVariable("securityId") String securityId) {
		logger.info("in getMagicNumberData method for securityId " + securityId);
		return magicNumberRepository.getMagicNumber(securityId);

	}
	
	@RequestMapping(value = "/magicNumber/{securityId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody MagicNumberData generateMagicNumberData(@RequestParam("userId") String userId, @PathVariable("securityId") String securityId) {
		logger.info("in generateMagicNumberData method for securityId " + securityId);
		return magicNumberGenerator.generateMagicNumber(MOVING_AVERAGE, securityId);

	}

	@RequestMapping(value = "/magicNumbers", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<MagicNumberData> generateMagicNumberDatas(@RequestParam("userId") String userId) {
		logger.info("in generateMagicNumberDatas method...");
		return magicNumberGenerator.generateMagicNumbers(MOVING_AVERAGE);
	}

}
