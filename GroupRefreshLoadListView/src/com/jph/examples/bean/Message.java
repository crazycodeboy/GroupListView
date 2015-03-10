package com.jph.examples.bean;

public class Message{
	private String title;
	private String content;
	private String date;
	public Message(Message message){
		if(message!=null){
			this.title=message.getTitle();
			this.content=message.getContent();
			this.date=message.getDate();
		}
	}
	public Message(String title, String content, String date) {
		this.title = title;
		this.content = content;
		this.date = date;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
}
