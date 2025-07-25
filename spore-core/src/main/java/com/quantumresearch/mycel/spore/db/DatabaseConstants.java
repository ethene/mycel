package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.settings.Settings;

interface DatabaseConstants {

	/**
	 * The maximum number of offered messages from each contact that will be
	 * stored. If offers arrive more quickly than requests can be sent and this
	 * limit is reached, additional offers will not be stored.
	 */
	int MAX_OFFERED_MESSAGES = 1000;

	/**
	 * The namespace of the {@link Settings} where the database schema version
	 * is stored.
	 */
	String DB_SETTINGS_NAMESPACE = "db";

	/**
	 * The {@link Settings} key under which the database schema version is
	 * stored.
	 */
	String SCHEMA_VERSION_KEY = "schemaVersion";

	/**
	 * The {@link Settings} key under which the flag is stored indicating
	 * whether the database is marked as dirty.
	 */
	String DIRTY_KEY = "dirty";
}
