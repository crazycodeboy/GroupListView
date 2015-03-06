package com.jph.lp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jph.adapter.ListViewAdapter;
import com.jph.utils.Utils;
import com.jph.view.ListViewPlus;
import com.jph.view.ListViewPlus.ListViewPlusListener;

public class MainActivity extends Activity implements ListViewPlusListener{
	private ListViewPlus lvPlus;
	private ListViewAdapter mAdapter;
	private ArrayList<String> items = new ArrayList<String>();
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			List<String> result = (List<String>) msg.obj;
			switch (msg.what) {
			case ListViewPlus.REFRESH:
				items.clear();
				items.addAll(result);
				onLoadComplete();
				break;
			case ListViewPlus.LOAD:
				items.addAll(result);
				onLoadComplete();
				break;
			}			
			mAdapter.notifyDataSetChanged();
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		
		lvPlus = (ListViewPlus) findViewById(R.id.lvPlus);
		lvPlus.setRefreshEnable(true);
		lvPlus.setLoadEnable(true);
		initData();
		mAdapter = new ListViewAdapter(this, items);
		lvPlus.setAdapter(mAdapter);
		lvPlus.setListViewPlusListener(this);
	}
	/**
	 * 初始化数据
	 */
	private void initData() {
		loadData(ListViewPlus.REFRESH);
	}
	private void onLoadComplete() {
		lvPlus.stopRefresh();
		lvPlus.stopLoadMore();
		lvPlus.setRefreshTime(Utils.getCurrentTime());
	}	
	@Override
	public void onRefresh() {		
		loadData(ListViewPlus.REFRESH);		
	}

	@Override
	public void onLoadMore() {
		loadData(ListViewPlus.LOAD);
	}
	private void loadData(final int what) {
		// 这里模拟从服务器获取数据
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Message msg = handler.obtainMessage();
				msg.what = what;
				msg.obj = getData();
				handler.sendMessage(msg);
			}
		}).start();
	}
	// 测试数据
	public List<String> getData() {
		List<String> result = new ArrayList<String>();
		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			long l = random.nextInt(10000);
			result.add("当前条目的ID：" + l);
		}
		return result;
	}
}
