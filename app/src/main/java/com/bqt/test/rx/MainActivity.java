package com.bqt.test.rx;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bqt.test.rx.observer.SimpleActivity;
import com.bqt.test.rx.rxbus.RxBusActivity;
import com.bqt.test.rx.ui.ConcatActivity;
import com.bqt.test.rx.ui.TestActivity1;
import com.bqt.test.rx.ui.TestActivity2;
import com.bqt.test.rx.ui.TestActivity3;
import com.bqt.test.rx.ui.TimingActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends ListActivity {
	private TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"传统的观察者模式和 rx 写的观察者模式",
				"create、just、map、subscribeOn、observeOn、subscribe、buffer、rxbinding2(RxTextView)、debounce、filter、interval、take、Retrofit系列",
				"combineLatest、timeout、error、retryWhen、range、extensions(MathFlowable)",
				"3",
				"RxBus",
				"合并：concat、concatEager、merge",
				"定时：timer、interval、take、delay",
				"网络监测",
				"测试",
				"",};
		tv = new TextView(this);
		tv.setTextColor(Color.BLUE);
		getListView().addFooterView(tv);
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				startActivity(new Intent(this, SimpleActivity.class));
				break;
			case 1:
				startActivity(new Intent(this, TestActivity1.class));
				break;
			case 2:
				startActivity(new Intent(this, TestActivity2.class));
				break;
			case 3:
				startActivity(new Intent(this, TestActivity3.class));
				break;
			case 4:
				tv.setText(R.string.msg_demo_rxbus_1);
				startActivity(new Intent(this, RxBusActivity.class));
				break;
			case 5:
				startActivity(new Intent(this, ConcatActivity.class));
				break;
			case 6:
				startActivity(new Intent(this, TimingActivity.class));
				break;
			case 7:
				
				break;
			default:
				break;
		}
	}
}