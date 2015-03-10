package com.jphb.examples.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.jph.examples.R;
import com.jph.examples.bean.Bill;

/**
 * 账单历史记录的适配器
 * @author JPH
 * @Date 2015-3-7下午10:43:10
 */
public class BillAdapter extends BaseAdapter implements PinnedSectionListAdapter{
	private static final int[] COLORS = new int[] { R.color.green_light,
		R.color.orange_light, R.color.blue_light, R.color.red_light };
	private Activity activity;
	/**服务器返回的历史账单列表**/
	private ArrayList<Bill>bills;
	/**本地分组后的账单**/
	private TreeMap<String,ArrayList<Bill>>groupBills;
	/**Adapter的数据源**/
	private ArrayList<Item>items;
	private SimpleDateFormat sdf;
	public BillAdapter(Activity activity,ArrayList<Bill> bills) {
		this.activity = activity;
		this.bills=bills;
		items=new ArrayList<Item>();
		groupBills=new TreeMap<String, ArrayList<Bill>>();
		sdf=new SimpleDateFormat("yyyy-MM", Locale.getDefault());
	}
	/**
	 * 初始化列表项
	 * @author JPH
	 */
	public void inflaterItems() {
		items.clear();
		groupBills.clear();
		for(Bill bill:bills){//遍历bills将集合中的所有数据以月份进行分类
			String groupName=sdf.format(new Date(bill.getDate()));
			if (groupBills.containsKey(groupName)) {//如果Map已经存在以该记录的日期为分组名的分组，则将该条记录插入到该分组中
				groupBills.get(groupName).add(bill);
			}else {//如果不存在，以该记录的日期作为分组名称创建分组，并将该记录插入到创建的分组中
				ArrayList<Bill>tempBills=new ArrayList<Bill>();
				tempBills.add(bill);
				groupBills.put(groupName, tempBills);
			}
		}
		
		Iterator<Entry<String, ArrayList<Bill>>>iterator=groupBills.descendingMap().entrySet().iterator();
		while(iterator.hasNext()){//将分组后的数据添加到数据源的集合中
			Entry<String, ArrayList<Bill>>entry=iterator.next();
			items.add(new Item(Item.SECTION, entry.getKey()));//将分组添加到集合中
			for(Bill bill:entry.getValue()){//将组中的数据添加到集合中
				items.add(new Item(Item.ITEM, bill));
			}
		}
	}
	@Override
	public int getCount() {
		return items.size();
	}
	@Override
	public Object getItem(int position) {
		return items.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Item item = (Item) getItem(position);
		if (item.type == Item.SECTION) {
			convertView=LayoutInflater.from(activity).inflate(R.layout.line_group_layout, null);
			convertView.setBackgroundColor(activity.getResources().getColor(COLORS[position%4]));
			TextView tvGroupName=(TextView) convertView.findViewById(R.id.tvGroupName);
			tvGroupName.setText(item.getGroupName());
		}else {
			convertView=LayoutInflater.from(activity).inflate(R.layout.line_content_layout, null);
			TextView tvTitle=(TextView) convertView.findViewById(R.id.tvTitle);
			TextView tvSum=(TextView) convertView.findViewById(R.id.tvSum);
			TextView tvDate=(TextView) convertView.findViewById(R.id.tvDateTime);
			TextView tvStatus=(TextView) convertView.findViewById(R.id.tvStatus);
			tvTitle.setText(item.getTitle());
			tvSum.setText(item.getSum());
			tvStatus.setText(item.getStatus());
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			tvDate.setText(sdf.format(new Date(item.getDate())));
		}
		return convertView;
	}
	@Override
	public int getItemViewType(int position) {
		return items.get(position).getType();
	}
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	@Override
	public boolean isItemViewTypePinned(int viewType) {
		return viewType==Item.SECTION;
	}
	@Override
	public void notifyDataSetChanged() {
		inflaterItems();//重新初始化数据
		super.notifyDataSetChanged();
	}
	public class Item extends Bill{
		public static final int ITEM = 0;
		public static final int SECTION = 1;
		private String groupName;
		public int type;
		public Item(int type,Bill bill) {
			super(bill);
			this.type=type;
		}
		public Item(int type,String groupName) {
			super(null);
			this.type=type;
			this.groupName=groupName;
		}
		public String getGroupName() {
			return groupName;
		}
		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
	}
}
