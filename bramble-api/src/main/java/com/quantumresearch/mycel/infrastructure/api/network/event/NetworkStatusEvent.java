package com.quantumresearch.mycel.infrastructure.api.network.event;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.network.NetworkStatus;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class NetworkStatusEvent extends Event {

	private final NetworkStatus status;

	public NetworkStatusEvent(NetworkStatus status) {
		this.status = status;
	}

	public NetworkStatus getStatus() {
		return status;
	}
}