package com.quantumresearch.mycel.spore.db;

interface BenchmarkTask<T> {

	void run(T context) throws Exception;
}
