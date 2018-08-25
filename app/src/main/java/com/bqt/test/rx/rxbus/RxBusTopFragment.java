package com.bqt.test.rx.rxbus;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class RxBusTopFragment extends Fragment {
	
	private RxBus rxBus;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		TextView tv = new TextView(getActivity());
		tv.setTextColor(Color.BLUE);
		tv.setBackgroundColor(Color.GRAY);
		tv.setText("点击后通过RxBus发送事件");
		tv.setGravity(Gravity.CENTER);
		tv.setOnClickListener(view -> {
			if (rxBus.hasObservers()) {
				String data = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault()).format(new Date());
				rxBus.send(new RxBusActivity.TapEvent("包青天 " + data));
			}
		});
		return tv;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		rxBus = ((RxBusActivity) Objects.requireNonNull(getActivity())).getRxBusSingleton();
	}
}