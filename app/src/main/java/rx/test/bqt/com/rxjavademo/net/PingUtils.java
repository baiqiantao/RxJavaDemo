package rx.test.bqt.com.rxjavademo.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PingUtils {
	public static final String BAIDU_IP = "119.75.217.109";
	public static final String APPLE_IP = "http://captive.apple.com";
	
	/**
	 * ping返回true表示ping通，false表示没有ping通
	 * 所谓没有ping通是指数据包没有返回，也就是客户端没有及时收到ping的返回包因此返回false
	 * 但是要是网络不可用则ping的时候也会返回true，因为ping指定有成功结束，只是ping的返回包是失败的数据包而不是成功的数据包
	 * 所以准确的说返回true表示ping指定返回完成，false表示没收到ping的返回数据
	 * 以上方法是阻塞的，android系统默认等待ping的超时是10s，可以自定义超时时间
	 * 也不用担心方法一直被阻塞，如果ping超时就会自动返回，不必担心方法被阻塞导致无法运行下面的代码
	 * 网上的一些ping的实现说方法会被一直阻塞，实际上是他们ping的命令没写好，以及使用io被阻塞了
	 */
	public static boolean ping(String host, int pingCount) {
		Process process = null;
		BufferedReader successReader = null;
		//String command = "/system/bin/ping -c " + pingCount + " -w 5 " + host;//-c 是指ping的次数，-w是指超时时间，单位为s。
		String command = "ping -c " + pingCount + " " + host;
		boolean isSuccess = false;
		try {
			Log.i("bqt", "【start ping，command：" + command + "】");
			process = Runtime.getRuntime().exec(command);
			if (process == null) {
				Log.i("bqt", "【ping fail：process is null.】");
				return false;
			}
			successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = successReader.readLine()) != null) {
				Log.i("bqt", line);
			}
			int status = process.waitFor();
			if (status == 0) {
				Log.i("bqt", "【exec cmd success】");
				isSuccess = true;
			} else {
				Log.i("bqt", "【exec cmd fail】");
				isSuccess = false;
			}
			Log.i("bqt", "【exec finished】");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			Log.i("bqt", "【ping exit】");
			if (process != null) {
				process.destroy();
			}
			if (successReader != null) {
				try {
					successReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isSuccess;
	}
	
	/**
	 * 简易版
	 */
	public static boolean pingIpAddress(String host, int pingCount) {
		try {
			String command = "ping -c " + pingCount + " -w 5 " + host;//-c 是指ping的次数，-w是指超时时间，单位为s。
			Log.i("bqt", "【start ping，command：" + command + "】");
			Process process = Runtime.getRuntime().exec(command);
			//其中参数-c 1是指ping的次数为1次，-w是指超时时间单位为s。
			boolean isSuccess = process != null && process.waitFor() == 0;//status 等于0的时候表示网络可用，status等于2时表示当前网络不可用
			Log.i("bqt", "【end ping，isSuccess：" + isSuccess + "】");
			return isSuccess;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
}