package com.bqt.test.rx.operator;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Operator2Activity extends ListActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"delay、delaySubscription",
				"",
				"",
				"",
				"",
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	private int i = -1;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		i++;
		switch (position) {
			case 0:
				
				break;
			case 1:
				break;
			case 2:
				
				break;
			case 3:
				
				break;
			case 4:
				
				break;
			case 5:
				
				break;
		}
	}

	private void log(String s) {
		String date = new SimpleDateFormat("HH:mm:ss SSS", Locale.getDefault()).format(new Date());
		Log.i("【bqt】", s + "，" + date + "，" + (Looper.myLooper() == Looper.getMainLooper()));
	}
}