package com.bqt.test.rx.plugins;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.bqt.test.rx.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.view.RxViewGroup;
import com.jakewharton.rxbinding2.widget.RxAbsListView;
import com.jakewharton.rxbinding2.widget.RxAdapter;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RxBindingActivity extends RxFragmentActivity {
	private static final String[] ARRAY = {"包青天", "白乾涛", "baiqiantao", "bqt", "RxBinding", "RxView", "1", "2", "3", "4", "5",};
	private ImageView iv1, iv2, iv3, iv4, iv5;
	private EditText et1, et2, et3;
	private Button btn;
	private CheckBox cb;
	private ListView listView;
	private int type;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rxbinding);
		
		iv1 = findViewById(R.id.iv1);
		iv2 = findViewById(R.id.iv2);
		iv3 = findViewById(R.id.iv3);
		iv4 = findViewById(R.id.iv4);
		iv5 = findViewById(R.id.iv5);
		btn = findViewById(R.id.btn);
		et1 = findViewById(R.id.et1);
		et2 = findViewById(R.id.et2);
		et3 = findViewById(R.id.et3);
		cb = findViewById(R.id.cb);
		listView = findViewById(R.id.lv);
		listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(ARRAY)));
		
		type = getIntent().getIntExtra("type", 0);
		log("type=" + type);
		switch (type) {
			case 0:
				event();
				break;
			case 1:
				accept();
				break;
			case 2:
				usefulDemo();
				break;
			
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (type == 3) {
			bufferDemo();
		}
	}
	
	@SuppressLint("NewApi")
	private void event() {
		RxView.attaches(iv1).subscribe(o -> log("attach event"));
		RxView.attachEvents(iv1).subscribe(viewAttachEvent -> log("attach event"));
		RxView.clicks(iv1).subscribe(o -> log("click event")); //setOnClickListener
		RxView.detaches(iv1).subscribe(o -> log("detach event"));
		RxView.drags(listView).subscribe(dragEvent -> log("drag event:" + dragEvent.getAction())); //setOnDragListener
		RxView.draws(listView).subscribe(o -> log("draw event")); // ViewTreeObserver#addOnDrawListener
		RxView.focusChanges(iv1).subscribe(hasFocus -> log("focus change event:" + hasFocus)); //setOnFocusChangeListener
		RxView.globalLayouts(listView).subscribe(o -> log("global event")); //ViewTreeObserver#addOnGlobalLayoutListener
		RxView.hovers(iv1).subscribe(motionEvent -> log("hover event:" + motionEvent.getAction())); //setOnHoverListene，悬停事件
		RxView.keys(iv1).subscribe(keyEvent -> log("key event:" + keyEvent.getAction())); //setOnKeyListener
		RxView.layoutChanges(listView).subscribe(o -> log("layout event")); //addOnLayoutChangeListener
		RxView.layoutChangeEvents(listView).subscribe(viewLayoutChangeEvent -> log("layout event"));//addOnLayoutChangeListener
		RxView.longClicks(iv1).subscribe(o -> log("long click event")); //setOnLongClickListener
		RxView.scrollChangeEvents(listView).subscribe(viewScrollChangeEvent -> log("scroll event")); //setOnScrollChangeListener
		RxView.systemUiVisibilityChanges(listView).subscribe(vii -> log("visible event" + vii)); //setOnSystemUiVisibilityChangeListener
		RxView.touches(listView).subscribe(motionEvent -> { //setOnTouchListener
			log("touch event:" + motionEvent.getAction());
			listView.onTouchEvent(motionEvent);//如果不传给listView，则listView将不能获取到Touch事件，那么listView会出现不能滑动等问题
		});
		
		RxAdapterView.itemClicks(listView).subscribe(position -> log("item click event:" + position)); //setOnItemClickListener
		RxAdapterView.itemClickEvents(listView).subscribe(adapterViewItemClickEvent -> log("item click event")); //点击
		RxAdapterView.itemLongClicks(listView).subscribe(position -> log("item long click event:" + position)); //setOnItemLongClickListener
		RxAdapterView.itemLongClickEvents(listView).subscribe(adapterViewItemLongClickEvent -> log("item long click event")); //长点击
		RxAdapterView.itemSelections(listView).subscribe(position -> log("item select event:" + position)); //setOnItemSelectedListener
		RxAdapterView.selectionEvents(listView).subscribe(adapterViewSelectionEvent -> log("item select event")); //选择
		
		RxAdapter.dataChanges(listView.getAdapter()).subscribe(listAdapter -> log("data change event"));//registerDataSetObserver
		RxAbsListView.scrollEvents(listView).subscribe(absListViewScrollEvent -> log("scroll event")); //setOnScrollListener
		RxViewGroup.changeEvents(listView).subscribe(viewGroupHierarchyChangeEvent -> log("哈"));//setOnHierarchyChangeListener
		
		RxCompoundButton.checkedChanges(cb).subscribe(isChecked -> log("check event:" + isChecked)); //setOnCheckedChangeListener
		
		RxTextView.textChanges(et1).subscribe(cs -> log("text change event:" + cs.toString())); //addTextChangedListener
		RxTextView.textChangeEvents(et2).subscribe(textViewTextChangeEvent -> log("text change event")); //addTextChangedListener
		RxTextView.afterTextChangeEvents(et3).subscribe(textViewAfterTextChangeEvent -> log("aftet text change event"));
		RxTextView.beforeTextChangeEvents(et3).subscribe(textViewBeforeTextChangeEvent -> log("before text change event"));
		RxTextView.editorActions(et1).subscribe(actionId -> log("editor event:" + actionId)); //setOnEditorActionListener，需要设置inputType
		RxTextView.editorActionEvents(et2).subscribe(textViewEditorActionEvent -> log("editor event")); //点击软键盘上的回车键
	}
	
	private void accept() {
		try {
			RxView.visibility(iv1).accept(false);//view.setVisibility(value ? View.VISIBLE : View.GONE)
			RxView.visibility(iv2, View.GONE).accept(true);//view.setVisibility(value ? View.VISIBLE : visibilityWhenFalse)
			RxView.visibility(iv3, View.GONE).accept(false);
			RxView.visibility(iv4, View.INVISIBLE).accept(false);
			RxView.visibility(iv5, View.INVISIBLE).accept(true);
			
			RxAdapterView.selection(listView).accept(1); //view.setSelection(position)
			
			RxCompoundButton.checked(cb).accept(true); //view.setChecked(value)
			RxCompoundButton.toggle(cb).accept(null); //view.toggle()，切换状态
			
			RxTextView.color(et1).accept(Color.RED); //view.setTextColor(color)
			RxTextView.textRes(et1).accept(R.string.app_name); //view.setText(int resId)
			RxTextView.hintRes(et2).accept(R.string.app_name); //view.setHint(int resId)
			RxTextView.errorRes(et3).accept(R.string.app_name); //view.setError(int resId)
			RxTextView.text(et1).accept("text"); //view.setText(String text)
			RxTextView.hint(et2).accept("hint"); //view.setHint(String text)
			RxTextView.error(et3).accept("error"); //view.setError(String text)
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void usefulDemo() {
		RxView.clicks(iv1)
				.throttleFirst(2, TimeUnit.SECONDS) //throttleFirst只响应第一次，throttleLast只响应最后一次
				.subscribe(o -> log("防抖动"));
		
		RxView.clicks(iv2)
				.compose(new RxPermissions(this).ensure(Manifest.permission.CAMERA)) //动态获取权限
				.subscribe(granted -> log(granted ? "已赋予权限" : "已拒绝权限"));
		
		RxTextView.textChanges(et1)
				.debounce(500, TimeUnit.MILLISECONDS) //防抖动，控件操作时间间隔，去除发送频率过快的项
				.subscribe(charSequence -> log(charSequence.toString()));
		
		Observable<CharSequence> observable1 = RxTextView.textChanges(et2).skip(1);
		Observable<CharSequence> observable2 = RxTextView.textChanges(et3).skip(1);
		Observable.combineLatest(observable1, observable2,  //合并监听、表单验证
				(phone, password) -> {
					log(phone + "_" + password);
					return phone.toString().startsWith("1") && password.toString().endsWith("1");
				}).subscribe(isValid -> btn.setEnabled(isValid));
		
		RxView.clicks(btn) //发送验证码功能
				.doOnNext(o -> btn.setEnabled(false))
				.subscribe(o -> Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
						.take(10)
						.compose(bindToLifecycle())
						.map(aLong -> 10 - aLong + " 秒后重新获取")
						.subscribe(string -> btn.setText(string),
								Throwable::printStackTrace,
								() -> {
									btn.setEnabled(true);
									btn.setText("重新获取");
								}));
	}
	
	private void bufferDemo() {
		RxView.clicks(iv1)
				.buffer(1000, TimeUnit.MILLISECONDS, 5) //效果仅仅是，每隔1秒钟收集一下此1秒钟内的点击次数
				.compose(bindUntilEvent(ActivityEvent.STOP))//在 onStop 时取消
				.subscribe(list -> Log.i("【bqt】", "一秒钟内的点击次数：" + list.size()));
		
		//这种效果可能不是你想要的效果，你想要的效果可能是：在1秒钟内点击次数为多少次就是几次连击
		Observable<Object> observable = RxView.clicks(iv2).share();
		observable.buffer(observable.debounce(200, TimeUnit.MILLISECONDS)
				.compose(bindUntilEvent(ActivityEvent.STOP)))
				.subscribe(list -> Log.i("【bqt】", "连续点击次数：" + list.size()));//这里的时间指的是任意两次点击最长间隔时间);
	}
	
	private void log(String s) {
		Log.i("【bqt】", s);
	}
}