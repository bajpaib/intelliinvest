package com.intelliinvest.data.model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="SEQUENCE")
public class SequenceId  implements Serializable {

	@Id private String id;
	private long seq;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getSeq() {
		return seq;
	}
	public void setSeq(long seq) {
		this.seq = seq;
	}
}
