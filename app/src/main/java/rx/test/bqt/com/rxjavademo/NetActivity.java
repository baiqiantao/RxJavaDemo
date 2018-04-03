package rx.test.bqt.com.rxjavademo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

public class NetActivity extends Activity {
	public static final int MESSAGE_WHAT_REFUSH_NOW_SPEED = 1;//刷新当前网速
	public static final int MESSAGE_WHAT_REFUSH_AVE_SPEED = 2;//刷新平均网速
	public static final int MESSAGE_WHAT_REFUSH_START_ANIMATION = 3;//开始开启动画
	public static final int MESSAGE_WHAT_REFUSH_RESET = 4;//重置
	
	private static final String URL_DOWNLOAD = "http://f2.market.xiaomi.com/download/AppStore/08caf4c947ec5ded753141a9ca98e9691ad43e32d/com.tencent.wework.apk";
	
	private static final int LENGTH_REFUSH_CURRENT_SPEED = 1024 * 100;//下载多少byte内容后刷新一次实时网速
	private static final int DURATION_AVE_SPEED = 300;//过多久时间后刷新一次平均网速
	private static final int DURATION_ANIMATION = 100;//动画转动时长
	private static final int DURATION_ANIMATION_INTERVAL = DURATION_ANIMATION + 50;//过多久时间后刷新一次动画
	private static final int DURATION_MAXCHECK = 5 * 1000;//整个测速过程允许的最大时间
	
	private TextView tv_type, tv_now_speed, tv_ave_speed;
	private Button btn;
	private ImageView needle;
	private boolean flag = false;
	private int lastDegree = 0;
	private Handler handler = new StaticUiHandler(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net);
		
		tv_type = findViewById(R.id.connection_type);
		tv_now_speed = findViewById(R.id.now_speed);
		tv_ave_speed = findViewById(R.id.ave_speed);
		needle = findViewById(R.id.needle);
		btn = findViewById(R.id.start_btn);
		
		btn.setOnClickListener(arg0 -> checkNetSpeed());
		checkNetType();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		flag = false;
		handler.removeCallbacksAndMessages(null);
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
	
	private void startAnimation(int cur_speed) {
		int curDegree = getDegree(cur_speed);
		Log.i("bqt", "当前角度" + curDegree + "   上次角度" + lastDegree);
		
		RotateAnimation rotateAnimation = new RotateAnimation(lastDegree, curDegree,//开始、结束时旋转的角度
				Animation.RELATIVE_TO_SELF, 1.0f,//x/y旋转时所使用的模式和中心点
				Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnimation.setFillAfter(true);
		rotateAnimation.setInterpolator(new LinearInterpolator());//匀速
		rotateAnimation.setDuration(DURATION_ANIMATION);
		lastDegree = curDegree;
		needle.startAnimation(rotateAnimation);
	}
	
	private int getDegree(int speed) {
		speed /= 1000;
		int ret;
		if (speed >= 0 && speed <= 512) {
			ret = (int) (15.0 * speed / 128.0);
		} else if (speed >= 512 && speed <= 1024) {
			ret = (int) (60 + 15.0 * speed / 256.0);
		} else if (speed >= 1024 && speed <= 10 * 1024) {
			ret = (int) (90 + 15.0 * speed / 1024.0);
		} else {
			ret = 180;
		}
		return ret;
	}
	
	private long lastTotalRxBytes = 0;
	
	/**
	 * 复原到初始状态
	 */
	private void reset() {
		if (flag) {
			flag = false;
			startAnimation(0);
			btn.setText("重新测试");
			btn.setEnabled(true);
			handler.removeCallbacksAndMessages(null);
		}
	}
	
	/**
	 * 监测网络类型
	 */
	private void checkNetType() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (manager != null) {
			NetworkInfo networkInfo = manager.getActiveNetworkInfo();
			tv_type.setText(networkInfo.getTypeName());
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
						//activity.tv_now_speed.setText(activity.formatData((int) msg.obj) + "/S");
						activity.showNetSpeed();
						break;
					case MESSAGE_WHAT_REFUSH_AVE_SPEED:
						activity.tv_ave_speed.setText(activity.formatData((int) msg.obj) + "/S");
						break;
					case MESSAGE_WHAT_REFUSH_START_ANIMATION:
						activity.startAnimation((int) msg.obj);
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
				long lastAnimationTime = 0;//上次开始动画的时间
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
						currentSpeed = (int) (temLen / arrayUsedTime * 1000);//当前网速
						Log.i("bqt", "自己计算的当前的网速为" + formatData(currentSpeed) + "/S");
						
						//刷新实时网速
						handler.sendMessage(Message.obtain(handler, MESSAGE_WHAT_REFUSH_NOW_SPEED, currentSpeed));
						
						//开启动画
						if (System.currentTimeMillis() - lastAnimationTime > DURATION_ANIMATION_INTERVAL) {
							lastAnimationTime = System.currentTimeMillis();
							handler.sendMessage(Message.obtain(handler, MESSAGE_WHAT_REFUSH_START_ANIMATION, currentSpeed));
						}
					}
					
					//每隔指定时间刷新一次平均网速
					allUsedTime = System.currentTimeMillis() - startTime;//毫秒
					downloadLen += temLen;
					if (System.currentTimeMillis() - tempUsedTime > DURATION_AVE_SPEED) {
						if (allUsedTime > 0) {//防止分母为零时报ArithmeticException
							aveSpeed = (int) (downloadLen / allUsedTime * 1000);//平均网速
							tempUsedTime = System.currentTimeMillis();
							handler.sendMessage(Message.obtain(handler, MESSAGE_WHAT_REFUSH_AVE_SPEED, aveSpeed));
							Log.i("bqt", "平均网速为 " + formatData(aveSpeed) + "/S   已下载的长度" + formatData(downloadLen));
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
	
	//region  其他方法
	private long lastTimeStamp = 0;

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
		int uid = getApplicationInfo().uid;
		long nowTimeStamp = System.currentTimeMillis();
		long totalRxBytes = TrafficStats.getTotalRxBytes();
		if (TrafficStats.getUidRxBytes(uid) != TrafficStats.UNSUPPORTED
				&& totalRxBytes > lastTotalRxBytes
				&& nowTimeStamp - lastTimeStamp > 0) {
			int speed = (int) ((totalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
			lastTimeStamp = nowTimeStamp;
			lastTotalRxBytes = TrafficStats.getTotalRxBytes();
			
			tv_now_speed.setText(formatData(speed) + "/S");
			Log.i("bqt", "从系统获取的当前的网速为" + formatData(speed) + "/S" + "   " + speed);
		}
	}
	
	//endregion
}