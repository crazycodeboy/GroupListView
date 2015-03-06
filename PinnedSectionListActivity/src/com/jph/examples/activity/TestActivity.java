package com.jph.examples.activity;

import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.hb.views.PinnedSectionListView;
import com.jph.examples.R;
import com.jph.examples.bean.BillList;
import com.jph.examples.bean.Message;
import com.jph.examples.bean.MessageGroup;
import com.jph.view.ListViewPlus;
import com.jph.view.ListViewPlus.ListViewPlusListener;
import com.jphb.examples.adapter.TestAdapter;
import com.jphb.examples.adapter.TestAdapter.Item;

public class TestActivity extends Activity implements ListViewPlusListener{
	private PinnedSectionListView listView;
	private TestAdapter adapter;
	private BillList billList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		listView=(PinnedSectionListView) findViewById(R.id.list);
		listView.setLoadEnable(true);
		listView.setListViewPlusListener(this);
		adapter=new TestAdapter(this, billList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Item item=(Item) adapter.getItem(position-1);
				if(item.getType()==Item.SECTION)return;
				Toast.makeText(TestActivity.this,item.getTitle() ,Toast.LENGTH_LONG).show();
			}
		});
		loadData(ListViewPlus.REFRESH);
	}
	private void loadData(final int what) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				ArrayList<MessageGroup> messageGroups = new ArrayList<MessageGroup>();
				if (ListViewPlus.REFRESH==what) {
					listView.stopRefresh();
				}else {
					listView.stopLoadMore();
				}
				for (int i = 0; i <10; i++) {
					ArrayList<Message>messages=new ArrayList<Message>();
					for (int j = 1; j < 10; j++) {
						messages.add(new Message(i+"-"+j, "", ""));
					}
					messageGroups.add(new MessageGroup(""+i, messages));
				}
				billList=new BillList(0, messageGroups);
				adapter.inflaterItems(billList, ListViewPlus.REFRESH==what);
				adapter.notifyDataSetChanged();
			}
		}, 1000);
	}
	@Override
	public void onRefresh() {
		loadData(ListViewPlus.REFRESH);
	}
	@Override
	public void onLoadMore() {
		loadData(ListViewPlus.LOAD);
	}
}
