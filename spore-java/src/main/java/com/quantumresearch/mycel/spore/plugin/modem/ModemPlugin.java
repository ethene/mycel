package com.quantumresearch.mycel.spore.plugin.modem;

import com.quantumresearch.mycel.spore.api.Pair;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.keyagreement.KeyAgreementListener;
import com.quantumresearch.mycel.spore.api.plugin.ConnectionHandler;
import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.plugin.PluginException;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.AbstractDuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.rendezvous.KeyMaterialSource;
import com.quantumresearch.mycel.spore.api.rendezvous.RendezvousEndpoint;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.NotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.ACTIVE;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.ENABLING;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.INACTIVE;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.STARTING_STOPPING;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;
import static com.quantumresearch.mycel.spore.util.StringUtils.isNullOrEmpty;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
class ModemPlugin implements DuplexPlugin, Modem.Callback {

	static final TransportId ID =
			new TransportId("com.quantumresearch.mycel.spore.modem");

	private static final Logger LOG =
			getLogger(ModemPlugin.class.getName());

	private final ModemFactory modemFactory;
	private final SerialPortList serialPortList;
	private final PluginCallback callback;
	private final long maxLatency;
	private final AtomicBoolean used = new AtomicBoolean(false);
	private final PluginState state = new PluginState();

	private volatile Modem modem = null;

	ModemPlugin(ModemFactory modemFactory, SerialPortList serialPortList,
			PluginCallback callback, long maxLatency) {
		this.modemFactory = modemFactory;
		this.serialPortList = serialPortList;
		this.callback = callback;
		this.maxLatency = maxLatency;
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return maxLatency;
	}

	@Override
	public int getMaxIdleTime() {
		// FIXME: Do we need keepalives for this transport?
		return Integer.MAX_VALUE;
	}

	@Override
	public void start() throws PluginException {
		if (used.getAndSet(true)) throw new IllegalStateException();
		state.setStarted();
		for (String portName : serialPortList.getPortNames()) {
			if (LOG.isLoggable(INFO))
				LOG.info("Trying to initialise modem on " + portName);
			modem = modemFactory.createModem(this, portName);
			try {
				if (!modem.start()) continue;
				if (LOG.isLoggable(INFO))
					LOG.info("Initialised modem on " + portName);
				state.setInitialised();
				return;
			} catch (IOException e) {
				logException(LOG, WARNING, e);
			}
		}
		LOG.warning("Failed to initialised modem");
		state.setFailed();
		throw new PluginException();
	}

	@Override
	public void stop() {
		state.setStopped();
		if (modem != null) {
			try {
				modem.stop();
			} catch (IOException e) {
				logException(LOG, WARNING, e);
			}
		}
	}

	@Override
	public State getState() {
		return state.getState();
	}

	@Override
	public int getReasonsDisabled() {
		return 0;
	}

	@Override
	public boolean shouldPoll() {
		return false;
	}

	@Override
	public int getPollingInterval() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void poll(Collection<Pair<TransportProperties, ConnectionHandler>>
			properties) {
		throw new UnsupportedOperationException();
	}

	private void resetModem() {
		if (getState() != ACTIVE) return;
		for (String portName : serialPortList.getPortNames()) {
			if (LOG.isLoggable(INFO))
				LOG.info("Trying to initialise modem on " + portName);
			modem = modemFactory.createModem(this, portName);
			try {
				if (!modem.start()) continue;
				if (LOG.isLoggable(INFO))
					LOG.info("Initialised modem on " + portName);
				return;
			} catch (IOException e) {
				logException(LOG, WARNING, e);
			}
		}
		LOG.warning("Failed to initialise modem");
		state.setFailed();
	}

	@Override
	public DuplexTransportConnection createConnection(TransportProperties p) {
		if (getState() != ACTIVE) return null;
		// Get the ISO 3166 code for the caller's country
		String fromIso = callback.getLocalProperties().get("iso3166");
		if (isNullOrEmpty(fromIso)) return null;
		// Get the ISO 3166 code for the callee's country
		String toIso = p.get("iso3166");
		if (isNullOrEmpty(toIso)) return null;
		// Get the callee's phone number
		String number = p.get("number");
		if (isNullOrEmpty(number)) return null;
		// Convert the number into direct dialling form
		number = CountryCodes.translate(number, fromIso, toIso);
		if (number == null) return null;
		// Dial the number
		try {
			if (!modem.dial(number)) return null;
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			resetModem();
			return null;
		}
		return new ModemTransportConnection();
	}

	@Override
	public boolean supportsKeyAgreement() {
		return false;
	}

	@Override
	public KeyAgreementListener createKeyAgreementListener(byte[] commitment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DuplexTransportConnection createKeyAgreementConnection(
			byte[] commitment, BdfList descriptor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean supportsRendezvous() {
		return false;
	}

	@Override
	public RendezvousEndpoint createRendezvousEndpoint(KeyMaterialSource k,
			boolean alice, ConnectionHandler incoming) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incomingCallConnected() {
		LOG.info("Incoming call connected");
		callback.handleConnection(new ModemTransportConnection());
	}

	private class ModemTransportConnection
			extends AbstractDuplexTransportConnection {

		private ModemTransportConnection() {
			super(ModemPlugin.this);
		}

		@Override
		protected InputStream getInputStream() throws IOException {
			return modem.getInputStream();
		}

		@Override
		protected OutputStream getOutputStream() throws IOException {
			return modem.getOutputStream();
		}

		@Override
		protected void closeConnection(boolean exception) {
			LOG.info("Call disconnected");
			try {
				modem.hangUp();
			} catch (IOException e) {
				logException(LOG, WARNING, e);
				exception = true;
			}
			if (exception) resetModem();
		}
	}

	@ThreadSafe
	@NotNullByDefault
	private class PluginState {

		@GuardedBy("this")
		private boolean started = false,
				stopped = false,
				initialised = false,
				failed = false;

		private synchronized void setStarted() {
			started = true;
			callback.pluginStateChanged(getState());
		}

		private synchronized void setStopped() {
			stopped = true;
			callback.pluginStateChanged(getState());
		}

		private synchronized void setInitialised() {
			initialised = true;
			callback.pluginStateChanged(getState());
		}

		private synchronized void setFailed() {
			failed = true;
			callback.pluginStateChanged(getState());
		}

		private State getState() {
			if (!started || stopped) return STARTING_STOPPING;
			if (failed) return INACTIVE;
			return initialised ? ACTIVE : ENABLING;
		}
	}
}
