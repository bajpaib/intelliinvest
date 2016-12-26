package com.intelliinvest.data.signals;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.util.Helper;

public class OscillatorSignalComponentBuilder implements SignalComponentBuilder {

	public void generateSignal(SignalComponentHolder signalComponentHolder) {
		double max_high = 0;
		double lowest = Double.MAX_VALUE;

		QuandlStockPrice QuandlStockPrice = signalComponentHolder.getQuandlStockPrices().getLast();

		double today_close_price = QuandlStockPrice.getClose();

		// getting max high and lowest of all
		for (QuandlStockPrice QuandlStockPriceTemp : signalComponentHolder.getQuandlStockPrices()) {
			double high = QuandlStockPriceTemp.getHigh();
			double low = QuandlStockPriceTemp.getLow();

			if (high > max_high)
				max_high = high;
			if (low < lowest)
				lowest = low;
		}

		double range = 0.001;

		if (max_high != lowest)
			range = max_high - lowest;

		double stochastic10Day = 100 * (today_close_price - lowest) / range;

		StockSignalsDTO stockSignalsDTO = signalComponentHolder.getStockSignalsDTOs().getLast();

		stockSignalsDTO.setHigh10Day(max_high);
		stockSignalsDTO.setLow10Day(lowest);
		stockSignalsDTO.setRange10Day(range);
		stockSignalsDTO.setStochastic10Day(stochastic10Day);

		if (stockSignalsDTO.getSplitMultiplier() == 0)
			stockSignalsDTO.setSplitMultiplier(1D);
		int quandlPricesSize = signalComponentHolder.getQuandlStockPrices().size();
		if (quandlPricesSize == 1) {
			stockSignalsDTO.setOscillatorSignal(IntelliinvestConstants.WAIT);
			stockSignalsDTO.setSignalPresentOscillator(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
			stockSignalsDTO.setPercentDFlow(stochastic10Day);
			return;
		}
		int ma = signalComponentHolder.getMa();
		StockSignalsDTO previousSignalComponent = signalComponentHolder.getStockSignalsDTOs().get(signalComponentHolder.getStockSignalsDTOs().size() - 2);
		if(quandlPricesSize<ma){
			stockSignalsDTO.setOscillatorSignal(IntelliinvestConstants.WAIT);
			stockSignalsDTO.setSignalPresentOscillator(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
			stockSignalsDTO.setPercentDFlow((previousSignalComponent.getStochastic10Day() + stochastic10Day)/2);
			stockSignalsDTO.setSignalPresentOscillator(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
			stockSignalsDTO.setOscillatorSignal(IntelliinvestConstants.WAIT);
			return;
		}
		
		stockSignalsDTO.setPercentDFlow( (previousSignalComponent.getPercentDFlow() * (ma - 1) / ma) 
				+ (stockSignalsDTO.getStochastic10Day() * (1D / Integer.valueOf(ma).doubleValue())) );
		String signal = "";
		if (stockSignalsDTO.getPercentDFlow() < signalComponentHolder.getMagicNumberOscillator())
			signal = IntelliinvestConstants.BUY;
		else if (stockSignalsDTO.getPercentDFlow() > (100 - signalComponentHolder.getMagicNumberOscillator()))
			signal = IntelliinvestConstants.SELL;
		else {
			if (previousSignalComponent.getOscillatorSignal() != null
					&& (previousSignalComponent.getOscillatorSignal().equals(IntelliinvestConstants.BUY)
							|| previousSignalComponent.getOscillatorSignal().equals(IntelliinvestConstants.HOLD))) {
				signal = IntelliinvestConstants.HOLD;
			} else {
				signal = IntelliinvestConstants.WAIT;
			}
		}

		stockSignalsDTO.setOscillatorSignal(signal);
		String signal_present=Helper.getSignalPresentData(signal, previousSignalComponent.getOscillatorSignal());
		stockSignalsDTO.setSignalPresentOscillator(signal_present);
		
	}

}
