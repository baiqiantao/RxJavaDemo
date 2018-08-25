package com.bqt.test.rx.simple;

import android.util.Log;

public class ConcreteWatcher implements Watcher {
	@Override
	public void update(String str) {
		Log.i("bqt", "观察者收到被观察者的消息：" + str);
	}
}