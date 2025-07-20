package com.quantumresearch.mycel.infrastructure.plugin.file;

import com.quantumresearch.mycel.infrastructure.api.plugin.PluginCallback;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.simplex.SimplexPlugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.simplex.SimplexPluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static java.util.concurrent.TimeUnit.DAYS;
import static com.quantumresearch.mycel.infrastructure.api.plugin.file.RemovableDriveConstants.ID;

@Immutable
@NotNullByDefault
public class RemovableDrivePluginFactory implements SimplexPluginFactory {

	static final long MAX_LATENCY = DAYS.toMillis(28);

	@Inject
	RemovableDrivePluginFactory() {
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
		return new RemovableDrivePlugin(callback, MAX_LATENCY);
	}
}
