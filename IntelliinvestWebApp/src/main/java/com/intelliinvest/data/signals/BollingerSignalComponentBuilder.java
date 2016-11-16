package com.intelliinvest.data.signals;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.util.Helper;

public class BollingerSignalComponentBuilder implements SignalComponentBuilder {
	private static Logger logger = Logger.getLogger(BollingerSignalComponentBuilder.class);

	public void generateSignal(SignalComponentHolder signalComponentHolder) {
		double close_avg = 0;
		LinkedList<QuandlStockPrice> quandlStockPrices = signalComponentHolder.getQuandlStockPrices();
		double close_prices[] = new double[quandlStockPrices.size()];
		int index = 0;
		for (QuandlStockPrice QuandlStockPrice : quandlStockPrices) {
			close_avg += QuandlStockPrice.getClose();
			close_prices[index++] = QuandlStockPrice.getClose();
		}
		close_avg = close_avg / quandlStockPrices.size();
		double sma = close_avg;
		double stdDeviation = getStdDeviation(close_avg, close_prices);

		double upperBound = sma + (2 * stdDeviation);
		double lowerBound = sma - (2 * stdDeviation);
		Double bandwidth = (upperBound - lowerBound) / sma;

		StockSignalsDTO stockSignalsDTO = signalComponentHolder.getStockSignalsDTOs().getLast();
		stockSignalsDTO.setUpperBound(upperBound);
		stockSignalsDTO.setLowerBound(lowerBound);
		stockSignalsDTO.setSma(sma);
		stockSignalsDTO.setBandwidth(bandwidth);

		if (quandlStockPrices.size() < signalComponentHolder.getMa()) {
			stockSignalsDTO.setBollingerSignal(IntelliinvestConstants.WAIT);
			stockSignalsDTO.setSignalPresentBollinger(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
			return;
		}

		StockSignalsDTO previousStockSignalsDTO = signalComponentHolder.getStockSignalsDTOs()
				.get(signalComponentHolder.getStockSignalsDTOs().size() - 2);

		Double plusDIn = stockSignalsDTO.getPlusDIn();
		Double minusDIn = stockSignalsDTO.getMinusDIn();

		if (bandwidth != null && bandwidth != -1) {
			String signal = "";
			if (bandwidth > signalComponentHolder.getMagicNumberBolliger() && plusDIn > minusDIn)
				signal = IntelliinvestConstants.BUY;
			else if (bandwidth > signalComponentHolder.getMagicNumberBolliger() && plusDIn < minusDIn)
				signal = IntelliinvestConstants.SELL;
			else {
				if (previousStockSignalsDTO.getBollingerSignal() != null
						&& (previousStockSignalsDTO.getBollingerSignal().equals(IntelliinvestConstants.BUY)
								|| previousStockSignalsDTO.getBollingerSignal().equals(IntelliinvestConstants.HOLD))) {
					signal = IntelliinvestConstants.HOLD;
				} else {
					signal = IntelliinvestConstants.WAIT;
				}
			}

			stockSignalsDTO.setBollingerSignal(signal);
			stockSignalsDTO.setSignalPresentBollinger(
					Helper.getSignalPresentData(signal, previousStockSignalsDTO.getBollingerSignal()));
		} else {
			logger.info("No bollinger signals has been generating for code:" + stockSignalsDTO.getSecurityId() + "--"
					+ stockSignalsDTO.getSignalDate());
			stockSignalsDTO.setBollingerSignal(IntelliinvestConstants.WAIT);
			stockSignalsDTO.setSignalPresentBollinger(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		}
	}

	private double getStdDeviation(double average, double[] close_prices) {
		double temp = 0;
		for (double price : close_prices) {
			temp += (average - price) * (average - price);
		}
		return Math.sqrt(temp / close_prices.length);
	}

}
