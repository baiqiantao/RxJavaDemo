package com.bqt.test.rx.plugins;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bqt.test.rx.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class RxBindingActivity extends Activity {
	public CompositeDisposable compositeDisposable;
	private static final String[] ARRAY = {"包青天", "白乾涛", "baiqiantao", "bqt"};
	private TextView tv_name;
	private ImageView iv_icon;
	private EditText et_phone, et_psd;
	private CheckBox cb_agree;
	private ListView lv_users;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rxbinding);
		
		tv_name = findViewById(R.id.tv_name);
		iv_icon = findViewById(R.id.iv_icon);
		et_phone = findViewById(R.id.et_phone);
		et_psd = findViewById(R.id.et_psd);
		cb_agree = findViewById(R.id.cb_agree);
		lv_users = findViewById(R.id.lv_users);
		lv_users.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(ARRAY)));
		
		simple();
		accept();
		useless();
		powerful();
	}
	
	private void simple() {
		RxView.clicks(tv_name).subscribe(o -> toast("点击事件"));
		RxView.longClicks(iv_icon).subscribe(o -> toast("长点击事件"));
		RxAdapterView.itemClicks(lv_users).subscribe(position -> toast("点击了 " + position));
		RxCompoundButton.checkedChanges(cb_agree).subscribe(isChecked -> toast(isChecked ? "选中" : "取消选中"));
		
		RxTextView.textChanges(et_phone).subscribe(this::toast); //内部封装了TextWatcher监听
		RxTextView.textChangeEvents(et_psd).subscribe(event -> toast(event.text())); //返回 TextViewTextChangeEvent
		RxTextView.editorActions(et_phone).subscribe(actionId -> toast("" + actionId));//EditorInfo.IME_ACTION_DONE=6
		RxTextView.editorActionEvents(et_psd).subscribe(event -> toast("" + event.actionId()));//TextViewEditorActionEvent
	}
	
	private void accept() {
		try {
			RxView.visibility(tv_name).accept(true);
			RxView.visibility(iv_icon, View.GONE).accept(true);
			
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
		RxView.drags(tv_name).subscribe(dragEvent -> toast("拖拽监听"));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			RxView.scrollChangeEvents(lv_users).subscribe(scrollChangeEvent -> toast("滑动监听"));
		}
	}
	
	private void powerful() {
		//真正增强功能、解放双手的 api
		RxView.clicks(iv_icon)
				.throttleFirst(2, TimeUnit.SECONDS) //throttleFirst只响应第一次，throttleLast只响应最后一次
				.subscribe(o -> toast("防抖动"));
		
		RxTextView.textChanges(et_phone)
				.debounce(500, TimeUnit.MILLISECONDS) //防抖动，控件操作时间间隔，去除发送频率过快的项
				.subscribe(this::toast);
		
		//合并监听、表单验证
		Observable.combineLatest(RxTextView.textChanges(et_phone), RxTextView.textChanges(et_psd), (phone, psd) -> {
			boolean match = !TextUtils.isEmpty(phone) && phone.length() > 3 && !TextUtils.isEmpty(psd) && psd.length() > 3;
			return match ? "全部满足条件" : "不满足条件";
		}).subscribe(this::toast);
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