package com.intelliinvest.common;

public class SMSObject {

	private String mob_no;
	private String messageContent;
	private String status;

	public SMSObject() {
		// TODO Auto-generated constructor stub
	}

	public SMSObject(String mob_no, String messageContent, String status) {
		super();
		this.mob_no = mob_no;
		this.messageContent = messageContent;
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMob_no() {
		return mob_no;
	}

	public void setMob_no(String mob_no) {
		this.mob_no = mob_no;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	@Override
	public String toString() {
		return "SMSObject [mob_no=" + mob_no + ", messageContent=" + messageContent + ", status=" + status + "]";
	}
}
