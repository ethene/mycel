package com.quantumresearch.mycel.infrastructure.db;

import com.quantumresearch.mycel.infrastructure.api.db.DatabaseConfig;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import org.junit.Ignore;

import java.sql.Connection;

/**
 * Sanity check for {@link DatabasePerformanceComparisonTest}: check that
 * if conditions A and B are identical, no significant difference is (usually)
 * detected.
 */
@Ignore
public class H2SelfDatabasePerformanceComparisonTest
		extends DatabasePerformanceComparisonTest {

	@Override
	Database<Connection> createDatabase(boolean conditionA,
			DatabaseConfig databaseConfig, MessageFactory messageFactory,
			Clock clock) {
		return new H2Database(databaseConfig, messageFactory, clock);
	}

	@Override
	protected String getTestName() {
		return getClass().getSimpleName();
	}
}
