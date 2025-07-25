package com.quantumresearch.mycel.spore.api.db;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface DbRunnable<E extends Exception> {

	void run(Transaction txn) throws DbException, E;
}
