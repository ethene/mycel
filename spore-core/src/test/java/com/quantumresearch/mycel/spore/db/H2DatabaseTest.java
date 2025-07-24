package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.db.DatabaseConfig;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.api.system.Clock;

public class H2DatabaseTest extends JdbcDatabaseTest {

	@Override
	protected JdbcDatabase createDatabase(DatabaseConfig config,
			MessageFactory messageFactory, Clock clock) {
		return new H2Database(config, messageFactory, clock);
	}
}
