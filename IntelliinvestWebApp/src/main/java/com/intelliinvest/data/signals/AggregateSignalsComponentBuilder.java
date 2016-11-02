package com.intelliinvest.data.signals;

import org.apache.log4j.Logger;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.StockSignalsDTO;

public class AggregateSignalsComponentBuilder implements SignalComponentBuilder {

	private static Logger logger = Logger
			.getLogger(AggregateSignalsComponentBuilder.class);

	@Override
	public void generateSignal(SignalComponentHolder signalComponentHolder) {
		int size=signalComponentHolder.getStockSignalsDTOs().size();
//		logger.debug("Size:" + signalComponentHolder.getStockSignalsDTOSize());
		if (size > 1) {
			generateAggregateSignals(
					signalComponentHolder.getStockSignalsDTOs().getLast(),
					signalComponentHolder.getStockSignalsDTOs().get(
							size - 2));
		} else
			generateAggregateSignals(signalComponentHolder
					.getStockSignalsDTOs().getLast(), null);
	}

	public static void generateAggregateSignals(
			StockSignalsDTO stockSignalsDTO, StockSignalsDTO preStockSignalsDTO) {

		String iiSignal = IntelliinvestConstants.WAIT;

		if (preStockSignalsDTO == null) {
//			logger.info("previous stock signal object is null, so setting default value....");
			stockSignalsDTO.setAggSignal(iiSignal);
			stockSignalsDTO
					.setAggSignal_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
			stockSignalsDTO.setAggSignal_previous(null);
			return;
		}
		int noOfWait = 0, noOfSell = 0, noOfBuy = 0, noOfHold = 0;

		if (stockSignalsDTO.getAdxSignal() != null
				&& stockSignalsDTO.getAdxSignal().equals(
						IntelliinvestConstants.HOLD))
			noOfHold++;
		else if (stockSignalsDTO.getAdxSignal() != null
				&& stockSignalsDTO.getAdxSignal().equals(
						IntelliinvestConstants.BUY))
			noOfBuy++;
		else if (stockSignalsDTO.getAdxSignal() != null
				&& stockSignalsDTO.getAdxSignal().equals(
						IntelliinvestConstants.SELL))
			noOfSell++;
		else
			noOfWait++;

		if (stockSignalsDTO.getOscillatorSignal() != null
				&& stockSignalsDTO.getOscillatorSignal().equals(
						IntelliinvestConstants.HOLD))
			noOfHold++;
		else if (stockSignalsDTO.getOscillatorSignal() != null
				&& stockSignalsDTO.getOscillatorSignal().equals(
						IntelliinvestConstants.BUY))
			noOfBuy++;
		else if (stockSignalsDTO.getOscillatorSignal() != null
				&& stockSignalsDTO.getOscillatorSignal().equals(
						IntelliinvestConstants.SELL))
			noOfSell++;
		else
			noOfWait++;

		if (stockSignalsDTO.getBollingerSignal() != null
				&& stockSignalsDTO.getBollingerSignal().equals(
						IntelliinvestConstants.HOLD))
			noOfHold++;
		else if (stockSignalsDTO.getBollingerSignal() != null
				&& stockSignalsDTO.getBollingerSignal().equals(
						IntelliinvestConstants.BUY))
			noOfBuy++;
		else if (stockSignalsDTO.getBollingerSignal() != null
				&& stockSignalsDTO.getBollingerSignal().equals(
						IntelliinvestConstants.SELL))
			noOfSell++;
		else
			noOfWait++;

		if (stockSignalsDTO.getMovingAverageSignals() != null
				&& stockSignalsDTO.getMovingAverageSignals()
						.getMovingAverageSignal_Main() != null
				&& stockSignalsDTO.getMovingAverageSignals()
						.getMovingAverageSignal_Main()
						.equals(IntelliinvestConstants.HOLD))
			noOfHold++;
		else if (stockSignalsDTO.getMovingAverageSignals() != null
				&& stockSignalsDTO.getMovingAverageSignals()
						.getMovingAverageSignal_Main() != null
				&& stockSignalsDTO.getMovingAverageSignals()
						.getMovingAverageSignal_Main()
						.equals(IntelliinvestConstants.BUY))
			noOfBuy++;
		else if (stockSignalsDTO.getMovingAverageSignals() != null
				&& stockSignalsDTO.getMovingAverageSignals()
						.getMovingAverageSignal_Main() != null
				&& stockSignalsDTO.getMovingAverageSignals()
						.getMovingAverageSignal_Main()
						.equals(IntelliinvestConstants.SELL))
			noOfSell++;
		else
			noOfWait++;

		if (noOfBuy == 4 || noOfBuy == 3 || (noOfBuy == 2 && noOfHold == 1))
			iiSignal = IntelliinvestConstants.BUY;
		else if (noOfSell == 4 || noOfSell == 3
				|| (noOfSell == 2 && noOfHold == 1))
			iiSignal = IntelliinvestConstants.SELL;
		else if (noOfHold == 4 || noOfHold == 3
				|| (noOfHold == 2 && noOfBuy == 1)
				|| (noOfHold == 2 && noOfSell == 1))
			iiSignal = IntelliinvestConstants.HOLD;
		else {
			if (preStockSignalsDTO.getAggSignal() == null
					|| preStockSignalsDTO.getAggSignal().equals(
							IntelliinvestConstants.WAIT))
				iiSignal = IntelliinvestConstants.WAIT;
			else
				iiSignal = IntelliinvestConstants.HOLD;

		}

		stockSignalsDTO.setAggSignal(iiSignal);
		stockSignalsDTO
				.setAggSignal_previous(preStockSignalsDTO.getAggSignal());

		String signalPresent = IntelliinvestConstants.SIGNAL_PRESENT;
		if (iiSignal.equals(IntelliinvestConstants.BUY)
				&& preStockSignalsDTO.getAggSignal() != null
				&& (preStockSignalsDTO.getAggSignal().equals(
						IntelliinvestConstants.BUY) || preStockSignalsDTO
						.getAggSignal().equals(IntelliinvestConstants.HOLD))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		} else if (iiSignal.equals(IntelliinvestConstants.HOLD)
				&& preStockSignalsDTO.getAggSignal() != null
				&& (preStockSignalsDTO.getAggSignal().equals(
						IntelliinvestConstants.BUY) || preStockSignalsDTO
						.getAggSignal().equals(IntelliinvestConstants.HOLD))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		} else if (iiSignal.equals(IntelliinvestConstants.SELL)
				&& preStockSignalsDTO.getAggSignal() != null
				&& (preStockSignalsDTO.getAggSignal().equals(
						IntelliinvestConstants.SELL) || preStockSignalsDTO
						.getAggSignal().equals(IntelliinvestConstants.WAIT))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		} else if (iiSignal.equals(IntelliinvestConstants.WAIT)
				&& preStockSignalsDTO.getAggSignal() != null
				&& (preStockSignalsDTO.getAggSignal().equals(
						IntelliinvestConstants.SELL) || preStockSignalsDTO
						.getAggSignal().equals(IntelliinvestConstants.WAIT))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		}
		stockSignalsDTO.setAggSignal_present(signalPresent);
	}

}
