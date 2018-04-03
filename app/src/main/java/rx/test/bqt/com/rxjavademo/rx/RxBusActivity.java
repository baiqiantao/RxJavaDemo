package rx.test.bqt.com.rxjavademo.rx;

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
				.replace(R.id.demo_rxbus_frag_1, new RxBusDemo_TopFragment())
				.replace(R.id.demo_rxbus_frag_2, new RxBusDemo_BottomFragment())
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
		
		public TapEvent(String name) {
			this.name = name;
		}
	}
}