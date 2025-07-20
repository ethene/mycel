package com.quantumresearch.mycel.infrastructure.test;

import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

@NotNullByDefault
public class ImmediateExecutor implements Executor {

	@Override
	public void execute(Runnable r) {
		r.run();
	}
}
