package com.intelliinvest.data.signals;

import org.apache.log4j.Logger;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.StockSignalsDTO;

public class AggregateSignalsComponentBuilder implements SignalComponentBuilder {

	private static Logger logger = Logger.getLogger(AggregateSignalsComponentBuilder.class);

	@Override
	public void generateSignal(SignalComponentHolder signalComponentHolder) {
		int size = signalComponentHolder.getStockSignalsDTOs().size();
		// logger.debug("Size:" +
		// signalComponentHolder.getStockSignalsDTOSize());
		if (size > 1) {
			generateAggregateSignals(signalComponentHolder.getStockSignalsDTOs().getLast(),
					signalComponentHolder.getStockSignalsDTOs().get(size - 2));
		} else
			generateAggregateSignals(signalComponentHolder.getStockSignalsDTOs().getLast(), null);
	}

	public static void generateAggregateSignals(StockSignalsDTO stockSignalsDTO, StockSignalsDTO preStockSignalsDTO) {

		String aggSignal = IntelliinvestConstants.WAIT;

		if (preStockSignalsDTO == null) {
			// logger.info("previous stock signal object is null, so setting
			// default value....");
			stockSignalsDTO.setAggSignal(aggSignal);
			stockSignalsDTO.setAggSignal_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
			stockSignalsDTO.setAggSignal_previous(null);
			return;
		}
		int noOfSell = 0, noOfBuy = 0;

		if (stockSignalsDTO.getAdxSignal() != null
				&& (stockSignalsDTO.getAdxSignal().equals(IntelliinvestConstants.HOLD)
						|| stockSignalsDTO.getAdxSignal().equals(IntelliinvestConstants.BUY)))
			noOfBuy++;
		else
			noOfSell++;

		if (stockSignalsDTO.getOscillatorSignal() != null
				&& (stockSignalsDTO.getOscillatorSignal().equals(IntelliinvestConstants.HOLD)
						|| stockSignalsDTO.getOscillatorSignal().equals(IntelliinvestConstants.BUY)))
			noOfBuy++;
		else
			noOfSell++;

		if (stockSignalsDTO.getBollingerSignal() != null
				&& (stockSignalsDTO.getBollingerSignal().equals(IntelliinvestConstants.HOLD)
						|| stockSignalsDTO.getBollingerSignal().equals(IntelliinvestConstants.BUY)))
			noOfBuy++;
		else
			noOfSell++;

		if (stockSignalsDTO.getMovingAverageSignals() != null
				&& stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main() != null
				&& (stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main()
						.equals(IntelliinvestConstants.HOLD)
						|| stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main()
								.equals(IntelliinvestConstants.BUY)))
			noOfBuy++;
		else
			noOfSell++;

		if (stockSignalsDTO.getMovingAverageSignals() != null
				&& stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_LongTerm() != null
				&& (stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_LongTerm()
						.equals(IntelliinvestConstants.HOLD)
						|| stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_LongTerm()
								.equals(IntelliinvestConstants.BUY)))
			noOfBuy++;
		else
			noOfSell++;

		if (noOfBuy >= 3)
			aggSignal = IntelliinvestConstants.BUY;
		else
			aggSignal = IntelliinvestConstants.SELL;

		stockSignalsDTO.setAggSignal(aggSignal);
		stockSignalsDTO.setAggSignal_previous(preStockSignalsDTO.getAggSignal());

		String signalPresent = IntelliinvestConstants.SIGNAL_PRESENT;

		if (aggSignal.equalsIgnoreCase(preStockSignalsDTO.getAggSignal()))
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;

		stockSignalsDTO.setAggSignal_present(signalPresent);
	}

}
