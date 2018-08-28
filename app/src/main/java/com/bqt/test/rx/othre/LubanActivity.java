package com.bqt.test.rx.othre;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class LubanActivity extends ListActivity {
	private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pics/";
	private static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pics/tem/";
	private List<String> paths = Arrays.asList(PATH + "pic.jpg", PATH + "icon.jpg", PATH + "icon.png", PATH + "flower.jpg");
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {
				"异步调用",
				"同步调用：批处理",
				"同步调用：只处理一个",
				"和 rxJava 一起使用",
				"常用 API 的使用演示",
		};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
		
		File filePath = new File(SAVE_PATH);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				Luban.with(this)
						.load(paths) //传入原图
						.setCompressListener(getListener())
						.launch();
				break;
			case 1:
				try {
					List<File> fileList = Luban.with(this).load(paths).get();
					for (File file : fileList) {
						Log.i("bqt", file.length() / 1024 + "，" + file.getAbsolutePath());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					String path = paths.get(0);
					File file1 = Luban.with(this).load(paths).get(path);//压缩了很多个，但只取指定其中一个压缩后的结果
					File file2 = Luban.with(this).load(path).get(path);//压缩了一个，取压缩后的结果
					Log.i("bqt", file1.length() / 1024 + "，" + file1.getAbsolutePath());
					Log.i("bqt", file2.length() / 1024 + "，" + file2.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 3:
				Flowable.just(paths)
						.observeOn(Schedulers.io()) //在子线程压缩
						.map(paths -> Luban.with(this).load(paths).get())//直接返回压缩后的文件
						.flatMap((Function<List<File>, Flowable<File>>) Flowable::fromIterable) //扁平化
						.subscribeOn(AndroidSchedulers.mainThread()) //回到主线程
						.subscribe(file -> Log.i("bqt", file.length() / 1024 + "，" + file.getAbsolutePath()));
				break;
			case 4:
				try {
					List<File> fileList = Luban.with(this)
							.load(paths)
							.ignoreBy(1)//设置不压缩的阈值，当原始图片大小小于此值时不压缩，单位为K，默认为 100K
							.setFocusAlpha(true) //设置是否保留透明通道，设为 false 速度更快，但是可能有一个黑色的背景，默认为true
							.setTargetDir(SAVE_PATH) //设置压缩后保存压缩图片的路径
							.filter(path -> !(TextUtils.isEmpty(path) || path.endsWith("flower.jpg"))) //设置不压缩的条件
							.setCompressListener(getListener())
							.get();
					for (File file : fileList) {
						//如果图片被压缩了，返回的是新生成的压缩图片的路径，否则返回的是原始图片的路径
						Log.i("bqt", file.length() / 1024 + "，" + file.getAbsolutePath());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
		}
	}
	
	@NonNull
	private OnCompressListener getListener() {
		return new OnCompressListener() { //内部采用IO线程进行图片压缩，外部调用只需设置好结果监听即可
			@Override
			public void onStart() {
				Log.i("bqt", "【onStart】" + isMainThread());//true
			}
			
			@Override
			public void onSuccess(File file) {
				Log.i("bqt", "【onSuccess】" + isMainThread() + "，" + file.length() / 1024 + "，" + file.getAbsolutePath());//true
			}
			
			@Override
			public void onError(Throwable e) {
				e.printStackTrace();
				Log.i("bqt", "【onError】" + e.getMessage());
			}
		};
	}
	
	private boolean isMainThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}
}