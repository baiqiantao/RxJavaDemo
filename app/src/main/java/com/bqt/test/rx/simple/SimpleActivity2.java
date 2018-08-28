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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class SimpleActivity2 extends ListActivity {
	
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault());
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {
				"map 操作符基本用法",
				"map 操作符基本用法简化形式",
				"flatMap 操作符基本用法",
				"flatMap 操作符基本用法简化形式",
				"flatMap 操作符基本用法2",
				"本示例中使用 flatMap 没有任何意义",
				"flatMap 实现多个网络请求依次依赖",
				"flatMap 实现多个网络请求依次依赖-简化代码",
		};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				Observable.just(System.currentTimeMillis())
						.map(new Function<Long, String>() { //泛型分别表示：发送的原始数据类型，转换后新的数据类型
							@Override
							public String apply(Long time) {
								//操作符 Operators 在消息发送者 Observable(被观察者) 和 消息消费者Subscriber(观察者) 之间起到操纵消息的作用
								return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(new Date(time));
							}
						})
						.subscribe(string -> Log.i("bqt", string));//发送的是 Long 类型，接收的是 String 类型
				break;
			case 1:
				Observable.just(System.currentTimeMillis())
						.map(time -> time + 1000 * 60 * 60 * 24)//时间加一天
						.map(time -> new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(new Date(time)))
						.subscribe(string -> Log.i("bqt", string));
				break;
			case 2:
				Observable.just(Arrays.asList("篮球", "足球", "排球"), Arrays.asList("画画", "跳舞"))
						.flatMap(new Function<List<String>, ObservableSource<String>>() { //泛型分别表示：发送的数据类型，转换后新的数据类型
							@Override
							public ObservableSource<String> apply(List<String> list) throws Exception {
								Log.i("bqt", "为每一个原始对象创建一个 Observable");//创建一个Observable，并将它激活，然后用它发送事件
								return Observable.fromIterable(list).delay(list.size() * 1000, TimeUnit.MILLISECONDS);
							}
						})
						.subscribe(string -> Log.i("bqt", "【接收到的内容】" + string));//发送的所有事件最终被汇入同一个Observable
				break;
			case 3:
				Observable.just(Arrays.asList("篮球", "足球", "排球"), Arrays.asList("画画", "跳舞"))
						.flatMap(Observable::fromIterable)
						.subscribe(string -> Log.i("bqt", "【接收到的内容】" + string));
				break;
			case 4:
				Observable.just(new Person(Arrays.asList("篮球", "足球", "排球")), new Person(Arrays.asList("画画", "跳舞")))
						.map(person -> person.loves)
						.flatMap(Observable::fromIterable)
						.subscribe(string -> Log.i("bqt", "【接收到的内容】" + string));
				break;
			case 5://本示例中 map 和 flatMap 没有任何区别，在这种情况下就应该用 map，用 flatMap 的都是傻叉、垃圾、装逼犯
				Observable.just(1).map(i -> "1-值为" + i).subscribe(s -> Log.i("bqt", s));
				Observable.just(1).map(i -> "2-值为" + i).flatMap(Observable::just).subscribe(s -> Log.i("bqt", s));
				Observable.just(1).flatMap(i -> Observable.just("3-值为" + i)).subscribe(s -> Log.i("bqt", s));
				Observable.just(1).flatMap(i -> Observable.just("4-值为" + i)).map(s -> s).subscribe(s -> Log.i("bqt", s));
				break;
			case 6:
				long parameter = System.currentTimeMillis();
				Log.i("bqt", "开始请求网络，参数：" + parameter + "，" + FORMAT.format(new Date(parameter)));
				getObservable1(parameter)
						.subscribeOn(Schedulers.io()) // 在io线程进行网络请求
						.observeOn(AndroidSchedulers.mainThread()) // 在主线程处理请求结果
						.doOnNext(response -> Log.i("bqt", "第一个网络请求结束，" + response + "，" + isMainThread()))//true
						.observeOn(Schedulers.io()) // 回到 io 线程去处理下一个网络请求
						.flatMap(string -> getObservable2(string, 1000 * 60))
						.observeOn(AndroidSchedulers.mainThread()) // 在主线程处理请求结果
						.doOnNext(response -> Log.i("bqt", "第二个网络请求结束，" + response + "，" + isMainThread()))//true
						.subscribe(string -> {
							long time = (System.currentTimeMillis() - parameter) / 1000;
							Log.i("bqt", "响应结果：" + string + "：" + isMainThread() + "，耗时：" + time + " 秒");//true，5 秒
						});
				break;
			case 7:
				String input = "原始值：" + FORMAT.format(new Date(System.currentTimeMillis()));
				Observable.create((ObservableOnSubscribe<String>) emitter -> {
					SystemClock.sleep(2000);//模拟网络请求
					emitter.onNext(input + "，第一次修改：" + FORMAT.format(new Date(System.currentTimeMillis())));
					emitter.onComplete();
				}).subscribeOn(Schedulers.io())
						.observeOn(Schedulers.io())
						.flatMap(string -> Observable.create((ObservableOnSubscribe<String>) emitter -> {
							SystemClock.sleep(3000);//模拟网络请求
							emitter.onNext(string + "，第二次修改：" + FORMAT.format(new Date(System.currentTimeMillis())));
							emitter.onComplete();
						})).observeOn(AndroidSchedulers.mainThread())
						.subscribe(string -> Log.i("bqt", string + "，结果：" + FORMAT.format(new Date(System.currentTimeMillis()))));
				break;
		}
	}
	
	private Observable<String> getObservable1(long parameter) {
		return Observable.create(emitter -> {
			Log.i("bqt", "第一个网络请求参数：" + parameter + "，" + isMainThread());//false
			SystemClock.sleep(2000);//模拟网络请求
			String response = FORMAT.format(new Date(parameter));
			Log.i("bqt", "第一个网络请求响应：" + response + "，" + isMainThread());//false
			emitter.onNext(response);
			emitter.onComplete();
		});
	}
	
	private Observable<String> getObservable2(String parameter, long dealy) {
		return Observable.create(emitter -> {
			try {
				Log.i("bqt", "第二个网络请求参数：" + parameter + "，" + isMainThread());//false
				SystemClock.sleep(3000);//模拟网络请求
				String response = FORMAT.format(new Date(FORMAT.parse(parameter).getTime() + dealy));
				Log.i("bqt", "第二个网络请求响应：" + response + "，" + isMainThread());//false
				emitter.onNext(response);
				emitter.onComplete();
			} catch (ParseException e) {
				e.printStackTrace();
				emitter.onError(e);
			}
		});
	}
	
	private boolean isMainThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}
}