package com.quantumresearch.mycel.spore.settings;

import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.settings.Settings;
import com.quantumresearch.mycel.spore.api.settings.SettingsManager;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class SettingsManagerImpl implements SettingsManager {

	private final DatabaseComponent db;

	@Inject
	SettingsManagerImpl(DatabaseComponent db) {
		this.db = db;
	}

	@Override
	public Settings getSettings(String namespace) throws DbException {
		return db.transactionWithResult(true, txn ->
				db.getSettings(txn, namespace));
	}

	@Override
	public Settings getSettings(Transaction txn, String namespace)
			throws DbException {
		return db.getSettings(txn, namespace);
	}

	@Override
	public void mergeSettings(Settings s, String namespace) throws DbException {
		db.transaction(false, txn -> db.mergeSettings(txn, s, namespace));
	}

	@Override
	public void mergeSettings(Transaction txn, Settings s, String namespace)
			throws DbException {
		db.mergeSettings(txn, s, namespace);
	}
}
