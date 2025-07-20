package com.quantumresearch.mycel.infrastructure.api.contact.event;

import com.quantumresearch.mycel.infrastructure.api.contact.PendingContact;
import com.quantumresearch.mycel.infrastructure.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a pending contact is added.
 */
@Immutable
@NotNullByDefault
public class PendingContactAddedEvent extends Event {

	private final PendingContact pendingContact;

	public PendingContactAddedEvent(PendingContact pendingContact) {
		this.pendingContact = pendingContact;
	}

	public PendingContact getPendingContact() {
		return pendingContact;
	}
}
