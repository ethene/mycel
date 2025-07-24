package com.quantumresearch.mycel.spore.api.plugin.event;

import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.plugin.Plugin.State;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a plugin enters the {@link State#ACTIVE}
 * state.
 */
@Immutable
@NotNullByDefault
public class TransportActiveEvent extends Event {

	private final TransportId transportId;

	public TransportActiveEvent(TransportId transportId) {
		this.transportId = transportId;
	}

	public TransportId getTransportId() {
		return transportId;
	}
}
