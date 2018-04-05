package rx.test.bqt.com.rxjavademo.rx.rxbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import rx.test.bqt.com.rxjavademo.R;

public class RxBusActivity extends AppCompatActivity {
	private RxBus rxBus = null;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rxbus);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.rxbus_frag_top, new RxBusTopFragment())
				.replace(R.id.rxbus_frag_bottom, new RxBusBottomFragment())
				.commit();
	}
	
	// This is better done with a DI Library like Dagger
	public RxBus getRxBusSingleton() {
		if (rxBus == null) {
			rxBus = new RxBus();
		}
		return rxBus;
	}
	
	static class TapEvent {
		public String name;
		
		TapEvent(String name) {
			this.name = name;
		}
	}
}