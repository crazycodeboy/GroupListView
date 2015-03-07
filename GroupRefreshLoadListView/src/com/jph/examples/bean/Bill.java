package com.jph.examples.bean;


/**
 * 账单概括实体类
 * @author JPH
 * @Date 2015-3-7下午9:49:39
 */
public class Bill {
	private int id;
	private String title;
	private String sum;
	private long date;
	private String status;
	public Bill(Bill bill){
		if(bill==null)return;
		this.id=bill.id;
		this.title=bill.title;
		this.sum=bill.sum;
		this.date=bill.date;
		this.status=bill.status;
	}
	public Bill(int id, String title, String money, long date, String status) {
		super();
		this.id = id;
		this.title = title;
		this.sum = money;
		this.date = date;
		this.status = status;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSum() {
		return sum;
	}
	public void setSum(String sum) {
		this.sum = sum;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
