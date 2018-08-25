package rx.test.bqt.com.rxjavademo.rx.rxbus;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class RxBusBottomFragment extends Fragment {
	
	private RxBus rxbus;
	private CompositeDisposable disposable;
	private TextView tv;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		tv = new TextView(getActivity());
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
		
		Disposable disposable1 = tapEventEmitter
				.filter(event -> event instanceof RxBusActivity.TapEvent)
				.subscribe(event -> tv.append(((RxBusActivity.TapEvent) event).name + "\n"));
		
		Disposable disposable2 = tapEventEmitter
				.publish(stream -> stream.buffer(stream.debounce(200, TimeUnit.MILLISECONDS)))
				.observeOn(AndroidSchedulers.mainThread())
				.filter(taps -> taps != null && taps.size() > 0)
				//.filter(taps -> taps.get(0) instanceof RxBusActivity.TapEvent)
				.subscribe(taps -> tv.append("\t连击次数：" + taps.size() + "\n"));
		
		this.disposable.add(disposable1);
		this.disposable.add(disposable2);
		this.disposable.add(tapEventEmitter.connect());
	}
	
	@Override
	public void onStop() {
		super.onStop();
		disposable.clear();
	}
}