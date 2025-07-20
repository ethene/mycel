package com.quantumresearch.mycel.infrastructure.api.contact.event;

import com.quantumresearch.mycel.infrastructure.api.contact.PendingContactId;
import com.quantumresearch.mycel.infrastructure.api.contact.PendingContactState;
import com.quantumresearch.mycel.infrastructure.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a pending contact's state is changed.
 */
@Immutable
@NotNullByDefault
public class PendingContactStateChangedEvent extends Event {

	private final PendingContactId id;
	private final PendingContactState state;

	public PendingContactStateChangedEvent(PendingContactId id,
			PendingContactState state) {
		this.id = id;
		this.state = state;
	}

	public PendingContactId getId() {
		return id;
	}

	public PendingContactState getPendingContactState() {
		return state;
	}

}
