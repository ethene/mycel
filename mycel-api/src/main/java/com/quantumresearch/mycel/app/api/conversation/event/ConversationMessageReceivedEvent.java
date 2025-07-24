package com.quantumresearch.mycel.app.api.conversation.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.app.api.conversation.ConversationMessageHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a new conversation message is received.
 */
@Immutable
@NotNullByDefault
public abstract class ConversationMessageReceivedEvent<H extends ConversationMessageHeader>
		extends Event {

	private final H messageHeader;
	private final ContactId contactId;

	public ConversationMessageReceivedEvent(H messageHeader,
			ContactId contactId) {
		this.messageHeader = messageHeader;
		this.contactId = contactId;
	}

	public H getMessageHeader() {
		return messageHeader;
	}

	public ContactId getContactId() {
		return contactId;
	}
}
