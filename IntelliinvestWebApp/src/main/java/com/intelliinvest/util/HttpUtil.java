package com.intelliinvest.util;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {
	
	private static Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);
	
	public static String getFromUrlAsString(String urlStr) throws IOException{
		return IOUtils.toString(new URL(urlStr), "UTF-8");
	}
	
}
