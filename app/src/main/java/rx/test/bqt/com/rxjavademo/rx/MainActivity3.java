package rx.test.bqt.com.rxjavademo.rx;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import rx.test.bqt.com.rxjavademo.R;

public class MainActivity3 extends ListActivity {
	private TextView tv;
	private String[] tipsArray;
	private Disposable disposable;
	private CompositeDisposable compositeDisposable;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tv = new TextView(this);
		tv.setTextColor(Color.BLUE);
		getListView().addFooterView(tv);
		String[] array = {getString(R.string.btn_demo_form_validation_combinel),
				
				getString(R.string.msg_demo_timeout),
				getString(R.string.btn_demo_exponential_backoff),
				getString(R.string.btn_demo_rotation_persist),
				getString(R.string.btn_demo_volley),
				getString(R.string.btn_demo_pagination),
				
				getString(R.string.btn_demo_pagination_more),
				getString(R.string.btn_demo_networkDetector),
				getString(R.string.btn_demo_using),
				getString(R.string.btn_demo_multicastPlayground),
				"",};
		
		tipsArray = new String[]{getString(R.string.btn_demo_form_validation_combinel),
				
				getString(R.string.msg_demo_timeout),
				getString(R.string.btn_demo_exponential_backoff),
				getString(R.string.btn_demo_rotation_persist),
				getString(R.string.btn_demo_volley),
				getString(R.string.btn_demo_pagination),
				
				getString(R.string.btn_demo_pagination_more),
				getString(R.string.btn_demo_networkDetector),
				getString(R.string.btn_demo_using),
				getString(R.string.btn_demo_multicastPlayground),
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		tv.setText(tipsArray[position]);
		switch (position) {
			case 0:
				_0();
				break;
			case 1:
				_1();
				break;
			case 2:
				_2();
				break;
			case 3:
				_3();
				break;
			case 4:
				_4();
				break;
			case 5:
				_5();
				break;
			case 6:
				_6();
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
	
	private void _0() {
	
	}
	
	private void _1() {
	
	}
	
	private void _2() {
	
	}
	
	private void _3() {
	
	}
	
	private void _4() {
	
	}
	
	private void _5() {
	
	}
	
	private void _6() {
	
	}
	
	private void _7() {
	
	}
	
	private void _8() {
	
	}
}