package com.bqt.test.rx.observer;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;

public class SimpleActivity extends ListActivity {
	private Watcher watcher;
	private Watched watched;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"被观察者注册观察者",
				"被观察者取消注册观察者",
				"被观察者通知观察者",
				"",
				"",
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
		watcher = new ConcreteWatcher();
		watched = new ConcreteWatched();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				watched.addWatcher(watcher);
				break;
			case 1:
				watched.removeWatcher(watcher);
				break;
			case 2:
				watched.notifyWatchers("救命啊");
				break;
		}
	}
}