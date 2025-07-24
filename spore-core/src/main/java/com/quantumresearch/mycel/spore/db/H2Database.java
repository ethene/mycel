package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.db.DatabaseConfig;
import com.quantumresearch.mycel.spore.api.db.DbClosedException;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.MigrationListener;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.util.StringUtils;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.db.JdbcUtils.tryToClose;
import static com.quantumresearch.mycel.spore.util.IoUtils.isNonEmptyDirectory;
import static com.quantumresearch.mycel.spore.util.LogUtils.logFileOrDir;

/**
 * Contains all the H2-specific code for the database.
 */
@NotNullByDefault
class H2Database extends JdbcDatabase {

	private static final Logger LOG = getLogger(H2Database.class.getName());

	private static final String HASH_TYPE = "BINARY(32)";
	private static final String SECRET_TYPE = "BINARY(32)";
	private static final String BINARY_TYPE = "BINARY";
	private static final String COUNTER_TYPE = "INT NOT NULL AUTO_INCREMENT";
	private static final String STRING_TYPE = "VARCHAR";
	private static final DatabaseTypes dbTypes = new DatabaseTypes(HASH_TYPE,
			SECRET_TYPE, BINARY_TYPE, COUNTER_TYPE, STRING_TYPE);

	private final DatabaseConfig config;
	private final String url;

	@Nullable
	private volatile SecretKey key = null;

	@Inject
	H2Database(DatabaseConfig config, MessageFactory messageFactory,
			Clock clock) {
		super(dbTypes, messageFactory, clock);
		this.config = config;
		File dir = config.getDatabaseDirectory();
		String path = new File(dir, "db").getAbsolutePath();
		url = "jdbc:h2:split:" + path + ";CIPHER=AES;MULTI_THREADED=1"
				+ ";WRITE_DELAY=0";
	}

	@Override
	public boolean open(SecretKey key, @Nullable MigrationListener listener)
			throws DbException {
		this.key = key;
		File dir = config.getDatabaseDirectory();
		if (LOG.isLoggable(INFO)) {
			LOG.info("Contents of account directory before opening DB:");
			logFileOrDir(LOG, INFO, dir.getParentFile());
		}
		boolean reopen = isNonEmptyDirectory(dir);
		if (LOG.isLoggable(INFO)) LOG.info("Reopening DB: " + reopen);
		if (!reopen && dir.mkdirs()) LOG.info("Created database directory");
		super.open("org.h2.Driver", reopen, key, listener);
		if (LOG.isLoggable(INFO)) {
			LOG.info("Contents of account directory after opening DB:");
			logFileOrDir(LOG, INFO, dir.getParentFile());
		}
		return reopen;
	}

	@Override
	public void close() throws DbException {
		// H2 will close the database when the last connection closes
		Connection c = null;
		Statement s = null;
		try {
			c = createConnection();
			closeAllConnections();
			LOG.info("Compacting DB");
			s = c.createStatement();
			s.execute("SHUTDOWN COMPACT");
			LOG.info("Finished compacting DB");
			s.close();
			c.close();
			// Reopen the DB to mark it as clean after compacting
			c = createConnection();
			setDirty(c, false);
			LOG.info("Marked DB as clean");
			c.close();
		} catch (SQLException e) {
			tryToClose(s, LOG, WARNING);
			tryToClose(c, LOG, WARNING);
			throw new DbException(e);
		}
	}

	@Override
	protected Connection createConnection() throws DbException, SQLException {
		SecretKey key = this.key;
		if (key == null) throw new DbClosedException();
		Properties props = new Properties();
		props.setProperty("user", "user");
		// Separate the file password from the user password with a space
		String hex = StringUtils.toHexString(key.getBytes());
		props.put("password", hex + " password");
		return DriverManager.getConnection(getUrl(), props);
	}

	String getUrl() {
		return url;
	}

	@Override
	protected void compactAndClose() throws DbException {
		Connection c = null;
		Statement s = null;
		try {
			c = createConnection();
			closeAllConnections();
			s = c.createStatement();
			s.execute("SHUTDOWN COMPACT");
			LOG.info("Finished compacting DB");
			s.close();
			c.close();
		} catch (SQLException e) {
			tryToClose(s, LOG, WARNING);
			tryToClose(c, LOG, WARNING);
			throw new DbException(e);
		}
	}
}
