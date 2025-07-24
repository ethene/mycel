package com.quantumresearch.mycel.spore.api.contact.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when the alias for a contact changed.
 */
@Immutable
@NotNullByDefault
public class ContactAliasChangedEvent extends Event {

	private final ContactId contactId;
	@Nullable
	private final String alias;

	public ContactAliasChangedEvent(ContactId contactId,
			@Nullable String alias) {
		this.contactId = contactId;
		this.alias = alias;
	}

	public ContactId getContactId() {
		return contactId;
	}

	@Nullable
	public String getAlias() {
		return alias;
	}
}
