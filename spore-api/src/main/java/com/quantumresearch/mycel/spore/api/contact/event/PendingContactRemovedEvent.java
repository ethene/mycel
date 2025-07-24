package com.quantumresearch.mycel.spore.api.contact.event;

import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a pending contact is removed.
 */
@Immutable
@NotNullByDefault
public class PendingContactRemovedEvent extends Event {

	private final PendingContactId id;

	public PendingContactRemovedEvent(PendingContactId id) {
		this.id = id;
	}

	public PendingContactId getId() {
		return id;
	}

}
