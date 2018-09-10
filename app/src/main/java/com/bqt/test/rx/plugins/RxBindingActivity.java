package com.bqt.test.rx.plugins;

import android.Manifest;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bqt.test.rx.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class RxBindingActivity extends FragmentActivity {
	public CompositeDisposable compositeDisposable;
	private static final String[] ARRAY = {"包青天", "白乾涛", "baiqiantao", "bqt"};
	private ImageView iv1, iv2, iv3, iv4;
	private EditText et_account, et_phone, et_psd, et_mail, et_name;
	private CheckBox cb_agree;
	private ListView lv_users;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rxbinding);
		
		iv1 = findViewById(R.id.iv1);
		iv2 = findViewById(R.id.iv2);
		iv3 = findViewById(R.id.iv3);
		iv4 = findViewById(R.id.iv4);
		et_account = findViewById(R.id.et_account);
		et_phone = findViewById(R.id.et_phone);
		et_psd = findViewById(R.id.et_psd);
		et_mail = findViewById(R.id.et_mail);
		et_name = findViewById(R.id.et_name);
		cb_agree = findViewById(R.id.cb_agree);
		lv_users = findViewById(R.id.lv_users);
		lv_users.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(ARRAY)));
		
		switch (getIntent().getIntExtra("type", 0)) {
			case 0:
				simple();
				break;
			case 1:
				accept();
				break;
			case 2:
				useless();
				break;
			case 3:
				powerful();
				break;
			default:
				break;
		}
	}
	
	private void simple() {
		RxView.clicks(iv1).subscribe(o -> toast("点击事件"));
		RxView.longClicks(iv2).subscribe(o -> toast("长点击事件"));
		RxAdapterView.itemClicks(lv_users).subscribe(position -> toast("点击了 " + position));
		RxCompoundButton.checkedChanges(cb_agree).subscribe(isChecked -> toast(isChecked ? "选中" : "取消选中"));
		
		RxTextView.textChanges(et_account).subscribe(this::toast); //内部封装了TextWatcher监听
		RxTextView.textChangeEvents(et_phone).subscribe(event -> toast(event.text())); //返回 TextViewTextChangeEvent
		RxTextView.afterTextChangeEvents(et_psd).subscribe(event -> toast(event.view().getText()));//TextViewAfterTextChangeEvent
		RxTextView.editorActions(et_mail).subscribe(actionId -> toast("" + actionId));//EditorInfo.IME_ACTION_DONE=6
		RxTextView.editorActionEvents(et_name).subscribe(event -> toast("" + event.actionId()));//TextViewEditorActionEvent
	}
	
	private void accept() {
		try {
			RxView.clicks(iv1).subscribe(o -> RxView.visibility(iv1).accept(true));
			RxView.clicks(iv1).subscribe(o -> RxView.visibility(iv2, View.GONE).accept(true));
			
			RxCompoundButton.checked(cb_agree).accept(true);
			RxCompoundButton.toggle(cb_agree).accept(null); //.toggle() 等价于 setChecked(!mChecked)
			
			RxTextView.text(et_phone).accept("重新设置的值");
			RxTextView.hint(et_psd).accept("重新设置的值");
			RxTextView.color(et_phone).accept(Color.parseColor("#00ff00"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void useless() {
		RxView.draws(cb_agree).subscribe(o -> toast("绘制监听"));
		RxView.drags(cb_agree).subscribe(dragEvent -> toast("拖拽监听"));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			RxView.scrollChangeEvents(lv_users).subscribe(scrollChangeEvent -> toast("滑动监听"));
		}
	}
	
	private void powerful() {
		RxView.clicks(iv1)
				.throttleFirst(2, TimeUnit.SECONDS) //throttleFirst只响应第一次，throttleLast只响应最后一次
				.subscribe(o -> toast("防抖动"));
		
		RxView.clicks(iv2)
				.compose(new RxPermissions(this).ensure(Manifest.permission.CAMERA)) //动态权限
				.subscribe(granted -> Toast.makeText(this, granted ? "已赋予权限" : "已拒绝权限", Toast.LENGTH_SHORT).show());
		
		RxView.clicks(iv3)
				.buffer(800, TimeUnit.MILLISECONDS, 5) //连击
				.subscribe(list -> toast(list.size() + "连击：" + list.toString()));
		
		RxTextView.textChanges(et_phone)
				.debounce(500, TimeUnit.MILLISECONDS) //防抖动，控件操作时间间隔，去除发送频率过快的项
				.subscribe(this::toast);
		
		Observable.combineLatest(RxTextView.textChanges(et_phone), RxTextView.textChanges(et_psd),  //合并监听、表单验证
				(phone, psd) -> phone + "--" + psd)
				.subscribe(this::toast);
	}
	
	private void toast(CharSequence charSequence) {
		Toast.makeText(this, charSequence, Toast.LENGTH_SHORT).show();
		Log.i("bqt", charSequence == null ? "null" : charSequence.toString());
	}
	
	public void addDisposable(Disposable disposable) {
		if (compositeDisposable == null) compositeDisposable = new CompositeDisposable();
		compositeDisposable.add(disposable);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (compositeDisposable != null) {
			compositeDisposable.clear();
		}
	}
}