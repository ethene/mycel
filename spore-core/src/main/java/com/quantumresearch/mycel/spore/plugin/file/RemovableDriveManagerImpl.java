package com.quantumresearch.mycel.spore.plugin.file;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveManager;
import com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveTask;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.transport.KeyManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveConstants.ID;
import static com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveConstants.PROP_SUPPORTED;
import static com.quantumresearch.mycel.spore.plugin.file.RemovableDrivePluginFactory.MAX_LATENCY;

@ThreadSafe
@NotNullByDefault
class RemovableDriveManagerImpl
		implements RemovableDriveManager, RemovableDriveTaskRegistry {

	private final Executor ioExecutor;
	private final DatabaseComponent db;
	private final KeyManager keyManager;
	private final TransportPropertyManager transportPropertyManager;
	private final RemovableDriveTaskFactory taskFactory;
	private final Object lock = new Object();

	@GuardedBy("lock")
	@Nullable
	private RemovableDriveTask reader = null;
	@GuardedBy("lock")
	@Nullable
	private RemovableDriveTask writer = null;

	@Inject
	RemovableDriveManagerImpl(
			@IoExecutor Executor ioExecutor,
			DatabaseComponent db,
			KeyManager keyManager,
			TransportPropertyManager transportPropertyManager,
			RemovableDriveTaskFactory taskFactory) {
		this.ioExecutor = ioExecutor;
		this.db = db;
		this.keyManager = keyManager;
		this.transportPropertyManager = transportPropertyManager;
		this.taskFactory = taskFactory;
	}

	@Nullable
	@Override
	public RemovableDriveTask getCurrentReaderTask() {
		synchronized (lock) {
			return reader;
		}
	}

	@Nullable
	@Override
	public RemovableDriveTask getCurrentWriterTask() {
		synchronized (lock) {
			return writer;
		}
	}

	@Override
	public RemovableDriveTask startReaderTask(TransportProperties p) {
		RemovableDriveTask created;
		synchronized (lock) {
			if (reader != null) return reader;
			reader = created = taskFactory.createReader(this, p);
		}
		ioExecutor.execute(created);
		return created;
	}

	@Override
	public RemovableDriveTask startWriterTask(ContactId c,
			TransportProperties p) {
		RemovableDriveTask created;
		synchronized (lock) {
			if (writer != null) return writer;
			writer = created = taskFactory.createWriter(this, c, p);
		}
		ioExecutor.execute(created);
		return created;
	}

	@Override
	public boolean isTransportSupportedByContact(ContactId c)
			throws DbException {
		if (!keyManager.canSendOutgoingStreams(c, ID)) return false;
		TransportProperties p =
				transportPropertyManager.getRemoteProperties(c, ID);
		return "true".equals(p.get(PROP_SUPPORTED));
	}

	@Override
	public boolean isWriterTaskNeeded(ContactId c) throws DbException {
		return db.transactionWithResult(true, txn ->
				db.containsAcksToSend(txn, c) ||
						db.containsMessagesToSend(txn, c, MAX_LATENCY, true));
	}

	@Override
	public void removeReader(RemovableDriveTask task) {
		synchronized (lock) {
			if (reader == task) reader = null;
		}
	}

	@Override
	public void removeWriter(RemovableDriveTask task) {
		synchronized (lock) {
			if (writer == task) writer = null;
		}
	}
}
