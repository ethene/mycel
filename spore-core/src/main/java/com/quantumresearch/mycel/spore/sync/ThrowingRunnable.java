package com.quantumresearch.mycel.spore.sync;

interface ThrowingRunnable<T extends Throwable> {

	void run() throws T;
}
