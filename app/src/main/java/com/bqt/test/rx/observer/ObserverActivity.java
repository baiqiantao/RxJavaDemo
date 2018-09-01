package com.bqt.test.rx.observer;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.SystemClock;
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
import io.reactivex.schedulers.Schedulers;

public class ObserverActivity extends ListActivity {
	private Watcher watcher;
	private Watched watched;
	private Disposable disposable;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"被观察者注册观察者",
				"被观察者取消注册观察者",
				"被观察者通知观察者",
				"rx 被观察者 订阅 观察者",
				"rx 链式调用形式",
				"rx 链式调用简化形式：使用just代替create，使用Consumer或Action代替Observer",
				"rx 链式调用简化形式：去掉不关注的回调",
				"just 的扩展用法",
				"调用 disposable 取消还未发送的消息"};
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
						if (new Random().nextBoolean()) emitter.onComplete();
						else emitter.onError(new Throwable("异常啦"));
						
						emitter.onNext("调用 onComplete 或 onError 后就取消订阅了，再调用其方法就没有任何意义了");
						Log.i("bqt", "调用 onComplete 或 onError 后 isDisposed=" + emitter.isDisposed());//true
					}
				});
				
				Observer<String> observer = new Observer<String>() {
					@Override
					public void onSubscribe(Disposable d) {
						Log.i("bqt", "【被观察者成功订阅了此观察者】onSubscribe-------isDisposed=" + d.isDisposed());//false
					}
					
					@Override
					public void onNext(String s) {
						Log.i("bqt", "【观察者收到被观察者的消息】onNext:" + s);
					}
					
					@Override
					public void onError(Throwable e) {
						Log.i("bqt", "onError:" + e.getMessage());
					}
					
					@Override
					public void onComplete() {
						Log.i("bqt", "【整个流程已经结束】onComplete");
					}
				};
				observable.subscribe(observer);//被观察者注册观察者
				break;
			case 4:
				Observable.create((ObservableOnSubscribe<String>) emitter -> {
					emitter.onNext("救命啊");
					emitter.onComplete();
				}).subscribe(new Observer<String>() {
					@Override
					public void onSubscribe(Disposable d) {
						Log.i("bqt", "【被观察者成功订阅了此观察者】onSubscribe");
					}
					
					@Override
					public void onNext(String s) {
						Log.i("bqt", "【观察者收到被观察者的消息】onNext:" + s);
					}
					
					@Override
					public void onError(Throwable e) {
						Log.i("bqt", "onError:" + e.getMessage());
					}
					
					@Override
					public void onComplete() {
						Log.i("bqt", "【整个流程已经结束】onComplete");
					}
				});
				break;
			case 5:
				disposable = Observable.just("救命啊") //顺序：onNext()，onError，onComplete，onSubscribe
						.subscribe(s -> Log.i("bqt", "【观察者收到被观察者的消息】onNext:" + s),
								e -> Log.i("bqt", "onError:" + e.getMessage()),
								() -> Log.i("bqt", "【整个流程已经结束】onComplete"), //只有 onComplete 用的是 Action
								d -> Log.i("bqt", "【被观察者成功订阅了此观察者】onSubscribe" + "，isDisposed:" + d.isDisposed()));
				break;
			case 6:
				disposable = Observable.just("救命啊")
						.subscribe(s -> Log.i("bqt", "【观察者收到被观察者的消息】onNext:" + s));
				Log.i("bqt", "isDisposed=" + disposable.isDisposed());//true，默认是同步执行的，当执行到这里时事件流已经结束了
				break;
			case 7:
				disposable = Observable.just("救命啊", 10086, true, 1.5f, new Person("包青天", 28)) //被观察者【发出消息】
						.subscribeOn(Schedulers.io())//放在子线程中执行
						.subscribe(s -> {
							SystemClock.sleep(1000);
							Log.i("bqt", "【观察者收到被观察者的消息】onNext:" + s); //观察者收到后【处理消息】
						});
				Log.i("bqt", "isDisposed=" + disposable.isDisposed());//false，异步执行时，当执行到这里时事件流还没有结束
				break;
			case 8:
				if (disposable != null) {
					Log.i("bqt", "isDisposed=" + disposable.isDisposed());//false 或 true
					disposable.dispose();//不管有没有 isDisposed 都可以调用 dispose
					Log.i("bqt", "isDisposed=" + disposable.isDisposed());//true
				}
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (disposable != null) {
			disposable.dispose();
		}
	}
}