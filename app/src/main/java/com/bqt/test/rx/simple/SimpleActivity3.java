package com.bqt.test.rx.simple;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SimpleActivity3 extends ListActivity {
	private Disposable disposable;
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
				Observable.just("救命啊")
						.doOnNext(s -> Log.i("bqt", "【doOnNext】" + currentData() + "，" + isMainThread()))
						.doOnError(e -> Log.i("bqt", "【doOnError】" + currentData() + "，" + isMainThread()))
						.doOnComplete(() -> Log.i("bqt", "【doOnComplete】 " + currentData() + "，" + isMainThread()))
						.doOnSubscribe(disposable -> Log.i("bqt", "【doOnSubscribe】" + currentData() + "，" + isMainThread()))
						.subscribe(string -> Log.i("bqt", "【onNext】" + currentData() + "，" + isMainThread()),
								e -> Log.i("bqt", "【onError】" + currentData() + "，" + isMainThread()),
								() -> Log.i("bqt", "【onComplete】" + currentData() + "，" + isMainThread()),
								disposable -> Log.i("bqt", "【onSubscribe】" + currentData() + "，" + isMainThread()));
				break;
			case 1:
				flatMap_concatMap();
				
				break;
			case 2:
				flatMap_concatMap2();
				break;
		}
	}
	
	private void flatMap_concatMap() {
		final long start1 = System.currentTimeMillis();
		disposable = Observable.just(Arrays.asList("篮球", "足球", "排球"), Arrays.asList("画画", "跳舞"))
				.flatMap(list -> Observable.fromIterable(list).delay(list.size() * 1000, TimeUnit.MILLISECONDS))
				.subscribe((string -> Log.i("bqt", "【flatMap后接收到的内容】" + string)), throwable -> {
				}, () -> {
					long time = (System.currentTimeMillis() - start1) / 1000;//flatMap是无序的
					Log.i("bqt", "flatMap历时 " + time + " 秒"); //3秒
				});
		
		final long start2 = System.currentTimeMillis();
		Log.i("bqt", "两者相隔时间为 " + (start2 - start1) + " 毫秒"); //14 毫秒左右，说明 delay 是异步的
		disposable = Observable.just(Arrays.asList("篮球", "足球", "排球"), Arrays.asList("画画", "跳舞"))
				.concatMap(list -> Observable.fromIterable(list).delay(list.size() * 1000, TimeUnit.MILLISECONDS))
				.subscribe((string -> Log.i("bqt", "【concatMap后接收到的内容】" + string)), throwable -> {
				}, () -> {
					long time = (System.currentTimeMillis() - start1) / 1000;//concatMap是有序的
					Log.i("bqt", "concatMap历时 " + time + " 秒");//5秒
				});
	}
	
	private void flatMap_concatMap2() {
		final long start1 = System.currentTimeMillis();
		disposable = Observable.just(Arrays.asList("篮球", "足球", "排球"), Arrays.asList("画画", "跳舞"))
				.subscribeOn(Schedulers.io())
				.flatMap(list -> {
					Log.i("bqt", "开始转换" + currentData() + "，" + isMainThread());
					SystemClock.sleep(list.size() * 1000);
					return Observable.fromIterable(list);
				})
				.doOnSubscribe(consumer -> Log.i("bqt", "doOnNext" + currentData() + "，" + isMainThread()))
				.doOnNext(s -> Log.i("bqt", "doOnNext" + currentData() + "，" + isMainThread()))
				.doOnComplete(() -> Log.i("bqt", "flatMap历时 " + (System.currentTimeMillis() - start1) / 1000 + " 秒")) //3秒
				.subscribe((string -> Log.i("bqt", "【flatMap后接收到的内容】" + string)));
		
		final long start2 = System.currentTimeMillis();
		Log.i("bqt", "两者相隔时间为 " + (start2 - start1) + " 毫秒"); //14 毫秒左右，说明 delay 是异步的
		disposable = Observable.just(Arrays.asList("篮球", "足球", "排球"), Arrays.asList("画画", "跳舞"))
				.concatMap(list -> Observable.fromIterable(list).delay(list.size() * 1000, TimeUnit.MILLISECONDS))
				.subscribe((string -> Log.i("bqt", "【concatMap后接收到的内容】" + string)), throwable -> {
				}, () -> {
					long time = (System.currentTimeMillis() - start1) / 1000;//concatMap是有序的
					Log.i("bqt", "concatMap历时 " + time + " 秒");//5秒
				});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (disposable != null) {
			disposable.dispose();
		}
	}
	
	private boolean isMainThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}
	
	private String currentData() {
		return FORMAT.format(new Date(System.currentTimeMillis()));
	}
}