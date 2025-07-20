package com.quantumresearch.mycel.infrastructure.plugin.file;

import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionManager;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.event.EventListener;
import com.quantumresearch.mycel.infrastructure.api.plugin.PluginManager;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.infrastructure.api.plugin.simplex.SimplexPlugin;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportProperties;
import com.quantumresearch.mycel.infrastructure.api.sync.event.MessagesSentEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.infrastructure.api.plugin.file.RemovableDriveConstants.ID;
import static com.quantumresearch.mycel.infrastructure.util.LogUtils.logException;

@NotNullByDefault
class RemovableDriveWriterTask extends RemovableDriveTaskImpl
		implements EventListener {

	private static final Logger LOG =
			getLogger(RemovableDriveWriterTask.class.getName());

	private final DatabaseComponent db;
	private final ContactId contactId;

	RemovableDriveWriterTask(
			DatabaseComponent db,
			Executor eventExecutor,
			PluginManager pluginManager,
			ConnectionManager connectionManager,
			EventBus eventBus,
			RemovableDriveTaskRegistry registry,
			ContactId contactId,
			TransportProperties transportProperties) {
		super(eventExecutor, pluginManager, connectionManager, eventBus,
				registry, transportProperties);
		this.db = db;
		this.contactId = contactId;
	}

	@Override
	public void run() {
		SimplexPlugin plugin = getPlugin();
		TransportConnectionWriter w = plugin.createWriter(transportProperties);
		if (w == null) {
			LOG.warning("Failed to create writer");
			registry.removeWriter(this);
			setSuccess(false);
			return;
		}
		try {
			setTotal(db.transactionWithResult(true, txn ->
					db.getUnackedMessageBytesToSend(txn, contactId)));
		} catch (DbException e) {
			logException(LOG, WARNING, e);
			registry.removeWriter(this);
			setSuccess(false);
			return;
		}
		eventBus.addListener(this);
		connectionManager.manageOutgoingConnection(contactId, ID,
				new DecoratedWriter(w));
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof MessagesSentEvent) {
			MessagesSentEvent m = (MessagesSentEvent) e;
			if (contactId.equals(m.getContactId())) {
				if (LOG.isLoggable(INFO)) {
					LOG.info(m.getMessageIds().size() + " messages sent");
				}
				addDone(m.getTotalLength());
			}
		}
	}

	private class DecoratedWriter implements TransportConnectionWriter {

		private final TransportConnectionWriter delegate;

		private DecoratedWriter(TransportConnectionWriter delegate) {
			this.delegate = delegate;
		}

		@Override
		public long getMaxLatency() {
			return delegate.getMaxLatency();
		}

		@Override
		public int getMaxIdleTime() {
			return delegate.getMaxIdleTime();
		}

		@Override
		public boolean isLossyAndCheap() {
			return delegate.isLossyAndCheap();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return delegate.getOutputStream();
		}

		@Override
		public void dispose(boolean exception) throws IOException {
			delegate.dispose(exception);
			registry.removeWriter(RemovableDriveWriterTask.this);
			eventBus.removeListener(RemovableDriveWriterTask.this);
			setSuccess(!exception);
		}
	}
}
