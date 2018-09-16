package com.bqt.test.rx.operator;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Operator1Activity extends ListActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"delay、delaySubscription",
				"merge、concat",
				"merge",
				"concat",
				"flatMap、concatMap",
				"flatMap",
				"zip",
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	private int i = -1;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				delay(i % 2 == 0);
				break;
			case 1:
				mergeConcat();
				break;
			case 2:
				merge();
				break;
			case 3:
				concat();
				break;
			case 4:
				flatMapConcatMap();
				break;
			case 5:
				flatMap();
				break;
			case 6:
				zip();
				break;
		}
	}
	
	private void delay(boolean onComplete) {
		log("开始");
		Observable<String> observable = Observable.create(emitter -> {
			log("create");
			emitter.onNext("包青天");
			emitter.onNext("白乾涛");
			if (onComplete) emitter.onComplete();
			else emitter.onError(new Throwable("onError"));//发送onError时不完整回调中必须指定异常回调，否则会崩溃
		});
		observable.delay(2, TimeUnit.SECONDS) //延时两秒发射，调用之后订阅者接收事件是在子线程中
				//.observeOn(AndroidSchedulers.mainThread())
				.subscribe(i -> log("d:" + i), e -> log("d:onError"), () -> log("d:onComplet"), d -> log("d:onSubscribe"));
		observable.delaySubscription(2, TimeUnit.SECONDS) //延时两秒订阅，调用之后订阅者接收事件是在子线程中
				//.observeOn(AndroidSchedulers.mainThread())
				.subscribe(i -> log("s:" + i), e -> log("s:onError"), () -> log("s:onComplete"), d -> log("s:onSubscribe"));
		log("结束");
	}
	
	private void mergeConcat() {
		Observable<String> o1 = Observable.just("【1】", "【2】").delay(1000, TimeUnit.MILLISECONDS);
		Observable<String> o2 = Observable.just("【3】", "【4】").delay(500, TimeUnit.MILLISECONDS);
		Observable.merge(o1, o2).subscribe(s -> log("merge:" + s));
		Observable.concat(o1, o2).subscribe(s -> log("concat:" + s));
	}
	
	private void merge() {
		log("开始");
		Observable<String> observable1 = Observable.just("包青天").delay(1500, TimeUnit.MILLISECONDS);//模拟网络请求
		Observable<Integer> observable2 = Observable.just(28).delay(500, TimeUnit.MILLISECONDS);
		Observable<String[]> observable3 = Observable.just(new String[]{"香蕉", "茄子"}).delay(1000, TimeUnit.MILLISECONDS);
		Observable.merge(observable1, observable2, observable3) //如果为互不相干的数据源，即不需要保证有序，则应该使用merge而不是concat
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(serializable -> {
					if (serializable instanceof String) log("姓名：" + serializable);
					else if (serializable instanceof Integer) log("年龄：" + serializable);
					else if (serializable instanceof String[]) log("购买记录：" + Arrays.toString((String[]) serializable));
				});
		log("结束");
	}
	
	private void concat() {
		long start = System.currentTimeMillis();
		Observable.concat(getObservable("内存缓存"), getObservable("磁盘缓存"), getObservable("网络缓存"))
				.first("默认数据") //the default item to emit if the source ObservableSource doesn't emit anything
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(s -> log("结果来自：" + s + "，耗时:" + (System.currentTimeMillis() - start) + "毫秒"));
	}
	
	private Observable<String> getObservable(String text) {
		return Observable.create(emitter -> {
			SystemClock.sleep(1000);//模拟耗时操作
			if (new Random().nextBoolean()) emitter.onNext(text);
			emitter.onComplete();
		});
	}
	
	private void flatMapConcatMap() {
		log("开始");
		List<String> list1 = Arrays.asList("【1】", "【2】", "【3】");
		List<String> list2 = Arrays.asList("【4】", "【5】");
		
		Observable.just(list1, list2)
				.flatMap(list -> Observable.fromIterable(list).delay(list.size(), TimeUnit.SECONDS))//flatMap是无序的
				.subscribe((s -> log("flatMap:" + s)), e -> log(""), () -> log("flatMap:onComplet")); //3秒
		
		Observable.just(list1, list2)
				.concatMap(list -> Observable.fromIterable(list).delay(list.size(), TimeUnit.SECONDS))//concatMap是有序的
				.subscribe(s -> log("concatMap:" + s), e -> log(""), () -> log("concatMap:onComplet")); //5秒
		log("结束");
	}
	
	private void flatMap() {
		long start = System.currentTimeMillis();
		Observable.just("包青天").delay(1000, TimeUnit.MILLISECONDS)
				.flatMap(s -> Observable.just(s + "，男").delay(1000, TimeUnit.MILLISECONDS))
				.flatMap(s -> Observable.just(s + "，28岁").delay(1000, TimeUnit.MILLISECONDS))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(s -> log(s + "，耗时:" + (System.currentTimeMillis() - start) + "毫秒"));
	}
	
	private void zip() {
		long start = System.currentTimeMillis();
		Observable<String> observable1 = Observable.just("包青天").delay(1500, TimeUnit.MILLISECONDS);//模拟网络请求
		Observable<Integer> observable2 = Observable.just(28).delay(500, TimeUnit.MILLISECONDS);
		Observable<String[]> observable3 = Observable.just(new String[]{"香蕉", "茄子"}).delay(1000, TimeUnit.MILLISECONDS);
		Observable.zip(observable1, observable2, observable3, MyZipBean::new)//(name, age, favors) -> new MyZipBean(name, age, favors)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(myZipBean -> log(myZipBean.toString() + "，耗时:" + (System.currentTimeMillis() - start) + "毫秒"));
	}
	
	class MyZipBean {
		String name;
		int age;
		String[] favors;
		
		MyZipBean(String name, int age, String[] favors) {
			this.name = name;
			this.age = age;
			this.favors = favors;
		}
		
		@Override
		public String toString() {
			return name + "，" + age + "，" + Arrays.toString(favors);
		}
	}
	
	private void log(String s) {
		String date = new SimpleDateFormat("HH:mm:ss SSS", Locale.getDefault()).format(new Date());
		Log.i("【bqt】", s + "，" + date + "，" + (Looper.myLooper() == Looper.getMainLooper()));
	}
}