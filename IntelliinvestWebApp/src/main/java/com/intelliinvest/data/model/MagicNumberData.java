package com.intelliinvest.data.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "MAGIC_NUMBER_DATA")
public class MagicNumberData{
	@Id
	String securityId;
	Integer movingAverage;
	Integer magicNumberADX = 45;
	Double magicNumberBollinger = .17;
	Integer magicNumberOscillator = 15;
	Double pnlADX = 0D;
	Double pnlBollinger = 0D;
	Double pnlOscillator = 0D;
	
	public MagicNumberData(String securityId, Integer movingAverage) {
		this.securityId = securityId;
		this.movingAverage = movingAverage;
	}
	
	public String getSecurityId() {
		return securityId;
	}
	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}
	public Integer getMovingAverage() {
		return movingAverage;
	}
	public void setMovingAverage(Integer movingAverage) {
		this.movingAverage = movingAverage;
	}
	public Integer getMagicNumberADX() {
		return magicNumberADX;
	}
	public void setMagicNumberADX(Integer magicNumberADX) {
		this.magicNumberADX = magicNumberADX;
	}
	public Double getMagicNumberBollinger() {
		return magicNumberBollinger;
	}
	public void setMagicNumberBollinger(Double magicNumberBollinger) {
		this.magicNumberBollinger = magicNumberBollinger;
	}
	public Integer getMagicNumberOscillator() {
		return magicNumberOscillator;
	}
	public void setMagicNumberOscillator(Integer magicNumberOscillator) {
		this.magicNumberOscillator = magicNumberOscillator;
	}
	public Double getPnlADX() {
		return pnlADX;
	}
	public void setPnlADX(Double pnlADX) {
		this.pnlADX = pnlADX;
	}
	public Double getPnlBollinger() {
		return pnlBollinger;
	}
	public void setPnlBollinger(Double pnlBollinger) {
		this.pnlBollinger = pnlBollinger;
	}
	public Double getPnlOscillator() {
		return pnlOscillator;
	}
	public void setPnlOscillator(Double pnlOscillator) {
		this.pnlOscillator = pnlOscillator;
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("MagicNumberData [")
					.append("securityId - ").append(securityId)
					.append(", movingAverage - ").append(movingAverage)
					.append(", magicNumberADX - ").append(magicNumberADX)
					.append(", magicNumberBollinger - ").append(magicNumberBollinger)
					.append(", magicNumberOscillator - ").append(magicNumberOscillator)
					.append(", pnlADX - ").append(pnlADX)
					.append(", pnlBollinger - ").append(pnlBollinger)
					.append(", pnlOscillator - ").append(pnlOscillator)
					.append("]");
		return stringBuffer.toString();
					
	}
	
}
