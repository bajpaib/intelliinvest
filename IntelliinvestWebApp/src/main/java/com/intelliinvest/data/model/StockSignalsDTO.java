package com.intelliinvest.data.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class StockSignalsDTO {

	String securityId;
	// String previousSignalType = "";
	String adxSignal = "Wait";

	LocalDate signalDate;
	String adxSignalPresent;

	String oscillatorSignal;
	// String previousOscillatorSignal = "";
	String signalPresentOscillator;

	String bollingerSignal;
	// String previousBollingerSignal = "";
	String signalPresentBollinger;

	Double TR;
	Double plusDM1 = 0D;
	Double minusDM1 = 0D;
	Double TRn = 0D;
	Double plusDMn = 0D;
	Double minusDMn = 0D;
	Double plusDIn = 0D;
	Double minusDIn = 0D;
	Double diffDIn = 0D;
	Double sumDIn = 0D;
	Double DX = 0D;
	Double ADXn = 0D;
	Double splitMultiplier = 0D;

	@DateTimeFormat(iso = ISO.DATE)
	// Oscillator Algo Parameters
	Double high10Day = -1D;
	Double low10Day = -1D;
	Double range10Day = -1D;
	Double stochastic10Day = -1D;
	Double percentKFlow = -1D;
	Double percentDFlow = -1D;
	// Bollinger Algo Parameters
	Double sma;
	Double upperBound;
	Double lowerBound;
	Double bandwidth;

	MovingAverageComponents movingAverageComponents;
	MovingAverageSignals movingAverageSignals;

	private String aggSignal;
	private String aggSignal_present;
	private String aggSignal_previous;

	public StockSignalsDTO() {

	}

	public StockSignalsDTO(String securityId, String signalType, LocalDate signalDate, String signalPresent,
			String oscillatorSignal, String signalPresentOscillator, String bollingerSignal,
			String signalPresentBollinger, Double tR, Double plusDM1, Double minusDM1, Double tRn, Double plusDMn,
			Double minusDMn, Double plusDIn, Double minusDIn, Double diffDIn, Double sumDIn, Double dX, Double aDXn,
			Double splitMultiplier, Double high10Day, Double low10Day, Double range10Day, Double stochastic10Day,
			Double percentKFlow, Double percentDFlow, Double sma, Double upperBound, Double lowerBound,
			Double bandwidth, MovingAverageComponents movingAverageComponents,
			MovingAverageSignals movingAverageSignals, String aggSignal, String aggSignal_present,
			String aggSignal_previous) {
		super();
		this.securityId = securityId;
		this.adxSignal = signalType;
		this.signalDate = signalDate;
		this.adxSignalPresent = signalPresent;
		this.oscillatorSignal = oscillatorSignal;
		this.signalPresentOscillator = signalPresentOscillator;
		this.bollingerSignal = bollingerSignal;
		this.signalPresentBollinger = signalPresentBollinger;
		TR = tR;
		this.plusDM1 = plusDM1;
		this.minusDM1 = minusDM1;
		TRn = tRn;
		this.plusDMn = plusDMn;
		this.minusDMn = minusDMn;
		this.plusDIn = plusDIn;
		this.minusDIn = minusDIn;
		this.diffDIn = diffDIn;
		this.sumDIn = sumDIn;
		DX = dX;
		ADXn = aDXn;
		this.splitMultiplier = splitMultiplier;
		this.high10Day = high10Day;
		this.low10Day = low10Day;
		this.range10Day = range10Day;
		this.stochastic10Day = stochastic10Day;
		this.percentKFlow = percentKFlow;
		this.percentDFlow = percentDFlow;
		this.sma = sma;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		this.bandwidth = bandwidth;
		this.movingAverageComponents = movingAverageComponents;
		this.movingAverageSignals = movingAverageSignals;
		this.aggSignal = aggSignal;
		this.aggSignal_present = aggSignal_present;
		this.aggSignal_previous = aggSignal_previous;
	}

	public String getAggSignal() {
		return aggSignal;
	}

	public void setAggSignal(String aggSignal) {
		this.aggSignal = aggSignal;
	}

	public String getAggSignal_present() {
		return aggSignal_present;
	}

	public void setAggSignal_present(String aggSignal_present) {
		this.aggSignal_present = aggSignal_present;
	}

	public String getAggSignal_previous() {
		return aggSignal_previous;
	}

	public void setAggSignal_previous(String aggSignal_previous) {
		this.aggSignal_previous = aggSignal_previous;
	}

	public MovingAverageSignals getMovingAverageSignals() {
		return movingAverageSignals;
	}

	public void setMovingAverageSignals(MovingAverageSignals movingAverageSignals) {
		this.movingAverageSignals = movingAverageSignals;
	}

	public MovingAverageComponents getMovingAverageComponents() {
		return movingAverageComponents;
	}

	public void setMovingAverageComponents(MovingAverageComponents movingAverageComponents) {
		this.movingAverageComponents = movingAverageComponents;
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	// public String getPreviousSignalType() {
	// return previousSignalType;
	// }
	//
	// public void setPreviousSignalType(String previousSignalType) {
	// this.previousSignalType = previousSignalType;
	// }

	public String getAdxSignal() {
		return adxSignal;
	}

	public void setAdxSignal(String adxSignal) {
		this.adxSignal = adxSignal;
	}

	public LocalDate getSignalDate() {
		return signalDate;
	}

	public void setSignalDate(LocalDate signalDate) {
		this.signalDate = signalDate;
	}

	public String getAdxSignalPresent() {
		return adxSignalPresent;
	}

	public void setAdxSignalPresent(String adxSignalPresent) {
		this.adxSignalPresent = adxSignalPresent;
	}

	public String getOscillatorSignal() {
		return oscillatorSignal;
	}

	public void setOscillatorSignal(String oscillatorSignal) {
		this.oscillatorSignal = oscillatorSignal;
	}

	// public String getPreviousOscillatorSignal() {
	// return previousOscillatorSignal;
	// }
	//
	// public void setPreviousOscillatorSignal(String previousOscillatorSignal)
	// {
	// this.previousOscillatorSignal = previousOscillatorSignal;
	// }

	public String getSignalPresentOscillator() {
		return signalPresentOscillator;
	}

	public void setSignalPresentOscillator(String signalPresentOscillator) {
		this.signalPresentOscillator = signalPresentOscillator;
	}

	public String getBollingerSignal() {
		return bollingerSignal;
	}

	public void setBollingerSignal(String bollingerSignal) {
		this.bollingerSignal = bollingerSignal;
	}

	// public String getPreviousBollingerSignal() {
	// return previousBollingerSignal;
	// }
	//
	// public void setPreviousBollingerSignal(String previousBollingerSignal) {
	// this.previousBollingerSignal = previousBollingerSignal;
	// }

	public String getSignalPresentBollinger() {
		return signalPresentBollinger;
	}

	public void setSignalPresentBollinger(String signalPresentBollinger) {
		this.signalPresentBollinger = signalPresentBollinger;
	}

	public Double getTR() {
		return TR;
	}

	public void setTR(Double tR) {
		TR = tR;
	}

	public Double getPlusDM1() {
		return plusDM1;
	}

	public void setPlusDM1(Double plusDM1) {
		this.plusDM1 = plusDM1;
	}

	public Double getMinusDM1() {
		return minusDM1;
	}

	public void setMinusDM1(Double minusDM1) {
		this.minusDM1 = minusDM1;
	}

	public Double getTRn() {
		return TRn;
	}

	public void setTRn(Double tRn) {
		TRn = tRn;
	}

	public Double getPlusDMn() {
		return plusDMn;
	}

	public void setPlusDMn(Double plusDMn) {
		this.plusDMn = plusDMn;
	}

	public Double getMinusDMn() {
		return minusDMn;
	}

	public void setMinusDMn(Double minusDMn) {
		this.minusDMn = minusDMn;
	}

	public Double getPlusDIn() {
		return plusDIn;
	}

	public void setPlusDIn(Double plusDIn) {
		this.plusDIn = plusDIn;
	}

	public Double getMinusDIn() {
		return minusDIn;
	}

	public void setMinusDIn(Double minusDIn) {
		this.minusDIn = minusDIn;
	}

	public Double getDiffDIn() {
		return diffDIn;
	}

	public void setDiffDIn(Double diffDIn) {
		this.diffDIn = diffDIn;
	}

	public Double getSumDIn() {
		return sumDIn;
	}

	public void setSumDIn(Double sumDIn) {
		this.sumDIn = sumDIn;
	}

	public Double getDX() {
		return DX;
	}

	public void setDX(Double dX) {
		DX = dX;
	}

	public Double getADXn() {
		return ADXn;
	}

	public void setADXn(Double aDXn) {
		ADXn = aDXn;
	}

	public Double getSplitMultiplier() {
		return splitMultiplier;
	}

	public void setSplitMultiplier(Double splitMultiplier) {
		this.splitMultiplier = splitMultiplier;
	}

	public Double getHigh10Day() {
		return high10Day;
	}

	public void setHigh10Day(Double high10Day) {
		this.high10Day = high10Day;
	}

	public Double getLow10Day() {
		return low10Day;
	}

	public void setLow10Day(Double low10Day) {
		this.low10Day = low10Day;
	}

	public Double getRange10Day() {
		return range10Day;
	}

	public void setRange10Day(Double range10Day) {
		this.range10Day = range10Day;
	}

	public Double getStochastic10Day() {
		return stochastic10Day;
	}

	public void setStochastic10Day(Double stochastic10Day) {
		this.stochastic10Day = stochastic10Day;
	}

	public Double getPercentKFlow() {
		return percentKFlow;
	}

	public void setPercentKFlow(Double percentKFlow) {
		this.percentKFlow = percentKFlow;
	}

	public Double getPercentDFlow() {
		return percentDFlow;
	}

	public void setPercentDFlow(Double percentDFlow) {
		this.percentDFlow = percentDFlow;
	}

	public Double getSma() {
		return sma;
	}

	public void setSma(Double sma) {
		this.sma = sma;
	}

	public Double getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(Double upperBound) {
		this.upperBound = upperBound;
	}

	public Double getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(Double lowerBound) {
		this.lowerBound = lowerBound;
	}

	public Double getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(Double bandwidth) {
		this.bandwidth = bandwidth;
	}

	public class MovingAverageComponents {
		private double movingAverage_5;
		private double movingAverage_10;
		private double movingAverage_15;
		private double movingAverage_25;
		private double movingAverage_50;

		public MovingAverageComponents() {
			// TODO Auto-generated constructor stub
		}

		public MovingAverageComponents(double movingAverage_5, double movingAverage_10, double movingAverage_15,
				double movingAverage_25, double movingAverage_50) {
			super();
			this.movingAverage_5 = movingAverage_5;
			this.movingAverage_10 = movingAverage_10;
			this.movingAverage_15 = movingAverage_15;
			this.movingAverage_25 = movingAverage_25;
			this.movingAverage_50 = movingAverage_50;
		}

		public double getMovingAverage_5() {
			return movingAverage_5;
		}

		public void setMovingAverage_5(double movingAverage_5) {
			this.movingAverage_5 = movingAverage_5;
		}

		public double getMovingAverage_10() {
			return movingAverage_10;
		}

		public void setMovingAverage_10(double movingAverage_10) {
			this.movingAverage_10 = movingAverage_10;
		}

		public double getMovingAverage_15() {
			return movingAverage_15;
		}

		public void setMovingAverage_15(double movingAverage_15) {
			this.movingAverage_15 = movingAverage_15;
		}

		public double getMovingAverage_25() {
			return movingAverage_25;
		}

		public void setMovingAverage_25(double movingAverage_25) {
			this.movingAverage_25 = movingAverage_25;
		}

		public double getMovingAverage_50() {
			return movingAverage_50;
		}

		public void setMovingAverage_50(double movingAverage_50) {
			this.movingAverage_50 = movingAverage_50;
		}

		@Override
		public String toString() {
			return "MovingAverageComponents [movingAverage_5=" + movingAverage_5 + ", movingAverage_10="
					+ movingAverage_10 + ", movingAverage_15=" + movingAverage_15 + ", movingAverage_25="
					+ movingAverage_25 + ", movingAverage_50=" + movingAverage_50 + "]";
		}

	}

	public class MovingAverageSignals {
		private String movingAverageSignal_SmallTerm;
		private String movingAverageSignal_Main;
		private String movingAverageSignal_MidTerm;
		private String movingAverageSignal_LongTerm;

		// private String previousMovingAverageSignal_SmallTerm;
		// private String previousMovingAverageSignal_Main;
		// private String previousMovingAverageSignal_MidTerm;
		// private String previousMovingAverageSignal_LongTerm;

		private String movingAverageSignal_SmallTerm_present;
		private String movingAverageSignal_Main_present;
		private String movingAverageSignal_MidTerm_present;
		private String movingAverageSignal_LongTerm_present;

		public MovingAverageSignals() {
			// TODO Auto-generated constructor stub
		}

		public MovingAverageSignals(String movingAverageSignal_SmallTerm, String movingAverageSignal_Main,
				String movingAverageSignal_MidTerm, String movingAverageSignal_LongTerm,
				String movingAverageSignal_SmallTerm_present, String movingAverageSignal_Main_present,
				String movingAverageSignal_MidTerm_present, String movingAverageSignal_LongTerm_present) {
			super();
			this.movingAverageSignal_SmallTerm = movingAverageSignal_SmallTerm;
			this.movingAverageSignal_Main = movingAverageSignal_Main;
			this.movingAverageSignal_MidTerm = movingAverageSignal_MidTerm;
			this.movingAverageSignal_LongTerm = movingAverageSignal_LongTerm;
			this.movingAverageSignal_SmallTerm_present = movingAverageSignal_SmallTerm_present;
			this.movingAverageSignal_Main_present = movingAverageSignal_Main_present;
			this.movingAverageSignal_MidTerm_present = movingAverageSignal_MidTerm_present;
			this.movingAverageSignal_LongTerm_present = movingAverageSignal_LongTerm_present;
		}

		public String getMovingAverageSignal_SmallTerm() {
			return movingAverageSignal_SmallTerm;
		}

		public void setMovingAverageSignal_SmallTerm(String movingAverageSignal_SmallTerm) {
			this.movingAverageSignal_SmallTerm = movingAverageSignal_SmallTerm;
		}

		public String getMovingAverageSignal_Main() {
			return movingAverageSignal_Main;
		}

		public void setMovingAverageSignal_Main(String movingAverageSignal_Main) {
			this.movingAverageSignal_Main = movingAverageSignal_Main;
		}

		public String getMovingAverageSignal_MidTerm() {
			return movingAverageSignal_MidTerm;
		}

		public void setMovingAverageSignal_MidTerm(String movingAverageSignal_MidTerm) {
			this.movingAverageSignal_MidTerm = movingAverageSignal_MidTerm;
		}

		public String getMovingAverageSignal_LongTerm() {
			return movingAverageSignal_LongTerm;
		}

		public void setMovingAverageSignal_LongTerm(String movingAverageSignal_LongTerm) {
			this.movingAverageSignal_LongTerm = movingAverageSignal_LongTerm;
		}

		// public String getPreviousMovingAverageSignal_SmallTerm() {
		// return previousMovingAverageSignal_SmallTerm;
		// }
		// public void setPreviousMovingAverageSignal_SmallTerm(
		// String previousMovingAverageSignal_SmallTerm) {
		// this.previousMovingAverageSignal_SmallTerm =
		// previousMovingAverageSignal_SmallTerm;
		// }
		// public String getPreviousMovingAverageSignal_Main() {
		// return previousMovingAverageSignal_Main;
		// }
		// public void setPreviousMovingAverageSignal_Main(
		// String previousMovingAverageSignal_Main) {
		// this.previousMovingAverageSignal_Main =
		// previousMovingAverageSignal_Main;
		// }
		// public String getPreviousMovingAverageSignal_MidTerm() {
		// return previousMovingAverageSignal_MidTerm;
		// }
		// public void setPreviousMovingAverageSignal_MidTerm(
		// String previousMovingAverageSignal_MidTerm) {
		// this.previousMovingAverageSignal_MidTerm =
		// previousMovingAverageSignal_MidTerm;
		// }
		// public String getPreviousMovingAverageSignal_LongTerm() {
		// return previousMovingAverageSignal_LongTerm;
		// }
		// public void setPreviousMovingAverageSignal_LongTerm(
		// String previousMovingAverageSignal_LongTerm) {
		// this.previousMovingAverageSignal_LongTerm =
		// previousMovingAverageSignal_LongTerm;
		// }
		public String getMovingAverageSignal_SmallTerm_present() {
			return movingAverageSignal_SmallTerm_present;
		}

		public void setMovingAverageSignal_SmallTerm_present(String movingAverageSignal_SmallTerm_present) {
			this.movingAverageSignal_SmallTerm_present = movingAverageSignal_SmallTerm_present;
		}

		public String getMovingAverageSignal_Main_present() {
			return movingAverageSignal_Main_present;
		}

		public void setMovingAverageSignal_Main_present(String movingAverageSignal_Main_present) {
			this.movingAverageSignal_Main_present = movingAverageSignal_Main_present;
		}

		public String getMovingAverageSignal_MidTerm_present() {
			return movingAverageSignal_MidTerm_present;
		}

		public void setMovingAverageSignal_MidTerm_present(String movingAverageSignal_MidTerm_present) {
			this.movingAverageSignal_MidTerm_present = movingAverageSignal_MidTerm_present;
		}

		public String getMovingAverageSignal_LongTerm_present() {
			return movingAverageSignal_LongTerm_present;
		}

		public void setMovingAverageSignal_LongTerm_present(String movingAverageSignal_LongTerm_present) {
			this.movingAverageSignal_LongTerm_present = movingAverageSignal_LongTerm_present;
		}

		@Override
		public String toString() {
			return "MovingAverageSignals [movingAverageSignal_SmallTerm=" + movingAverageSignal_SmallTerm
					+ ", movingAverageSignal_Main=" + movingAverageSignal_Main + ", movingAverageSignal_MidTerm="
					+ movingAverageSignal_MidTerm + ", movingAverageSignal_LongTerm=" + movingAverageSignal_LongTerm
					+ ", movingAverageSignal_SmallTerm_present=" + movingAverageSignal_SmallTerm_present
					+ ", movingAverageSignal_Main_present=" + movingAverageSignal_Main_present
					+ ", movingAverageSignal_MidTerm_present=" + movingAverageSignal_MidTerm_present
					+ ", movingAverageSignal_LongTerm_present=" + movingAverageSignal_LongTerm_present + "]";
		}

	}

	@Override
	public String toString() {
		return "StockSignalsDTO [securityId=" + securityId + ", adxSignal=" + adxSignal + ", signalDate=" + signalDate
				+ ", adxSignalPresent=" + adxSignalPresent + ", oscillatorSignal=" + oscillatorSignal
				+ ", signalPresentOscillator=" + signalPresentOscillator + ", bollingerSignal=" + bollingerSignal
				+ ", signalPresentBollinger=" + signalPresentBollinger + ", TR=" + TR + ", plusDM1=" + plusDM1
				+ ", minusDM1=" + minusDM1 + ", TRn=" + TRn + ", plusDMn=" + plusDMn + ", minusDMn=" + minusDMn
				+ ", plusDIn=" + plusDIn + ", minusDIn=" + minusDIn + ", diffDIn=" + diffDIn + ", sumDIn=" + sumDIn
				+ ", DX=" + DX + ", ADXn=" + ADXn + ", splitMultiplier=" + splitMultiplier + ", high10Day=" + high10Day
				+ ", low10Day=" + low10Day + ", range10Day=" + range10Day + ", stochastic10Day=" + stochastic10Day
				+ ", percentKFlow=" + percentKFlow + ", percentDFlow=" + percentDFlow + ", sma=" + sma + ", upperBound="
				+ upperBound + ", lowerBound=" + lowerBound + ", bandwidth=" + bandwidth + ", movingAverageComponents="
				+ movingAverageComponents + ", movingAverageSignals=" + movingAverageSignals + ", aggSignal="
				+ aggSignal + ", aggSignal_present=" + aggSignal_present + ", aggSignal_previous=" + aggSignal_previous
				+ "]";
	}
}
