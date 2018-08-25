package com.bqt.test.rx.ui;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bqt.test.rx.R;
import com.bqt.test.rx.othre.RetryWithDelay;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import hu.akarnokd.rxjava2.math.MathFlowable;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class TestActivity2 extends ListActivity {
	private TextView tvTips;
	private String[] tipsArray;
	private Disposable disposable;
	private CompositeDisposable compositeDisposable;
	private int _counter = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tvTips = new TextView(this);
		tvTips.setTextColor(Color.BLUE);
		getListView().addFooterView(tvTips);
		String[] array = {getString(R.string.btn_demo_form_validation_combinel),
				getString(R.string.btn_demo_timeout),
				getString(R.string.btn_demo_exponential_backoff),
				
				getString(R.string.btn_demo_rotation_persist),
				getString(R.string.btn_demo_volley),
				getString(R.string.btn_demo_multicastPlayground),};
		
		tipsArray = new String[]{getString(R.string.msg_demo_form_comb_latest),
				getString(R.string.msg_demo_timeout),
				getString(R.string.msg_demo_exponential_backoff),
				
				getString(R.string.btn_demo_rotation_persist),
				getString(R.string.btn_demo_volley),
				getString(R.string.btn_demo_multicastPlayground),};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		_counter++;
		tvTips.setText(position < tipsArray.length ? tipsArray[position] : "");
		printWithTime("点击");
		
		switch (position) {
			case 0:
				_0();
				break;
			case 1:
				_1();
				break;
			case 2:
				_2();
				break;
			case 3:
				_3();
				break;
			case 4:
				_4();
				break;
			case 5:
				_5();
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();//要在onDestroy或onPause中调用dispose()方法，防止内存泄漏
		if (disposable != null) {
			disposable.dispose();
		}
		if (compositeDisposable != null) {
			compositeDisposable.clear();
		}
	}
	
	private void _0() {
		EditText _email = new EditText(this);
		_email.setHint("输入有效的邮箱");
		EditText _password = new EditText(this);
		_password.setHint("password，超过8个字符");
		getListView().addFooterView(_email);
		getListView().addFooterView(_password);
		
		Flowable<CharSequence> emailObs = RxTextView.textChanges(_email).skip(1).toFlowable(BackpressureStrategy.LATEST);
		Flowable<CharSequence> numberObs = RxTextView.textChanges(_password).skip(1).toFlowable(BackpressureStrategy.LATEST);
		
		Flowable.combineLatest(emailObs, numberObs, (newEmail, newPassword) -> {
			boolean emailValid = !TextUtils.isEmpty(newEmail) && Patterns.EMAIL_ADDRESS.matcher(newEmail).matches();
			if (!emailValid) {
				_email.setError("Invalid Email!");
			}
			
			boolean passValid = !TextUtils.isEmpty(newPassword) && newPassword.length() > 8;
			if (!passValid) {
				_password.setError("Invalid Password!");
			}
			
			return emailValid && passValid;
		})
				.subscribe(new DisposableSubscriber<Boolean>() {
					@Override
					public void onNext(Boolean formValid) {
						printWithTime("onNext 是否有效：" + formValid);
					}
					
					@Override
					public void onComplete() {
						printWithTime("onComplete");
					}
					
					@Override
					public void onError(Throwable e) {
						printWithTime("onError " + e.getMessage());
					}
				});
	}
	
	private void _1() {
		Observable.create((ObservableOnSubscribe<Long>) subscriber -> {
			long time = 2500L + 1000L * new Random().nextInt(2);
			new Handler(Looper.getMainLooper()).post(() -> {
				String msg = String.format("【create】启动一个 %s s 的任务", time);
				printWithTime(msg);//注意，因为这里不是主线程，所以不能直接操作UI
			});
			SystemClock.sleep(time);
			subscriber.onNext(20094L);
			subscriber.onComplete();//没超时执行onComplete
		})
				.timeout(3, TimeUnit.SECONDS,//maximum duration between emitted items before a timeout occurs
						//the source ObservableSource modified to notify observers of a TimeoutException in case of a timeout
						Observable.create(subscriber ->
								new Handler(Looper.getMainLooper()).post(() -> {
									printWithTime("超时时执行此Observable");//注意，这里也不是主线程
									subscriber.onError(new RuntimeException("超时了呀！！"));//超时执行onError
								})))
				.subscribeOn(Schedulers.computation())//计算
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new DisposableObserver<Long>() {
					@Override
					public void onComplete() {
						printWithTime("【onComplete】");
					}
					
					@Override
					public void onError(Throwable e) {
						printWithTime("【onError】" + e.getMessage());
					}
					
					@Override
					public void onNext(Long number) {
						printWithTime("【onNext】" + number);
					}
				});
	}
	
	private void _2() {
		DisposableSubscriber<Object> disposableSubscriber =
				new DisposableSubscriber<Object>() {
					@Override
					public void onComplete() {
						printWithTime("【onComplete】");
					}
					
					@Override
					public void onError(Throwable e) {
						printWithTime("【onError】" + e.getMessage());
					}
					
					@Override
					public void onNext(Object object) {
						printWithTime("【onNext】" + object);
					}
				};
		if (_counter % 2 == 0) {
			Flowable.error(new RuntimeException("达到最大尝试次数后传给onError的异常数据"))
					.retryWhen(new RetryWithDelay(4, 1000))//用于产生一串具有你所期望规则的间隔值
					.doOnSubscribe(subscription -> printWithTime("【doOnSubscribe】最多尝试四次"))
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(disposableSubscriber);
		} else {
			Flowable.range(1, 4)//start: the value of the first Integer in the sequence 时间序列中的第一个值
					.delay(integer -> MathFlowable.sumInt(Flowable.range(1, integer))//用于产生一串具有等差数列规则的间隔值
							.flatMap(targetSecondDelay -> Flowable.just(integer).delay(targetSecondDelay, TimeUnit.SECONDS)))
					.doOnSubscribe(subscription -> printWithTime("【doOnSubscribe】延迟执行四次任务"))
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(disposableSubscriber);
		}
		compositeDisposable = new CompositeDisposable();
		compositeDisposable.add(disposableSubscriber);
	}
	
	private void _3() {
	
	}
	
	private void _4() {
	
	}
	
	private void _5() {
	
	}
	
	private void printWithTime(String tips) {
		String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault()).format(new Date());
		Log.i("bqt", date + "  " + tips);
		tvTips.append(date + "  " + tips + "\n");
	}
}