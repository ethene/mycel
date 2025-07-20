package com.quantumresearch.mycel.infrastructure.api.properties.event;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when {@link TransportProperties} are received
 * from a contact.
 */
@Immutable
@NotNullByDefault
public class RemoteTransportPropertiesUpdatedEvent extends Event {

	private final TransportId transportId;

	public RemoteTransportPropertiesUpdatedEvent(TransportId transportId) {
		this.transportId = transportId;
	}

	public TransportId getTransportId() {
		return transportId;
	}
}
