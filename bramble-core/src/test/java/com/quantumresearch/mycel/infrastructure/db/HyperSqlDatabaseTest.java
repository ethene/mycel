package com.quantumresearch.mycel.infrastructure.db;

import com.quantumresearch.mycel.infrastructure.api.db.DatabaseConfig;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import org.junit.Before;

import static com.quantumresearch.mycel.infrastructure.test.TestUtils.isCryptoStrengthUnlimited;
import static org.junit.Assume.assumeTrue;

public class HyperSqlDatabaseTest extends JdbcDatabaseTest {

	@Before
	public void setUp() {
		assumeTrue(isCryptoStrengthUnlimited());
	}

	@Override
	protected JdbcDatabase createDatabase(DatabaseConfig config,
			MessageFactory messageFactory, Clock clock) {
		return new HyperSqlDatabase(config, messageFactory ,clock);
	}
}
