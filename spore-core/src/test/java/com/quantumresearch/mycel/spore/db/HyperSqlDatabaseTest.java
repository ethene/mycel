package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.db.DatabaseConfig;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.api.system.Clock;
import org.junit.Before;

import static com.quantumresearch.mycel.spore.test.TestUtils.isCryptoStrengthUnlimited;
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
