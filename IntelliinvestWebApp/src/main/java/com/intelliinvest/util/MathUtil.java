package com.intelliinvest.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {

	public static final double EPSILON = 1e-12;
	private static final int DEFAULT_ROUND_PLACES = 2;

	public static double round(double value) {
		return round(value, DEFAULT_ROUND_PLACES);
	}
	
	public static String round(String valueStr) {
		double value = 0; 
		try {
			value = new Double(valueStr).doubleValue();
		}catch (Exception e){
			//not a double
		}
		
		return new Double(round(value, DEFAULT_ROUND_PLACES)).toString();
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static boolean isNearZero(final double d) {
		return isNearZero(d, EPSILON);
	}

	public static boolean isNearZero(final double d, final double TOLERANCE) {
		return Math.abs(d) < TOLERANCE;
	}
}