package com.intelliinvest.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class Portfolio implements Serializable {

	@Id
	private String portfolioName;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime createDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;
	private List<PortfolioItem> portfolioItems = new ArrayList<PortfolioItem>();

	public Portfolio() {
		super();
	}

	public String getPortfolioName() {
		return portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	public List<PortfolioItem> getPortfolioItems() {
		return portfolioItems;
	}

	public void setPortfolioItems(List<PortfolioItem> portfolioItems) {
		this.portfolioItems = portfolioItems;
	}

	public void addPortfolioItem(PortfolioItem portfolioItem) {
		this.portfolioItems.add(portfolioItem);
	}

	public PortfolioItem getPortfolioItemIdById(String portfolioItemId) {
		PortfolioItem retVal = null;
		for (PortfolioItem item : portfolioItems) {
			if (portfolioItemId.equals(item.getPortfolioItemId())) {
				retVal = item;
				break;
			}
		}
		return retVal;
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
