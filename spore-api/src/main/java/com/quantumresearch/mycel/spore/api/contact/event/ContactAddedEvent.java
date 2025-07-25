package com.quantumresearch.mycel.spore.api.contact.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a contact is added.
 */
@Immutable
@NotNullByDefault
public class ContactAddedEvent extends Event {

	private final ContactId contactId;
	private final boolean verified;

	public ContactAddedEvent(ContactId contactId, boolean verified) {
		this.contactId = contactId;
		this.verified = verified;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public boolean isVerified() {
		return verified;
	}
}
