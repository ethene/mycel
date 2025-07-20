package com.quantumresearch.mycel.infrastructure.api.rendezvous.event;

import com.quantumresearch.mycel.infrastructure.api.contact.PendingContactId;
import com.quantumresearch.mycel.infrastructure.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a rendezvous connection is closed.
 */
@Immutable
@NotNullByDefault
public class RendezvousConnectionClosedEvent extends Event {

	private final PendingContactId pendingContactId;
	private final boolean success;

	public RendezvousConnectionClosedEvent(PendingContactId pendingContactId,
			boolean success) {
		this.pendingContactId = pendingContactId;
		this.success = success;
	}

	public PendingContactId getPendingContactId() {
		return pendingContactId;
	}

	public boolean isSuccess() {
		return success;
	}
}
