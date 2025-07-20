package com.quantumresearch.mycel.infrastructure.keyagreement;

import com.quantumresearch.mycel.infrastructure.api.Pair;
import com.quantumresearch.mycel.infrastructure.api.crypto.KeyAgreementCrypto;
import com.quantumresearch.mycel.infrastructure.api.crypto.KeyPair;
import com.quantumresearch.mycel.infrastructure.api.data.BdfList;
import com.quantumresearch.mycel.infrastructure.api.keyagreement.KeyAgreementConnection;
import com.quantumresearch.mycel.infrastructure.api.keyagreement.KeyAgreementListener;
import com.quantumresearch.mycel.infrastructure.api.keyagreement.Payload;
import com.quantumresearch.mycel.infrastructure.api.keyagreement.TransportDescriptor;
import com.quantumresearch.mycel.infrastructure.api.plugin.BluetoothConstants;
import com.quantumresearch.mycel.infrastructure.api.plugin.LanTcpConstants;
import com.quantumresearch.mycel.infrastructure.api.plugin.Plugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.PluginManager;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.infrastructure.api.record.RecordReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.record.RecordWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import static java.util.Arrays.asList;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.infrastructure.api.keyagreement.KeyAgreementConstants.CONNECTION_TIMEOUT;
import static com.quantumresearch.mycel.infrastructure.util.LogUtils.logException;

@NotNullByDefault
class KeyAgreementConnector {

	interface Callbacks {
		void connectionWaiting();
	}

	private static final Logger LOG =
			getLogger(KeyAgreementConnector.class.getName());

	private static final List<TransportId> PREFERRED_TRANSPORTS =
			asList(BluetoothConstants.ID, LanTcpConstants.ID);

	private final Callbacks callbacks;
	private final KeyAgreementCrypto keyAgreementCrypto;
	private final PluginManager pluginManager;
	private final ConnectionChooser connectionChooser;
	private final RecordReaderFactory recordReaderFactory;
	private final RecordWriterFactory recordWriterFactory;

	private final List<KeyAgreementListener> listeners =
			new CopyOnWriteArrayList<>();
	private final CountDownLatch aliceLatch = new CountDownLatch(1);
	private final AtomicBoolean waitingSent = new AtomicBoolean(false);

	private volatile boolean alice = false, stopped = false;

	KeyAgreementConnector(Callbacks callbacks,
			KeyAgreementCrypto keyAgreementCrypto, PluginManager pluginManager,
			ConnectionChooser connectionChooser,
			RecordReaderFactory recordReaderFactory,
			RecordWriterFactory recordWriterFactory) {
		this.callbacks = callbacks;
		this.keyAgreementCrypto = keyAgreementCrypto;
		this.pluginManager = pluginManager;
		this.connectionChooser = connectionChooser;
		this.recordReaderFactory = recordReaderFactory;
		this.recordWriterFactory = recordWriterFactory;
	}

	Payload listen(KeyPair localKeyPair) {
		LOG.info("Starting BQP listeners");
		// Derive commitment
		byte[] commitment = keyAgreementCrypto.deriveKeyCommitment(
				localKeyPair.getPublic());
		// Start all listeners and collect their descriptors
		List<TransportDescriptor> descriptors = new ArrayList<>();
		for (DuplexPlugin plugin : pluginManager.getKeyAgreementPlugins()) {
			KeyAgreementListener l =
					plugin.createKeyAgreementListener(commitment);
			if (l != null) {
				TransportId id = plugin.getId();
				descriptors.add(new TransportDescriptor(id, l.getDescriptor()));
				if (LOG.isLoggable(INFO)) LOG.info("Listening via " + id);
				listeners.add(l);
				connectionChooser.submit(new ReadableTask(l::accept));
			}
		}
		return new Payload(commitment, descriptors);
	}

	void stopListening() {
		LOG.info("Stopping BQP listeners");
		stopped = true;
		aliceLatch.countDown();
		for (KeyAgreementListener l : listeners) l.close();
		connectionChooser.stop();
	}

	@Nullable
	public KeyAgreementTransport connect(Payload remotePayload, boolean alice) {
		// Let the ReadableTasks know if we are Alice
		this.alice = alice;
		aliceLatch.countDown();

		// Start connecting over supported transports in order of preference
		if (LOG.isLoggable(INFO)) {
			LOG.info("Starting outgoing BQP connections as "
					+ (alice ? "Alice" : "Bob"));
		}
		Map<TransportId, TransportDescriptor> descriptors = new HashMap<>();
		for (TransportDescriptor d : remotePayload.getTransportDescriptors()) {
			descriptors.put(d.getId(), d);
		}
		List<Pair<DuplexPlugin, BdfList>> transports = new ArrayList<>();
		for (TransportId id : PREFERRED_TRANSPORTS) {
			TransportDescriptor d = descriptors.get(id);
			Plugin p = pluginManager.getPlugin(id);
			if (d != null && p instanceof DuplexPlugin) {
				if (LOG.isLoggable(INFO))
					LOG.info("Connecting via " + id);
				transports.add(new Pair<>((DuplexPlugin) p, d.getDescriptor()));
			}
		}

		// TODO: If we don't have any transports in common with the peer,
		//  warn the user and give up (#1224)

		if (!transports.isEmpty()) {
			byte[] commitment = remotePayload.getCommitment();
			connectionChooser.submit(new ReadableTask(new ConnectorTask(
					transports, commitment)));
		}

		// Get chosen connection
		try {
			KeyAgreementConnection chosen =
					connectionChooser.poll(CONNECTION_TIMEOUT);
			if (chosen == null) return null;
			return new KeyAgreementTransport(recordReaderFactory,
					recordWriterFactory, chosen);
		} catch (InterruptedException e) {
			LOG.info("Interrupted while waiting for connection");
			Thread.currentThread().interrupt();
			return null;
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			return null;
		} finally {
			stopListening();
		}
	}

	private void waitingForAlice() {
		if (!waitingSent.getAndSet(true)) callbacks.connectionWaiting();
	}

	private class ConnectorTask implements Callable<KeyAgreementConnection> {

		private final List<Pair<DuplexPlugin, BdfList>> transports;
		private final byte[] commitment;

		private ConnectorTask(List<Pair<DuplexPlugin, BdfList>> transports,
				byte[] commitment) {
			this.transports = transports;
			this.commitment = commitment;
		}

		@Nullable
		@Override
		public KeyAgreementConnection call() throws Exception {
			// Repeat attempts until we connect, get stopped, or get interrupted
			while (!stopped) {
				for (Pair<DuplexPlugin, BdfList> pair : transports) {
					if (stopped) return null;
					DuplexPlugin plugin = pair.getFirst();
					BdfList descriptor = pair.getSecond();
					DuplexTransportConnection conn =
							plugin.createKeyAgreementConnection(commitment,
									descriptor);
					if (conn != null) {
						if (LOG.isLoggable(INFO))
							LOG.info(plugin.getId() + ": Outgoing connection");
						return new KeyAgreementConnection(conn, plugin.getId());
					}
				}
				// Wait 2s before retry (to circumvent transient failures)
				Thread.sleep(2000);
			}
			return null;
		}
	}

	private class ReadableTask implements Callable<KeyAgreementConnection> {

		private final Callable<KeyAgreementConnection> connectionTask;

		private ReadableTask(Callable<KeyAgreementConnection> connectionTask) {
			this.connectionTask = connectionTask;
		}

		@Nullable
		@Override
		public KeyAgreementConnection call() throws Exception {
			KeyAgreementConnection c = connectionTask.call();
			if (c == null) return null;
			aliceLatch.await();
			if (alice || stopped) return c;
			// Bob waits here for Alice to scan his QR code, determine her
			// role, and send her key
			InputStream in = c.getConnection().getReader().getInputStream();
			while (!stopped && in.available() == 0) {
				if (LOG.isLoggable(INFO))
					LOG.info(c.getTransportId() + ": Waiting for data");
				waitingForAlice();
				Thread.sleep(500);
			}
			if (!stopped && LOG.isLoggable(INFO))
				LOG.info(c.getTransportId().getString() + ": Data available");
			return c;
		}
	}
}
