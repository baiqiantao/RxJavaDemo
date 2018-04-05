package rx.test.bqt.com.rxjavademo.rx;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.test.bqt.com.rxjavademo.R;

public class ConcatActivity extends ListActivity {
	private TextView tv;
	private TextView tvDetail;
	private String[] tipsArray;
	private Disposable disposable;
	private boolean b = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tv = new TextView(this);
		tvDetail = new TextView(this);
		tv.setTextColor(Color.BLUE);
		tvDetail.setTextColor(Color.RED);
		getListView().addFooterView(tv);
		getListView().addFooterView(tvDetail);
		
		String[] array = {"concat(前一个完成后下一个才开始)",
				"concatEager(同时开始，且顺序有保证)",
				"merge(不保证顺序)",
				"merge(保证顺序，但会丢弃会导致顺序错乱的数据)",};
		tipsArray = new String[]{getString(R.string.msg_pseudoCache_demoInfo_concat),
				getString(R.string.msg_pseudoCache_demoInfo_concatEager),
				getString(R.string.msg_pseudoCache_demoInfo_merge),
				getString(R.string.msg_pseudoCache_demoInfo_mergeOptimized),};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();//要在onDestroy或onPause中调用dispose()方法，防止内存泄漏
		if (disposable != null) {
			disposable.dispose();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		b = !b;
		tv.setText(position < tipsArray.length ? tipsArray[position] : "");
		tvDetail.setText("getCachedDiskData：" + b + "\n");
		
		Observable<Contributor> observable = null;
		switch (position) {
			case 0:
				observable = Observable.concat(b ? getCachedDiskData() : getSlowCachedDiskData(), getFreshNetworkData());
				break;
			case 1:
				List<Observable<Contributor>> observables = new ArrayList<>(2);
				observables.add(b ? getCachedDiskData() : getSlowCachedDiskData());
				observables.add(getFreshNetworkData());
				observable = Observable.concatEager(observables);
				break;
			case 2:
				observable = Observable.merge(b ? getCachedDiskData() : getSlowCachedDiskData(), getFreshNetworkData());
				break;
			case 3:
				observable = getFreshNetworkData().publish(network ->
						Observable.merge(network, b ? getCachedDiskData() : getSlowCachedDiskData().takeUntil(network)));
				break;
		}
		if (observable != null) {
			disposable = observable.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(contributor -> {
						String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault()).format(new Date());
						tvDetail.append(date + "【" + contributor.login + "--" + contributor.contributions + "】\n");
					});
		}
	}
	
	//5秒后才获取磁盘数据，用于模拟从网络获取的数据慢的情况
	private Observable<Contributor> getSlowCachedDiskData() {
		return Observable.timer(5, TimeUnit.SECONDS).flatMap(dummy -> getCachedDiskData());
	}
	
	//立即获取磁盘数据，所以肯定是比从网络获取的数据快的
	private Observable<Contributor> getCachedDiskData() {
		List<Contributor> list = new ArrayList<>();
		list.add(new Contributor("JakeWharton", 0));
		list.add(new Contributor("包青天", 10086));
		list.add(new Contributor("白乾涛", 20094));
		list.add(new Contributor("你妹的", 10010));
		
		return Observable.fromIterable(list)
				.doOnSubscribe(data -> new Handler(Looper.getMainLooper()).post(() -> {
					String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault()).format(new Date());
					tvDetail.append(date + "【磁盘缓存 doOnSubscribe】\n");
				}))
				.doOnComplete(() -> new Handler(Looper.getMainLooper()).post(() -> {
					String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault()).format(new Date());
					tvDetail.append(date + "【磁盘缓存 doOnComplete】\n");
				}));
	}
	
	//从网络获取数据
	private Observable<Contributor> getFreshNetworkData() {
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
		
		return githubApi.contributors("square", "retrofit")
				.flatMap(Observable::fromIterable)
				.doOnSubscribe(data -> new Handler(Looper.getMainLooper()).post(() -> {
					String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault()).format(new Date());
					tvDetail.append(date + "【网络数据 doOnSubscribe】\n");
				}))
				.doOnComplete(() -> new Handler(Looper.getMainLooper()).post(() -> {
					String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault()).format(new Date());
					tvDetail.append(date + "【网络数据 doOnComplete】\n");
				}));
	}
}