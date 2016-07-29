package com.intelliinvest.data.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Document(collection = "NSE_BSE_CODES")
public class NSEtoBSEMap implements Serializable{
	@Id
	private String nseCode;
	private String bseCode;
	@DateTimeFormat(iso = ISO.DATE)
	private Date createDate;
	@DateTimeFormat(iso = ISO.DATE)
	private Date updateDate;
	
	public NSEtoBSEMap() {
		super();
	}

	public String getNseCode() {
		return nseCode;
	}

	public void setNseCode(String nseCode) {
		this.nseCode = nseCode;
	}

	public String getBseCode() {
		return bseCode;
	}

	public void setBseCode(String bseCode) {
		this.bseCode = bseCode;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public String toString() {
		return "NSEtoBSEMap [nseCode=" + nseCode + ", bseCode=" + bseCode + ", createDate=" + createDate
				+ ", updateDate=" + updateDate + "]";
	}

}
