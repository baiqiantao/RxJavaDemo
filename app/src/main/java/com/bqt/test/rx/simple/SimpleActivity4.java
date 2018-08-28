package com.bqt.test.rx.simple;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class SimpleActivity4 extends ListActivity {
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault());
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"",
				"",
				"",
				"",
				"",
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
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
	
	private boolean isMainThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}
	
	private String currentData() {
		return FORMAT.format(new Date(System.currentTimeMillis()));
	}
}