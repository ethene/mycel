package com.quantumresearch.mycel.infrastructure.api.sync.event;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a message is offered by a contact and needs
 * to be requested.
 */
@Immutable
@NotNullByDefault
public class MessageToRequestEvent extends Event {

	private final ContactId contactId;

	public MessageToRequestEvent(ContactId contactId) {
		this.contactId = contactId;
	}

	public ContactId getContactId() {
		return contactId;
	}
}
