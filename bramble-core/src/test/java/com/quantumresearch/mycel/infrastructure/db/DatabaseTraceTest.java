package com.quantumresearch.mycel.infrastructure.db;

import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseConfig;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.system.SystemClock;
import com.quantumresearch.mycel.infrastructure.test.TestDatabaseConfig;
import com.quantumresearch.mycel.infrastructure.test.TestMessageFactory;
import com.quantumresearch.mycel.infrastructure.util.IoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;

import javax.annotation.Nullable;

import static com.quantumresearch.mycel.infrastructure.test.TestUtils.deleteTestDirectory;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getSecretKey;

public abstract class DatabaseTraceTest extends DatabasePerformanceTest {

	private SecretKey databaseKey = getSecretKey();

	abstract Database<Connection> createDatabase(DatabaseConfig databaseConfig,
			MessageFactory messageFactory, Clock clock);

	@Nullable
	protected abstract File getTraceFile();

	@Override
	protected void benchmark(String name,
			BenchmarkTask<Database<Connection>> task) throws Exception {
		deleteTestDirectory(testDir);
		Database<Connection> db = openDatabase();
		populateDatabase(db);
		db.close();
		File traceFile = getTraceFile();
		if (traceFile != null) traceFile.delete();
		db = openDatabase();
		task.run(db);
		db.close();
		if (traceFile != null) copyTraceFile(name, traceFile);
	}

	private Database<Connection> openDatabase() throws DbException {
		Database<Connection> db = createDatabase(
				new TestDatabaseConfig(testDir), new TestMessageFactory(),
				new SystemClock());
		db.open(databaseKey, null);
		return db;
	}

	private void copyTraceFile(String name, File src) throws IOException {
		if (!src.exists()) return;
		String filename = getTestName() + "." + name + ".trace.txt";
		File dest = new File(testDir.getParentFile(), filename);
		IoUtils.copyAndClose(new FileInputStream(src),
				new FileOutputStream(dest));
	}
}
