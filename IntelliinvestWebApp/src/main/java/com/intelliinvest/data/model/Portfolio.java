package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.DateUtil;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

public class Portfolio implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	private String portfolioName;
	@JsonSerialize(using=JsonDateSerializer.class)
	private LocalDate createDate = DateUtil.getLocalDate();
	@JsonSerialize(using=JsonDateTimeSerializer.class)
	private LocalDateTime updateDate = DateUtil.getLocalDateTime();
	private Collection<PortfolioItem> portfolioItems = new ArrayList<PortfolioItem>();
	
	@Transient
	private Collection<PortfolioItem> summaryPortfolioItems = new ArrayList<PortfolioItem>();
	
	public Portfolio() {
		super();
	}
	
	public Portfolio(String portfolioName) {
		this.portfolioName = portfolioName;
	}

	public String getPortfolioName() {
		return portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}

	public LocalDate getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDate createDate) {
		this.createDate = createDate;
	}

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	public Collection<PortfolioItem> getPortfolioItems() {
		return portfolioItems;
	}

	public void setPortfolioItems(Collection<PortfolioItem> portfolioItems) {
		this.portfolioItems = portfolioItems;
	}
	
	public Collection<PortfolioItem> getSummaryPortfolioItems() {
		return summaryPortfolioItems;
	}
	
	public void setSummaryPortfolioItems(Collection<PortfolioItem> summaryPortfolioItems) {
		this.summaryPortfolioItems = summaryPortfolioItems;
	}
	
	public void addPortfolioItem(PortfolioItem portfolioItem) {
		this.portfolioItems.add(portfolioItem);
	}
	
	public void addPortfolioItems(Collection<PortfolioItem> portfolioItems) {
		this.portfolioItems.addAll(portfolioItems);
	}
	
	public List<PortfolioItem> getPortfolioItemsByCode(String stockCode) {
		List<PortfolioItem> portfolioItems = new ArrayList<PortfolioItem>();
		for (PortfolioItem portfolioItem : this.portfolioItems) {
			if (stockCode.equals(portfolioItem.getCode())) {
				portfolioItems.add(portfolioItem);
			}
		}
		return portfolioItems;
	}
	
	public PortfolioItem getPortfolioItem(String portfolioItemId) {
		for (PortfolioItem portfolioItemToCheck : this.portfolioItems) {
			if(portfolioItemId.equals(portfolioItemToCheck.getPortfolioItemId())){
				return portfolioItemToCheck;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		return portfolioName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Portfolio other = (Portfolio) obj;
		if (portfolioName == null) {
			if (other.portfolioName != null)
				return false;
		} else if (!portfolioName.equals(other.portfolioName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Portfolio [");
		builder.append("\n");
		builder.append("portfolioName=" + portfolioName);
		builder.append("\n");
		builder.append("createDate=" + createDate.toString());
		builder.append("\n");
		builder.append("updateDate=" + updateDate.toString());
		builder.append("\n");
		for (PortfolioItem item : portfolioItems) {
			builder.append("portfolioItems=" + item.toString());
			builder.append("\n");
		}
		builder.append("]");
		return builder.toString();

	}

}
