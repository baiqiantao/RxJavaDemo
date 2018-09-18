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
import java.util.Arrays;
import java.util.Date;
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
				"7、buffer",
				"8、",
		};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
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
				break;
			case 7:
				Observable.just(1, 2, 3, 4, 5) //定期从被观察者发送的事件中获取一定数量的事件并放到缓存区中，然后把这些数据集合打包发射
						.buffer(3, 1) // 设置缓存区大小(每次从被观察者中获取的事件最大数量) ，步长(每次获取新事件的数量)
						.subscribe(list -> log("缓存区中事件：" + list.toString()), t -> log(""), () -> log("onComplete"));
				break;
			case 8:
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