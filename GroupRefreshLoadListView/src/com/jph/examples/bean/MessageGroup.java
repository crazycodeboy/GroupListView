package com.jph.examples.bean;

import java.util.ArrayList;

public class MessageGroup {
	/**分组名称**/
	private String groupName;
	private ArrayList<Message>messages;
	public MessageGroup() {
	}
	public MessageGroup(String groupName, ArrayList<Message> messages) {
		this.groupName = groupName;
		this.messages = messages;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public ArrayList<Message> getMessages() {
		return messages;
	}
	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}
}
