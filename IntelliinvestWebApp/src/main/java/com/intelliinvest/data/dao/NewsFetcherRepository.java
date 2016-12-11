package com.intelliinvest.data.dao;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.intelliinvest.data.model.Stock;
import com.intelliinvest.util.HttpUtil;

@ManagedResource(objectName = "bean:name=NewsFetcherRepository", description = "NewsFetcherRepository")
public class NewsFetcherRepository {

	private static Logger logger = Logger.getLogger(NewsFetcherRepository.class);
	static String GOOGLE_NEWS_URL = "https://www.google.com/finance/company_news?q=#EXCHANGE#:#CODE#&output=rss";
	static String GOOGLE_TOP_STORIES_NEWS_URL = "https://news.google.com/news/feeds?pz=1&cf=all&ned=in&hl=en&output=rss&topic=b";

	@Autowired
	StockRepository stockRepository;

	public String getTopStories() {
		logger.debug("in topStories....");
		try {
			return HttpUtil.getFromHttpUrlAsString(GOOGLE_TOP_STORIES_NEWS_URL);
		} catch (Exception e) {
			logger.info("Error while getting top stories " + e.getMessage());
			throw new RuntimeException("Error fetching top stories");
		}
	}

	public String getNews(String stockCode, Integer count) {
		logger.debug("in getNewsFromGoogle .....");
		try {
			String exchange = "NSE";
			Stock stock = stockRepository.getStockById(stockCode);

			if (stock != null && !stock.isNseStock()) {
				exchange = "BOM";
				stockCode = stock.getBseCode();
			}
			String url = GOOGLE_NEWS_URL.replace("#CODE#", stockCode.replace("&", "%26")).replace("#EXCHANGE#",
					exchange);
			logger.info("URL:"+url);
			return limitNews(HttpUtil.getFromHttpUrlAsString(url), count);
		} catch (Exception e) {
			logger.info("Error retreiving new from google  " + e.getMessage());
			throw new RuntimeException("Error fetching new for stock " + stockCode);
		}
	}
	
	private String limitNews(String response, Integer count) throws Exception{
		logger.info("in limit news response....");
		InputSource inputSource = new InputSource(new StringReader(response));
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputSource);
		if(count>0){
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.compile("//item").evaluate(doc, XPathConstants.NODESET);
			int currentCount = 0;
			for (int i = 0; i < nodeList.getLength(); i++,currentCount++) {
				if(currentCount>=count){
					nodeList.item(i).getParentNode().removeChild(nodeList.item(i));
				}
			}
		}
		return getAsString(doc);
	}

	private String getAsString(Document doc) throws Exception {
	   DOMSource domSource = new DOMSource(doc);
       StringWriter writer = new StringWriter();
       StreamResult result = new StreamResult(writer);
       TransformerFactory tf = TransformerFactory.newInstance();
       Transformer transformer = tf.newTransformer();
       transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
       transformer.transform(domSource, result);
		return writer.toString();
	}
}
