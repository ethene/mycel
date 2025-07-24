package com.quantumresearch.mycel.spore.plugin.file;

import com.quantumresearch.mycel.spore.api.Pair;
import com.quantumresearch.mycel.spore.api.plugin.ConnectionHandler;
import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.plugin.PluginException;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

import static com.quantumresearch.mycel.spore.api.mailbox.MailboxConstants.ID;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.ACTIVE;

@NotNullByDefault
class MailboxPlugin extends FilePlugin {

	MailboxPlugin(PluginCallback callback, long maxLatency) {
		super(callback, maxLatency);
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public int getMaxIdleTime() {
		// Unused for simplex transports
		throw new UnsupportedOperationException();
	}

	@Override
	public void start() throws PluginException {
		callback.pluginStateChanged(ACTIVE);
	}

	@Override
	public void stop() throws PluginException {
	}

	@Override
	public State getState() {
		return ACTIVE;
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
	public void poll(
			Collection<Pair<TransportProperties, ConnectionHandler>> properties) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLossyAndCheap() {
		return false;
	}
}
