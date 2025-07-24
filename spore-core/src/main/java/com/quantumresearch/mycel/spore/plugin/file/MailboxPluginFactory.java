package com.quantumresearch.mycel.spore.plugin.file;

import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.mailbox.MailboxConstants.ID;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxConstants.MAX_LATENCY;

@NotNullByDefault
public class MailboxPluginFactory implements SimplexPluginFactory {

	@Inject
	MailboxPluginFactory() {
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return MAX_LATENCY;
	}

	@Nullable
	@Override
	public SimplexPlugin createPlugin(PluginCallback callback) {
		return new MailboxPlugin(callback, MAX_LATENCY);
	}
}
