package com.quantumresearch.mycel.spore.api;

import org.briarproject.nullsafety.NotNullByDefault;

import java.lang.ref.WeakReference;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Provider;

/**
 * A {@link Provider} that keeps a {@link WeakReference} to the last provided
 * instance and provides the same instance again until the instance is garbage
 * collected.
 */
@NotNullByDefault
public abstract class WeakSingletonProvider<T> implements Provider<T> {

	private final Object lock = new Object();
	@GuardedBy("lock")
	private WeakReference<T> ref = new WeakReference<>(null);

	@Override
	public T get() {
		synchronized (lock) {
			T instance = ref.get();
			if (instance == null) {
				instance = createInstance();
				ref = new WeakReference<>(instance);
			}
			return instance;
		}
	}

	public abstract T createInstance();
}
