package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.db.DatabaseConfig;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.system.SystemClock;
import com.quantumresearch.mycel.spore.test.TestDatabaseConfig;
import com.quantumresearch.mycel.spore.test.TestMessageFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import static com.quantumresearch.mycel.spore.test.TestUtils.deleteTestDirectory;
import static com.quantumresearch.mycel.spore.test.TestUtils.getMean;
import static com.quantumresearch.mycel.spore.test.TestUtils.getMedian;
import static com.quantumresearch.mycel.spore.test.TestUtils.getSecretKey;
import static com.quantumresearch.mycel.spore.test.TestUtils.getStandardDeviation;

public abstract class SingleDatabasePerformanceTest
		extends DatabasePerformanceTest {

	abstract Database<Connection> createDatabase(DatabaseConfig databaseConfig,
			MessageFactory messageFactory, Clock clock);

	private SecretKey databaseKey = getSecretKey();

	@Override
	protected void benchmark(String name,
			BenchmarkTask<Database<Connection>> task) throws Exception {
		deleteTestDirectory(testDir);
		Database<Connection> db = openDatabase();
		populateDatabase(db);
		db.close();
		db = openDatabase();
		// Measure the first iteration
		long firstDuration = measureOne(db, task);
		// Measure blocks of iterations until we reach a steady state
		SteadyStateResult result = measureSteadyState(db, task);
		db.close();
		writeResult(name, result.blocks, firstDuration, result.durations);
	}

	private Database<Connection> openDatabase() throws DbException {
		Database<Connection> db = createDatabase(
				new TestDatabaseConfig(testDir), new TestMessageFactory(),
				new SystemClock());
		db.open(databaseKey, null);
		return db;
	}

	private void writeResult(String name, int blocks, long firstDuration,
			List<Double> durations) throws IOException {
		String result = String.format("%s\t%d\t%,d\t%,d\t%,d\t%,d", name,
				blocks, firstDuration, (long) getMean(durations),
				(long) getMedian(durations),
				(long) getStandardDeviation(durations));
		writeResult(result);
	}
}
