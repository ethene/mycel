package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.system.Clock;

import java.util.concurrent.atomic.AtomicLong;

public class SettableClock implements Clock {

	private final AtomicLong time;

	public SettableClock(AtomicLong time) {
		this.time = time;
	}

	@Override
	public long currentTimeMillis() {
		return time.get();
	}

	@Override
	public void sleep(long milliseconds) throws InterruptedException {
		Thread.sleep(milliseconds);
	}
}
