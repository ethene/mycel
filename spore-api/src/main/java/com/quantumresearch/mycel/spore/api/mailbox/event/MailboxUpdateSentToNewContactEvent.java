package com.quantumresearch.mycel.spore.api.mailbox.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdate;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when the first mailbox update is sent to a
 * newly added contact, which happens in the same transaction in which the
 * contact is added.
 * <p>
 * This event is not broadcast when the first mailbox update is sent to an
 * existing contact when setting up the
 * {@link MailboxUpdateManager mailbox update client}.
 */
@Immutable
@NotNullByDefault
public class MailboxUpdateSentToNewContactEvent extends Event {

	private final ContactId contactId;
	private final MailboxUpdate mailboxUpdate;

	public MailboxUpdateSentToNewContactEvent(ContactId contactId,
			MailboxUpdate mailboxUpdate) {
		this.contactId = contactId;
		this.mailboxUpdate = mailboxUpdate;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public MailboxUpdate getMailboxUpdate() {
		return mailboxUpdate;
	}
}
