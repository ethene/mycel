package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.db.DatabaseConfig;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.api.system.Clock;
import org.junit.Ignore;

@Ignore
public class HyperSqlDatabasePerformanceTest
		extends SingleDatabasePerformanceTest {

	@Override
	protected String getTestName() {
		return getClass().getSimpleName();
	}

	@Override
	protected JdbcDatabase createDatabase(DatabaseConfig config,
			MessageFactory messageFactory, Clock clock) {
		return new HyperSqlDatabase(config, messageFactory, clock);
	}
}
