package rx.test.bqt.com.rxjavademo.rx;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.test.bqt.com.rxjavademo.R;

import static android.text.TextUtils.isEmpty;

public class MainActivity1 extends ListActivity {
	private TextView tv;
	private String[] tipsArray;
	private Disposable disposable;
	private CompositeDisposable compositeDisposable;
	private int _counter = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tv = new TextView(this);
		tv.setTextColor(Color.BLUE);
		getListView().addFooterView(tv);
		String[] array = {getString(R.string.btn_demo_schedulers),
				getString(R.string.btn_demo_buffer),
				getString(R.string.btn_demo_debounce),
				
				getString(R.string.btn_demo_retrofit),
				getString(R.string.btn_demo_double_binding_textview),
				getString(R.string.btn_demo_polling),
				"",};
		
		tipsArray = new String[]{getString(R.string.msg_demo_concurrency_schedulers),
				getString(R.string.msg_demo_buffer),
				getString(R.string.msg_demo_debounce),
				
				getString(R.string.msg_demo_retrofit),
				getString(R.string.msg_demo_doublebinding),
				getString(R.string.msg_demo_polling),
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		tv.setText(tipsArray[position]);
		switch (position) {
			case 0:
				_0();
				break;
			case 1:
				_1(v);
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
		Observable.just(true)
				.map(aBoolean -> {
					Log.i("bqt", "主线程：" + (Looper.myLooper() == Looper.getMainLooper()));//false
					SystemClock.sleep(3000);
					return aBoolean;
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new DisposableObserver<Boolean>() {
					@Override
					public void onNext(Boolean bool) {
						Log.i("bqt", "onNext：" + bool + "  主线程：" + (Looper.myLooper() == Looper.getMainLooper()));//true
					}
					
					@Override
					public void onComplete() {
						Log.i("bqt", "onComplete");
					}
					
					@Override
					public void onError(Throwable e) {
						Log.i("bqt", "onError");
					}
				});
	}
	
	@SuppressLint("CheckResult")
	private void _1(View v) {
		disposable = RxView.clicks(v)
				.map(onClickEvent -> {
					Log.i("bqt", "接收到一次点击");
					return 1;
				})
				.buffer(2, TimeUnit.SECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeWith(new DisposableObserver<List<Integer>>() {
					@Override
					public void onComplete() {
						Log.i("bqt", "onComplete");
					}
					
					@Override
					public void onError(Throwable e) {
						Log.i("bqt", "onError");
					}
					
					@Override
					public void onNext(List<Integer> integers) {
						if (integers.size() > 0) {
							Log.i("bqt", "onNext：个数=" + integers.size() + "，开始统计下一个点击周期");
						} else {
							Log.i("bqt", "onNext：没有收到点击事件");
						}
					}
				});
	}
	
	private void _2() {
		EditText editText = new EditText(this);
		editText.setHint("会在停止输入后的400ms后判断内容是否有改变");
		getListView().addFooterView(editText);
		disposable = RxTextView.textChangeEvents(editText)
				.debounce(400, TimeUnit.MILLISECONDS) //会在停止输入后的400ms后判断是否有改变
				.filter(changes -> !TextUtils.isEmpty(changes.text().toString()))//过滤掉空字符串
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeWith(new DisposableObserver<TextViewTextChangeEvent>() {
					@Override
					public void onComplete() {
						Log.i("bqt", "onComplete");
					}
					
					@Override
					public void onError(Throwable e) {
						Log.i("bqt", "onError");
					}
					
					@Override
					public void onNext(TextViewTextChangeEvent onTextChangeEvent) {
						Log.i("bqt", "onNext：" + onTextChangeEvent.text().toString());
					}
				});
	}
	
	private void _3() {
		compositeDisposable = new CompositeDisposable();
		
		Interceptor interceptor = chain -> {
			Request request = chain.request()
					.newBuilder()
					.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
					.addHeader("Accept-Encoding", "gzip, deflate")
					.addHeader("Connection", "keep-alive")
					.addHeader("Accept", "*/*")//自定义Header拼接到后面即可
					.build();
			return chain.proceed(request);
		};
		
		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(interceptor)
				.connectTimeout(5, TimeUnit.SECONDS)
				.build();
		
		Retrofit retrofit = new Retrofit.Builder()
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl("https://api.github.com")
				.client(client)
				.build();
		
		GithubApi githubApi = retrofit.create(GithubApi.class);
		
		Disposable addDisposable = githubApi.contributors("square", "retrofit")
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeWith(new DisposableObserver<List<Contributor>>() {
					@Override
					public void onComplete() {
						Log.i("bqt", "onComplete");
					}
					
					@Override
					public void onError(Throwable e) {
						Log.i("bqt", "onError:" + e.getMessage());
					}
					
					@Override
					public void onNext(List<Contributor> contributors) {
						for (Contributor c : contributors) {
							Log.i("bqt", "onNext【" + c.login + "   " + c.contributions + "】");
						}
					}
				});
		
		Disposable addDisposable2 = githubApi
				.contributors("square", "retrofit")
				.flatMap(Observable::fromIterable)
				.flatMap(contributor -> {
					Observable<User> ob = githubApi.user(contributor.login).filter(user -> !isEmpty(user.name) && !isEmpty(user.email));
					return Observable.zip(ob, Observable.just(contributor), Pair::new);
				})
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeWith(
						new DisposableObserver<Pair<User, Contributor>>() {
							@Override
							public void onComplete() {
								Log.i("bqt", "onComplete2");
							}
							
							@Override
							public void onError(Throwable e) {
								Log.i("bqt", "onError2:" + e.getMessage());
							}
							
							@Override
							public void onNext(Pair<User, Contributor> pair) {
								User user = pair.first;
								Contributor contributor = pair.second;
								Log.i("bqt", "onNext2【" + user.name + "   " + contributor.contributions + "】");
							}
						});
		
		compositeDisposable.add(addDisposable);
		compositeDisposable.add(addDisposable2);
	}
	
	private void _4() {
		Toast.makeText(this, "没研究", Toast.LENGTH_SHORT).show();
	}
	
	private void _5() {
		compositeDisposable = new CompositeDisposable();
		Disposable d = Flowable.interval(0, 1000, TimeUnit.MILLISECONDS)//初始延迟，期，单位
				.map(attempt -> {
					SystemClock.sleep(1000 * new Random().nextInt(5));
					_counter++;
					return String.valueOf(_counter);
				})
				.take(8)//轮询次数
				.doOnSubscribe(subscription -> Log.i("bqt", String.format("开始简单轮询 - %s", _counter)))
				.subscribe(taskName -> Log.i("bqt", String.format("执行轮询任务 [%s]", taskName)));
		compositeDisposable.add(d);
	}
}