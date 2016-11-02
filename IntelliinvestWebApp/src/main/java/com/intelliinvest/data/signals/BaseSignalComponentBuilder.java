package com.intelliinvest.data.signals;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.log4j.Logger;

import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockSignalsDTO;

public class BaseSignalComponentBuilder implements SignalComponentBuilder {
	private Logger logger = Logger.getLogger(BaseSignalComponentBuilder.class);

	public void generateSignal(SignalComponentHolder signalComponentHolder) {
		QuandlStockPrice quandlStockPrice = signalComponentHolder.getQuandlStockPrices().getLast();
		StockSignalsDTO stockSignalsDTO = signalComponentHolder.getStockSignalsDTOs().getLast();
		int quandlPricesSize = signalComponentHolder.getQuandlStockPrices().size();
		if (quandlPricesSize == 1) {
			stockSignalsDTO.setSplitMultiplier(1D);
			return;
		}
		StockSignalsDTO previousSignalComponent = signalComponentHolder.getStockSignalsDTOs()
				.get(signalComponentHolder.getStockSignalsDTOs().size() - 2);
		QuandlStockPrice quandlStockPrice_1 = signalComponentHolder.getQuandlStockPrices().get(quandlPricesSize - 2);
		try {

			Double high_1 = quandlStockPrice_1.getHigh() * previousSignalComponent.getSplitMultiplier();
			Double low_1 = quandlStockPrice_1.getLow() * previousSignalComponent.getSplitMultiplier();
			Double close_1 = quandlStockPrice_1.getClose() * previousSignalComponent.getSplitMultiplier();
			Double open = quandlStockPrice.getOpen() * previousSignalComponent.getSplitMultiplier();

			if (open < close_1) {

				Double multiplier = 1D;
				if (open != 0)
					multiplier = close_1 / open;
				BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
				if (splitMultiplier.intValue() >= 2) {
					stockSignalsDTO.setSplitMultiplier(
							splitMultiplier.doubleValue() * previousSignalComponent.getSplitMultiplier());
				} else {
					stockSignalsDTO.setSplitMultiplier(previousSignalComponent.getSplitMultiplier());
				}
			} else {
				Double multiplier = 1D;
				if (close_1 != 0)
					multiplier = open / close_1;
				BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
				if (splitMultiplier.intValue() >= 2) {
					stockSignalsDTO.setSplitMultiplier(
							previousSignalComponent.getSplitMultiplier() / splitMultiplier.doubleValue());
				} else {
					stockSignalsDTO.setSplitMultiplier(previousSignalComponent.getSplitMultiplier());
				}
			}

			Double high = quandlStockPrice.getHigh() * stockSignalsDTO.getSplitMultiplier();
			Double low = quandlStockPrice.getLow() * stockSignalsDTO.getSplitMultiplier();

			Double high_low = high - low;
			Double high_close = Math.abs(high - close_1);
			Double low_close = Math.abs(low - close_1);

			// TR
			stockSignalsDTO.setTR(max(high_low, high_close, low_close));

			// plusDM1
			if ((high - high_1) > (low_1 - low)) {
				stockSignalsDTO.setPlusDM1(max(high - high_1, 0D));
			}

			// minusDM1
			if ((low_1 - low) > (high - high_1)) {
				stockSignalsDTO.setMinusDM1(max(low_1 - low, 0D));
			}

			// TRn
			int ma = signalComponentHolder.getMa();

			stockSignalsDTO
					.setTRn((previousSignalComponent.getTRn() * (quandlPricesSize - 1) / ma) + stockSignalsDTO.getTR());

			// plusDMn
			stockSignalsDTO.setPlusDMn((previousSignalComponent.getPlusDMn() * (quandlPricesSize - 1) / ma)
					+ stockSignalsDTO.getPlusDM1());

			// minusDMn
			stockSignalsDTO.setMinusDMn((previousSignalComponent.getMinusDMn() * (quandlPricesSize - 1) / ma)
					+ stockSignalsDTO.getMinusDM1());

			// plusDIn
			// minusDIn
			if (stockSignalsDTO.getTRn() != 0) {
				stockSignalsDTO.setPlusDIn(100 * (stockSignalsDTO.getPlusDMn() / stockSignalsDTO.getTRn()));

				stockSignalsDTO.setMinusDIn(100 * (stockSignalsDTO.getMinusDMn() / stockSignalsDTO.getTRn()));
			} else {
				stockSignalsDTO.setPlusDIn(0D);
				stockSignalsDTO.setMinusDIn(0d);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error while calculating components of base signals...." + e.getMessage()
					+ e.getStackTrace()[0].getLineNumber());
			// for (StackTraceElement element : e.getStackTrace()) {
			// logger.error(element.getLineNumber() + " method: "
			// + element.getMethodName());
			// }
			logger.error("Quandl Price Object is:" + quandlStockPrice.toString());
			logger.error("Quandl Price 1 Object is:" + quandlStockPrice_1.toString());
			logger.error("Pre Signal Component Object is:" + previousSignalComponent.toString());
//			throw e;
		}
	}

	private Double max(Double value1, Double value2) {
		if (value1 > value2) {
			return value1;
		}
		return value2;

	}

	private Double max(Double value1, Double value2, Double value3) {
		Double max;
		if (value1 > value2) {
			if (value1 > value3) {
				max = value1;
			} else {
				max = value3;
			}
		} else {
			if (value2 > value3) {
				max = value2;
			} else {
				max = value3;
			}
		}
		return max;
	}

}
