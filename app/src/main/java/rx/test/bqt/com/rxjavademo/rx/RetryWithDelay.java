package rx.test.bqt.com.rxjavademo.rx;

import android.util.Log;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

public class RetryWithDelay implements Function<Flowable<? extends Throwable>, Publisher<?>> {
	
	private final int maxRetry;
	private final int retryDelayMillis;
	private int tempRetryCount;
	
	public RetryWithDelay(final int maxRetry, final int retryDelayMillis) {
		this.maxRetry = maxRetry;
		this.retryDelayMillis = retryDelayMillis;
		tempRetryCount = 1;
	}
	
	// this is a notification handler, all that is cared about here is the emission "type" not emission "content"
	// only onNext triggers a re-subscription (onError + onComplete kills it)
	@Override
	public Publisher<?> apply(Flowable<? extends Throwable> inputObservable) {
		// it is critical to use inputObservable in the chain for the result ignoring it and doing your own thing will break the sequence
		return inputObservable.flatMap(throwable -> {
			if (++tempRetryCount < maxRetry) {
				// When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed)
				long delay = (long) (Math.pow(2, tempRetryCount) * retryDelayMillis);
				Log.i("bqt", String.format("正在执行第 %d 次重试，将耗时 %d ms", tempRetryCount, delay));
				return Flowable.timer(delay, TimeUnit.MILLISECONDS);
			} else {
				Log.i("bqt", "已达到最大重试次数");
				// Pass an error so the chain is forcibly completed only onNext triggers a re-subscription (onError + onComplete kills it)
				return Flowable.error(throwable);
			}
		});
	}
}