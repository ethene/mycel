package com.quantumresearch.mycel.infrastructure.api.settings;

import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface SettingsManager {

	/**
	 * Returns all settings in the given namespace.
	 */
	Settings getSettings(String namespace) throws DbException;

	/**
	 * Returns all settings in the given namespace.
	 */
	Settings getSettings(Transaction txn, String namespace) throws DbException;

	/**
	 * Merges the given settings with any existing settings in the given
	 * namespace.
	 */
	void mergeSettings(Settings s, String namespace) throws DbException;

	/**
	 * Merges the given settings with any existing settings in the given
	 * namespace.
	 */
	void mergeSettings(Transaction txn, Settings s, String namespace)
			throws DbException;
}
