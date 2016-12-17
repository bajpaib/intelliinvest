package com.intelliinvest.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.log4j.Logger;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.common.SMSObject;
import com.sun.mail.util.MailSSLSocketFactory;

public class SMSUtil {

	static String USERNAME = "kapbulk";
	static String PWD = "kapbulk@user!123";
	static String SMS_HOST = "http://123.63.33.43/blank/sms/user/balance_check.php";
	static String REPORT_HOST = "http://www.smsjust.com/sms/user/response.php";
	static String SENDER_ID = "KAPMSG";
	static String PENDING_STATUS = "PENDING";
	static String TOKEN_SPLITTER = "<BR>";
	static String MESSAGE_TYPE = "TXT";

	private static Logger logger = Logger.getLogger(SMSUtil.class);

	@PostConstruct
	public void init() {
		USERNAME = IntelliInvestStore.properties.getProperty("sms.username");
		PWD = IntelliInvestStore.properties.getProperty("sms.password");
		SENDER_ID = IntelliInvestStore.properties.getProperty("sms.senderId");
		SMS_HOST = IntelliInvestStore.properties.getProperty("sms.host");
		REPORT_HOST = IntelliInvestStore.properties.getProperty("sms.report.host");
		PENDING_STATUS = IntelliInvestStore.properties.getProperty("sms.report.delivery.status.pending");
		TOKEN_SPLITTER = IntelliInvestStore.properties.getProperty("sms.report.token.splitter");
		MESSAGE_TYPE = IntelliInvestStore.properties.getProperty("sms.messagetype");
	}

	public static void main(String[] args) throws Exception {
		String postData = "";
		String retval = "";
		// give all Parameters In String
		String username = "kapbulk";
		String password = "kapbulk@user!123";
		String mobileNo = "9716942945";
		String message = "Test message from java code";
		String senderID = "KAPMSG";
		String delivery_report_host = "http://www.smsjust.com/sms/user/response.php";
		String host = "http://123.63.33.43/sms/user/XMLAPI/send.php";
		postData += "username=" + username + "&password=" + password + "&to=" + mobileNo + "&sender=" + senderID
				+ "&message=" + message;
		// // URL url = new URL("http://instant.kapsystem.com/web2sms.php?");

		URL url = new URL("http://123.63.33.43/blank/sms/user/urlsmstemp.php?username=" + username + "&pass=" + password
				+ "&senderid=" + senderID + "&dest_mobileno=" + mobileNo + "&message=" + message + "&response=Y");

		// URL url = new URL("?username=" + username + "&pass=" + password);

		// HttpURLConnection urlconnection = (HttpURLConnection)
		// url.openConnection();
		// urlconnection.setRequestMethod("POST");
		// urlconnection.setRequestProperty("Content-Type",
		// "application/x-www-form-urlencoded");
		// urlconnection.setDoOutput(true);
		// OutputStreamWriter out = new
		// OutputStreamWriter(urlconnection.getOutputStream());
		// out.write(postData);
		// out.close();
		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(urlconnection.getInputStream()));
		// String decodedString;
		// while ((decodedString = in.readLine()) != null) {
		// retval += decodedString;
		// }
		// in.close();
		// System.out.println(retval);

		List<SMSObject> smsObjects = new ArrayList<>();
		smsObjects.add(new SMSObject("9716942945", "Hi YG", null));
		smsObjects.add(new SMSObject("9654724009", "Hi MA", null));

		List<String> mob_nos = new ArrayList<>();
		mob_nos.add("7503273656");
		mob_nos.add("123456789");
		mob_nos.add("987654321");

		System.out.println(getXmlNode(username, password, senderID, "TXT", smsObjects));
		System.out.println("=========================================================");
		System.out.println(getXmlNode(username, password, senderID,"TXT", mob_nos, "Hi, common msg to multiple nos...."));
		System.out.println("=========================================================");
		System.out.println(getXmlNode(username, password, senderID,"TXT", "9716942945", "Hi, single message to single no...."));
		
	}

	// public String sendSMS(String mobileNo, String message) {
	// // String postData = "";
	// String retVal = "";
	// // OutputStreamWriter out =null;
	// BufferedReader in = null;
	// try {
	//
	// URL url;
	// url = new URL(SMS_HOST + "?username=" + this.USERNAME + "&pass=" +
	// this.PWD + "&senderid=" + this.SENDER_ID
	// + "&dest_mobileno=" + mobileNo + "&message=" + message + "&response=Y");
	//
	// HttpURLConnection urlconnection = (HttpURLConnection)
	// url.openConnection();
	// urlconnection.setRequestMethod("POST");
	// urlconnection.setRequestProperty("Content-Type",
	// "application/x-www-form-urlencoded");
	// urlconnection.setDoOutput(true);
	// // OutputStreamWriter out = new
	// // OutputStreamWriter(urlconnection.getOutputStream());
	// // out.write(postData);
	// // out.close();
	// in = new BufferedReader(new
	// InputStreamReader(urlconnection.getInputStream()));
	// String decodedString;
	// while ((decodedString = in.readLine()) != null) {
	// retVal += decodedString;
	// }
	// in.close();
	// System.out.println(retVal);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } finally {
	//
	// }
	// return retVal;
	// }

	public String sendSMS(SMSObject smsObject) {
		List<SMSObject> smsObjects = new ArrayList<SMSObject>();
		smsObjects.add(smsObject);
		return sendSMS(smsObjects).get(0).getStatus();
	}

	public List<SMSObject> sendSMS(List<SMSObject> smsObjects) {
		try {
			String deliveryStatusS = getDeliveryReport(REPORT_HOST,
					getToken(sendMessages(SMS_HOST, getXmlNode(USERNAME, PWD, SENDER_ID, MESSAGE_TYPE, smsObjects))));

			String delivery_status[] = deliveryStatusS.split(TOKEN_SPLITTER);

			for (int i = 0; i < smsObjects.size(); i++) {
				smsObjects.get(i).setStatus(delivery_status[i]);
			}
			return smsObjects;
		} catch (Exception e) {
			logger.debug("Exception while sending sms:::");
			e.printStackTrace();
		}

		return null;
	}

	public List<String> sendSMS(List<String> mob_nos, String message) {
		try {
			String deliveryStatusS = getDeliveryReport(REPORT_HOST, getToken(
					sendMessages(SMS_HOST, getXmlNode(USERNAME, PWD, SENDER_ID, MESSAGE_TYPE, mob_nos, message))));

			return Arrays.asList(deliveryStatusS.split(TOKEN_SPLITTER));
		} catch (Exception e) {
			logger.debug("Exception while sending sms:::");
			e.printStackTrace();
		}

		return null;
	}

	private static String getMessageNode(String message_content, String mob_no) {
		return "<message-text><text>" + message_content + "</text><to>" + mob_no + "</to></message-text>";
	}

	private static String getXmlNode(String username, String password, String senderId, String messageType,
			String message_content, String mob_no) {
		String returnString = "data=<message-submit-request><username>" + username + "</username>" + "<password>"
				+ password + "</password>" + "<sender-id>" + senderId + "</sender-id>" + "<MType>" + messageType
				+ "</MType>" + getMessageNode(message_content, mob_no) + "</message-submit-request>";
		return returnString;
	}

	private static String getXmlNode(String username, String password, String senderId, String messageType,
			List<SMSObject> smsObjects) {
		StringBuilder returnString = new StringBuilder("data=<message-submit-request><username>" + username
				+ "</username>" + "<password>" + password + "</password>" + "<sender-id>" + senderId + "</sender-id>"
				+ "<MType>" + messageType + "</MType>");

		for (SMSObject smsObject : smsObjects) {
			returnString.append(getMessageNode(smsObject.getMessageContent(), smsObject.getMob_no()));
		}

		returnString.append("</message-submit-request>");
		return returnString.toString();
	}

	private static String getXmlNode(String username, String password, String senderId, String messageType,
			List<String> mob_nos, String message) {
		StringBuilder returnString = new StringBuilder("data=<message-submit-request><username>" + username
				+ "</username>" + "<password>" + password + "</password>" + "<sender-id>" + senderId + "</sender-id>"
				+ "<MType>" + messageType + "</MType>");

		for (String mob_no : mob_nos) {
			returnString.append(getMessageNode(message, mob_no));
		}

		returnString.append("</message-submit-request>");
		return returnString.toString();
	}

	private static String[] getToken(String multipleTokenString) {
		if (multipleTokenString != null)
			return multipleTokenString.split(TOKEN_SPLITTER);
		return null;
	}

	private static String getBalance(String host, String username, String password) throws Exception {
		String balanceCheck_parameters = "?username=" + username + "&pass=" + password;
		URL url = new URL(host + balanceCheck_parameters);
		HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
		urlconnection.setRequestMethod("POST");
		urlconnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		urlconnection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(urlconnection.getOutputStream());
		// out.write(xmlMessage);
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
		String decodedString;
		String retval = "";
		while ((decodedString = in.readLine()) != null) {
			retval += decodedString;
		}
		in.close();
		return retval;
	}

	private static String getDeliveryReport(String host, String[] tokens) throws Exception {

		String retval = "";
		for (String token : tokens) {
			String deliveryReport_parameter = "?Scheduleid=" + token;
			// +"&username=kapbulk&password=kapbulk@user!123";
			System.out.println(host + deliveryReport_parameter);
			URL url = new URL(host + deliveryReport_parameter);
			HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
			urlconnection.setRequestMethod("POST");
			urlconnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			urlconnection.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(urlconnection.getOutputStream());
			// out.write(xmlMessage);
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
			String decodedString;

			while ((decodedString = in.readLine()) != null) {
				retval += decodedString;
				if (retval.indexOf(PENDING_STATUS) != -1) {
					retval = "";
					Thread.sleep(2000);
				}
			}

			in.close();
			retval += TOKEN_SPLITTER;
		}
		return retval;
	}

	private static String sendMessages(String host, String xmlMessage) throws Exception {
		URL url = new URL(host);
		System.out.println(url);
		HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
		urlconnection.setRequestMethod("POST");
		urlconnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		urlconnection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(urlconnection.getOutputStream());
		out.write(xmlMessage);
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
		String decodedString;
		String retval = "";
		while ((decodedString = in.readLine()) != null) {
			retval += decodedString;
		}
		in.close();
		System.out.println("Return messages is:" + retval);
		return retval;
	}
}