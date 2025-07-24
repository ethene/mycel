package com.quantumresearch.mycel.spore.api.plugin;

public interface BackoffFactory {

	Backoff createBackoff(int minInterval, int maxInterval,
			double base);
}
