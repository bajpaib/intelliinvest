package com.intelliinvest.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import com.intelliinvest.common.IntelliInvestStore;
import com.sun.mail.util.MailSSLSocketFactory;

public class SMSUtil {

	String username = "kapbulk";
	String password = "kapbulk@user!123";
	String host = "http://123.63.33.43/blank/sms/user/balance_check.php";
	String senderID = "KAPMSG";

	@PostConstruct
	public void init() {
		username = IntelliInvestStore.properties.getProperty("sms.username");
		password = IntelliInvestStore.properties.getProperty("sms.password");
		senderID = IntelliInvestStore.properties.getProperty("sms.senderId");
		host = IntelliInvestStore.properties.getProperty("sms.host");
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
		postData += "username=" + username + "&password=" + password + "&to=" + mobileNo + "&sender=" + senderID
				+ "&message=" + message;
		// // URL url = new URL("http://instant.kapsystem.com/web2sms.php?");

		URL url = new URL("http://123.63.33.43/blank/sms/user/urlsmstemp.php?username=" + username + "&pass=" + password
				+ "&senderid=" + senderID + "&dest_mobileno=" + mobileNo + "&message=" + message + "&response=Y");

		// URL url = new URL("?username=" + username + "&pass=" + password);

		HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
		urlconnection.setRequestMethod("POST");
		urlconnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		urlconnection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(urlconnection.getOutputStream());
		out.write(postData);
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
		String decodedString;
		while ((decodedString = in.readLine()) != null) {
			retval += decodedString;
		}
		in.close();
		System.out.println(retval);
	}

	public String sendSMS(String mobileNo, String message) {
		// String postData = "";
		String retVal = "";
		// OutputStreamWriter out =null;
		BufferedReader in = null;
		try {

			URL url;
			url = new URL(host + "?username=" + this.username + "&pass=" + this.password + "&senderid=" + this.senderID
					+ "&dest_mobileno=" + mobileNo + "&message=" + message + "&response=Y");

			HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
			urlconnection.setRequestMethod("POST");
			urlconnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			urlconnection.setDoOutput(true);
			// OutputStreamWriter out = new
			// OutputStreamWriter(urlconnection.getOutputStream());
			// out.write(postData);
			// out.close();
			in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
			String decodedString;
			while ((decodedString = in.readLine()) != null) {
				retVal += decodedString;
			}
			in.close();
			System.out.println(retVal);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
		return retVal;
	}
}