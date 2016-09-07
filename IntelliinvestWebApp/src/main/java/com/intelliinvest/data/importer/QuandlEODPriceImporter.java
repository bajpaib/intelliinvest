package com.intelliinvest.data.importer;

import org.springframework.stereotype.Component;

import com.intelliinvest.common.exception.IntelliInvestPropertyHoler;
import com.intelliinvest.util.HttpUtil;

@Component
public class QuandlEODPriceImporter implements EODPriceImporter {
	
	static String QUANDL_URL = "https://www.quandl.com/api/v3/datasets/NSE/%s.csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=%s&end_date=%s";
	
	public String importData(String code, String startDate, String endDate) throws Exception{
		String url = String.format(QUANDL_URL, IntelliInvestPropertyHoler.getQuandlStockCode(code), startDate, endDate);
		return HttpUtil.getFromUrlAsString(url);
	}
	
//	public static void main(String[] args) throws Exception{
//		EODPriceImporter quandlEODPriceImporter = new QuandlEODPriceImporter(new IntelliInvestRepository(null));
//		System.out.println(quandlEODPriceImporter.importData("INFY", "2016-08-01", "2016-08-10"));
//	}
//	
}