package com.intelliinvest.data.signals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.log4j.Logger;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.data.model.StockSignalsDTO.MovingAverageSignals;

public class SignalComponentsEnhancer {

	private static Logger logger = Logger
			.getLogger(SignalComponentsEnhancer.class);

	int ma;

	public SignalComponentsEnhancer(int ma) {
		this.ma = ma;
	}

	StockSignalsDTO init9(QuandlStockPrice quandlStockPrice,
			QuandlStockPrice quandlStockPrice_1) {
		StockSignalsDTO stockSignalsBO = new StockSignalsDTO();
		stockSignalsBO.setSymbol(quandlStockPrice.getSecurityId());
		stockSignalsBO.setSignalDate(quandlStockPrice.getEodDate());

		Double high = quandlStockPrice.getHigh();
		Double low = quandlStockPrice.getLow();
		// Double close = QuandlStockPrice.getClose();

		Double high_1 = quandlStockPrice_1.getHigh();
		Double low_1 = quandlStockPrice_1.getLow();
		Double close_1 = quandlStockPrice_1.getClose();

		Double high_low = high - low;
		Double high_close = high - close_1;
		Double low_close = low - close_1;
		stockSignalsBO.setTR(max(high_low, high_close, low_close));

		// plusDM1
		if ((high - high_1) > (low_1 - low)) {
			stockSignalsBO.setPlusDM1(max(high - high_1, 0D));
		}

		// minusDM1
		if ((low_1 - low) > (high - high_1)) {
			stockSignalsBO.setMinusDM1(max(low_1 - low, 0D));
		}

		stockSignalsBO.setSplitMultiplier(1D);
		stockSignalsBO
				.setSignalPresent(IntelliinvestConstants.SIGNAL_NOT_PRESENT);

		setBollingerOscillatorSignalsDefaultValue(stockSignalsBO);
		return stockSignalsBO;
	}

	private void setBollingerOscillatorSignalsDefaultValue(
			StockSignalsDTO signalComponents) {
		signalComponents.setBollingerSignal(IntelliinvestConstants.WAIT);
		signalComponents
				.setSignalPresentBollinger(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		signalComponents.setOscillatorSignal(IntelliinvestConstants.WAIT);
		signalComponents
				.setSignalPresentOscillator(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
	}

	StockSignalsDTO init10(Integer magicNumber,
			List<QuandlStockPrice> quandlStockPrices,
			List<StockSignalsDTO> signals) {
		QuandlStockPrice quandlStockPrice = quandlStockPrices
				.get(quandlStockPrices.size() - 1);
		QuandlStockPrice quandlStockPrice_1 = quandlStockPrices
				.get(quandlStockPrices.size() - 2);
		StockSignalsDTO prevSignalComponents = signals.get(signals.size() - 1);
		StockSignalsDTO signalComponents = new StockSignalsDTO();
		signalComponents.setSymbol(quandlStockPrice.getSecurityId());
		signalComponents.setSignalDate(quandlStockPrice.getEodDate());
		Double TRnTmp = 0D;
		Double plusDMnTmp = 0D;
		Double minusDMnTmp = 0D;

		for (StockSignalsDTO signalComponentsTmp : signals) {
			TRnTmp = TRnTmp + signalComponentsTmp.getTR();
			plusDMnTmp = plusDMnTmp + signalComponentsTmp.getPlusDM1();
			minusDMnTmp = minusDMnTmp + signalComponentsTmp.getMinusDM1();
		}

		Double high_1 = quandlStockPrice_1.getHigh()
				* prevSignalComponents.getSplitMultiplier();
		Double low_1 = quandlStockPrice_1.getLow()
				* prevSignalComponents.getSplitMultiplier();
		Double close_1 = quandlStockPrice_1.getClose()
				* prevSignalComponents.getSplitMultiplier();

		Double open = quandlStockPrice.getOpen()
				* prevSignalComponents.getSplitMultiplier();

		if (open < close_1) {
			Double multiplier = close_1 / open;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0,
					RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				signalComponents.setSplitMultiplier(splitMultiplier
						.doubleValue()
						* prevSignalComponents.getSplitMultiplier());
			} else {
				signalComponents.setSplitMultiplier(prevSignalComponents
						.getSplitMultiplier());
			}
			// System.out.println(" calculated split multiplier for " + close_1
			// + " " + open + " " + splitMultiplier + " is " +
			// signalComponents.getSplitMultiplier());
		} else {
			Double multiplier = open / close_1;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0,
					RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				signalComponents.setSplitMultiplier(prevSignalComponents
						.getSplitMultiplier() / splitMultiplier.doubleValue());
			} else {
				signalComponents.setSplitMultiplier(prevSignalComponents
						.getSplitMultiplier());
			}
			// System.out.println(" calculated split multiplier for " + close_1
			// + " " + open + " " + splitMultiplier + " is " +
			// signalComponents.getSplitMultiplier());
		}

		Double high = quandlStockPrice.getHigh()
				* signalComponents.getSplitMultiplier();
		Double low = quandlStockPrice.getLow()
				* signalComponents.getSplitMultiplier();
		// Double close = QuandlStockPrice.getClose() *
		// signalComponents.getSplitMultiplier();

		Double high_low = high - low;
		Double high_close = Math.abs(high - close_1);
		Double low_close = Math.abs(low - close_1);

		// TR
		signalComponents.setTR(max(high_low, high_close, low_close));

		// plusDM1
		if ((high - high_1) > (low_1 - low)) {
			signalComponents.setPlusDM1(max(high - high_1, 0D));
		}

		// minusDM1
		if ((low_1 - low) > (high - high_1)) {
			signalComponents.setMinusDM1(max(low_1 - low, 0D));
		}

		signalComponents.setTRn(TRnTmp);

		// plusDMn
		signalComponents.setPlusDMn(plusDMnTmp);

		// minusDMn
		signalComponents.setMinusDMn(minusDMnTmp);

		// plusDIn
		signalComponents.setPlusDIn(100 * (plusDMnTmp / TRnTmp));

		// minusDIn
		signalComponents.setMinusDIn(100 * (minusDMnTmp / TRnTmp));

		// diffDIn
		signalComponents.setDiffDIn(signalComponents.getPlusDIn()
				- signalComponents.getMinusDIn());

		// sumDIn
		signalComponents.setSumDIn(signalComponents.getPlusDIn()
				+ signalComponents.getMinusDIn());

		// DX
		signalComponents
				.setDX(100 * (signalComponents.getDiffDIn() / signalComponents
						.getSumDIn()));

		// ADX
		signalComponents.setADXn(signalComponents.getDX());

		Double delta = new Double(
				IntelliInvestStore.properties.getProperty("delta"));

		// signal
		String signal = "";
		if (signalComponents.getADXn() > magicNumber
				&& (signalComponents.getPlusDIn() - signalComponents
						.getMinusDIn()) > delta) {
			signal = IntelliinvestConstants.BUY;
		} else if (signalComponents.getADXn() > magicNumber
				&& (signalComponents.getPlusDIn() - signalComponents
						.getMinusDIn()) < (-1 * delta)) {
			signal = IntelliinvestConstants.SELL;
		} else {
			if (prevSignalComponents.getSignalType().equals(
					IntelliinvestConstants.BUY)
					|| prevSignalComponents.getSignalType().equals(
							IntelliinvestConstants.HOLD)) {
				signal = IntelliinvestConstants.HOLD;
			} else {
				signal = IntelliinvestConstants.WAIT;
			}
		}
		signalComponents.setSignalType(signal);

		String signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		signalComponents.setSignalPresent(signalPresent);
		setBollingerOscillatorSignalsDefaultValue(signalComponents);

		// System.out.println(signalComponents);
		return signalComponents;
	}

	public static void main(String[] args) {
		Double close_1 = 1250D;
		Double open = 240D;
		if (open < close_1) {
			Double multiplier = close_1 / open;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0,
					RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				System.out.println(" calculated split multiplier for "
						+ close_1 + "  " + open + "  " + splitMultiplier
						+ " is " + (splitMultiplier.doubleValue() * 1D));
			}
		} else {
			Double multiplier = open / close_1;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0,
					RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				System.out.println(" calculated split multiplier for "
						+ close_1 + "  " + open + "  " + splitMultiplier
						+ " is " + (1D / splitMultiplier.doubleValue()));
			}
		}
	}

	StockSignalsDTO init(Integer magicNumber,
			List<QuandlStockPrice> quandlStockPrices,
			StockSignalsDTO prevSignalComponents, List<StockSignalsDTO> signals) {
		QuandlStockPrice quandlStockPrice = quandlStockPrices
				.get(quandlStockPrices.size() - 1);
		QuandlStockPrice quandlStockPrice_1 = quandlStockPrices
				.get(quandlStockPrices.size() - 2);
		StockSignalsDTO signalComponents = new StockSignalsDTO();
		signalComponents.setSymbol(quandlStockPrice.getSecurityId());
		signalComponents.setSignalDate(quandlStockPrice.getEodDate());
//		logger.debug("last stock price date:" + quandlStockPrice.getEodDate()
//				+ " for stock code:" + quandlStockPrice.getSecurityId());
		Double high_1 = quandlStockPrice_1.getHigh()
				* prevSignalComponents.getSplitMultiplier();
		Double low_1 = quandlStockPrice_1.getLow()
				* prevSignalComponents.getSplitMultiplier();
		Double close_1 = quandlStockPrice_1.getClose()
				* prevSignalComponents.getSplitMultiplier();

		Double open = quandlStockPrice.getOpen()
				* prevSignalComponents.getSplitMultiplier();

		if (open < close_1) {
			Double multiplier = close_1 / open;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0,
					RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				signalComponents.setSplitMultiplier(splitMultiplier
						.doubleValue()
						* prevSignalComponents.getSplitMultiplier());
			} else {
				signalComponents.setSplitMultiplier(prevSignalComponents
						.getSplitMultiplier());
			}
			// System.out.println(" calculated split multiplier for " + close_1
			// + " " + open + " " + splitMultiplier + " is " +
			// signalComponents.getSplitMultiplier());
		} else {
			Double multiplier = open / close_1;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0,
					RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				signalComponents.setSplitMultiplier(prevSignalComponents
						.getSplitMultiplier() / splitMultiplier.doubleValue());
			} else {
				signalComponents.setSplitMultiplier(prevSignalComponents
						.getSplitMultiplier());
			}
			// System.out.println(" calculated split multiplier for " + close_1
			// + " " + open + " " + splitMultiplier + " is " +
			// signalComponents.getSplitMultiplier());
		}

		Double high = quandlStockPrice.getHigh()
				* signalComponents.getSplitMultiplier();
		Double low = quandlStockPrice.getLow()
				* signalComponents.getSplitMultiplier();
		// Double close = QuandlStockPrice.getClose() *
		// signalComponents.getSplitMultiplier();

		Double high_low = high - low;
		Double high_close = Math.abs(high - close_1);
		Double low_close = Math.abs(low - close_1);

		// TR
		signalComponents.setTR(max(high_low, high_close, low_close));

		// plusDM1
		if ((high - high_1) > (low_1 - low)) {
			signalComponents.setPlusDM1(max(high - high_1, 0D));
		}

		// minusDM1
		if ((low_1 - low) > (high - high_1)) {
			signalComponents.setMinusDM1(max(low_1 - low, 0D));
		}

		// TRn
		signalComponents.setTRn((prevSignalComponents.getTRn() * (ma - 1) / ma)
				+ signalComponents.getTR());

		// plusDMn
		signalComponents.setPlusDMn((prevSignalComponents.getPlusDMn()
				* (ma - 1) / ma)
				+ signalComponents.getPlusDM1());

		// minusDMn
		signalComponents.setMinusDMn((prevSignalComponents.getMinusDMn()
				* (ma - 1) / ma)
				+ signalComponents.getMinusDM1());

		// plusDIn
		signalComponents
				.setPlusDIn(100 * (signalComponents.getPlusDMn() / signalComponents
						.getTRn()));

		// minusDIn
		signalComponents
				.setMinusDIn(100 * (signalComponents.getMinusDMn() / signalComponents
						.getTRn()));

		// diffDIn
		signalComponents.setDiffDIn(Math.abs(signalComponents.getPlusDIn()
				- signalComponents.getMinusDIn()));

		// sumDIn
		signalComponents.setSumDIn(signalComponents.getPlusDIn()
				+ signalComponents.getMinusDIn());

		// DX
		signalComponents
				.setDX(100 * (signalComponents.getDiffDIn() / signalComponents
						.getSumDIn()));

		Double previousVolumeAverage = 0D;
		for (int i = 0; i < quandlStockPrices.size() - 1; i++) {
			previousVolumeAverage += quandlStockPrices.get(i).getTradedQty();
		}

		previousVolumeAverage = previousVolumeAverage
				/ (quandlStockPrices.size() - 1D);

		Double volumeWeightage = quandlStockPrice.getTradedQty()
				/ previousVolumeAverage;

		if (volumeWeightage > 0D && volumeWeightage < 1D) {
			volumeWeightage = 1D;
		} else if (volumeWeightage > 1D && volumeWeightage < 2D) {
			volumeWeightage = 1.1D;
		} else if (volumeWeightage > 2D && volumeWeightage < 4D) {
			volumeWeightage = 1.2D;
		} else {
			volumeWeightage = 1.3D;
		}

		// ADX
		signalComponents
				.setADXn(((prevSignalComponents.getADXn() * ((ma - 1D) / ma)) + (signalComponents
						.getDX() * (1D / ma))) * volumeWeightage);

		Double delta = new Double(
				IntelliInvestStore.properties.getProperty("delta"));
		// signal
		String signal = "";
		if (signalComponents.getADXn() > magicNumber
				&& (signalComponents.getPlusDIn() - signalComponents
						.getMinusDIn()) > delta) {
			signal = IntelliinvestConstants.BUY;
		} else if (signalComponents.getADXn() > magicNumber
				&& (signalComponents.getPlusDIn() - signalComponents
						.getMinusDIn()) < (-1 * delta)) {
			signal = IntelliinvestConstants.SELL;
		} else {
			if (prevSignalComponents.getSignalType().equals(
					IntelliinvestConstants.BUY)
					|| prevSignalComponents.getSignalType().equals(
							IntelliinvestConstants.HOLD)) {
				signal = IntelliinvestConstants.HOLD;
			} else {
				signal = IntelliinvestConstants.WAIT;
			}
		}

		signalComponents.setSignalType(signal);

		String signalPresent = IntelliinvestConstants.SIGNAL_PRESENT;
		if (signal.equals(IntelliinvestConstants.BUY)
				&& (prevSignalComponents.getSignalType().equals(
						IntelliinvestConstants.BUY) || prevSignalComponents
						.getSignalType().equals(IntelliinvestConstants.HOLD))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		} else if (signal.equals(IntelliinvestConstants.HOLD)
				&& (prevSignalComponents.getSignalType().equals(
						IntelliinvestConstants.BUY) || prevSignalComponents
						.getSignalType().equals(IntelliinvestConstants.HOLD))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		} else if (signal.equals(IntelliinvestConstants.SELL)
				&& (prevSignalComponents.getSignalType().equals(
						IntelliinvestConstants.SELL) || prevSignalComponents
						.getSignalType().equals(IntelliinvestConstants.WAIT))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		} else if (signal.equals(IntelliinvestConstants.WAIT)
				&& (prevSignalComponents.getSignalType().equals(
						IntelliinvestConstants.SELL) || prevSignalComponents
						.getSignalType().equals(IntelliinvestConstants.WAIT))) {
			signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
		}

		// signalComponents.setPreviousSignalType(prevSignalComponents
		// .getSignalType());
		signalComponents.setSignalPresent(signalPresent);

		// TODO remove this hard code magic number
		setBollingerSignals(.17, quandlStockPrices, signalComponents,
				prevSignalComponents);
		setOscillatorSignals(15, quandlStockPrices, signals, signalComponents,
				prevSignalComponents);
		// generateAggregateSignals(signalComponents, prevSignalComponents);
		// System.out.println(signalComponents);

		return signalComponents;
	}

	boolean setBollingerSignals(Double magicNumber,
			List<QuandlStockPrice> QuandlStockPrices,
			StockSignalsDTO signalComponent,
			StockSignalsDTO prevSignalComponents) {
		// logger.info("Generating bollinger signal...");
		try {
			// logger.info("QuandlStockPrice Size is: " +
			// QuandlStockPrices.size());
			double close_avg = 0;
			double close_prices[] = new double[QuandlStockPrices.size()];
			int index = 0;
			for (QuandlStockPrice QuandlStockPrice : QuandlStockPrices) {
				close_avg += QuandlStockPrice.getClose();
				close_prices[index++] = QuandlStockPrice.getClose();
			}
			close_avg = close_avg / QuandlStockPrices.size();
			double sma = close_avg;
			double stdDeviation = getStdDeviation(close_avg, close_prices);

			double upperBound = sma + (2 * stdDeviation);
			double lowerBound = sma - (2 * stdDeviation);
			Double bandwidth = (upperBound - lowerBound) / sma;

			signalComponent.setUpperBound(upperBound);
			signalComponent.setLowerBound(lowerBound);
			signalComponent.setSma(sma);
			signalComponent.setBandwidth(bandwidth);

			Double plusDIn = signalComponent.getPlusDIn();
			Double minusDIn = signalComponent.getMinusDIn();

			if (bandwidth != null && bandwidth != -1) {
				String signal = "";
				String signalPresent = IntelliinvestConstants.SIGNAL_PRESENT;
				if (bandwidth > magicNumber && plusDIn > minusDIn)
					signal = IntelliinvestConstants.BUY;
				else if (bandwidth > magicNumber && plusDIn < minusDIn)
					signal = IntelliinvestConstants.SELL;
				else {
					if (prevSignalComponents.getBollingerSignal() != null
							&& (prevSignalComponents.getBollingerSignal()
									.equals(IntelliinvestConstants.BUY) || prevSignalComponents
									.getBollingerSignal().equals(
											IntelliinvestConstants.HOLD))) {
						signal = IntelliinvestConstants.HOLD;
					} else {
						signal = IntelliinvestConstants.WAIT;
					}
				}

				if (signal.equals(IntelliinvestConstants.BUY)
						&& (prevSignalComponents.getBollingerSignal() != null && (prevSignalComponents
								.getBollingerSignal().equals(
										IntelliinvestConstants.BUY) || prevSignalComponents
								.getBollingerSignal().equals(
										IntelliinvestConstants.HOLD)))) {
					signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
				} else if (signal.equals(IntelliinvestConstants.HOLD)
						&& (prevSignalComponents.getBollingerSignal() != null && (prevSignalComponents
								.getBollingerSignal().equals(
										IntelliinvestConstants.BUY) || prevSignalComponents
								.getBollingerSignal().equals(
										IntelliinvestConstants.HOLD)))) {
					signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
				} else if (signal.equals(IntelliinvestConstants.SELL)
						&& (prevSignalComponents.getBollingerSignal() != null && (prevSignalComponents
								.getBollingerSignal().equals(
										IntelliinvestConstants.SELL) || prevSignalComponents
								.getBollingerSignal().equals(
										IntelliinvestConstants.WAIT)))) {
					signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
				} else if (signal.equals(IntelliinvestConstants.WAIT)
						&& (prevSignalComponents.getBollingerSignal() != null && (prevSignalComponents
								.getBollingerSignal().equals(
										IntelliinvestConstants.SELL) || prevSignalComponents
								.getBollingerSignal().equals(
										IntelliinvestConstants.WAIT)))) {
					signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
				}
				signalComponent.setBollingerSignal(signal);
				signalComponent.setSignalPresentBollinger(signalPresent);
				// signalComponent.setPreviousBollingerSignal(prevSignalComponents
				// .getBollingerSignal());
			} else {
				logger.info("No bollinger signals has been generating for code:"
						+ signalComponent.getSymbol()
						+ "--"
						+ signalComponent.getSignalDate());
				signalComponent.setBollingerSignal(IntelliinvestConstants.WAIT);
				signalComponent
						.setSignalPresentBollinger(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while setting bollinger signals..."
					+ signalComponent.getSymbol() + "----"
					+ signalComponent.getSignalDate());
			return false;
		}

		return true;
	}

	boolean setOscillatorSignals(Integer magicNumber,
			List<QuandlStockPrice> QuandlStockPrices,
			List<StockSignalsDTO> prev_signals,
			StockSignalsDTO signalComponent,
			StockSignalsDTO prevSignalComponents) {
		// logger.info("Generating oscillator signal...");
		try {
			double max_high = 0;
			double lowest = Double.MAX_VALUE;

			QuandlStockPrice QuandlStockPrice = QuandlStockPrices
					.get(QuandlStockPrices.size() - 1);

			double today_close_price = QuandlStockPrice.getClose();

			// getting max high and lowest of all
			for (QuandlStockPrice QuandlStockPriceTemp : QuandlStockPrices) {
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

			int signals_length = prev_signals.size();
			Double percentKFlow = getPercentKFlow(
					prev_signals.get(signals_length - 1).getStochastic10Day(),
					prev_signals.get(signals_length - 2).getStochastic10Day(),
					stochastic10Day);

			Double percentDFlow = null;
			if (percentKFlow != null) {
				percentDFlow = getPercentDFlow(
						prev_signals.get(signals_length - 1).getPercentKFlow(),
						prev_signals.get(signals_length - 2).getPercentKFlow(),
						percentKFlow);
			}

			signalComponent.setHigh10Day(max_high);
			signalComponent.setLow10Day(lowest);
			signalComponent.setRange10Day(range);
			signalComponent.setStochastic10Day(stochastic10Day);
			signalComponent.setPercentDFlow(percentDFlow);
			signalComponent.setPercentKFlow(percentKFlow);

			if (percentDFlow != null) {
				String signal = "";
				String signalPresent = IntelliinvestConstants.SIGNAL_PRESENT;

				if (percentDFlow > magicNumber && percentKFlow > percentDFlow)
					signal = IntelliinvestConstants.BUY;
				else if (percentDFlow > (100 - magicNumber)
						&& percentKFlow < percentDFlow)
					signal = IntelliinvestConstants.SELL;
				else {

					if (prevSignalComponents.getOscillatorSignal() != null
							&& (prevSignalComponents.getOscillatorSignal()
									.equals(IntelliinvestConstants.BUY) || prevSignalComponents
									.getOscillatorSignal().equals(
											IntelliinvestConstants.HOLD))) {
						signal = IntelliinvestConstants.HOLD;
					} else {
						signal = IntelliinvestConstants.WAIT;
					}
				}

				if (signal.equals(IntelliinvestConstants.BUY)
						&& (prevSignalComponents.getOscillatorSignal() != null && (prevSignalComponents
								.getOscillatorSignal().equals(
										IntelliinvestConstants.BUY) || prevSignalComponents
								.getOscillatorSignal().equals(
										IntelliinvestConstants.HOLD)))) {
					signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
				} else if (signal.equals(IntelliinvestConstants.HOLD)
						&& (prevSignalComponents.getOscillatorSignal() != null && (prevSignalComponents
								.getOscillatorSignal().equals(
										IntelliinvestConstants.BUY) || prevSignalComponents
								.getOscillatorSignal().equals(
										IntelliinvestConstants.HOLD)))) {
					signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
				} else if (signal.equals(IntelliinvestConstants.SELL)
						&& (prevSignalComponents.getOscillatorSignal() != null && (prevSignalComponents
								.getOscillatorSignal().equals(
										IntelliinvestConstants.SELL) || prevSignalComponents
								.getOscillatorSignal().equals(
										IntelliinvestConstants.WAIT)))) {
					signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
				} else if (signal.equals(IntelliinvestConstants.WAIT)
						&& (prevSignalComponents.getOscillatorSignal() != null && (prevSignalComponents
								.getOscillatorSignal().equals(
										IntelliinvestConstants.SELL) || prevSignalComponents
								.getOscillatorSignal().equals(
										IntelliinvestConstants.WAIT)))) {
					signalPresent = IntelliinvestConstants.SIGNAL_NOT_PRESENT;
				}
				signalComponent.setOscillatorSignal(signal);
				signalComponent.setSignalPresentOscillator(signalPresent);
				// signalComponent
				// .setPreviousOscillatorSignal(prevSignalComponents
				// .getOscillatorSignal());
			} else {
				logger.info("No oscillator signals has been generating for code:"
						+ signalComponent.getSymbol()
						+ "--"
						+ signalComponent.getSignalDate());
				signalComponent.setOscillatorSignal("Wait");
				signalComponent
						.setSignalPresentOscillator(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
			}

		} catch (Exception e) {
			logger.error("Error while setting oscillator signals..."
					+ signalComponent.getSymbol() + "----"
					+ signalComponent.getSignalDate());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private Double getPercentKFlow(Double stochastic10Day_1,
			Double stochastic10Day_2, double stochastic10Day_3) {
		if (stochastic10Day_1 == null || stochastic10Day_2 == null) {
			logger.info("Null entry for stochastic, so returning null for percentkflow....");
			return null;
		} else
			return average(stochastic10Day_1, stochastic10Day_2,
					stochastic10Day_3);

	}

	private Double getPercentDFlow(Double percentKFlow_1,
			Double percentKFlow_2, double percentKFlow_3) {
		if (percentKFlow_1 == null || percentKFlow_2 == null)
			return null;
		else
			return average(percentKFlow_1, percentKFlow_2, percentKFlow_3);

	}

	private double getStdDeviation(double average, double[] close_prices) {
		double temp = 0;
		for (double price : close_prices) {
			temp += (average - price) * (average - price);
		}
		return Math.sqrt(temp / close_prices.length);
	}

	Double max(Double value1, Double value2) {
		if (value1 > value2) {
			return value1;
		}
		return value2;

	}

	Double max(Double value1, Double value2, Double value3) {
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

	Double average(Double value1, Double value2, Double value3) {
		return (value1 + value2 + value3) / 3;
	}

	public void generateMovingAverageSignals(
			List<QuandlStockPrice> quandlStockPrices,
			StockSignalsDTO stockSignalsDTO, StockSignalsDTO preStockSignalsDTO) {

		int size = quandlStockPrices.size();
		int counter = size;

		// logger.debug("QuandlStockPrice list size is : " + size
		// + " while generating moving average signals...");
		int period_5 = 5, period_10 = 10, period_15 = 15, period_25 = 25, period_50 = 50;

		double movingAverage_5 = -1, movingAverage_10 = -1, movingAverage_15 = -1, movingAverage_25 = -1, movingAverage_50 = -1;

		if (size < period_5) {
			setMovingAverageSignalsDefault(stockSignalsDTO);
		} else {
			if (size >= period_5) {
				movingAverage_5 = getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
			}
			if (size >= period_10) {
				movingAverage_10 = movingAverage_5
						+ getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;

			}
			if (size >= period_15) {
				movingAverage_15 = movingAverage_10
						+ getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;

			}
			if (size >= period_25) {
				movingAverage_25 = movingAverage_15
						+ getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_25 = movingAverage_25
						+ getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
			}
			if (size >= period_50) {
				movingAverage_50 = movingAverage_25
						+ getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_50 = movingAverage_50
						+ getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_50 = movingAverage_50
						+ getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_50 = movingAverage_50
						+ getClosePrice(quandlStockPrices, counter);
				counter = counter - 5;
				movingAverage_50 = movingAverage_50
						+ getClosePrice(quandlStockPrices, counter);
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
				movingAverage_5, movingAverage_10, movingAverage_15,
				movingAverage_25, movingAverage_50);
		stockSignalsDTO.setMovingAverageComponents(movingAverageComponents);

		setMovingAverageSignals(stockSignalsDTO, preStockSignalsDTO);
	}

	private double getClosePrice(List<QuandlStockPrice> quandlStockPrices,
			int movingAverageCounter) {
		movingAverageCounter--;
		return quandlStockPrices.get(movingAverageCounter).getClose()
				+ quandlStockPrices.get(movingAverageCounter--).getClose()
				+ quandlStockPrices.get(movingAverageCounter--).getClose()
				+ quandlStockPrices.get(movingAverageCounter--).getClose()
				+ quandlStockPrices.get(movingAverageCounter--).getClose();
	}

	private void setMovingAverageSignals(StockSignalsDTO stockSignalsDTO,
			StockSignalsDTO preStockSignalsDTO) {

		StockSignalsDTO.MovingAverageSignals movingAverageSignals = stockSignalsDTO
				.getMovingAverageSignals();
		if (movingAverageSignals == null) {
			movingAverageSignals = stockSignalsDTO.new MovingAverageSignals();

		}

		setMovingAverageSmallTermSignal(stockSignalsDTO, movingAverageSignals);
		setMovingAverageMainSignal(stockSignalsDTO, movingAverageSignals);
		setMovingAverageMidTermSignal(stockSignalsDTO, movingAverageSignals);
		setMovingAverageLongTermSignal(stockSignalsDTO, movingAverageSignals);
		stockSignalsDTO.setMovingAverageSignals(movingAverageSignals);
		// setMovingAveragePreviousSignals(movingAverageSignals,
		// preStockSignalsDTO);

	}

	// private void setMovingAveragePreviousSignals(
	// MovingAverageSignals movingAverageSignals,
	// StockSignalsDTO preStockSignalsDTO) {
	// movingAverageSignals
	// .setPreviousMovingAverageSignal_LongTerm(preStockSignalsDTO
	// .getMovingAverageSignals()
	// .getMovingAverageSignal_LongTerm());
	//
	// movingAverageSignals
	// .setPreviousMovingAverageSignal_MidTerm(preStockSignalsDTO
	// .getMovingAverageSignals()
	// .getMovingAverageSignal_MidTerm());
	//
	// movingAverageSignals
	// .setPreviousMovingAverageSignal_SmallTerm(preStockSignalsDTO
	// .getMovingAverageSignals()
	// .getMovingAverageSignal_SmallTerm());
	//
	// movingAverageSignals
	// .setPreviousMovingAverageSignal_Main(preStockSignalsDTO
	// .getMovingAverageSignals()
	// .getMovingAverageSignal_Main());
	// }

	private void setMovingAverageSmallTermSignal(
			StockSignalsDTO stockSignalsDTO,
			MovingAverageSignals movingAverageSignals) {
		if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_5() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_10() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_5() >= stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_10()) {
			movingAverageSignals
					.setMovingAverageSignal_SmallTerm(IntelliinvestConstants.BUY);
			movingAverageSignals
					.setMovingAverageSignal_SmallTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);

		} else if (stockSignalsDTO.getMovingAverageComponents()
				.getMovingAverage_5() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_10() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_5() < stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_10()) {
			movingAverageSignals
					.setMovingAverageSignal_SmallTerm(IntelliinvestConstants.SELL);
			movingAverageSignals
					.setMovingAverageSignal_SmallTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);
		}

		/*
		 * if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_5()
		 * == -1 || stockSignalsDTO.getMovingAverageComponents()
		 * .getMovingAverage_10() == -1)
		 */

		else {
			movingAverageSignals
					.setMovingAverageSignal_SmallTerm(IntelliinvestConstants.WAIT);
			movingAverageSignals
					.setMovingAverageSignal_SmallTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		}

	}

	private void setMovingAverageMainSignal(StockSignalsDTO stockSignalsDTO,
			MovingAverageSignals movingAverageSignals) {
		if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_10() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_10() >= stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_25()) {
			movingAverageSignals
					.setMovingAverageSignal_Main(IntelliinvestConstants.BUY);
			movingAverageSignals
					.setMovingAverageSignal_Main_present(IntelliinvestConstants.SIGNAL_PRESENT);

		} else if (stockSignalsDTO.getMovingAverageComponents()
				.getMovingAverage_10() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_10() < stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_25()) {
			movingAverageSignals
					.setMovingAverageSignal_Main(IntelliinvestConstants.SELL);
			movingAverageSignals
					.setMovingAverageSignal_Main_present(IntelliinvestConstants.SIGNAL_PRESENT);
		}

		/*
		 * if
		 * (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_10()
		 * == -1 && stockSignalsDTO.getMovingAverageComponents()
		 * .getMovingAverage_25() == -1)
		 */
		else {
			movingAverageSignals
					.setMovingAverageSignal_Main(IntelliinvestConstants.WAIT);
			movingAverageSignals
					.setMovingAverageSignal_Main_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		}

	}

	private void setMovingAverageMidTermSignal(StockSignalsDTO stockSignalsDTO,
			MovingAverageSignals movingAverageSignals) {
		if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_15() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_15() >= stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_25()) {
			movingAverageSignals
					.setMovingAverageSignal_MidTerm(IntelliinvestConstants.BUY);
			movingAverageSignals
					.setMovingAverageSignal_MidTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);

		} else if (stockSignalsDTO.getMovingAverageComponents()
				.getMovingAverage_15() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_15() < stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_25()) {
			movingAverageSignals
					.setMovingAverageSignal_MidTerm(IntelliinvestConstants.SELL);
			movingAverageSignals
					.setMovingAverageSignal_MidTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);
		}
		/*
		 * if
		 * (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_15()
		 * == -1 && stockSignalsDTO.getMovingAverageComponents()
		 * .getMovingAverage_25() == -1)
		 */
		else {
			movingAverageSignals
					.setMovingAverageSignal_MidTerm(IntelliinvestConstants.WAIT);
			movingAverageSignals
					.setMovingAverageSignal_MidTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		}

	}

	private void setMovingAverageLongTermSignal(
			StockSignalsDTO stockSignalsDTO,
			MovingAverageSignals movingAverageSignals) {
		if (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_50() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_25() >= stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_50()) {
			movingAverageSignals
					.setMovingAverageSignal_LongTerm(IntelliinvestConstants.BUY);
			movingAverageSignals
					.setMovingAverageSignal_LongTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);

		} else if (stockSignalsDTO.getMovingAverageComponents()
				.getMovingAverage_25() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_50() != -1
				&& stockSignalsDTO.getMovingAverageComponents()
						.getMovingAverage_25() < stockSignalsDTO
						.getMovingAverageComponents().getMovingAverage_50()) {
			movingAverageSignals
					.setMovingAverageSignal_LongTerm(IntelliinvestConstants.SELL);
			movingAverageSignals
					.setMovingAverageSignal_LongTerm_present(IntelliinvestConstants.SIGNAL_PRESENT);
		}

		/*
		 * if
		 * (stockSignalsDTO.getMovingAverageComponents().getMovingAverage_25()
		 * == -1 && stockSignalsDTO.getMovingAverageComponents()
		 * .getMovingAverage_50() == -1)
		 */
		else {
			movingAverageSignals
					.setMovingAverageSignal_LongTerm(IntelliinvestConstants.WAIT);
			movingAverageSignals
					.setMovingAverageSignal_LongTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		}

	}

	public void setMovingAverageSignalsDefault(StockSignalsDTO stockSignalsDTO) {
		StockSignalsDTO.MovingAverageSignals movingAverageSignals = stockSignalsDTO.new MovingAverageSignals();
		movingAverageSignals
				.setMovingAverageSignal_SmallTerm(IntelliinvestConstants.WAIT);
		movingAverageSignals
				.setMovingAverageSignal_Main(IntelliinvestConstants.WAIT);
		movingAverageSignals
				.setMovingAverageSignal_MidTerm(IntelliinvestConstants.WAIT);
		movingAverageSignals
				.setMovingAverageSignal_LongTerm(IntelliinvestConstants.WAIT);

		// movingAverageSignals
		// .setPreviousMovingAverageSignal_Main(IntelliinvestConstants.WAIT);
		// movingAverageSignals
		// .setPreviousMovingAverageSignal_SmallTerm(IntelliinvestConstants.WAIT);
		// movingAverageSignals
		// .setPreviousMovingAverageSignal_MidTerm(IntelliinvestConstants.WAIT);
		// movingAverageSignals
		// .setPreviousMovingAverageSignal_LongTerm(IntelliinvestConstants.WAIT);
		movingAverageSignals
				.setMovingAverageSignal_Main_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		movingAverageSignals
				.setMovingAverageSignal_SmallTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		movingAverageSignals
				.setMovingAverageSignal_MidTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);
		movingAverageSignals
				.setMovingAverageSignal_LongTerm_present(IntelliinvestConstants.SIGNAL_NOT_PRESENT);

		stockSignalsDTO.setMovingAverageSignals(movingAverageSignals);

		StockSignalsDTO.MovingAverageComponents movingAverageComponents = stockSignalsDTO.new MovingAverageComponents(
				-1, -1, -1, -1, -1);
		stockSignalsDTO.setMovingAverageComponents(movingAverageComponents);
	}

	public static void generateAggregateSignals(
			StockSignalsDTO stockSignalsDTO, StockSignalsDTO preStockSignalsDTO) {

		String iiSignal = IntelliinvestConstants.WAIT;
		int noOfWait = 0, noOfSell = 0, noOfBuy = 0, noOfHold = 0;

		if (stockSignalsDTO.getSignalType() != null
				&& stockSignalsDTO.getSignalType().equals(
						IntelliinvestConstants.HOLD))
			noOfHold++;
		else if (stockSignalsDTO.getSignalType() != null
				&& stockSignalsDTO.getSignalType().equals(
						IntelliinvestConstants.BUY))
			noOfBuy++;
		else if (stockSignalsDTO.getSignalType() != null
				&& stockSignalsDTO.getSignalType().equals(
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
