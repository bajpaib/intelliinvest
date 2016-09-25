package com.intelliinvest.data.signals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.log4j.Logger;

import com.intelliinvest.common.IntelliInvestStore;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.web.dto.StockSignalsDTO;

public class SignalComponentsEnhancer {

	private static Logger logger = Logger.getLogger(SignalComponentsEnhancer.class);

	int ma;

	public SignalComponentsEnhancer(int ma) {
		this.ma = ma;
	}

	StockSignalsDTO init9(QuandlStockPrice quandlStockPrice, QuandlStockPrice quandlStockPrice_1) {
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
		stockSignalsBO.setSignalPresent("N");

		setBollingerOscillatorSignalsDefaultValue(stockSignalsBO);
		return stockSignalsBO;
	}

	private void setBollingerOscillatorSignalsDefaultValue(StockSignalsDTO signalComponents) {
		signalComponents.setBollingerSignal("Wait");
		signalComponents.setSignalPresentBollinger("N");
		signalComponents.setOscillatorSignal("Wait");
		signalComponents.setSignalPresentOscillator("N");
	}

	StockSignalsDTO init10(Integer magicNumber, List<QuandlStockPrice> QuandlStockPrices,
			List<StockSignalsDTO> signals) {
		QuandlStockPrice QuandlStockPrice = QuandlStockPrices.get(QuandlStockPrices.size() - 1);
		QuandlStockPrice QuandlStockPrice_1 = QuandlStockPrices.get(QuandlStockPrices.size() - 2);
		StockSignalsDTO prevSignalComponents = signals.get(signals.size() - 1);
		StockSignalsDTO signalComponents = new StockSignalsDTO();
		signalComponents.setSymbol(QuandlStockPrice.getSecurityId());
		signalComponents.setSignalDate(QuandlStockPrice.getEodDate());
		Double TRnTmp = 0D;
		Double plusDMnTmp = 0D;
		Double minusDMnTmp = 0D;

		for (StockSignalsDTO signalComponentsTmp : signals) {
			TRnTmp = TRnTmp + signalComponentsTmp.getTR();
			plusDMnTmp = plusDMnTmp + signalComponentsTmp.getPlusDM1();
			minusDMnTmp = minusDMnTmp + signalComponentsTmp.getMinusDM1();
		}

		Double high_1 = QuandlStockPrice_1.getHigh() * prevSignalComponents.getSplitMultiplier();
		Double low_1 = QuandlStockPrice_1.getLow() * prevSignalComponents.getSplitMultiplier();
		Double close_1 = QuandlStockPrice_1.getClose() * prevSignalComponents.getSplitMultiplier();

		Double open = QuandlStockPrice.getOpen() * prevSignalComponents.getSplitMultiplier();

		if (open < close_1) {
			Double multiplier = close_1 / open;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				signalComponents
						.setSplitMultiplier(splitMultiplier.doubleValue() * prevSignalComponents.getSplitMultiplier());
			} else {
				signalComponents.setSplitMultiplier(prevSignalComponents.getSplitMultiplier());
			}
			// System.out.println(" calculated split multiplier for " + close_1
			// + " " + open + " " + splitMultiplier + " is " +
			// signalComponents.getSplitMultiplier());
		} else {
			Double multiplier = open / close_1;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				signalComponents
						.setSplitMultiplier(prevSignalComponents.getSplitMultiplier() / splitMultiplier.doubleValue());
			} else {
				signalComponents.setSplitMultiplier(prevSignalComponents.getSplitMultiplier());
			}
			// System.out.println(" calculated split multiplier for " + close_1
			// + " " + open + " " + splitMultiplier + " is " +
			// signalComponents.getSplitMultiplier());
		}

		Double high = QuandlStockPrice.getHigh() * signalComponents.getSplitMultiplier();
		Double low = QuandlStockPrice.getLow() * signalComponents.getSplitMultiplier();
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
		signalComponents.setDiffDIn(signalComponents.getPlusDIn() - signalComponents.getMinusDIn());

		// sumDIn
		signalComponents.setSumDIn(signalComponents.getPlusDIn() + signalComponents.getMinusDIn());

		// DX
		signalComponents.setDX(100 * (signalComponents.getDiffDIn() / signalComponents.getSumDIn()));

		Double previousVolumeAverage = 0D;
		for (int i = 0; i < QuandlStockPrices.size() - 1; i++) {
			previousVolumeAverage += QuandlStockPrices.get(i).getTradedQty();
		}

		previousVolumeAverage = previousVolumeAverage / (QuandlStockPrices.size() - 1D);

		Double volumeWeightage = QuandlStockPrice.getTradedQty() / previousVolumeAverage;
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
		signalComponents.setADXn(signalComponents.getDX());

		Double delta = new Double(IntelliInvestStore.properties.getProperty("delta"));

		// signal
		String signal = "";
		if (signalComponents.getADXn() > magicNumber
				&& (signalComponents.getPlusDIn() - signalComponents.getMinusDIn()) > delta) {
			signal = "Buy";
		} else if (signalComponents.getADXn() > magicNumber
				&& (signalComponents.getPlusDIn() - signalComponents.getMinusDIn()) < (-1 * delta)) {
			signal = "Sell";
		} else {
			if (prevSignalComponents.getSignalType().equals("Buy")
					|| prevSignalComponents.getSignalType().equals("Hold")) {
				signal = "Hold";
			} else {
				signal = "Wait";
			}
		}
		signalComponents.setSignalType(signal);

		String signalPresent = "N";
		// if(signal.equals("Buy") &&
		// (prevSignalComponents.getSignal().equals("Buy") ||
		// prevSignalComponents.getSignal().equals("Hold"))){
		// signalPresent = "N";
		// }else if(signal.equals("Hold") &&
		// (prevSignalComponents.getSignal().equals("Buy") ||
		// prevSignalComponents.getSignal().equals("Hold"))){
		// signalPresent = "N";
		// }else if(signal.equals("Sell") &&
		// (prevSignalComponents.getSignal().equals("Sell") ||
		// prevSignalComponents.getSignal().equals("Wait"))){
		// signalPresent = "N";
		// }else if(signal.equals("Wait") &&
		// (prevSignalComponents.getSignal().equals("Sell") ||
		// prevSignalComponents.getSignal().equals("Wait"))){
		// signalPresent = "N";
		// }

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
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				System.out.println(" calculated split multiplier for " + close_1 + "  " + open + "  " + splitMultiplier
						+ " is " + (splitMultiplier.doubleValue() * 1D));
			}
		} else {
			Double multiplier = open / close_1;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				System.out.println(" calculated split multiplier for " + close_1 + "  " + open + "  " + splitMultiplier
						+ " is " + (1D / splitMultiplier.doubleValue()));
			}
		}
	}

	StockSignalsDTO init(Integer magicNumber, List<QuandlStockPrice> QuandlStockPrices,
			StockSignalsDTO prevSignalComponents, List<StockSignalsDTO> signals) {
		QuandlStockPrice QuandlStockPrice = QuandlStockPrices.get(QuandlStockPrices.size() - 1);
		QuandlStockPrice QuandlStockPrice_1 = QuandlStockPrices.get(QuandlStockPrices.size() - 2);
		StockSignalsDTO signalComponents = new StockSignalsDTO();
		signalComponents.setSymbol(QuandlStockPrice.getSecurityId());
		signalComponents.setSignalDate(QuandlStockPrice.getEodDate());

		Double high_1 = QuandlStockPrice_1.getHigh() * prevSignalComponents.getSplitMultiplier();
		Double low_1 = QuandlStockPrice_1.getLow() * prevSignalComponents.getSplitMultiplier();
		Double close_1 = QuandlStockPrice_1.getClose() * prevSignalComponents.getSplitMultiplier();

		Double open = QuandlStockPrice.getOpen() * prevSignalComponents.getSplitMultiplier();

		if (open < close_1) {
			Double multiplier = close_1 / open;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				signalComponents
						.setSplitMultiplier(splitMultiplier.doubleValue() * prevSignalComponents.getSplitMultiplier());
			} else {
				signalComponents.setSplitMultiplier(prevSignalComponents.getSplitMultiplier());
			}
			// System.out.println(" calculated split multiplier for " + close_1
			// + " " + open + " " + splitMultiplier + " is " +
			// signalComponents.getSplitMultiplier());
		} else {
			Double multiplier = open / close_1;
			BigDecimal splitMultiplier = new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
			if (splitMultiplier.intValue() >= 2) {
				signalComponents
						.setSplitMultiplier(prevSignalComponents.getSplitMultiplier() / splitMultiplier.doubleValue());
			} else {
				signalComponents.setSplitMultiplier(prevSignalComponents.getSplitMultiplier());
			}
			// System.out.println(" calculated split multiplier for " + close_1
			// + " " + open + " " + splitMultiplier + " is " +
			// signalComponents.getSplitMultiplier());
		}

		Double high = QuandlStockPrice.getHigh() * signalComponents.getSplitMultiplier();
		Double low = QuandlStockPrice.getLow() * signalComponents.getSplitMultiplier();
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
		signalComponents.setTRn((prevSignalComponents.getTRn() * (ma - 1) / ma) + signalComponents.getTR());

		// plusDMn
		signalComponents
				.setPlusDMn((prevSignalComponents.getPlusDMn() * (ma - 1) / ma) + signalComponents.getPlusDM1());

		// minusDMn
		signalComponents
				.setMinusDMn((prevSignalComponents.getMinusDMn() * (ma - 1) / ma) + signalComponents.getMinusDM1());

		// plusDIn
		signalComponents.setPlusDIn(100 * (signalComponents.getPlusDMn() / signalComponents.getTRn()));

		// minusDIn
		signalComponents.setMinusDIn(100 * (signalComponents.getMinusDMn() / signalComponents.getTRn()));

		// diffDIn
		signalComponents.setDiffDIn(Math.abs(signalComponents.getPlusDIn() - signalComponents.getMinusDIn()));

		// sumDIn
		signalComponents.setSumDIn(signalComponents.getPlusDIn() + signalComponents.getMinusDIn());

		// DX
		signalComponents.setDX(100 * (signalComponents.getDiffDIn() / signalComponents.getSumDIn()));

		Double previousVolumeAverage = 0D;
		for (int i = 0; i < QuandlStockPrices.size() - 1; i++) {
			previousVolumeAverage += QuandlStockPrices.get(i).getTradedQty();
		}

		previousVolumeAverage = previousVolumeAverage / (QuandlStockPrices.size() - 1D);

		Double volumeWeightage = QuandlStockPrice.getTradedQty() / previousVolumeAverage;

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
				.setADXn(((prevSignalComponents.getADXn() * ((ma - 1D) / ma)) + (signalComponents.getDX() * (1D / ma)))
						* volumeWeightage);

		Double delta = new Double(IntelliInvestStore.properties.getProperty("delta"));
		// signal
		String signal = "";
		if (signalComponents.getADXn() > magicNumber
				&& (signalComponents.getPlusDIn() - signalComponents.getMinusDIn()) > delta) {
			signal = "Buy";
		} else if (signalComponents.getADXn() > magicNumber
				&& (signalComponents.getPlusDIn() - signalComponents.getMinusDIn()) < (-1 * delta)) {
			signal = "Sell";
		} else {
			if (prevSignalComponents.getSignalType().equals("Buy")
					|| prevSignalComponents.getSignalType().equals("Hold")) {
				signal = "Hold";
			} else {
				signal = "Wait";
			}
		}

		signalComponents.setSignalType(signal);

		String signalPresent = "Y";
		if (signal.equals("Buy") && (prevSignalComponents.getSignalType().equals("Buy")
				|| prevSignalComponents.getSignalType().equals("Hold"))) {
			signalPresent = "N";
		} else if (signal.equals("Hold") && (prevSignalComponents.getSignalType().equals("Buy")
				|| prevSignalComponents.getSignalType().equals("Hold"))) {
			signalPresent = "N";
		} else if (signal.equals("Sell") && (prevSignalComponents.getSignalType().equals("Sell")
				|| prevSignalComponents.getSignalType().equals("Wait"))) {
			signalPresent = "N";
		} else if (signal.equals("Wait") && (prevSignalComponents.getSignalType().equals("Sell")
				|| prevSignalComponents.getSignalType().equals("Wait"))) {
			signalPresent = "N";
		}

		signalComponents.setPreviousSignalType(prevSignalComponents.getSignalType());
		signalComponents.setSignalPresent(signalPresent);

		// TODO remove this hard code magic number
		setBollingerSignals(.17, QuandlStockPrices, signalComponents, prevSignalComponents);
		setOscillatorSignals(15, QuandlStockPrices, signals, signalComponents, prevSignalComponents);

		// System.out.println(signalComponents);

		return signalComponents;
	}

	boolean setBollingerSignals(Double magicNumber, List<QuandlStockPrice> QuandlStockPrices,
			StockSignalsDTO signalComponent, StockSignalsDTO prevSignalComponents) {
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
				String signalPresent = "Y";
				if (bandwidth > magicNumber && plusDIn > minusDIn)
					signal = "Buy";
				else if (bandwidth > magicNumber && plusDIn < minusDIn)
					signal = "Sell";
				else {
					if (prevSignalComponents.getBollingerSignal() != null
							&& (prevSignalComponents.getBollingerSignal().equals("Buy")
									|| prevSignalComponents.getBollingerSignal().equals("Hold"))) {
						signal = "Hold";
					} else {
						signal = "Wait";
					}
				}

				if (signal.equals("Buy") && (prevSignalComponents.getBollingerSignal() != null
						&& (prevSignalComponents.getBollingerSignal().equals("Buy")
								|| prevSignalComponents.getBollingerSignal().equals("Hold")))) {
					signalPresent = "N";
				} else if (signal.equals("Hold") && (prevSignalComponents.getBollingerSignal() != null
						&& (prevSignalComponents.getBollingerSignal().equals("Buy")
								|| prevSignalComponents.getBollingerSignal().equals("Hold")))) {
					signalPresent = "N";
				} else if (signal.equals("Sell") && (prevSignalComponents.getBollingerSignal() != null
						&& (prevSignalComponents.getBollingerSignal().equals("Sell")
								|| prevSignalComponents.getBollingerSignal().equals("Wait")))) {
					signalPresent = "N";
				} else if (signal.equals("Wait") && (prevSignalComponents.getBollingerSignal() != null
						&& (prevSignalComponents.getBollingerSignal().equals("Sell")
								|| prevSignalComponents.getBollingerSignal().equals("Wait")))) {
					signalPresent = "N";
				}
				signalComponent.setBollingerSignal(signal);
				signalComponent.setSignalPresentBollinger(signalPresent);
				signalComponent.setPreviousBollingerSignal(prevSignalComponents.getBollingerSignal());
			} else {
				logger.info("No bollinger signals has been generating for code:" + signalComponent.getSymbol() + "--"
						+ signalComponent.getSignalDate());
				signalComponent.setBollingerSignal("Wait");
				signalComponent.setSignalPresentBollinger("N");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error while setting bollinger signals..." + signalComponent.getSymbol() + "----"
					+ signalComponent.getSignalDate());
			return false;
		}

		return true;
	}

	boolean setOscillatorSignals(Integer magicNumber, List<QuandlStockPrice> QuandlStockPrices,
			List<StockSignalsDTO> prev_signals, StockSignalsDTO signalComponent, StockSignalsDTO prevSignalComponents) {
		// logger.info("Generating oscillator signal...");
		try {
			double max_high = 0;
			double lowest = Double.MAX_VALUE;

			QuandlStockPrice QuandlStockPrice = QuandlStockPrices.get(QuandlStockPrices.size() - 1);

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
			Double percentKFlow = getPercentKFlow(prev_signals.get(signals_length - 1).getStochastic10Day(),
					prev_signals.get(signals_length - 2).getStochastic10Day(), stochastic10Day);

			Double percentDFlow = null;
			if (percentKFlow != null) {
				percentDFlow = getPercentDFlow(prev_signals.get(signals_length - 1).getPercentKFlow(),
						prev_signals.get(signals_length - 2).getPercentKFlow(), percentKFlow);
			}

			signalComponent.setHigh10Day(max_high);
			signalComponent.setLow10Day(lowest);
			signalComponent.setRange10Day(range);
			signalComponent.setStochastic10Day(stochastic10Day);
			signalComponent.setPercentDFlow(percentDFlow);
			signalComponent.setPercentKFlow(percentKFlow);

			if (percentDFlow != null) {
				String signal = "";
				String signalPresent = "Y";

				if (percentDFlow > magicNumber && percentKFlow > percentDFlow)
					signal = "Buy";
				else if (percentDFlow > (100 - magicNumber) && percentKFlow < percentDFlow)
					signal = "Sell";
				else {

					if (prevSignalComponents.getOscillatorSignal() != null
							&& (prevSignalComponents.getOscillatorSignal().equals("Buy")
									|| prevSignalComponents.getOscillatorSignal().equals("Hold"))) {
						signal = "Hold";
					} else {
						signal = "Wait";
					}
				}

				if (signal.equals("Buy") && (prevSignalComponents.getOscillatorSignal() != null
						&& (prevSignalComponents.getOscillatorSignal().equals("Buy")
								|| prevSignalComponents.getOscillatorSignal().equals("Hold")))) {
					signalPresent = "N";
				} else if (signal.equals("Hold") && (prevSignalComponents.getOscillatorSignal() != null
						&& (prevSignalComponents.getOscillatorSignal().equals("Buy")
								|| prevSignalComponents.getOscillatorSignal().equals("Hold")))) {
					signalPresent = "N";
				} else if (signal.equals("Sell") && (prevSignalComponents.getOscillatorSignal() != null
						&& (prevSignalComponents.getOscillatorSignal().equals("Sell")
								|| prevSignalComponents.getOscillatorSignal().equals("Wait")))) {
					signalPresent = "N";
				} else if (signal.equals("Wait") && (prevSignalComponents.getOscillatorSignal() != null
						&& (prevSignalComponents.getOscillatorSignal().equals("Sell")
								|| prevSignalComponents.getOscillatorSignal().equals("Wait")))) {
					signalPresent = "N";
				}
				signalComponent.setOscillatorSignal(signal);
				signalComponent.setSignalPresentOscillator(signalPresent);
				signalComponent.setPreviousOscillatorSignal(prevSignalComponents.getOscillatorSignal());
			} else {
				logger.info("No oscillator signals has been generating for code:" + signalComponent.getSymbol() + "--"
						+ signalComponent.getSignalDate());
				signalComponent.setOscillatorSignal("Wait");
				signalComponent.setSignalPresentOscillator("N");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error while setting oscillator signals..." + signalComponent.getSymbol() + "----"
					+ signalComponent.getSignalDate());
			return false;
		}
		return true;
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

}
