package com.bqt.test.rx.operator;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class FilterOperatorActivity extends ListActivity {
	private Format format = new SimpleDateFormat("HH:mm:ss SSS", Locale.getDefault());
	private EditText et;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		et = new EditText(this);
		String[] array = {"0、filter、ofType",
				"1、distinct、distinctUntilChanged",
				"2、ignoreElements",
				"3、throttleFirst",
				"4、throttleLast、sample",
				"5、throttleLatest",
				"6、debounce、throttleWithTimeout",
				"7、timeout",
				"8、take、takeLast",
				"9、takeUntil、takeWhile",
				"10、skip、skipLast、skipUntil、skipWhile",
				"11、first、last",
				"12、firstElement、lastElement、elementAt、firstOrError、lastOrError、elementAtOrError",
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
		getListView().addFooterView(et);
	}
	
	private int i = -1;
	
	@Override
	protected void onListItemClick(ListView listView, View v, int position, long id) {
		i++;
		switch (position) {
			case 0: //filter、ofType
				Observable.range(1, 10)
						.filter(i -> i % 3 == 0)
						.subscribe(i -> log("" + i));//3,6,9
				Observable.just(1, true, "包青天", 2, "哈哈")
						.ofType(String.class)
						.subscribe(i -> log("" + i));//包青天，哈哈
				break;
			case 1: //distinct、distinctUntilChanged
				Observable.just(1, 2, 2, 3, 4, 3, 5)
						.distinct()
						.subscribe(i -> log("" + i)); //1, 2, 3, 4, 5
				Observable.just("a", "bcd", "e", "fg", "h")
						.distinct(String::length) //通过比较函数的返回值而不是数据本身来判定两个数据是否是不同的
						.subscribe(i -> log("" + i)); //a，bcd，fg
				Observable.just(1, 2, 2, 3, 4, 3, 5)
						.distinctUntilChanged()
						.subscribe(i -> log("" + i)); //1, 2, 3, 4, 3, 5
				break;
			case 2: //ignoreElements
				Observable.create(emitter -> {
					emitter.onNext(1);
					emitter.onError(new Throwable("异常"));
				}).ignoreElements()
						.subscribe(() -> log("onComplete"), e -> log(e.getMessage())); // 异常
				break;
			case 3: //throttleFirst
				RxView.clicks(v)
						.map(o -> format.format(new Date()))
						.doOnEach(notification -> log("点击事件的时间为：" + notification.getValue()))
						.throttleFirst(1, TimeUnit.SECONDS)
						.subscribe(s -> log("防止按钮重复点击，值为：" + s));
				break;
			case 4: //throttleLast
				RxView.clicks(v)
						.map(o -> format.format(new Date()))
						.doOnEach(notification -> log("点击事件的时间为：" + notification.getValue()))
						.throttleLast(1, TimeUnit.SECONDS)
						.subscribe(s -> log("定时对数据进行抽样，值为：" + s));
				break;
			case 5: //throttleLatest
				RxView.clicks(v)
						.map(o -> format.format(new Date()))
						.doOnEach(notification -> log("点击事件的时间为：" + notification.getValue()))
						.throttleLatest(1, TimeUnit.SECONDS)
						.subscribe(s -> log("获取最接近采样点的值，值为：" + s));
				break;
			case 6: //debounce
				RxTextView.textChanges(et)
						.doOnEach(notification -> log("值为：" + notification.getValue()))
						.debounce(2000, TimeUnit.MILLISECONDS) //防抖动，去除发送频率过快的项
						.subscribe(s -> log("2秒钟内没有发生变化才发送，值为：" + s), e -> log("异常"), () -> log("完成"));
				break;
			case 7: //timeout
				Observable<Integer> observable = Observable.create(emitter -> {
					emitter.onNext(1);
					SystemClock.sleep(150L + 100L * new Random().nextInt(2));
					emitter.onNext(2);
					if (new Random().nextBoolean()) emitter.onComplete();
				});
				if (i % 3 == 0) {
					observable.timeout(200, TimeUnit.MILLISECONDS)
							.subscribe(i -> log("" + i), e -> log("超时了:" + e.getMessage()), () -> log("完成"));
				} else if (i % 3 == 1) {
					observable.timeout(200, TimeUnit.MILLISECONDS, observer -> observer.onNext(3))
							.subscribe(i -> log("" + i), e -> log("超时了:" + e.getMessage()), () -> log("完成"));
				} else {
					observable.timeout(i -> Observable.just("这里并不会发射数据给订阅者" + i).delay(200, TimeUnit.MILLISECONDS))
							.subscribe(i -> log("" + i), e -> log("超时了:" + e.getClass().getSimpleName()), () -> log("完成"));
				}
				break;
			case 8: //take、takeLast
				Observable.range(1, 5)
						.take(2)
						.subscribe(i -> log("" + i), e -> log("异常"), () -> log("完成")); //1, 2,完成
				Observable.range(11, 1)
						.take(2)
						.subscribe(i -> log("" + i), e -> log("异常"), () -> log("完成")); //11,完成
				Observable.create(emitter -> {
					emitter.onNext(21);
					emitter.onNext(22);
					SystemClock.sleep(150L + 100L * new Random().nextInt(2));
					emitter.onNext(23);
					emitter.onComplete();
				}).take(200, TimeUnit.MILLISECONDS)
						.subscribe(i -> log("" + i), e -> log("异常"), () -> log("完成")); //21, 22,完成 或 21, 22,23,完成
				break;
			case 9: //takeUntil、takeWhile
				Observable.range(10, 5)
						.flatMap(Observable::just)
						.takeUntil(l -> l >= 12) //当满足条件时原始的Observable会停止发射
						.subscribe(i -> log("" + i), e -> log("异常1"), () -> log("完成1")); //10, 11, 12, 完成
				Observable.interval(200, TimeUnit.MILLISECONDS)
						.takeUntil(Observable.just("开始发射数据时原始的Observable会停止发射").delay(500, TimeUnit.MILLISECONDS))
						.subscribe(i -> log("" + i), e -> log("异常2"), () -> log("完成2")); //0, 1,完成
				Observable.range(20, 5)
						.delay(600, TimeUnit.MILLISECONDS)
						.flatMap(Observable::just)
						.takeWhile(l -> l <= 22) //当不满足条件时原始的Observable会停止发射
						.subscribe(i -> log("" + i), e -> log("异常3"), () -> log("完成3")); //20, 21, 22, 完成
				break;
			case 10: //skip skipLast skipUntil skipWhile
				if (i % 2 == 1) {
					Observable.range(0, 5).skip(2).subscribe(i -> log("" + i)); //2.3.4
					Observable.range(10, 5).skipLast(2).subscribe(i -> log("" + i)); //10,11,12
					Observable.intervalRange(20, 5, 200, 200, TimeUnit.MILLISECONDS)
							.skip(500, TimeUnit.MILLISECONDS).subscribe(i -> log("" + i)); //22,23,24
					Observable.intervalRange(30, 5, 500, 200, TimeUnit.MILLISECONDS)
							.skipLast(500, TimeUnit.MILLISECONDS).subscribe(i -> log("" + i)); //30,31
				} else {
					Observable.intervalRange(40, 5, 200, 200, TimeUnit.MILLISECONDS)
							.skipUntil(Observable.just("开始发射数据时源Observable发射的数据才不会被丢掉").delay(500, TimeUnit.MILLISECONDS))
							.subscribe(i -> log("" + i)); //42,43,44
					Observable.range(50, 5)
							.delay(1000, TimeUnit.MILLISECONDS)
							.flatMap(Observable::just)
							.skipWhile(l -> l <= 52) //当满足条件时原始的Observable发射的数据会被丢掉
							.subscribe(i -> log("" + i)); //53,54
				}
				break;
			case 11:
				Observable.range(0, 5)
						.subscribe(i -> log("" + i)); //42,43,44
				break;
			case 12:
				break;
			case 13:
				break;
		}
	}
	
	private void log(String s) {
		Log.i("【bqt】", s);
	}
}