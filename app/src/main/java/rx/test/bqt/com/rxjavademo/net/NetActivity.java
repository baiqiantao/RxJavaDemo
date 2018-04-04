package rx.test.bqt.com.rxjavademo.net;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rx.test.bqt.com.rxjavademo.R;

public class NetActivity extends Activity {
	public static final int MESSAGE_WHAT_REFUSH_NOW_SPEED = 1;//刷新当前网速
	public static final int MESSAGE_WHAT_REFUSH_AVE_SPEED = 2;//刷新平均网速
	public static final int MESSAGE_WHAT_REFUSH_RESET = 4;//重置
	
	private static final String URL_DOWNLOAD = "http://f2.market.xiaomi.com/download/AppStore/08caf4c947ec5ded753141a9ca98e9691ad43e32d/com.tencent.wework.apk";
	
	private static final int LENGTH_REFUSH_CURRENT_SPEED = 1024 * 10;//下载多少byte内容后刷新一次实时网速
	private static final int DURATION_AVE_SPEED = 300;//过多久时间后刷新一次平均网速
	private static final int DURATION_MAXCHECK = 5 * 1000;//整个测速过程允许的最大时间
	
	private TextView tv_type, tv_now_speed, tv_ave_speed;
	private DashboardView mDashboardView;
	private Button btn;
	private boolean flag = false;
	private Handler handler = new StaticUiHandler(this);
	private Disposable pingDisposable;
	
	private long lastTotalRxBytes = 0;
	private long lastTimeStamp = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net);
		
		tv_type = findViewById(R.id.connection_type);
		tv_now_speed = findViewById(R.id.now_speed);
		tv_ave_speed = findViewById(R.id.ave_speed);
		btn = findViewById(R.id.start_btn);
		mDashboardView = findViewById(R.id.dashboard_view);
		
		btn.setOnClickListener(arg0 -> checkNetSpeed());
		checkNetSpeed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		flag = false;
		handler.removeCallbacksAndMessages(null);
		if (pingDisposable != null) {
			pingDisposable.dispose();
		}
	}
	
	/**
	 * 监测网速
	 */
	private void checkNetSpeed() {
		flag = true;
		handler.postDelayed(this::reset, DURATION_MAXCHECK);
		checkNetType();
		
		btn.setText("测试中");
		btn.setEnabled(false);
		new DownloadThread().start();
	}
	
	/**
	 * 监测网络类型
	 */
	private void checkNetType() {
		//先ping一下百度看能不能通
		pingDisposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
			boolean isSuccess = PingUtils.pingIpAddress(PingUtils.BAIDU_IP, 2);
			emitter.onNext(isSuccess);
		})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(aBoolean -> {
					//通的话再检查是什么网，或者网络不可用
					if (aBoolean) {
						ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
						if (manager != null) {
							NetworkInfo networkInfo = manager.getActiveNetworkInfo();
							if (networkInfo != null) {
								tv_type.setText(networkInfo.getTypeName());//网络类型
							} else {
								tv_type.setText("网络不可用");
							}
						} else {
							tv_type.setText("网络不可用");
						}
						//不同的话说明没网
					} else {
						tv_type.setText("无网络");
					}
				});
	}
	
	/**
	 * 复原到初始状态
	 */
	private void reset() {
		if (flag) {
			flag = false;
			btn.setText("重新测试");
			btn.setEnabled(true);
			handler.removeCallbacksAndMessages(null);
		}
	}
	
	/**
	 * 格式化文件大小
	 *
	 * @param size 文件大小
	 */
	private String formatData(long size) {
		DecimalFormat formater = new DecimalFormat("####.00");
		if (size < 1024) return size + "B";
		else if (size < Math.pow(1024, 2)) return formater.format(size * Math.pow(1024, -1)) + "KB";
		else if (size < Math.pow(1024, 3)) return formater.format(size * Math.pow(1024, -2)) + "MB";
		else if (size < Math.pow(1024, 4)) return formater.format(size * Math.pow(1024, -3)) + "GB";
		else return "";
	}
	
	private void showNetSpeed() {
		long nowTimeStamp = System.currentTimeMillis();
		long totalRxBytes = TrafficStats.getTotalRxBytes();
		if (TrafficStats.getUidRxBytes(getApplicationInfo().uid) != TrafficStats.UNSUPPORTED
				&& totalRxBytes > lastTotalRxBytes && nowTimeStamp - lastTimeStamp > 0) {
			int speed = (int) ((totalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换成秒
			lastTimeStamp = nowTimeStamp;
			lastTotalRxBytes = TrafficStats.getTotalRxBytes();
			
			tv_now_speed.setText(formatData(speed) + "/S");
			Log.i("bqt", "从系统获取的当前网速：" + formatData(speed) + "/S" + "   " + speed * 1.0f / 1024 / 1024);
			
			mDashboardView.setRealTimeValue(speed * 1.0f / 1024 / 1024);
		}
	}
	
	//region  Handler
	
	static class StaticUiHandler extends Handler {
		private SoftReference<NetActivity> mSoftReference;
		
		StaticUiHandler(NetActivity activity) {
			mSoftReference = new SoftReference<>(activity);
		}
		
		@SuppressLint("SetTextI18n")
		@Override
		public void handleMessage(Message msg) {
			NetActivity activity = mSoftReference.get();
			if (activity != null && msg != null) {
				switch (msg.what) {
					case MESSAGE_WHAT_REFUSH_NOW_SPEED:
						activity.showNetSpeed();
						break;
					case MESSAGE_WHAT_REFUSH_AVE_SPEED:
						activity.tv_ave_speed.setText(activity.formatData((int) msg.obj) + "/S");
						break;
					case MESSAGE_WHAT_REFUSH_RESET:
						activity.reset();
						break;
				}
			}
		}
	}
	//endregion
	
	//region  子线程
	
	/**
	 * 下载资源，下载过程中，根据已下载长度、总长度、时间计算实时网速
	 */
	class DownloadThread extends Thread {
		@Override
		public void run() {
			try {
				URLConnection connection = new URL(URL_DOWNLOAD).openConnection();
				InputStream inputStream = connection.getInputStream();
				
				long startTime = System.currentTimeMillis();//开始时间
				long allUsedTime;//已经使用的时长
				long tempUsedTime = 0, endReadArrayTime = 0, arrayUsedTime;
				
				int totalByte = connection.getContentLength();//总长度
				Log.i("bqt", "总长度：" + formatData(totalByte));
				
				int currentSpeed, aveSpeed;//当前网速和平均网速
				int temLen, downloadLen = 0;//已下载的长度
				byte[] buf = new byte[LENGTH_REFUSH_CURRENT_SPEED];
				
				while ((temLen = inputStream.read(buf)) != -1 && flag) {
					//刷新实时网速
					arrayUsedTime = System.currentTimeMillis() - endReadArrayTime;
					if (arrayUsedTime > 0) {//防止分母为零时报ArithmeticException
						currentSpeed = (int) (temLen / arrayUsedTime) * 1000;//当前网速
						Log.i("bqt", "自己计算的当前的网速为" + formatData(currentSpeed) + "/S");
						
						//刷新实时网速。ps：如果要根据时间来刷新实时网速，请采用类似平均网速的方式计算
						handler.sendMessage(Message.obtain(handler, MESSAGE_WHAT_REFUSH_NOW_SPEED, currentSpeed));
					}
					
					//每隔指定时间刷新一次平均网速
					allUsedTime = System.currentTimeMillis() - startTime;//毫秒
					downloadLen += temLen;
					if (System.currentTimeMillis() - tempUsedTime > DURATION_AVE_SPEED) {
						if (allUsedTime > 0) {//防止分母为零时报ArithmeticException
							aveSpeed = (int) (downloadLen / allUsedTime * 1000);//平均网速
							tempUsedTime = System.currentTimeMillis();
							handler.sendMessage(Message.obtain(handler, MESSAGE_WHAT_REFUSH_AVE_SPEED, aveSpeed));
							Log.i("bqt", "平均网速：" + formatData(aveSpeed) + "/S   已下载长度：" + formatData(downloadLen));
						}
					}
					
					endReadArrayTime = System.currentTimeMillis();
				}
				
				//最后再发一个平均网速
				aveSpeed = (int) (downloadLen / (System.currentTimeMillis() - startTime) * 1000);//平均网速
				handler.sendMessage(Message.obtain(handler, MESSAGE_WHAT_REFUSH_AVE_SPEED, aveSpeed));
				handler.sendMessage(Message.obtain(handler, MESSAGE_WHAT_REFUSH_RESET));
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	//endregion
}