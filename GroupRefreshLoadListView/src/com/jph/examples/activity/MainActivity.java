package com.jph.examples.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.hb.views.PinnedSectionListView;
import com.jph.examples.R;
import com.jph.examples.bean.Bill;
import com.jph.view.ListViewPlus;
import com.jph.view.ListViewPlus.ListViewPlusListener;
import com.jphb.examples.adapter.BillAdapter;
import com.jphb.examples.adapter.BillAdapter.Item;

/**
 * 方支付宝账单页分组+上拉加载下拉刷新效果
 * @author JPH
 * @Date 2015-3-7下午11:27:07
 */
public class MainActivity extends Activity implements ListViewPlusListener{
	private PinnedSectionListView listView;
	private BillAdapter adapter;
	private ArrayList<Bill>bills;
	private int page=1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		listView=(PinnedSectionListView) findViewById(R.id.list);
		bills=new ArrayList<Bill>();
		listView.setLoadEnable(true);
		listView.setListViewPlusListener(this);
		adapter=new BillAdapter(this, bills);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Item item=(Item) adapter.getItem(position-1);
				if(item.getType()==Item.SECTION)return;
				Toast.makeText(MainActivity.this,item.getTitle() ,Toast.LENGTH_LONG).show();
			}
		});
		loadData(ListViewPlus.REFRESH);
	}
	private void loadData(final int what) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (ListViewPlus.REFRESH==what) {
					bills.clear();
					listView.stopRefresh();
					page=1;
				}else {
					listView.stopLoadMore();
					page++;
				}
				for (int i = (page-1)*20; i <page*20; i++) {
					Bill bill=new Bill(i, "title:"+i, i*100+"元",System.currentTimeMillis()-1000L*60*60*24*12*i, "交易成功");
					bills.add(bill);
				}
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
