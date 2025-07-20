package com.quantumresearch.mycel.infrastructure.api.mailbox.event;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxUpdate;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when {@link MailboxUpdate} are received
 * from a contact.
 */
@Immutable
@NotNullByDefault
public class RemoteMailboxUpdateEvent extends Event {

	private final ContactId contactId;
	private final MailboxUpdate mailboxUpdate;

	public RemoteMailboxUpdateEvent(ContactId contactId,
			MailboxUpdate mailboxUpdate) {
		this.contactId = contactId;
		this.mailboxUpdate = mailboxUpdate;
	}

	public ContactId getContact() {
		return contactId;
	}

	public MailboxUpdate getMailboxUpdate() {
		return mailboxUpdate;
	}
}
