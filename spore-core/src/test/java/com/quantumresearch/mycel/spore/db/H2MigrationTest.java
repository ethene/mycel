package com.quantumresearch.mycel.spore.db;

import org.briarproject.nullsafety.NotNullByDefault;

import java.sql.Connection;
import java.util.List;

@NotNullByDefault
public class H2MigrationTest extends DatabaseMigrationTest {

	@Override
	Database<Connection> createDatabase(
			List<Migration<Connection>> migrations) {
		return new H2Database(config, messageFactory, clock) {
			@Override
			List<Migration<Connection>> getMigrations() {
				return migrations;
			}
		};
	}
}
