package com.bqt.test.rx.plugins;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class RxPermissionsFragmentActivity extends FragmentActivity {
	private TextView view;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = new TextView(this);
		view.setBackgroundColor(0xff00ff00);
		setContentView(view);
		int type = getIntent().getIntExtra("type", 0);
		init(type);
	}
	
	private void init(int type) {
		switch (type) {
			case 0:
				new RxPermissions(this)
						.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
						.subscribe(granted -> {
							if (granted) { //在 Android M(6.0) 之前始终为true
								Toast.makeText(this, "已赋予权限", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(this, "已拒绝权限", Toast.LENGTH_SHORT).show();
							}
						});
				break;
			case 1:
				RxView.clicks(view)
						.compose(new RxPermissions(this).ensure(Manifest.permission.CAMERA))
						.subscribe(granted -> Toast.makeText(this, granted ? "已赋予权限" : "已拒绝权限", Toast.LENGTH_SHORT).show());
				break;
			case 2:
				new RxPermissions(this)
						.request(Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS)//只有所有权限都赋予了才会回调成功
						.subscribe(granted -> Toast.makeText(this, granted ? "已赋予权限" : "已拒绝权限", Toast.LENGTH_SHORT).show());
				RxView.clicks(view)
						.compose(new RxPermissions(this).ensure(Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS))
						.subscribe(granted -> Toast.makeText(this, granted ? "已赋予权限" : "已拒绝权限", Toast.LENGTH_SHORT).show());
				break;
			case 3:
				RxView.clicks(view)
						.compose(new RxPermissions(this).ensureEach(Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS))
						.subscribe(permission -> {//申请了多少个权限就回调多少次，相当于 flatMap 的效果
							if (permission.granted) {
								Toast.makeText(this, "已赋予权限：" + permission.name, Toast.LENGTH_SHORT).show();
							} else if (permission.shouldShowRequestPermissionRationale) { //拒绝了权限，但是没有勾选"ask never again"
								Toast.makeText(this, "已拒绝权限：" + permission.name + " ，但可以继续申请", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(this, "已拒绝权限：" + permission.name + " ，并且不会再显示", Toast.LENGTH_SHORT).show();
							}
						});
				break;
			case 4:
				RxView.clicks(view)
						.compose(new RxPermissions(this).ensureEachCombined(Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS))
						.subscribe(permission -> {//只回调一次，回调的 permission 的属性值是多个原始 permission 的组合值
							if (permission.granted) {
								Toast.makeText(this, "已赋予全部所需的权限：" + permission.name, Toast.LENGTH_SHORT).show();
							} else if (permission.shouldShowRequestPermissionRationale) { //拒绝了权限，但是没有勾选"ask never again"
								Toast.makeText(this, "有权限被拒绝：" + permission.name + " ，但可以继续申请", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(this, "有权限被拒绝：" + permission.name + " ，并且不会再显示", Toast.LENGTH_SHORT).show();
							}
						});
				break;
			case 5:
				boolean isGranted = new RxPermissions(this).isGranted(Manifest.permission.CAMERA);
				Toast.makeText(this, "是否已赋予权限：" + isGranted, Toast.LENGTH_SHORT).show();
				
				new RxPermissions(this)
						.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS)
						.subscribe(granted -> Toast.makeText(this, granted ? "全部权限已授权，或有权限被拒绝但可以继续申请" :
								"有权限被拒绝，并且不会再显示", Toast.LENGTH_SHORT).show());
				break;
			default:
				break;
		}
	}
}