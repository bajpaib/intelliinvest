package com.intelliinvest.data.signals;

import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockSignalsDTO;

public class SignalComponentInitilaizer{
	
	public void init(SignalComponentHolder signalComponentHolder, QuandlStockPrice quandlStockPrice){
		signalComponentHolder.addQuandlStockPrice(quandlStockPrice);
		StockSignalsDTO stockSignalsDTO = new StockSignalsDTO();
		stockSignalsDTO.setSecurityId(quandlStockPrice.getSecurityId());
		stockSignalsDTO.setSignalDate(quandlStockPrice.getEodDate());
		signalComponentHolder.addStockSignalsDTO(stockSignalsDTO);
	}
}
