package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = "STOCK")
public class Stock implements Serializable {
	@Id
	private String securityId;
	private String bseCode;
	private String nseCode;
	private String fundamentalCode;
	private String name;	
	private String isin;
	private String industry;
	private boolean worldStock;
	private boolean niftyStock;
	private boolean nseStock;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;

	public Stock() {
		super();
	}

	public Stock(String securityId, String bseCode, String nseCode, String fundamentalCode, String name, String isin,
			String industry, boolean worldStock, boolean niftyStock, boolean nseStock, LocalDateTime updateDate) {
		super();
		this.securityId = securityId;
		this.bseCode = bseCode;
		this.nseCode = nseCode;
		this.fundamentalCode = fundamentalCode;
		this.name = name;
		this.isin = isin;
		this.industry = industry;
		this.worldStock = worldStock;
		this.niftyStock = niftyStock;
		this.nseStock = nseStock;
		this.updateDate = updateDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((securityId == null) ? 0 : securityId.hashCode());
		return result;
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public String getBseCode() {
		return bseCode;
	}

	public void setBseCode(String bseCode) {
		this.bseCode = bseCode;
	}

	public String getNseCode() {
		return nseCode;
	}

	public void setNseCode(String nseCode) {
		this.nseCode = nseCode;
	}

	public String getFundamentalCode() {
		return fundamentalCode;
	}

	public void setFundamentalCode(String fundamentalCode) {
		this.fundamentalCode = fundamentalCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public boolean isWorldStock() {
		return worldStock;
	}

	public void setWorldStock(boolean worldStock) {
		this.worldStock = worldStock;
	}

	public boolean isNiftyStock() {
		return niftyStock;
	}

	public void setNiftyStock(boolean niftyStock) {
		this.niftyStock = niftyStock;
	}

	public boolean isNseStock() {
		return nseStock;
	}

	public void setNseStock(boolean nseStock) {
		this.nseStock = nseStock;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stock other = (Stock) obj;
		if (securityId == null) {
			if (other.securityId != null)
				return false;
		} else if (!securityId.equals(other.securityId))
			return false;
		return true;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Stock(securityId, bseCode, nseCode, fundamentalCode, name, isin, industry, worldStock, niftyStock, nseStock, updateDate);
	}

}
