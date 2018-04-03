package rx.test.bqt.com.rxjavademo;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import rx.test.bqt.com.rxjavademo.rx.MainActivity1;
import rx.test.bqt.com.rxjavademo.rx.MainActivity2;
import rx.test.bqt.com.rxjavademo.rx.MainActivity3;
import rx.test.bqt.com.rxjavademo.rx.RxBusActivity;

public class MainActivity extends ListActivity {
	private TextView tv;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"1",
				"2",
				"3",
				getString(R.string.btn_demo_rxbus),
				"",
				"",};
		tv = new TextView(this);
		tv.setTextColor(Color.BLUE);
		getListView().addFooterView(tv);
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				startActivity(new Intent(this, MainActivity1.class));
				break;
			case 1:
				startActivity(new Intent(this, MainActivity2.class));
				break;
			case 2:
				startActivity(new Intent(this, MainActivity3.class));
				break;
			case 3:
				tv.setText(R.string.msg_demo_rxbus_1);
				startActivity(new Intent(this, RxBusActivity.class));
				break;
			case 4:
				
				break;
			case 5:
				
				break;
		}
	}
}