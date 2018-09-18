package com.bqt.test.rx.observer;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ObserverPatternActivity extends ListActivity {
	private Watcher watcher;
	private Watched watched;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"0、被观察者注册观察者",
				"1、被观察者取消注册观察者",
				"2、被观察者通知观察者",
				"3、rx 被观察者 订阅 观察者",
				"4、rx 链式调用简化形式",
				"5、just 的扩展用法"};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
		
		watcher = new ConcreteWatcher();
		watched = new ConcreteWatched();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				watched.addWatcher(watcher);
				break;
			case 1:
				watched.removeWatcher(watcher);
				break;
			case 2:
				watched.notifyWatchers("救命啊");
				break;
			case 3:
				Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
					@Override
					public void subscribe(ObservableEmitter<String> emitter) {
						emitter.onNext("救命啊-------isDisposed=" + emitter.isDisposed());//false
						if (new Random().nextBoolean()) emitter.onComplete(); //发送onComplete
						else emitter.onError(new Throwable("发送onError")); //发送onComplete或onError事件后就取消注册了
						Log.i("bqt", "调用 onComplete 或 onError 后 isDisposed=" + emitter.isDisposed());//true
					}
				});
				
				Observer<String> observer = new Observer<String>() {
					@Override
					public void onSubscribe(Disposable d) {
						Log.i("bqt", "成功订阅-------isDisposed=" + d.isDisposed());//false
					}
					
					@Override
					public void onNext(String s) {
						Log.i("bqt", "【观察者接收到了事件】onNext:" + s);
					}
					
					@Override
					public void onError(Throwable e) {
						Log.i("bqt", "对Error事件作出响应:" + e.getMessage());
					}
					
					@Override
					public void onComplete() {
						Log.i("bqt", "对Complete事件作出响应");
					}
				};
				observable.subscribe(observer);//被观察者注册观察者
				break;
			case 4:
				Observable.just("救命啊") //快速创建并发送1个被观察者对象，相当于执行了onNext("救命啊")、onComplete()
						.subscribe(s -> Log.i("bqt", "onNext:" + s),
								e -> Log.i("bqt", "onError:" + e.getMessage()),
								() -> Log.i("bqt", "onComplete"),
								d -> Log.i("bqt", "onSubscribe" + "，isDisposed:" + d.isDisposed()));
				Observable.just("救命啊2").subscribe(s -> Log.i("bqt", "onNext:" + s), Throwable::printStackTrace);
				break;
			case 5:
				Observable.just("救命啊", 10086, true, 1.5f, new Person("包青天", 28)) //最多只能发送10个参数
						.subscribe(s -> Log.i("bqt", "onNext:" + s), Throwable::printStackTrace);
				break;
		}
	}
}