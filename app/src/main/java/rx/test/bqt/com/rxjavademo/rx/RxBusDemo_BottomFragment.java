package rx.test.bqt.com.rxjavademo.rx;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;

public class RxBusDemo_BottomFragment extends Fragment {
	
	private RxBus rxbus;
	private CompositeDisposable disposable;
	private TextView tv;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		tv = new TextView(getContext());
		tv.setTextColor(Color.RED);
		tv.setBackgroundColor(Color.YELLOW);
		return tv;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		rxbus = ((RxBusActivity) Objects.requireNonNull(getActivity())).getRxBusSingleton();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		disposable = new CompositeDisposable();
		
		ConnectableFlowable<Object> tapEventEmitter = rxbus.asFlowable().publish();
		
		Disposable d = tapEventEmitter.subscribe(event -> {
			if (event instanceof RxBusActivity.TapEvent) {
				tv.append(((RxBusActivity.TapEvent) event).name + "\n");
			}
		});
		Disposable d2 = tapEventEmitter.publish(stream -> stream.buffer(stream.debounce(1, TimeUnit.SECONDS)))
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(taps -> {
					if (taps != null && taps.size() > 0) {
						tv.append("      连击次数：" + taps.size() + "\n");
						for (int i = 0; i < taps.size(); i++) {
							if (taps.get(i) instanceof RxBusActivity.TapEvent) {
								tv.append("            " + ((RxBusActivity.TapEvent) taps.get(i)).name + "\n");
							}
						}
					}
				});
		
		disposable.add(d);
		disposable.add(d2);
		disposable.add(tapEventEmitter.connect());
	}
	
	@Override
	public void onStop() {
		super.onStop();
		disposable.clear();
	}
}