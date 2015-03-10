package com.jphb.examples.adapter;

import java.util.ArrayList;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.jph.examples.R;
import com.jph.examples.bean.BillList;
import com.jph.examples.bean.Message;
import com.jph.examples.bean.MessageGroup;

public class TestAdapter extends BaseAdapter implements PinnedSectionListAdapter{
	private static final int[] COLORS = new int[] { R.color.green_light,
		R.color.orange_light, R.color.blue_light, R.color.red_light };
	private Activity activity;
	private ArrayList<Item>items;
	public TestAdapter(Activity activity, BillList billList) {
		this.activity = activity;
		items=new ArrayList<Item>();
		inflaterItems(billList, true);
	}
	/**
	 * 初始化列表项
	 * @param billList
	 * @param isClear 是否清除原有原有数据
	 * @return
	 */
	public void inflaterItems(BillList billList,boolean isClear) {
		if (isClear) items.clear();
		if(billList==null)return;
		ArrayList<MessageGroup>messageGroups=billList.getMessageGroups();
		for(MessageGroup messageGroup:messageGroups){
			ArrayList<Message>messages=messageGroup.getMessages();
			items.add(new Item(Item.SECTION, messageGroup.getGroupName()));
			for(Message message:messages){
				items.add(new Item(Item.ITEM, message));
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
			TextView tvContent=(TextView) convertView.findViewById(R.id.tvDateTime);
			TextView tvDate=(TextView) convertView.findViewById(R.id.tvSum);
			tvTitle.setText(item.getTitle());
			tvContent.setText(item.getContent());
			tvDate.setText(item.getDate());
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
	public class Item extends Message{
		public static final int ITEM = 0;
		public static final int SECTION = 1;
		private String groupName;
		public int type;
		public Item(int type,Message message) {
			super(message);
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
