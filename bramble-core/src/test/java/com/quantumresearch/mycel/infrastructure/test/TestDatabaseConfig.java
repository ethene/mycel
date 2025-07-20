package com.quantumresearch.mycel.infrastructure.test;

import com.quantumresearch.mycel.infrastructure.api.crypto.KeyStrengthener;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseConfig;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;

import javax.annotation.Nullable;

@NotNullByDefault
public class TestDatabaseConfig implements DatabaseConfig {

	private final File dbDir, keyDir;

	public TestDatabaseConfig(File testDir) {
		dbDir = new File(testDir, "db");
		keyDir = new File(testDir, "key");
	}

	@Override
	public File getDatabaseDirectory() {
		return dbDir;
	}

	@Override
	public File getDatabaseKeyDirectory() {
		return keyDir;
	}

	@Nullable
	@Override
	public KeyStrengthener getKeyStrengthener() {
		return null;
	}
}
