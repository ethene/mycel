package com.quantumresearch.mycel.spore.api.db;

import com.quantumresearch.mycel.spore.api.crypto.KeyStrengthener;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;

import javax.annotation.Nullable;

@NotNullByDefault
public interface DatabaseConfig {

	/**
	 * Returns the directory where the database stores its data.
	 */
	File getDatabaseDirectory();

	/**
	 * Returns the directory where the encrypted database key is stored.
	 */
	File getDatabaseKeyDirectory();

	/**
	 * Returns a {@link KeyStrengthener} for strengthening the encryption of
	 * the database key, or null if no strengthener should be used.
	 */
	@Nullable
	KeyStrengthener getKeyStrengthener();
}
