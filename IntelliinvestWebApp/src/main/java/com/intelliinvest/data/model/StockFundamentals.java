package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = "STOCK_FUNDAMENTALS")
public class StockFundamentals implements Serializable {
	private String securityId;
	private String attrName;
	Map<String, String> yearQuarterAttrVal = new HashMap<String, String>();
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;

	public StockFundamentals() {
		super();
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}
	
	public void addYearQuarterAttrVal(String yearQuarter, String attrVal){
		yearQuarterAttrVal.put(yearQuarter, attrVal);
	}
	
	public Map<String, String> getYearQuarterAttrVal(){
		return yearQuarterAttrVal;
	}

	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attrName == null) ? 0 : attrName.hashCode());
		result = prime * result + ((securityId == null) ? 0 : securityId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockFundamentals other = (StockFundamentals) obj;
		if (attrName == null) {
			if (other.attrName != null)
				return false;
		} else if (!attrName.equals(other.attrName))
			return false;
		if (securityId == null) {
			if (other.securityId != null)
				return false;
		} else if (!securityId.equals(other.securityId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StockFundamentals [securityId=" + securityId + ", attrName=" + attrName + ", yearQuarterAttrVal="
				+ yearQuarterAttrVal + ", updateDate=" + updateDate + "]";
	}

}
