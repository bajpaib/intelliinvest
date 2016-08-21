package com.intelliinvest.data.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;

@JsonAutoDetect
public class PortfolioItem implements Serializable {
	@Id
	private String portfolioItemId;
	private String code;
	private double price;
	private int quantity;
	private String direction;
	@DateTimeFormat(iso = ISO.DATE)
	private Date tradeDate;
	@Transient
	private int remainingQuantity;
	@Transient
	private double realisedPnl = 0D;
	@Transient
	private double cp = 0D;
	@Transient
	private double currentPrice = 0D;
	@Transient
	private double amount = 0D;
	@Transient
	private double totalAmount = 0D;
	@Transient
	private double unrealisedPnl = 0D;
	@Transient
	private double todaysPnl = 0D;

	public PortfolioItem() {
		super();
	}

	public PortfolioItem(String portfolioItemId, String code, double price, int quantity, int remainingQuantity,
			String direction, Date tradeDate, double realisedPnl, double cp, double currentPrice, double amount,
			double totalAmount, double unrealisedPnl, double todaysPnl) {
		super();
		this.portfolioItemId = portfolioItemId;
		this.code = code;
		this.price = price;
		this.quantity = quantity;
		this.remainingQuantity = remainingQuantity;
		this.direction = direction;
		this.tradeDate = tradeDate;
		this.realisedPnl = realisedPnl;
		this.cp = cp;
		this.currentPrice = currentPrice;
		this.amount = amount;
		this.totalAmount = totalAmount;
		this.unrealisedPnl = unrealisedPnl;
		this.todaysPnl = todaysPnl;
	}

	public String getPortfolioItemId() {
		return portfolioItemId;
	}

	public void setPortfolioItemId(String portfolioItemId) {
		this.portfolioItemId = portfolioItemId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getRemainingQuantity() {
		return remainingQuantity;
	}

	public void setRemainingQuantity(int remainingQuantity) {
		this.remainingQuantity = remainingQuantity;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(Date tradeDate) {
		this.tradeDate = tradeDate;
	}

	public double getRealisedPnl() {
		return realisedPnl;
	}

	public void setRealisedPnl(double realisedPnl) {
		this.realisedPnl = realisedPnl;
	}

	public double getCp() {
		return cp;
	}

	public void setCp(double cp) {
		this.cp = cp;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public double getUnrealisedPnl() {
		return unrealisedPnl;
	}

	public void setUnrealisedPnl(double unrealisedPnl) {
		this.unrealisedPnl = unrealisedPnl;
	}

	public double getTodaysPnl() {
		return todaysPnl;
	}

	public void setTodaysPnl(double todaysPnl) {
		this.todaysPnl = todaysPnl;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PortfolioItem other = (PortfolioItem) obj;
		if (portfolioItemId == null) {
			if (other.portfolioItemId != null)
				return false;
		} else if (!portfolioItemId.equals(other.portfolioItemId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PortfolioItem [portfolioItemId=" + portfolioItemId + ", code=" + code + ", price=" + price
				+ ", quantity=" + quantity + ", remainingQuantity=" + remainingQuantity + ", direction=" + direction
				+ ", tradeDate=" + tradeDate + ", realisedPnl=" + realisedPnl + ", cp=" + cp + ", currentPrice="
				+ currentPrice + ", amount=" + amount + ", totalAmount=" + totalAmount + ", unrealisedPnl="
				+ unrealisedPnl + ", todaysPnl=" + todaysPnl + "]";
	}

	@Override
	public PortfolioItem clone() {
		return new PortfolioItem(portfolioItemId, code, price, quantity, remainingQuantity, direction, tradeDate,
				realisedPnl, cp, currentPrice, amount, totalAmount, unrealisedPnl, todaysPnl);
	}

}
