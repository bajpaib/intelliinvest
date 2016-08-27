package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDateTime;

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
	private LocalDateTime createDate;
	@DateTimeFormat(iso = ISO.DATE)
	private LocalDateTime updateDate;
	
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

	@Override
	public String toString() {
		return "NSEtoBSEMap [nseCode=" + nseCode + ", bseCode=" + bseCode + ", createDate=" + createDate
				+ ", updateDate=" + updateDate + "]";
	}

}
