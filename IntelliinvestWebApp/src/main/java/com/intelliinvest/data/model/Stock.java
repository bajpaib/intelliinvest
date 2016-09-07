package com.intelliinvest.data.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intelliinvest.util.JsonDateTimeSerializer;

@Document(collection = Stock.COLLECTION_NAME)
public class Stock implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String COLLECTION_NAME = "STOCK";
	@Id
	private String code;
	private String name;
	private boolean worldStock;
	private boolean niftyStock;
	@JsonSerialize(using=JsonDateTimeSerializer.class)
	private LocalDateTime updateDate;

	public Stock() {
		super();
	}

	public Stock(String code, String name, boolean worldStock, boolean niftyStock, LocalDateTime updateDate) {
		super();
		this.code = code;
		this.name = name;
		this.worldStock = worldStock;
		this.niftyStock = niftyStock;
		this.updateDate = updateDate;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		if(obj instanceof String){
			Stock stock = new Stock();
			stock.setCode(obj.toString());
			obj = stock;
		}
		if (getClass() != obj.getClass())
			return false;
		Stock other = (Stock) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Stock [code=" + code + ", name=" + name + ", worldStock=" + worldStock + ", niftyStock=" + niftyStock
				+ ", updateDate=" + updateDate + "]";
	}

	@Override
	protected Stock clone() {
		return new Stock(code, name, worldStock, niftyStock, updateDate);

	}

}
