package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.transport.KeySetId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import static org.briarproject.nullsafety.NullSafety.requireExactlyOneNull;

@NotThreadSafe
@NotNullByDefault
class MutableTransportKeySet {

	private final KeySetId keySetId;
	@Nullable
	private final ContactId contactId;
	@Nullable
	private final PendingContactId pendingContactId;
	private final MutableTransportKeys keys;

	MutableTransportKeySet(KeySetId keySetId, @Nullable ContactId contactId,
			@Nullable PendingContactId pendingContactId,
			MutableTransportKeys keys) {
		requireExactlyOneNull(contactId, pendingContactId);
		this.keySetId = keySetId;
		this.contactId = contactId;
		this.pendingContactId = pendingContactId;
		this.keys = keys;
	}

	KeySetId getKeySetId() {
		return keySetId;
	}

	@Nullable
	ContactId getContactId() {
		return contactId;
	}

	@Nullable
	PendingContactId getPendingContactId() {
		return pendingContactId;
	}

	MutableTransportKeys getKeys() {
		return keys;
	}
}
