package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.system.Clock;

public class ArrayClock implements Clock {

	private final long[] times;
	private int index = 0;

	public ArrayClock(long... times) {
		this.times = times;
	}

	@Override
	public long currentTimeMillis() {
		return times[index++];
	}

	@Override
	public void sleep(long milliseconds) throws InterruptedException {
		Thread.sleep(milliseconds);
	}
}
