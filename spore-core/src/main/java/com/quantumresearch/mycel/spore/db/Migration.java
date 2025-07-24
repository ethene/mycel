package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.db.DbException;

interface Migration<T> {

	/**
	 * Returns the schema version from which this migration starts.
	 */
	int getStartVersion();

	/**
	 * Returns the schema version at which this migration ends.
	 */
	int getEndVersion();

	void migrate(T txn) throws DbException;
}
