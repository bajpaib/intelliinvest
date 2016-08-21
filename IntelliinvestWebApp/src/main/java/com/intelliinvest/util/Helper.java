package com.intelliinvest.util;

import java.util.List;

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

}