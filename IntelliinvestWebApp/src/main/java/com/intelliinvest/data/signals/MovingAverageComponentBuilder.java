package com.intelliinvest.data.signals;

import java.util.List;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.data.model.StockSignalsDTO.MovingAverageComponents;
import com.intelliinvest.data.model.StockSignalsDTO.MovingAverageSignals;
import com.intelliinvest.util.Helper;

public class MovingAverageComponentBuilder implements SignalComponentBuilder {

	@Override
	public void generateSignal(SignalComponentHolder signalComponentHolder) {
		int size = signalComponentHolder.getStockSignalsDTOs().size();
		// logger.debug("Size:" +
		// signalComponentHolder.getStockSignalsDTOSize());
		if (size > 1) {

			generateMovingAverageSignals(signalComponentHolder.getQuandlStockPrices(),
					signalComponentHolder.getStockSignalsDTOs().getLast(), signalComponentHolder.getStockSignalsDTOs()
							.get(signalComponentHolder.getStockSignalsDTOSize() - 2));
		} else {
			generateMovingAverageSignals(signalComponentHolder.getQuandlStockPrices(),
					signalComponentHolder.getStockSignalsDTOs().getLast(), null);
		}

	}

	public void generateMovingAverageSignals(List<QuandlStockPrice> quandlStockPrices, StockSignalsDTO stockSignalsDTO,
			StockSignalsDTO preStockSignalsDTO) {

		int size = quandlStockPrices.size();
		int counter = size;

		// logger.debug("QuandlStockPrice list size is : " + size
		// + " while generating moving average signals...");
		int period_5 = 5, period_10 = 10, period_15 = 15, period_25 = 25, period_50 = 50;

		double movingAverage_5 = -1, movingAverage_10 = -1, movingAverage_15 = -1, movingAverage_25 = -1,
				movingAverage_50 = -1;

		if (size < period_5) {
			setMovingAverageSignalsDefault(stockSignalsDTO);
		} else {
			if (size >= period_5) {
				movingAverage_5 = getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
			}
			if (size >= period_10) {
				movingAverage_10 = movingAverage_5 + getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;

			}
			if (size >= period_15) {
				movingAverage_15 = movingAverage_10 + getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;

			}
			if (size >= period_25) {
				movingAverage_25 = movingAverage_15 + getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_25 = movingAverage_25 + getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
			}
			if (size >= period_50) {
				movingAverage_50 = movingAverage_25 + getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_50 = movingAverage_50 + getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_50 = movingAverage_50 + getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_50 = movingAverage_50 + getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_50 = movingAverage_50 + getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;

			}
		}
		if (movingAverage_5 != -1)
			movingAverage_5 = movingAverage_5 / 5;
		if (movingAverage_10 != -1)
			movingAverage_10 = movingAverage_10 / 10;
		if (movingAverage_15 != -1)
			movingAverage_15 = movingAverage_15 / 15;
		if (movingAverage_25 != -1)
			movingAverage_25 = movingAverage_25 / 25;
		if (movingAverage_50 != -1)
			movingAverage_50 = movingAverage_50 / 50;

		StockSignalsDTO.MovingAverageComponents movingAverageComponents = stockSignalsDTO.new MovingAverageComponents(
				movingAverage_5, movingAverage_10, movingAverage_15, movingAverage_25, movingAverage_50);
		stockSignalsDTO.setMovingAverageComponents(movingAverageComponents);

		setMovingAverageSignals(stockSignalsDTO, preStockSignalsDTO);
	}

	private void setMovingAverageSignals(StockSignalsDTO stockSignalsDTO, StockSignalsDTO preStockSignalsDTO) {

		StockSignalsDTO.MovingAverageSignals movingAverageSignals = stockSignalsDTO.getMovingAverageSignals();
		if (movingAverageSignals == null) {
			movingAverageSignals = stockSignalsDTO.new MovingAverageSignals();

		}

		movingAverageSignals.setMovingAverageSignal_SmallTerm(getMovingAverageSmallTermSignal(stockSignalsDTO));
		movingAverageSignals.setMovingAverageSignal_Main(getMovingAverageMainSignal(stockSignalsDTO));
		movingAverageSignals.setMovingAverageSignal_MidTerm(getMovingAverageMidTermSignal(stockSignalsDTO));
		movingAverageSignals.setMovingAverageSignal_LongTerm(getMovingAverageLongTermSignal(stockSignalsDTO));

		if (preStockSignalsDTO != null)
			setSignalPresentData(movingAverageSignals, preStockSignalsDTO);
		stockSignalsDTO.setMovingAverageSignals(movingAverageSignals);

	}

	private void setSignalPresentData(MovingAverageSignals movingAverageSignals, StockSignalsDTO preStockSignalsDTO) {
		MovingAverageSignals preMovingAverageSignals = preStockSignalsDTO.getMovingAverageSignals();

		movingAverageSignals.setMovingAverageSignal_SmallTerm_present(
				Helper.getSignalPresentData(movingAverageSignals.getMovingAverageSignal_SmallTerm(),
						preMovingAverageSignals.getMovingAverageSignal_SmallTerm()));

		movingAverageSignals.setMovingAverageSignal_Main_present(
				Helper.getSignalPresentData(movingAverageSignals.getMovingAverageSignal_Main(),
						preMovingAverageSignals.getMovingAverageSignal_Main()));

		movingAverageSignals.setMovingAverageSignal_MidTerm_present(
				Helper.getSignalPresentData(movingAverageSignals.getMovingAverageSignal_MidTerm(),
						preMovingAverageSignals.getMovingAverageSignal_MidTerm()));

		movingAverageSignals.setMovingAverageSignal_LongTerm_present(
				Helper.getSignalPresentData(movingAverageSignals.getMovingAverageSignal_LongTerm(),
						preMovingAverageSignals.getMovingAverageSignal_LongTerm()));

	}

	private double getClosePrice(List<QuandlStockPrice> quandlStockPrices, int movingAverageCounter) {
		movingAverageCounter--;
		return quandlStockPrices.get(movingAverageCounter).getClose()
				+ quandlStockPrices.get(movingAverageCounter--).getClose()
				+ quandlStockPrices.get(movingAverageCounter--).getClose()
				+ quandlStockPrices.get(movingAverageCounter--).getClose()
				+ quandlStockPrices.get(movingAverageCounter--).getClose();
	}

	private String getMovingAverageSmallTermSignal(StockSignalsDTO stockSignalsDTO) {
		if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_5() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_10() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_5() >= stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_10()) {
			return IntelliinvestConstants.BUY;
			// movingAverageSignals
			// .setMovingAverageSignal_SmallTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);

		} else if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_5() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_10() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_5() < stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_10()) {
			return IntelliinvestConstants.SELL;
			// movingAverageSignals
			// .setMovingAverageSignal_SmallTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);
		}

		else {
			return IntelliinvestConstants.WAIT;
			// movingAverageSignals
			// .setMovingAverageSignal_SmallTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		}

	}

	private String getMovingAverageMainSignal(StockSignalsDTO stockSignalsDTO) {
		if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_10() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_10() >= stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_25()) {
			return IntelliinvestConstants.BUY;
			// movingAverageSignals
			// .setMovingAverageSignal_Main_present(IntelliinvestConstants.SIGNAL_PRESENT);

		} else if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_10() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_10() < stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_25()) {
			return IntelliinvestConstants.SELL;
			// movingAverageSignals
			// .setMovingAverageSignal_Main_present(IntelliinvestConstants.SIGNAL_PRESENT);
		}

		/*
		 * if
		 * (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_10()
		 * == -1 && stockSignalsDTO.getMovingAverageComponents()
		 * .getMovingAverage_25() == -1)
		 */
		else {
			return IntelliinvestConstants.WAIT;
			// movingAverageSignals
			// .setMovingAverageSignal_Main_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		}

	}

	private String getMovingAverageMidTermSignal(StockSignalsDTO stockSignalsDTO) {
		if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_15() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_15() >= stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_25()) {
			return IntelliinvestConstants.BUY;
			// movingAverageSignals
			// .setMovingAverageSignal_MidTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);

		} else if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_15() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_15() < stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_25()) {
			return IntelliinvestConstants.SELL;
			// movingAverageSignals
			// .setMovingAverageSignal_MidTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);
		}

		else {
			return IntelliinvestConstants.WAIT;
			// movingAverageSignals
			// .setMovingAverageSignal_MidTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		}

	}

	private String getMovingAverageLongTermSignal(StockSignalsDTO stockSignalsDTO) {
		if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_50() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() >= stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_50()) {
			return IntelliinvestConstants.BUY;
			// movingAverageSignals
			// .setMovingAverageSignal_LongTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);

		} else if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_50() != -1
				&& stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() < stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_50()) {
			return IntelliinvestConstants.SELL;
			// movingAverageSignals
			// .setMovingAverageSignal_LongTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);
		}

		else {
			return IntelliinvestConstants.WAIT;
			// movingAverageSignals
			// .setMovingAverageSignal_LongTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		}

	}

	public void setMovingAverageSignalsDefault(StockSignalsDTO stockSignalsDTO) {
		StockSignalsDTO.MovingAverageSignals movingAverageSignals = stockSignalsDTO.new MovingAverageSignals();
		movingAverageSignals.setMovingAverageSignal_SmallTerm(IntelliinvestConstants.WAIT);
		movingAverageSignals.setMovingAverageSignal_Main(IntelliinvestConstants.WAIT);
		movingAverageSignals.setMovingAverageSignal_MidTerm(IntelliinvestConstants.WAIT);
		movingAverageSignals.setMovingAverageSignal_LongTerm(IntelliinvestConstants.WAIT);

		// movingAverageSignals
		// .setPreviousMovingAverageSignal_Main(IntelliinvestConstants.WAIT);
		// movingAverageSignals
		// .setPreviousMovingAverageSignal_SmallTerm(IntelliinvestConstants.WAIT);
		// movingAverageSignals
		// .setPreviousMovingAverageSignal_MidTerm(IntelliinvestConstants.WAIT);
		// movingAverageSignals
		// .setPreviousMovingAverageSignal_LongTerm(IntelliinvestConstants.WAIT);
		movingAverageSignals.setMovingAverageSignal_Main_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		movingAverageSignals.setMovingAverageSignal_SmallTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		movingAverageSignals.setMovingAverageSignal_MidTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		movingAverageSignals.setMovingAverageSignal_LongTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);

		stockSignalsDTO.setMovingAverageSignals(movingAverageSignals);

		StockSignalsDTO.MovingAverageComponents movingAverageComponents = stockSignalsDTO.new MovingAverageComponents(
				-1, -1, -1, -1, -1);
		stockSignalsDTO.setMovingAverageComponents(movingAverageComponents);
	}

}
