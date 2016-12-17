package com.intelliinvest.util;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.intelliinvest.common.IntelliinvestConstants;

public class Helper {

	public static boolean isNotNullAndNonEmpty(String str) {
		if (str != null && str.trim().length() > 0)
			return true;
		else
			return false;
	}

	public static boolean isNotNullAndNonEmpty(List list) {
		if (list != null && list.size() > 0)
			return true;
		else
			return false;
	}

	public static boolean isNotNullAndNonEmpty(Set<String> set) {
		if (set != null && set.size() > 0)
			return true;
		else
			return false;
	}

	public static String formatDecimalNumber(Double doubleNum) {
		DecimalFormat df = new DecimalFormat("###.##");
		return df.format(doubleNum);
	}

	public static String getSignalPresentData(String signal, String preSignal) {
		String signalPresent = IntelliinvestConstants.SIGNAL_PRESENT;
		if (signal.equals(IntelliinvestConstants.BUY)
				&& (preSignal.equals(IntelliinvestConstants.BUY) || preSignal.equals(IntelliinvestConstants.HOLD))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		} else if (signal.equals(IntelliinvestConstants.HOLD)
				&& (preSignal.equals(IntelliinvestConstants.BUY) || preSignal.equals(IntelliinvestConstants.HOLD))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		} else if (signal.equals(IntelliinvestConstants.SELL)
				&& (preSignal.equals(IntelliinvestConstants.SELL) || preSignal.equals(IntelliinvestConstants.WAIT))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		} else if (signal.equals(IntelliinvestConstants.WAIT)
				&& (preSignal.equals(IntelliinvestConstants.SELL) || preSignal.equals(IntelliinvestConstants.WAIT))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		}
		return signalPresent;
	}

	public static String getIIRandomNumber(int maxLength) {

		Random random = new Random();

		double no = random.nextDouble();
		String randomNumber = "ii" + no;
		if (randomNumber.length() > maxLength)
			return randomNumber.substring(0, maxLength);
		else
			return randomNumber;

	}

	public static void main(String[] args) {
		HashSet<String> randomNumbers = new HashSet<>();

		for (int i = 0; i < 1000; i++) {
			String rNo = getIIRandomNumber(35);
			if (!randomNumbers.contains(rNo))
				randomNumbers.add(rNo);
			else
				System.out.println("repeat case:" + rNo);
		}
	}
}