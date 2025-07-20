package com.quantumresearch.mycel.infrastructure.db;

interface BenchmarkTask<T> {

	void run(T context) throws Exception;
}
