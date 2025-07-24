package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.db.DbCallable;
import com.quantumresearch.mycel.spore.api.db.DbRunnable;
import com.quantumresearch.mycel.spore.api.db.NullableDbCallable;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import org.jmock.Expectations;

public class DbExpectations extends Expectations {

	protected <E extends Exception> DbRunnable<E> withDbRunnable(
			Transaction txn) {
		addParameterMatcher(any(DbRunnable.class));
		currentBuilder().setAction(new RunTransactionAction(txn));
		return null;
	}

	protected <R, E extends Exception> DbCallable<R, E> withDbCallable(
			Transaction txn) {
		addParameterMatcher(any(DbCallable.class));
		currentBuilder().setAction(new RunTransactionWithResultAction(txn));
		return null;
	}

	protected <R, E extends Exception> NullableDbCallable<R, E> withNullableDbCallable(
			Transaction txn) {
		addParameterMatcher(any(NullableDbCallable.class));
		currentBuilder().setAction(
				new RunTransactionWithNullableResultAction(txn));
		return null;
	}

}
