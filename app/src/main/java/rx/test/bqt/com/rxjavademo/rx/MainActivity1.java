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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

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
				getString(R.string.btn_demo_polling),};
		
		tipsArray = new String[]{getString(R.string.msg_demo_concurrency_schedulers),
				getString(R.string.msg_demo_buffer),
				getString(R.string.msg_demo_debounce),
				
				getString(R.string.msg_demo_retrofit),
				getString(R.string.msg_demo_doublebinding),
				getString(R.string.msg_demo_polling),};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		_counter++;
		tv.setText(position < tipsArray.length ? tipsArray[position] : "");
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
		//简化版1
		if (_counter % 2 == 0) {
			disposable = Observable.create(emitter -> {
				Log.i("bqt", "create是否执行在主线程：" + (Looper.myLooper() == Looper.getMainLooper()));//false
				SystemClock.sleep(3000);
				emitter.onNext(new Random().nextBoolean());
			}).map(b -> {
				Log.i("bqt", "map是否执行在主线程：" + (Looper.myLooper() == Looper.getMainLooper()));//false
				SystemClock.sleep(3000);
				return b + "---";
			})
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(b -> {
						Log.i("bqt", "subscribe中收到的值：" + b);
						Log.i("bqt", "subscribe是否执行在主线程：" + (Looper.myLooper() == Looper.getMainLooper()));//true
					});
		} else {
			//简化版2
			disposable = Observable.just(true, "你好", 20094)
					.map(object -> {
						Log.i("bqt", "map2是否执行在主线程：" + (Looper.myLooper() == Looper.getMainLooper()));//false
						SystemClock.sleep(1000);
						return object;
					})
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(object -> Log.i("bqt", "subscribe2中收到的值：" + object));
		}
	}
	
	@SuppressLint("CheckResult")
	private void _1(View v) {
		disposable = RxView.clicks(v)
				.map(notification -> {
					Log.i("bqt", "接收到一次点击：" + notification.getClass());//com.jakewharton.rxbinding2.internal.Notification
					return 1;
				})
				.buffer(2, TimeUnit.SECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				//.subscribe(integers -> Log.i("bqt", "2秒内接收到的点击事件个数：" + integers.size()));
				//如果有异常但没有重写onError方法捕获异常则会直接崩溃：OnErrorNotImplementedException
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
						Log.i("bqt", "2秒内接收到的点击事件个数：" + integers.size());
					}
				});
	}
	
	private void _2() {
		EditText editText = new EditText(this);
		editText.setHint("会在停止输入后的500ms后判断内容是否有改变");
		getListView().addFooterView(editText);
		disposable = RxTextView.textChangeEvents(editText)
				.debounce(500, TimeUnit.MILLISECONDS) //会在停止输入后的500ms后判断是否有改变
				.filter(textChangeEvent -> {
					String text = textChangeEvent.text().toString();
					Log.i("bqt", "改变后的内容：" + text + "   " + textChangeEvent.getClass());
					return !TextUtils.isEmpty(text);//过滤掉空字符串
				})
				.map(textChangeEvent -> textChangeEvent.text().toString())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(text -> Log.i("bqt", "最新内容为：" + text));
	}
	
	private void _3() {
		Interceptor interceptor = chain -> {
			Request request = chain.request()
					.newBuilder()//自定义Header拼接到后面即可
					.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
					.build();
			return chain.proceed(request);
		};
		
		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(interceptor)
				.connectTimeout(5, TimeUnit.SECONDS)
				.build();
		
		Gson gson = new GsonBuilder()
				.serializeNulls()//序列化null
				.setDateFormat("yyyy-MM-dd") // 设置日期时间格式，另有2个重载方法。在序列化和反序化时均生效
				.setPrettyPrinting()//格式化输出。设置后，gson序列号后的字符串为一个格式化的字符串
				.setLenient()//默认情况下，Gson是严格的，只接受RFC 4627规定的JSON，设置后对JSON格式的要求更宽松
				.create();
		
		Retrofit retrofit = new Retrofit.Builder()
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create(gson))
				.baseUrl("https://api.github.com/")
				.client(client)
				.build();
		
		GithubApi githubApi = retrofit.create(GithubApi.class);
		
		if (_counter % 2 == 0) {
			//这垃圾老是拒绝访问：HTTP 403 Forbidden
			disposable = githubApi.contributors("square", "retrofit")
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(contributors -> {
						for (Contributor c : contributors) {
							Log.i("bqt", "onNext【" + c.login + "   " + c.contributions + "】");
						}
					});
		} else {
			disposable = githubApi.contributors("square", "retrofit")
					.flatMap(Observable::fromIterable)
					.flatMap(contributor -> {
						Observable<User> ob = githubApi.user(contributor.login).filter(user -> !isEmpty(user.name) && !isEmpty(user.email));
						return Observable.zip(ob, Observable.just(contributor), Pair::new);
					})
					.subscribeOn(Schedulers.newThread())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(pair -> {
						User user = pair.first;
						Contributor contributor = pair.second;
						Log.i("bqt", "onNext2【" + user.name + "   " + contributor.contributions + "】");
					});
		}
		compositeDisposable = new CompositeDisposable();
		compositeDisposable.add(disposable);
	}
	
	private void _4() {
		Toast.makeText(this, "没研究", Toast.LENGTH_SHORT).show();
	}
	
	private void _5() {
		disposable = Flowable.interval(0, 1000, TimeUnit.MILLISECONDS)//初始延迟，间隔，单位
				.map(attempt -> {
					long time = 1000 + 1000 * new Random().nextInt(5);
					SystemClock.sleep(time);
					return String.format("已完成第 %s 次轮询，耗时：%s ms", attempt, time);
				})
				.take(5)//轮询次数
				.doOnSubscribe(subscription -> Log.i("bqt", "【doOnSubscribe】" + subscription.getClass()))
				.subscribe(taskName -> Log.i("bqt", "【subscribe】" + taskName));
		compositeDisposable = new CompositeDisposable();
		compositeDisposable.add(disposable);
	}
}