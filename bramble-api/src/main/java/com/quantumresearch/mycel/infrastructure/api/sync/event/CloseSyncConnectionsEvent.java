package com.quantumresearch.mycel.infrastructure.api.sync.event;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when all sync connections using a given
 * transport should be closed.
 */
@Immutable
@NotNullByDefault
public class CloseSyncConnectionsEvent extends Event {

	private final TransportId transportId;

	public CloseSyncConnectionsEvent(TransportId transportId) {
		this.transportId = transportId;
	}

	public TransportId getTransportId() {
		return transportId;
	}
}
