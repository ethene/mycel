package com.quantumresearch.mycel.infrastructure.sync;

interface ThrowingRunnable<T extends Throwable> {

	void run() throws T;
}
