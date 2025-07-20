package com.quantumresearch.mycel.infrastructure.plugin.file;

import com.quantumresearch.mycel.infrastructure.api.plugin.PluginCallback;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.simplex.SimplexPlugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.simplex.SimplexPluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxConstants.ID;
import static com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxConstants.MAX_LATENCY;

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
