package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateSerializer;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = NSEtoBSEMapping.COLLECTION_NAME)
public class NSEtoBSEMapping implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final String COLLECTION_NAME = "NSE_BSE_CODES";
	@Id
	private String nseCode;
	private String bseCode;
	@JsonSerialize(using=JsonDateSerializer.class)
	private LocalDate createDate;
	@JsonSerialize(using=JsonDateTimeSerializer.class)
	private LocalDateTime updateDate;
	
	public NSEtoBSEMapping() {
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

	@Override
	public String toString() {
		return "NSEtoBSEMap [nseCode=" + nseCode + ", bseCode=" + bseCode + ", createDate=" + createDate
				+ ", updateDate=" + updateDate + "]";
	}

}
