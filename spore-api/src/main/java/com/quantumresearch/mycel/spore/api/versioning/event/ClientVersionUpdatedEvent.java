package com.quantumresearch.mycel.spore.api.versioning.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersion;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when we receive a client versioning update from
 * a contact.
 */
@Immutable
@NotNullByDefault
public class ClientVersionUpdatedEvent extends Event {

	private final ContactId contactId;
	private final ClientVersion clientVersion;

	public ClientVersionUpdatedEvent(ContactId contactId,
			ClientVersion clientVersion) {
		this.contactId = contactId;
		this.clientVersion = clientVersion;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public ClientVersion getClientVersion() {
		return clientVersion;
	}
}
