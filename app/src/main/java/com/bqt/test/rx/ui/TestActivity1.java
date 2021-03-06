package com.bqt.test.rx.ui;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bqt.test.rx.retrofit.Contributor;
import com.bqt.test.rx.retrofit.GithubApi;
import com.bqt.test.rx.retrofit.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.text.TextUtils.isEmpty;

public class TestActivity1 extends ListActivity {
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
		String[] array = {"调度和并发[schedulers & concurrency]",
				"请求累计[accumulate calls]",
				"搜索文本监听器，去抖动[debounce]",
				
				"Retrofit + RxJava",
				"双重绑定",
				"用RxJava轮询[Polling]",};
		
		tipsArray = new String[]{"这是如何将一个耗时操作转移到[offloaded to]后台线程的演示。当操作完成后，我们重新恢复到][resume back]主线程。要想真正看到这个演示的闪光点[shine]，请多次点击按钮，可以看到作为一个UI操作的按钮点击操作是永远不会被阻止的，因为耗时操作只放在后台运行",
				
				"这是演示如何使用buffer操作来累积[accumulated]事件。 重复点击下面的按钮，您会注意到日志中按钮点击收集的时间跨度为2s",
				
				"当你在输入框中输入内容时，它不会在每次输入字符更改时发出[shoot out]日志消息，而只会选择最后一个。就是当N个结点发生的时间太靠近，会自动过滤掉前N-1个结点。",
				
				"这些是 Jake Wharton 提供的例子。 真正唯一有趣的地方在于代码和日志。",
				
				"观看在您更改输入时结果如何自动更新sed。使用这样的技术，您可以在Angular Js中实现双向绑定，或者更高效地使用像MVP这样的模式。",
				"使用RxJava反复轮询或发起请求。简单轮询：在日志中注意观察一个网络请求是如何在后台重复made的",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		_counter++;
		tv.setText(position < tipsArray.length ? tipsArray[position] : "");
		switch (position) {
			case 0:

				break;
			case 1:

				break;
			case 2:

				break;
			case 3:
				_3();
				break;
			case 4:

				break;
			case 5:

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

}