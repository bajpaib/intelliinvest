package com.intelliinvest.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpUtil {
	
	private static Logger logger = Logger.getLogger(HttpUtil.class);
	
	public static byte[] getFromUrlAsBytes(String urlStr) throws IOException{
		InputStream instream = null;
		try{
			URL url = new URL(urlStr);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(10000);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
			instream = connection.getInputStream();
			return IOUtils.toByteArray(instream);
		}catch(Exception e){
			logger.error("Error executing request " + urlStr);
			throw new ClientProtocolException("Unexpected response status: " + e.getMessage());
		}finally{
			if(null!=instream){
				instream.close();
			}
		}
	}
	
	public static String getFromUrlAsString(String urlStr) throws IOException{
		InputStream instream = null;
		try{
			URL url = new URL(urlStr);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(10000);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
			instream = connection.getInputStream();
			return IOUtils.toString(instream);
		}catch(Exception e){
			logger.error("Error executing request " + urlStr);
			throw new ClientProtocolException("Unexpected response status: " + e.getMessage());
		}finally{
			if(null!=instream){
				instream.close();
			}
		}
	}
	
	public static byte[] getFromHttpUrlAsBytes(String url) throws IOException{
		HttpParams my_httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(my_httpParams, 10000);
		HttpClient httpclient = new DefaultHttpClient(my_httpParams);
        final HttpGet httpget = new HttpGet(url);
        // Create a custom response handler
        ResponseHandler<byte[]> responseHandler = new ResponseHandler<byte[]>() {
            public byte[] handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toByteArray(entity) : null;
                } else {
                	logger.error("Error executing request " + httpget.getURI());
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };
        byte[] responseBody = httpclient.execute(httpget, responseHandler);
        return responseBody;
	}
	
	public static String getFromHttpUrlAsString(String url) throws IOException{
		HttpParams my_httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(my_httpParams, 10000);
        DefaultHttpClient httpclient = new DefaultHttpClient(my_httpParams);
        final HttpGet httpget = new HttpGet(url);
        // Create a custom response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			public String handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					logger.error("Error executing request " + httpget.getURI());
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			}
		};
		String responseBody = httpclient.execute(httpget, responseHandler);
        return responseBody;
	}
}
