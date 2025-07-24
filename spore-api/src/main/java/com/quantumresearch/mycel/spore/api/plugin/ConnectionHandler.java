package com.quantumresearch.mycel.spore.api.plugin;

import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPlugin;
import org.briarproject.nullsafety.NotNullByDefault;

/**
 * An interface for handling connections created by transport plugins.
 */
@NotNullByDefault
public interface ConnectionHandler {

	/**
	 * Handles a connection created by a {@link DuplexPlugin}.
	 */
	void handleConnection(DuplexTransportConnection c);

	/**
	 * Handles a reader created by a {@link SimplexPlugin}.
	 */
	void handleReader(TransportConnectionReader r);

	/**
	 * Handles a writer created by a {@link SimplexPlugin}.
	 */
	void handleWriter(TransportConnectionWriter w);
}
