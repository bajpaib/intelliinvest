package com.intelliinvest.data.signals;

import org.apache.log4j.Logger;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.util.Helper;

public class OscillatorSignalComponentBuilder implements SignalComponentBuilder {
	private static Logger logger = Logger.getLogger(OscillatorSignalComponentBuilder.class);

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
		int size = signalComponentHolder.getStockSignalsDTOs().size();
		if (size < signalComponentHolder.getStockSignalsDTOSize()) {
			stockSignalsDTO.setOscillatorSignal(IntelliinvestConstants.WAIT);
			stockSignalsDTO.setSignalPresentOscillator(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
			return;
		}
		StockSignalsDTO stockSignalsDTO_1 = signalComponentHolder.getStockSignalsDTOs().get(size - 2);
		StockSignalsDTO stockSignalsDTO_2 = signalComponentHolder.getStockSignalsDTOs().get(size - 3);

		if (signalComponentHolder.getStockSignalsDTOs().size() >= 3) {
			Double percentKFlow = getPercentKFlow(stockSignalsDTO_1.getStochastic10Day(),
					stockSignalsDTO_2.getStochastic10Day(), stochastic10Day);

			Double percentDFlow = null;
			if (percentKFlow != null) {
				percentDFlow = getPercentDFlow(stockSignalsDTO_1.getPercentKFlow(), stockSignalsDTO_2.getPercentKFlow(),
						percentKFlow);
			}
			stockSignalsDTO.setPercentDFlow(percentDFlow);
			stockSignalsDTO.setPercentKFlow(percentKFlow);
			if (percentDFlow != null) {
				String signal = "";
				if (percentDFlow > signalComponentHolder.getMagicNumberOscillator() && percentKFlow > percentDFlow)
					signal = IntelliinvestConstants.BUY;
				else if (percentDFlow > (100 - signalComponentHolder.getMagicNumberOscillator())
						&& percentKFlow < percentDFlow)
					signal = IntelliinvestConstants.SELL;
				else {
					if (stockSignalsDTO_1.getOscillatorSignal() != null
							&& (stockSignalsDTO_1.getOscillatorSignal().equals(IntelliinvestConstants.BUY)
									|| stockSignalsDTO_1.getOscillatorSignal().equals(IntelliinvestConstants.HOLD))) {
						signal = IntelliinvestConstants.HOLD;
					} else {
						signal = IntelliinvestConstants.WAIT;
					}
				}

				stockSignalsDTO.setOscillatorSignal(signal);
				String signal_present=Helper.getSignalPresentData(signal, stockSignalsDTO_1.getOscillatorSignal());
//				logger.info("Present Signal:"+signal);
//				logger.info("Pre Stock Object:"+stockSignalsDTO_1);
//				logger.info("Signal Present:"+ signal_present);
				stockSignalsDTO.setSignalPresentOscillator(signal_present);
			}
			else{
				stockSignalsDTO.setOscillatorSignal(IntelliinvestConstants.WAIT);
				stockSignalsDTO.setSignalPresentOscillator(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
					
			}
		}
	}

	private Double getPercentKFlow(Double stochastic10Day_1, Double stochastic10Day_2, double stochastic10Day_3) {
		if (stochastic10Day_1 == null || stochastic10Day_2 == null) {
			logger.info("Null entry for stochastic, so returning null for percentkflow....");
			return null;
		} else
			return average(stochastic10Day_1, stochastic10Day_2, stochastic10Day_3);

	}

	private Double getPercentDFlow(Double percentKFlow_1, Double percentKFlow_2, double percentKFlow_3) {
		if (percentKFlow_1 == null || percentKFlow_2 == null)
			return null;
		else
			return average(percentKFlow_1, percentKFlow_2, percentKFlow_3);

	}

	private Double average(Double value1, Double value2, Double value3) {
		return (value1 + value2 + value3) / 3;
	}

}
