package com.intelliinvest.web.bo.response;

public class StockPnlData {
	private String adxPnl;
	private String bollingerPnl;
	private String oscillatorPnl;
	private String movingAveragePnl;
	private String aggPnl;
	private String securityId;

	private String msg;
	private boolean success;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getAdxPnl() {
		return adxPnl;
	}

	public void setAdxPnl(String adxPnl) {
		this.adxPnl = adxPnl;
	}

	public String getBollingerPnl() {
		return bollingerPnl;
	}

	public void setBollingerPnl(String bollingerPnl) {
		this.bollingerPnl = bollingerPnl;
	}

	public String getOscillatorPnl() {
		return oscillatorPnl;
	}

	public void setOscillatorPnl(String oscillatorPnl) {
		this.oscillatorPnl = oscillatorPnl;
	}

	public String getMovingAveragePnl() {
		return movingAveragePnl;
	}

	public void setMovingAveragePnl(String movingAveragePnl) {
		this.movingAveragePnl = movingAveragePnl;
	}

	public String getAggPnl() {
		return aggPnl;
	}

	public void setAggPnl(String aggPnl) {
		this.aggPnl = aggPnl;
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	@Override
	public String toString() {
		return "StockPnlData [adxPnl=" + adxPnl + ", bollingerPnl=" + bollingerPnl + ", oscillatorPnl=" + oscillatorPnl
				+ ", movingAveragePnl=" + movingAveragePnl + ", aggPnl=" + aggPnl + ", securityId=" + securityId
				+ ", msg=" + msg + ", success=" + success + "]";
	}

}
