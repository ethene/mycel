package com.quantumresearch.mycel.app.api.messaging.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.app.api.conversation.event.ConversationMessageReceivedEvent;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessageHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a new private message is received.
 */
@Immutable
@NotNullByDefault
public class PrivateMessageReceivedEvent
		extends ConversationMessageReceivedEvent<PrivateMessageHeader> {

	public PrivateMessageReceivedEvent(PrivateMessageHeader messageHeader,
			ContactId contactId) {
		super(messageHeader, contactId);
	}

}
