package com.bqt.test.rx.operator;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bqt.test.rx.observer.Person;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TransformOperatorActivity extends ListActivity {
	private static Format FORMAT = new SimpleDateFormat("HH:mm:ss SSS", Locale.getDefault());
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"0、map",
				"1、flatMap 基础用法",
				"2、flatMap和concatMap的区别",
				"3、flatMap 实现多个网络请求依次依赖",
				"4、flatMap 实现多个网络请求依次依赖简化代码",
				"5、flatMapIterable 案例1",
				"6、flatMapIterable 案例2",
				"7、buffer(int count)",
				"8、buffer(count, skip)",
				"9、buffer(timespan, unit, count)",
		};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	private int i;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		i++;
		switch (position) {
			case 0:
				Observable.just(new Date()) // Date 类型
						.map(Date::getTime) // long 类型
						.map(time -> time + 1000 * 60 * 60)// 改变 long 类型时间的值
						.map(time -> new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(time))) //String 类型
						.subscribe(this::log);
				break;
			case 1:
				Observable.just(new Person(Arrays.asList("篮球", "足球", "排球")), new Person(Arrays.asList("画画", "跳舞")))
						.map(person -> person.loves)
						.flatMap(Observable::fromIterable) //fromIterable：逐个发送集合中的元素
						.subscribe(this::log);
				break;
			case 2:
				long start = System.currentTimeMillis();
				Observable.just(Arrays.asList(1, 2, 3), Arrays.asList(4, 5))
						.flatMap(list -> Observable.fromIterable(list).delay(list.size(), TimeUnit.SECONDS))//flatMap是无序的
						.subscribe((s -> log("f:" + s)), e -> log("f"), () -> log("f耗时" + (System.currentTimeMillis() - start))); //3秒
				Observable.just(Arrays.asList("A", "B", "C"), Arrays.asList("D", "E"))
						.concatMap(list -> Observable.fromIterable(list).delay(list.size(), TimeUnit.SECONDS))//concatMap是有序的
						.subscribe(s -> log("c:" + s), e -> log("c"), () -> log("c耗时" + (System.currentTimeMillis() - start))); //5秒
				break;
			case 3:
				firstRequest("原始值：" + FORMAT.format(new Date(System.currentTimeMillis())))
						.subscribeOn(Schedulers.io()) // 在io线程进行网络请求
						.observeOn(AndroidSchedulers.mainThread()) // 在主线程处理请求结果
						.doOnNext(response -> log("【第一个网络请求结束，响应为】" + response))//true
						.observeOn(Schedulers.io()) // 回到 io 线程去处理下一个网络请求
						.flatMap(this::secondRequest)//实现多个网络请求依次依赖
						.observeOn(AndroidSchedulers.mainThread()) // 在主线程处理请求结果
						.subscribe(string -> log("【第二个网络请求结束，响应为】" + string));//true，5 秒
				break;
			case 4:
				Observable.just("包青天").delay(1000, TimeUnit.MILLISECONDS) //第一个网络请求，返回姓名
						.flatMap(s -> Observable.just(s + "，男").delay(1000, TimeUnit.MILLISECONDS)) //第二个网络请求，返回性别
						.flatMap(s -> Observable.just(s + "，28岁").delay(1000, TimeUnit.MILLISECONDS)) //第三个网络请求，返回年龄
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::log); //包青天，男，28岁，耗时:3058毫秒，true
				break;
			case 5:
				Observable.just(Arrays.asList("篮球1", "足球1"))
						.flatMap(Observable::fromIterable) //返回一个 Observable
						.subscribe(string -> log("" + string));
				Observable.just(Arrays.asList("篮球2", "足球2"))
						.flatMapIterable(list -> list) //返回一个 Iterable 而不是另一个 Observable
						.subscribe(string -> log("" + string));
				Observable.fromIterable(Arrays.asList("篮球3", "足球3")) //和上面两种方式的结果一样
						.subscribe(string -> log("" + string));
				break;
			case 6:
				Observable.just(new Person(Arrays.asList("包青天", "哈哈")), new Person(Arrays.asList("白乾涛", "你好")))
						.map(person -> person.loves)
						.flatMap(Observable::fromIterable) //返回一个 Observable
						.flatMap(string -> Observable.fromArray(string.toCharArray())) //返回一个 Observable
						.subscribe(array -> log(Arrays.toString(array)));
				Observable.just(new Person(Arrays.asList("广州", "上海")), new Person(Arrays.asList("武汉", "长沙")))
						.map(person -> person.loves)
						.flatMap(Observable::fromIterable) //返回一个 Observable
						.flatMapIterable(string -> Arrays.asList(string.toCharArray())) //返回一个 Iterable 而不是另一个 Observable
						.subscribe(array -> log(Arrays.toString(array)));
				Observable.just(new Person(Arrays.asList("你妹", "泥煤")), new Person(Arrays.asList("你美", "你没")))
						.map(person -> person.loves)
						.flatMapIterable(list -> {
									List<char[]> charList = new ArrayList<>();
									for (String string : list) {
										charList.add(string.toCharArray());
									}
									return charList; //返回一个 Iterable 而不是另一个 Observable
								}
						).subscribe(array -> log(Arrays.toString(array)));
				break;
			case 7:
				if (i % 2 == 0) {
					Observable.range(1, 5).buffer(2)  //缓存区大小，步长==缓存区大小，等价于buffer(count, count)
							.subscribe(list -> log(list.toString()), t -> log(""), () -> log("完成")); //[1, 2]，[3, 4]，[5]，完成
				} else {
					Observable.range(1, 10).buffer(10)  //将所有元素组装到集合中的效果
							.subscribe(list -> log(list.toString())); //[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
				}
				break;
			case 8:
				if (i % 3 == 0) {
					Observable.range(1, 5).buffer(3, 1) // 缓存区大小，步长；队列效果(先进先出)
							.subscribe(list -> log(list.toString()));//[1, 2, 3]，[2, 3, 4]，[3, 4, 5]，[4, 5]，[5]
				} else if (i % 3 == 1) {
					Observable.range(1, 5).buffer(5, 1) //每次剔除一个效果
							.subscribe(list -> log(list.toString()));//[1, 2, 3, 4, 5]，[2, 3, 4, 5]，[3, 4, 5]，[4, 5]，[5]
				} else {
					Observable.range(1, 5).buffer(1, 2) //只取奇数个效果
							.subscribe(list -> log(list.toString()));//[1]，[3]，[5]
				}
				break;
			case 9:
				Observable<Integer> observable = Observable.create(emitter -> {
					for (int i = 0; i < 8; i++) {
						SystemClock.sleep(100);//模拟耗时操作
						emitter.onNext(i);
					}
					emitter.onComplete();
				});
				if (i % 3 == 0) { //周期性订阅多个结果：
					observable.buffer(250, TimeUnit.MILLISECONDS) //等价于 count = Integer.MAX_VALUE
							.subscribe(list -> log("缓存区中事件：" + list.toString())); //[0, 1]，[2, 3]，[4, 5, 6]，[7]
				} else { //当达到指定时间【或】缓冲区中达到指定数量时发射
					observable.buffer(250, TimeUnit.MILLISECONDS, 2) //可以指定工作所在的线程
							.subscribe(list -> log("缓存区中事件：" + list.toString())); //[0, 1]，[]，[2, 3]，[]，[4, 5]，[6]，[7]
				}
				break;
		}
	}
	
	private Observable<String> firstRequest(String parameter) {
		return Observable.create(emitter -> {
			SystemClock.sleep(2000);//模拟网络请求
			emitter.onNext(parameter + "，第一次修改：" + FORMAT.format(new Date(System.currentTimeMillis())));
			emitter.onComplete();
		});
	}
	
	private Observable<String> secondRequest(String parameter) {
		return Observable.create(emitter -> {
			SystemClock.sleep(3000);//模拟网络请求
			emitter.onNext(parameter + "，第二次修改：" + FORMAT.format(new Date(System.currentTimeMillis())));
			emitter.onComplete();
		});
	}
	
	private void log(String s) {
		String date = new SimpleDateFormat("HH:mm:ss SSS", Locale.getDefault()).format(new Date());
		Log.i("【bqt】", s + "，" + date + "，" + (Looper.myLooper() == Looper.getMainLooper()));
	}
}