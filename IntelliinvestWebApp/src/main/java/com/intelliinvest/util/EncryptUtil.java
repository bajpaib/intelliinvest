package com.intelliinvest.util;

import com.googlecode.gwt.crypto.client.TripleDesCipher;
import com.intelliinvest.common.exception.IntelliInvestException;

public class EncryptUtil {
	static final byte[] SECRET_KEY ={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
	
	public static String encrypt(String value){
		 TripleDesCipher cipher = new TripleDesCipher();
		 cipher.setKey(SECRET_KEY);
		 String enc_val;
		try {
			enc_val = cipher.encrypt(value);
		} catch (Exception e) {
			throw new IntelliInvestException("Error encrypting password", e);
		}
		 return enc_val;
	}
	
	public static String decrypt(String value){
		 TripleDesCipher cipher = new TripleDesCipher();
		 cipher.setKey(SECRET_KEY);
		 try {
			return cipher.decrypt(value);
		} catch (Exception e) {
			throw new IntelliInvestException("Error encrypting password", e);
		}
	}
	
	public static void main(String[] args) throws Exception
	{	
		System.out.println(encrypt("Welcome12#"));
		System.out.println(decrypt("Welcome12#"));
	}
}