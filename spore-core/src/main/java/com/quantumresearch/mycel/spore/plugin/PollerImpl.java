package com.quantumresearch.mycel.spore.plugin;

import com.quantumresearch.mycel.spore.api.Cancellable;
import com.quantumresearch.mycel.spore.api.Pair;
import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.connection.ConnectionRegistry;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.event.ContactAddedEvent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.event.EventListener;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.plugin.ConnectionHandler;
import com.quantumresearch.mycel.spore.api.plugin.Plugin;
import com.quantumresearch.mycel.spore.api.plugin.PluginManager;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.plugin.event.ConnectionClosedEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.ConnectionOpenedEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportActiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportInactiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPlugin;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.system.TaskScheduler;
import com.quantumresearch.mycel.spore.api.system.Wakeful;
import com.quantumresearch.mycel.spore.api.system.WakefulIoExecutor;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;

@ThreadSafe
@NotNullByDefault
class PollerImpl implements Poller, EventListener {

	private static final Logger LOG = getLogger(PollerImpl.class.getName());

	private final Executor ioExecutor, wakefulIoExecutor;
	private final TaskScheduler scheduler;
	private final ConnectionManager connectionManager;
	private final ConnectionRegistry connectionRegistry;
	private final PluginManager pluginManager;
	private final TransportPropertyManager transportPropertyManager;
	private final SecureRandom random;
	private final Clock clock;
	private final Lock lock;
	@GuardedBy("lock")
	private final Map<TransportId, ScheduledPollTask> tasks;

	@Inject
	PollerImpl(@IoExecutor Executor ioExecutor,
			@WakefulIoExecutor Executor wakefulIoExecutor,
			TaskScheduler scheduler,
			ConnectionManager connectionManager,
			ConnectionRegistry connectionRegistry,
			PluginManager pluginManager,
			TransportPropertyManager transportPropertyManager,
			SecureRandom random,
			Clock clock) {
		this.ioExecutor = ioExecutor;
		this.wakefulIoExecutor = wakefulIoExecutor;
		this.scheduler = scheduler;
		this.connectionManager = connectionManager;
		this.connectionRegistry = connectionRegistry;
		this.pluginManager = pluginManager;
		this.transportPropertyManager = transportPropertyManager;
		this.random = random;
		this.clock = clock;
		lock = new ReentrantLock();
		tasks = new HashMap<>();
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactAddedEvent) {
			ContactAddedEvent c = (ContactAddedEvent) e;
			// Connect to the newly added contact
			connectToContact(c.getContactId());
		} else if (e instanceof ConnectionClosedEvent) {
			ConnectionClosedEvent c = (ConnectionClosedEvent) e;
			// Reschedule polling, the polling interval may have decreased
			reschedule(c.getTransportId());
			// If an outgoing connection failed, try to reconnect
			if (!c.isIncoming() && c.isException()) {
				connectToContact(c.getContactId(), c.getTransportId());
			}
		} else if (e instanceof ConnectionOpenedEvent) {
			ConnectionOpenedEvent c = (ConnectionOpenedEvent) e;
			// Reschedule polling, the polling interval may have decreased
			reschedule(c.getTransportId());
		} else if (e instanceof TransportActiveEvent) {
			TransportActiveEvent t = (TransportActiveEvent) e;
			// Poll the newly activated transport
			pollNow(t.getTransportId());
		} else if (e instanceof TransportInactiveEvent) {
			TransportInactiveEvent t = (TransportInactiveEvent) e;
			// Cancel polling for the deactivated transport
			cancel(t.getTransportId());
		}
	}

	private void connectToContact(ContactId c) {
		for (SimplexPlugin s : pluginManager.getSimplexPlugins())
			if (s.shouldPoll()) connectToContact(c, s);
		for (DuplexPlugin d : pluginManager.getDuplexPlugins())
			if (d.shouldPoll()) connectToContact(c, d);
	}

	private void connectToContact(ContactId c, TransportId t) {
		Plugin p = pluginManager.getPlugin(t);
		if (p instanceof SimplexPlugin && p.shouldPoll())
			connectToContact(c, (SimplexPlugin) p);
		else if (p instanceof DuplexPlugin && p.shouldPoll())
			connectToContact(c, (DuplexPlugin) p);
	}

	private void connectToContact(ContactId c, SimplexPlugin p) {
		wakefulIoExecutor.execute(() -> {
			TransportId t = p.getId();
			if (connectionRegistry.isConnected(c, t)) return;
			try {
				TransportProperties props =
						transportPropertyManager.getRemoteProperties(c, t);
				TransportConnectionWriter w = p.createWriter(props);
				if (w != null)
					connectionManager.manageOutgoingConnection(c, t, w);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	private void connectToContact(ContactId c, DuplexPlugin p) {
		wakefulIoExecutor.execute(() -> {
			TransportId t = p.getId();
			if (connectionRegistry.isConnected(c, t)) return;
			try {
				TransportProperties props =
						transportPropertyManager.getRemoteProperties(c, t);
				DuplexTransportConnection d = p.createConnection(props);
				if (d != null)
					connectionManager.manageOutgoingConnection(c, t, d);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	private void reschedule(TransportId t) {
		Plugin p = pluginManager.getPlugin(t);
		if (p != null && p.shouldPoll())
			schedule(p, p.getPollingInterval(), false);
	}

	private void pollNow(TransportId t) {
		Plugin p = pluginManager.getPlugin(t);
		// Randomise next polling interval
		if (p != null && p.shouldPoll()) schedule(p, 0, true);
	}

	private void schedule(Plugin p, int delay, boolean randomiseNext) {
		// Replace any later scheduled task for this plugin
		long due = clock.currentTimeMillis() + delay;
		TransportId t = p.getId();
		lock.lock();
		try {
			ScheduledPollTask scheduled = tasks.get(t);
			if (scheduled == null || due < scheduled.task.due) {
				// If a later task exists, cancel it. If it's already started
				// it will abort safely when it finds it's been replaced
				if (scheduled != null) scheduled.cancellable.cancel();
				PollTask task = new PollTask(p, due, randomiseNext);
				Cancellable cancellable = scheduler.schedule(task, ioExecutor,
						delay, MILLISECONDS);
				tasks.put(t, new ScheduledPollTask(task, cancellable));
			}
		} finally {
			lock.unlock();
		}
	}

	private void cancel(TransportId t) {
		lock.lock();
		try {
			ScheduledPollTask scheduled = tasks.remove(t);
			if (scheduled != null) scheduled.cancellable.cancel();
		} finally {
			lock.unlock();
		}
	}

	@IoExecutor
	private void poll(Plugin p) {
		TransportId t = p.getId();
		if (LOG.isLoggable(INFO)) LOG.info("Polling plugin " + t);
		try {
			Map<ContactId, TransportProperties> remote =
					transportPropertyManager.getRemoteProperties(t);
			Collection<ContactId> connected =
					connectionRegistry.getConnectedOrBetterContacts(t);
			Collection<Pair<TransportProperties, ConnectionHandler>>
					properties = new ArrayList<>();
			for (Entry<ContactId, TransportProperties> e : remote.entrySet()) {
				ContactId c = e.getKey();
				if (!connected.contains(c))
					properties.add(new Pair<>(e.getValue(), new Handler(c, t)));
			}
			if (!properties.isEmpty()) p.poll(properties);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
		}
	}

	private class ScheduledPollTask {

		private final PollTask task;
		private final Cancellable cancellable;

		private ScheduledPollTask(PollTask task, Cancellable cancellable) {
			this.task = task;
			this.cancellable = cancellable;
		}
	}

	private class PollTask implements Runnable {

		private final Plugin plugin;
		private final long due;
		private final boolean randomiseNext;

		private PollTask(Plugin plugin, long due, boolean randomiseNext) {
			this.plugin = plugin;
			this.due = due;
			this.randomiseNext = randomiseNext;
		}

		@Override
		@IoExecutor
		@Wakeful
		public void run() {
			lock.lock();
			try {
				TransportId t = plugin.getId();
				ScheduledPollTask scheduled = tasks.get(t);
				if (scheduled != null && scheduled.task != this)
					return; // Replaced by another task
				tasks.remove(t);
			} finally {
				lock.unlock();
			}
			int delay = plugin.getPollingInterval();
			if (randomiseNext) delay = (int) (delay * random.nextDouble());
			schedule(plugin, delay, false);
			poll(plugin);
		}
	}

	private class Handler implements ConnectionHandler {

		private final ContactId contactId;
		private final TransportId transportId;

		private Handler(ContactId contactId, TransportId transportId) {
			this.contactId = contactId;
			this.transportId = transportId;
		}

		@Override
		public void handleConnection(DuplexTransportConnection c) {
			connectionManager.manageOutgoingConnection(contactId,
					transportId, c);
		}

		@Override
		public void handleReader(TransportConnectionReader r) {
			// TODO: Support simplex plugins that read from outgoing connections
			throw new UnsupportedOperationException();
		}

		@Override
		public void handleWriter(TransportConnectionWriter w) {
			connectionManager.manageOutgoingConnection(contactId,
					transportId, w);
		}
	}
}
