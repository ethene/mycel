package com.quantumresearch.mycel.spore.api.contact.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a contact is verified.
 */
@Immutable
@NotNullByDefault
public class ContactVerifiedEvent extends Event {

	private final ContactId contactId;

	public ContactVerifiedEvent(ContactId contactId) {
		this.contactId = contactId;
	}

	public ContactId getContactId() {
		return contactId;
	}

}
