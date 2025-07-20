package com.quantumresearch.mycel.infrastructure.api.rendezvous.event;

import com.quantumresearch.mycel.infrastructure.api.contact.PendingContactId;
import com.quantumresearch.mycel.infrastructure.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a rendezvous connection is opened.
 */
@Immutable
@NotNullByDefault
public class RendezvousConnectionOpenedEvent extends Event {

	private final PendingContactId pendingContactId;

	public RendezvousConnectionOpenedEvent(PendingContactId pendingContactId) {
		this.pendingContactId = pendingContactId;
	}

	public PendingContactId getPendingContactId() {
		return pendingContactId;
	}
}
