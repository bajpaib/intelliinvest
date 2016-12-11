package com.intelliinvest.data.signals;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.util.Helper;

public class ADXSignalComponentBuilder implements SignalComponentBuilder {

	public void generateSignal(SignalComponentHolder signalComponentHolder) {
		QuandlStockPrice quandlStockPrice = signalComponentHolder.getQuandlStockPrices().getLast();

		StockSignalsDTO stockSignalsDTO = signalComponentHolder.getStockSignalsDTOs().getLast();
		// diffDIn
		stockSignalsDTO.setDiffDIn(Math.abs(stockSignalsDTO.getPlusDIn() - stockSignalsDTO.getMinusDIn()));

		// sumDIn
		stockSignalsDTO.setSumDIn(stockSignalsDTO.getPlusDIn() + stockSignalsDTO.getMinusDIn());

		// DX
		stockSignalsDTO.setDX(100 * (stockSignalsDTO.getDiffDIn() / stockSignalsDTO.getSumDIn()));

		int quandlPricesSize = signalComponentHolder.getQuandlStockPrices().size();

		Double previousVolumeAverage = 0D;
		for (int i = 0; i < quandlPricesSize - 1; i++) {
			previousVolumeAverage += signalComponentHolder.getQuandlStockPrices().get(i).getTradedQty();
		}

		previousVolumeAverage = previousVolumeAverage / (quandlPricesSize - 1D);

		Double volumeWeightage = quandlStockPrice.getTradedQty() / previousVolumeAverage;

		if (volumeWeightage > 0D && volumeWeightage < 1D) {
			volumeWeightage = 1D;
		} else if (volumeWeightage > 1D && volumeWeightage < 2D) {
			volumeWeightage = 1.1D;
		} else if (volumeWeightage > 2D && volumeWeightage < 4D) {
			volumeWeightage = 1.2D;
		} else {
			volumeWeightage = 1.3D;
		}

		if (quandlPricesSize != signalComponentHolder.getMa()) {
			stockSignalsDTO.setADXn(stockSignalsDTO.getDX());
			stockSignalsDTO.setAdxSignalPresent(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		} else {
			StockSignalsDTO stockSignalsDTO_1 = signalComponentHolder.getStockSignalsDTOs()
					.get(signalComponentHolder.getStockSignalsDTOs().size() - 2);
			stockSignalsDTO.setADXn(((stockSignalsDTO_1.getADXn() * ((quandlPricesSize - 1D) / quandlPricesSize))
					+ (stockSignalsDTO.getDX() * (1D / quandlPricesSize))) * volumeWeightage);
			Double delta = 5D;
			// signal
			String signal = "";
			if (stockSignalsDTO.getADXn() > signalComponentHolder.getMagicNumberADX()
					&& (stockSignalsDTO.getPlusDIn() - stockSignalsDTO.getMinusDIn()) > delta) {
				signal = IntelliinvestConstants.BUY;
			} else if (stockSignalsDTO.getADXn() > signalComponentHolder.getMagicNumberADX()
					&& (stockSignalsDTO.getPlusDIn() - stockSignalsDTO.getMinusDIn()) < (-1 * delta)) {
				signal = IntelliinvestConstants.SELL;
			} else {
				if (stockSignalsDTO_1.getAdxSignal().equals(IntelliinvestConstants.BUY)
						|| stockSignalsDTO_1.getAdxSignal().equals(IntelliinvestConstants.HOLD)) {
					signal = IntelliinvestConstants.HOLD;
				} else {
					signal = IntelliinvestConstants.WAIT;
				}
			}

			stockSignalsDTO.setAdxSignal(signal);

			stockSignalsDTO.setAdxSignalPresent(Helper.getSignalPresentData(signal, stockSignalsDTO_1.getAdxSignal()));
		}
	}

}
