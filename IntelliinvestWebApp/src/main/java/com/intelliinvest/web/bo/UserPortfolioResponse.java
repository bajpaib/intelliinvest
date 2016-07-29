package com.intelliinvest.web.bo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.intelliinvest.data.model.PortfolioItem;
import com.intelliinvest.web.util.MathUtil;

@JsonAutoDetect
public class UserPortfolioResponse implements Serializable {

	private String userId;
	private String portfolioName;
	private List<PortfolioItem> portfolioItems;
	private List<PortfolioItem> portfolioSummaryItems;
	private boolean success;
	private String message;

	public UserPortfolioResponse() {
		super();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPortfolioName() {
		return portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}

	public List<PortfolioItem> getPortfolioItems() {
		return portfolioItems;
	}

	public void setPortfolioItems(List<PortfolioItem> portfolioItems) {
		this.portfolioItems = new ArrayList<PortfolioItem>();
		for (PortfolioItem temp : portfolioItems) {
			this.portfolioItems.add(new PortfolioItem(temp.getPortfolioItemId(), temp.getCode(),
					MathUtil.round(temp.getPrice()), temp.getQuantity(), temp.getRemainingQuantity(),
					temp.getDirection(), temp.getTradeDate(), MathUtil.round(temp.getRealisedPnl()),
					MathUtil.round(temp.getCp()), MathUtil.round(temp.getCurrentPrice()),
					MathUtil.round(temp.getAmount()), MathUtil.round(temp.getTotalAmount()),
					MathUtil.round(temp.getUnrealisedPnl()), MathUtil.round(temp.getTodaysPnl())));
		}
		
		this.portfolioItems.sort(new Comparator<PortfolioItem>() {		
			public int compare(PortfolioItem item1, PortfolioItem item2) {			
				if(item1.getCode()!=item2.getCode()){
					return item1.getCode().compareTo(item2.getCode());
				}else{
					return item1.getTradeDate().compareTo(item2.getTradeDate());
				}	
			}
		});
	}

	public List<PortfolioItem> getPortfolioSummaryItems() {
		return portfolioSummaryItems;
	}

	public void setPortfolioSummaryItems(List<PortfolioItem> portfolioSummaryItems) {
		this.portfolioSummaryItems = new ArrayList<PortfolioItem>();
		for (PortfolioItem temp : portfolioSummaryItems) {
			this.portfolioSummaryItems.add(new PortfolioItem(temp.getPortfolioItemId(), temp.getCode(),
					MathUtil.round(temp.getPrice()), temp.getQuantity(), temp.getRemainingQuantity(),
					temp.getDirection(), temp.getTradeDate(), MathUtil.round(temp.getRealisedPnl()),
					MathUtil.round(temp.getCp()), MathUtil.round(temp.getCurrentPrice()),
					MathUtil.round(temp.getAmount()), MathUtil.round(temp.getTotalAmount()),
					MathUtil.round(temp.getUnrealisedPnl()), MathUtil.round(temp.getTodaysPnl())));
		}

		this.portfolioSummaryItems.sort(new Comparator<PortfolioItem>() {
			public int compare(PortfolioItem item1, PortfolioItem item2) {
				return item1.getCode().compareTo(item2.getCode());			
			}
		});
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
