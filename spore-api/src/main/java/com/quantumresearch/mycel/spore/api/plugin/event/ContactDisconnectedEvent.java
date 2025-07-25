package com.quantumresearch.mycel.spore.api.plugin.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a contact disconnects and is no longer
 * connected via any transport.
 */
@Immutable
@NotNullByDefault
public class ContactDisconnectedEvent extends Event {

	private final ContactId contactId;

	public ContactDisconnectedEvent(ContactId contactId) {
		this.contactId = contactId;
	}

	public ContactId getContactId() {
		return contactId;
	}
}
