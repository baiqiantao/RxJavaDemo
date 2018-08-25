package com.bqt.test.rx.ui;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bqt.test.rx.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class TimingActivity extends ListActivity {
	private TextView tvTips;
	private DisposableSubscriber<Long> disposableSubscriber;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setTextColor(Color.BLUE);
		tv.setText(R.string.msg_demo_timing);
		getListView().addHeaderView(tv);
		tvTips = new TextView(this);
		tvTips.setTextColor(Color.BLUE);
		getListView().addFooterView(tvTips);
		
		String[] array = {"Flowable<Long> timer(long delay, TimeUnit unit)",
				"Flowable<Long> interval(long period, TimeUnit unit)",
				"Flowable<Long> interval(long initialDelay, long period, TimeUnit unit)",
				"Flowable<T> take(long count)",
				"doOnNext 和 Flowable<T> delay(long delay, TimeUnit unit)",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (disposableSubscriber != null && !disposableSubscriber.isDisposed()) {
			disposableSubscriber.dispose();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		tvTips.setText("");
		printWithTime("点击");
		if (disposableSubscriber != null && !disposableSubscriber.isDisposed()) {
			disposableSubscriber.dispose();
			printWithTime("dispose");
		} else {
			disposableSubscriber = new DisposableSubscriber<Long>() {
				@Override
				public void onComplete() {
					printWithTime("onComplete");
				}
				
				@Override
				public void onError(Throwable e) {
					printWithTime("onError  " + e.getMessage());
				}
				
				@Override
				public void onNext(Long number) {
					printWithTime("onNext   " + number);
				}
			};
			
			switch (position - 1) {
				case 0:
					Flowable.timer(2, TimeUnit.SECONDS)//delay: the initial delay before emitting a single 0L
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(disposableSubscriber);
					break;
				case 1:
					Flowable.interval(1, TimeUnit.SECONDS)//period: the period size in time units
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(disposableSubscriber);
					break;
				case 2:
					Flowable.interval(0, 1, TimeUnit.SECONDS) //initialDelay: the initial delay time to wait before emitting the first value of 0L
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(disposableSubscriber);
					break;
				case 3:
					Flowable.interval(1, TimeUnit.SECONDS)
							.take(5)//the maximum number of items to emit
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(disposableSubscriber);
					break;
				case 4:
					Flowable.just(20094L)
							.doOnNext(input -> printWithTime("【doOnNext】Do task A right away " + input))
							.delay(2, TimeUnit.SECONDS)//delay: the delay to shift the source by
							.observeOn(AndroidSchedulers.mainThread())//必须切到主线程才能修改TextView的内容
							
							.doOnNext(input -> printWithTime("【doOnNext】Doing Task B after a delay 【oldInput】" + input))
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(disposableSubscriber);
					break;
			}
		}
	}
	
	private void printWithTime(String tips) {
		String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault()).format(new Date());
		Log.i("bqt", date + "  " + tips);
		tvTips.append(date + "  " + tips + "\n");
	}
}