package com.quantumresearch.mycel.app.api.autodelete.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class AutoDeleteTimerMirroredEvent extends Event {

	private final ContactId contactId;
	private final long newTimer;

	public AutoDeleteTimerMirroredEvent(ContactId contactId, long newTimer) {
		this.contactId = contactId;
		this.newTimer = newTimer;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public long getNewTimer() {
		return newTimer;
	}
}
