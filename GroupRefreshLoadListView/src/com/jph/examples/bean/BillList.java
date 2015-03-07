package com.jph.examples.bean;

import java.util.ArrayList;

public class BillList {
	private int status;
	private ArrayList<MessageGroup>messageGroups;
	public BillList(int status, ArrayList<MessageGroup> messageGroups) {
		this.status = status;
		this.messageGroups = messageGroups;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public ArrayList<MessageGroup> getMessageGroups() {
		return messageGroups;
	}
	public void setMessageGroups(ArrayList<MessageGroup> messageGroups) {
		this.messageGroups = messageGroups;
	}
}
