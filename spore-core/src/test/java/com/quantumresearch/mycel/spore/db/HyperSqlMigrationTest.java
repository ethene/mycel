package com.quantumresearch.mycel.spore.db;

import org.briarproject.nullsafety.NotNullByDefault;
import org.junit.Before;

import java.sql.Connection;
import java.util.List;

import static com.quantumresearch.mycel.spore.test.TestUtils.isCryptoStrengthUnlimited;
import static org.junit.Assume.assumeTrue;

@NotNullByDefault
public class HyperSqlMigrationTest extends DatabaseMigrationTest {

	@Before
	public void setUp() {
		assumeTrue(isCryptoStrengthUnlimited());
	}

	@Override
	Database<Connection> createDatabase(
			List<Migration<Connection>> migrations) {
		return new HyperSqlDatabase(config, messageFactory, clock) {
			@Override
			List<Migration<Connection>> getMigrations() {
				return migrations;
			}
		};
	}
}
