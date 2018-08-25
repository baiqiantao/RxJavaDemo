package com.bqt.test.rx.simple;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ConcreteWatched implements Watched {
	private List<Watcher> list = new ArrayList<>();
	
	@Override
	public void addWatcher(Watcher watcher) {
		if (list.contains(watcher)) {
			Log.i("bqt", "失败，被观察者已经注册过此观察者");
		} else {
			Log.i("bqt", "成功，被观察者成功注册了此观察者");
			list.add(watcher);
		}
	}
	
	@Override
	public void removeWatcher(Watcher watcher) {
		if (list.contains(watcher)) {
			boolean success = list.remove(watcher);
			Log.i("bqt", "此观察者解除注册观察者结果：" + (success ? "成功" : "失败"));
		} else {
			Log.i("bqt", "失败，此观察者并未注册到被观察者");
		}
	}
	
	@Override
	public void notifyWatchers(String str) {
		for (Watcher watcher : list) {
			watcher.update(str);
		}
	}
}