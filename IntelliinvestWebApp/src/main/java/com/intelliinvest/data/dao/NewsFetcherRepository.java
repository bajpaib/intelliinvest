package com.intelliinvest.data.dao;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.intelliinvest.common.IntelliinvestException;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.util.HttpUtil;

@ManagedResource(objectName = "bean:name=NewsFetcherRepository", description = "NewsFetcherRepository")
public class NewsFetcherRepository {

	private static Logger logger = Logger.getLogger(NewsFetcherRepository.class);

	// static String YAHOO_NEWS_URL =
	// "http://query.yahooapis.com/v1/public/yql?q=select%20link%2Ctitle%2Cdescription%2CpubDate%20from%20rss%20where%20url%3D%22http%3A%2F%2Ffeeds.finance.yahoo.com%2Frss%2F2.0%2Fheadline%3Fs%3D#CODE#%26region%3DUS%26lang%3Den-US%22&diagnostics=true";
	// static String YAHOO_TOP_STORIES_NEWS_URL =
	// "http://query.yahooapis.com/v1/public/yql?q=select%20link%2Ctitle%2Cdescription%2CpubDate%20from%20rss%20where%20url%3D%22http%3A%2F%2Frss.news.yahoo.com%2Frss%2Ftopstories%22&diagnostics=true";

	static String GOOGLE_NEWS_URL = "https://www.google.com/finance/company_news?q=#EXCHANGE#:#CODE#&output=rss";
	static String GOOGLE_TOP_STORIES_NEWS_URL = "https://news.google.com/news/feeds?pz=1&cf=all&ned=in&hl=en&output=rss&topic=b";

	// public static String NEWS_FROM_GOOGLE = "GOOGLE";
	// public static String NEWS_FROM_YAHOO = "YAHOO";

	DocumentBuilder dBuilder = null;

	@Autowired
	StockRepository stockRepository;

	public NewsFetcherRepository() throws IntelliinvestException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IntelliinvestException("Error creating dom builder for nesw");
		}
	}

	public List<String> getTopStories() {
		logger.debug("in topStories....");
		List<String> descriptions = new ArrayList<String>();
		try {
			String topStories = HttpUtil.getFromHttpUrlAsString(GOOGLE_TOP_STORIES_NEWS_URL);
			topStories = getCorrectedResponse(topStories);
			descriptions = getDescriptions(topStories);
			return descriptions;
		} catch (Exception e) {
			logger.info("Error while getting top stories " + e.getMessage());
			throw new RuntimeException("Error fetching top stories");
		}
	}

	public List<String> getNewsFromGoogle(String stockCode) throws CharacterCodingException {
		logger.debug("in getNewsFromGoogle .....");
		List<String> descriptions = new ArrayList<String>();
		try {
			String exchange = "NSE";
			Stock stock = stockRepository.getStockById(stockCode);

			if (stock != null && !stock.isNseStock()) {
				exchange = "BOM";
				stockCode = stock.getBseCode();
			}
			String url = GOOGLE_NEWS_URL.replace("#CODE#", stockCode.replace("&", "%26")).replace("#EXCHANGE#",
					exchange);
			String response = HttpUtil.getFromHttpUrlAsString(url);
			response = getCorrectedResponse(response);
			descriptions = getDescriptions(response);
		} catch (Exception e) {
			logger.info("Error retreiving new from google  " + e.getMessage());
			throw new RuntimeException("Error fetching new for stock " + stockCode);
		}
		return descriptions;
	}

	private String getCorrectedResponse(String response) throws CharacterCodingException {
		if (response != null) {
			CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
			decoder.onMalformedInput(CodingErrorAction.REPLACE);
			decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
			ByteBuffer bb = ByteBuffer.wrap(response.getBytes());
			CharBuffer correctedResponse = decoder.decode(bb);
			logger.info("response received");
			return correctedResponse.toString();
		}
		return null;
	}

	private List<String> getDescriptions(String response) throws Exception {
		InputSource inputSource = new InputSource(new StringReader(response));
		Document doc = dBuilder.parse(inputSource);
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile("//item/description").evaluate(doc, XPathConstants.NODESET);
		List<String> descriptions = new ArrayList<String>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			descriptions.add(nodeList.item(i).getTextContent());
		}
		return descriptions;
	}
}
