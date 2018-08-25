package com.bqt.test.rx;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bqt.test.rx.simple.SimpleActivity;
import com.bqt.test.rx.simple.SimpleActivity2;

import java.util.Arrays;

public class MainActivity extends ListActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"传统的观察者模式和使用 rx 写的观察者模式",
				"最常用的几个操作符：map、flatMap、concatMap",
				"",
				"",
				"",
				"create、just、map、subscribeOn、observeOn、subscribe、buffer、rxbinding2(RxTextView)、debounce、filter、interval、take、Retrofit系列",
				"combineLatest、timeout、error、retryWhen、range、extensions(MathFlowable)",
				"3",
				"RxBus",
				"合并：concat、concatEager、merge",
				"定时：timer、interval、take、delay",
				"网络监测",
				"测试",
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				startActivity(new Intent(this, SimpleActivity.class));
				break;
			case 1:
				startActivity(new Intent(this, SimpleActivity2.class));
				break;
//			case 1:
//				startActivity(new Intent(this, TestActivity1.class));
//				break;
//			case 2:
//				startActivity(new Intent(this, TestActivity2.class));
//				break;
//			case 3:
//				startActivity(new Intent(this, TestActivity3.class));
//				break;
//			case 4:
//				startActivity(new Intent(this, RxBusActivity.class));
//				break;
//			case 5:
//				startActivity(new Intent(this, ConcatActivity.class));
//				break;
//			case 6:
//				startActivity(new Intent(this, TimingActivity.class));
//				break;
//			case 7:
//
//				break;
			default:
				break;
		}
	}
}