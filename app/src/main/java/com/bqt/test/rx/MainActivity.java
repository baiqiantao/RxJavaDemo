package com.bqt.test.rx;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bqt.test.rx.observer.ObserverPatternActivity;
import com.bqt.test.rx.operator.CreateOperatorActivity;
import com.bqt.test.rx.operator.Operator1Activity;
import com.bqt.test.rx.operator.TransformOperatorActivity;
import com.bqt.test.rx.plugins.LubanActivity;
import com.bqt.test.rx.plugins.RxBindingActivity;
import com.bqt.test.rx.plugins.RxPermissionsActivity;

import java.util.Arrays;

public class MainActivity extends ListActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"0、传统的观察者模式和使用 rx 写的观察者模式对比",
				"1、创建操作符 create just from defer timer interval repeat",
				"2、变换操作符 map flatMap concatMap buffer",
				"3、变换操作符 scan groupBy window cast",
				"4、",
				"5、 ",
				"6、 ",
				"7、",
				"8、Luban 图片压缩",
				"9、RxPermissions 动态权限申请",
				"10、RxBinding",
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	private int type = 0;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				startActivity(new Intent(this, ObserverPatternActivity.class));
				break;
			case 1:
				startActivity(new Intent(this, CreateOperatorActivity.class));
				break;
			case 2:
				startActivity(new Intent(this, TransformOperatorActivity.class));
				break;
			case 3:
				startActivity(new Intent(this, Operator1Activity.class));
				break;
			case 4:
				startActivity(new Intent(this, Operator1Activity.class));
				break;
			case 5:
				
				break;
			case 6:
				
				break;
			case 7:
				
				break;
			case 8:
				startActivity(new Intent(this, LubanActivity.class));
				break;
			case 9:
				startActivity(new Intent(this, RxPermissionsActivity.class));
				break;
			case 10:
				Intent intent = new Intent(this, RxBindingActivity.class);
				intent.putExtra("type", (type++) % 4);
				startActivity(intent);
				break;
			
			default:
				break;
		}
	}
}