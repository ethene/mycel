package com.quantumresearch.mycel.infrastructure.api.plugin;

public interface BackoffFactory {

	Backoff createBackoff(int minInterval, int maxInterval,
			double base);
}
