package com.quantumresearch.mycel.infrastructure.plugin;

import com.quantumresearch.mycel.infrastructure.api.plugin.Backoff;
import com.quantumresearch.mycel.infrastructure.api.plugin.BackoffFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class BackoffFactoryImpl implements BackoffFactory {

	@Override
	public Backoff createBackoff(int minInterval, int maxInterval,
			double base) {
		return new BackoffImpl(minInterval, maxInterval, base);
	}
}
