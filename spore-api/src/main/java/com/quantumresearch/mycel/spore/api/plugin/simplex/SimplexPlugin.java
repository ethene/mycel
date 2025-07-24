package com.quantumresearch.mycel.spore.api.plugin.simplex;

import com.quantumresearch.mycel.spore.api.plugin.Plugin;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.system.Wakeful;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

/**
 * An interface for transport plugins that support simplex communication.
 */
@NotNullByDefault
public interface SimplexPlugin extends Plugin {

	/**
	 * Returns true if the transport is likely to lose streams and the cost of
	 * transmitting redundant copies of data is cheap.
	 */
	boolean isLossyAndCheap();

	/**
	 * Attempts to create and return a reader for the given transport
	 * properties. Returns null if a reader cannot be created.
	 */
	@Wakeful
	@Nullable
	TransportConnectionReader createReader(TransportProperties p);

	/**
	 * Attempts to create and return a writer for the given transport
	 * properties. Returns null if a writer cannot be created.
	 */
	@Wakeful
	@Nullable
	TransportConnectionWriter createWriter(TransportProperties p);
}
