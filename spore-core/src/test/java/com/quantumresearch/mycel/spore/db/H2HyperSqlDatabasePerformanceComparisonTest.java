package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.db.DatabaseConfig;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.api.system.Clock;
import org.junit.Ignore;

import java.sql.Connection;

@Ignore
public class H2HyperSqlDatabasePerformanceComparisonTest
		extends DatabasePerformanceComparisonTest {

	@Override
	Database<Connection> createDatabase(boolean conditionA,
			DatabaseConfig databaseConfig, MessageFactory messageFactory,
			Clock clock) {
		if (conditionA)
			return new H2Database(databaseConfig, messageFactory, clock);
		else return new HyperSqlDatabase(databaseConfig, messageFactory, clock);
	}

	@Override
	protected String getTestName() {
		return getClass().getSimpleName();
	}
}
