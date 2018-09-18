package com.bqt.test.rx.operator;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CreateOperatorActivity extends ListActivity {
	private Format format = new SimpleDateFormat("HH:mm:ss SSS", Locale.getDefault());
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"create",
				"just",
				"fromIterable",
				"fromArray",
				"empty、error、never",
				"defer",
				"timer",
				"interval、intervalRange",
				"range、rangeLong",
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	@Override
	protected void onListItemClick(ListView listView, View v, int position, long id) {
		switch (position) {
			case 0:
				Observable.create(emitter -> {
					emitter.onNext(1); //onNext:1
					emitter.onNext(2); //onNext:2
					emitter.onNext(3); //onNext:3
					emitter.onComplete(); //onComplete
				}).subscribe(s -> log("onNext:" + s), t -> log("onError"), () -> log("onComplete"));
				break;
			case 1:
				Observable.just(1, 2, 3).subscribe(s -> log("onNext:" + s), t -> log("onError"), () -> log("onComplete"));//和上面一样
				Observable.just(Arrays.asList(4, 5, 6)).subscribe(list -> log("onNext:" + list.toString()));//发送一个对象，onNext:[4, 5, 6]
				Observable.just(new int[]{7, 8, 9}).subscribe(arr -> log("onNext:" + Arrays.toString(arr)));//发送一个对象，onNext:[7, 8, 9]
				break;
			case 2:
				Observable.fromIterable(Arrays.asList(1, 2, 3)).subscribe(i -> log("*" + i));//逐个发送对象，*1 *2 *3
				break;
			case 3:
				Observable.fromArray(1, 2, 3).subscribe(i -> log("*" + i));//整个发送传入的对象，*1 *2 *3
				Observable.fromArray(new int[]{4, 5, 6}).subscribe(arr -> log(Arrays.toString(arr)));//整个发送传入的对象，[4, 5, 6]
				Observable.fromArray(new int[]{7, 8}, new int[]{9, 10}).subscribe(arr -> log(Arrays.toString(arr)));//[7, 8]  [9, 10]
				break;
			case 4:
				Observable.empty().subscribe(o -> log("onNext"),
						t -> log("onError"), () -> log("onComplete"), d -> log("onSubscribe"));//onSubscribe onComplete
				Observable.error(new Throwable("")).subscribe(o -> log("onNext"),
						t -> log("onError"), () -> log("onComplete"), d -> log("onSubscribe"));//onSubscribe onError
				Observable.never().subscribe(o -> log("onNext"),
						t -> log("onError"), () -> log("onComplete"), d -> log("onSubscribe"));//onSubscribe
				break;
			case 5:
				Observable<String> observable = Observable.defer(() -> Observable.just(format.format(new Date())));//订阅前不创建
				log("当前时间：" + format.format(new Date()));   //当前时间：21:19:12 523
				SystemClock.sleep(1000);
				observable.subscribe(l -> log("发送对象：" + l));//订阅后才会调用defer创建被观察者对象：21:19:13 028
				break;
			case 6:
				Observable.timer(1000, TimeUnit.MILLISECONDS) //延迟指定时间后，发送1个值为0的Long类型对象，默认在子线程上
						.doOnSubscribe(s -> log2("doOnSubscribe1")) //doOnSubscribe1，21:33:02 109，true
						.subscribe(l -> log2("onNext1：" + l)); //onNext1：0，21:33:03 112，false
				Observable.timer(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) //指定订阅者接收事件所在线程
						.doOnSubscribe(s -> log2("doOnSubscribe2")) //doOnSubscribe2，21:33:02 111，true
						.subscribe(l -> log2("onNext2：" + l)); //onNext2：0，21:33:03 113，true
				break;
			case 7:
				log2("开始时间"); //每次递增1，默认在子线程上，可指定线程调度器
				Observable.interval(5000, 1000, TimeUnit.MILLISECONDS)//首次延迟时间、间隔时间，
						.subscribe(l -> log2("接收的对象2：" + l)); //不会自动结束的
				Observable.intervalRange(100, 3, 0, 1, TimeUnit.SECONDS) //起始值，发送总数量
						.subscribe(l -> log2("接收的对象1：" + l), t -> log2("onError"), () -> log2("接收的对象1：onComplete"));
				break;
			case 8:
				Observable.range(10, 3).subscribe(i -> log2("" + i), t -> log2(""), () -> log2("onComplete1"));
				Observable.rangeLong(20, 2).subscribe(i -> log2("" + i), t -> log2(""), () -> log2("onComplete2"));
				break;
			case 9:
				
				break;
		}
	}
	
	private void log(String s) {
		Log.i("【bqt】", s);
	}
	
	private void log2(String s) {
		Log.i("【bqt】", s + "，" + format.format(new Date()) + "，" + (Looper.myLooper() == Looper.getMainLooper()));
	}
}