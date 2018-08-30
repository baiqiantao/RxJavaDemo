package com.bqt.test.rx.othre;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;

public class RxPermissionsActivity extends ListActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"最简单实用的案例，request",
				"和 RxView 一起使用，compose + ensure",
				"一次请求多个权限",
				"观察详细的结果，requestEach、ensureEach",
				"观察详细的结果，requestEachCombined 和 ensureEachCombined",
				"shouldShowRequestPermissionRationale 和 isGranted 的使用 ",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(array)));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, RxPermissionsFragmentActivity.class);
		intent.putExtra("type", position);
		startActivity(intent);
	}
}