package com.quantumresearch.mycel.spore.api.sync.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a message is added to the database.
 */
@Immutable
@NotNullByDefault
public class MessageAddedEvent extends Event {

	private final Message message;
	@Nullable
	private final ContactId contactId;

	public MessageAddedEvent(Message message, @Nullable ContactId contactId) {
		this.message = message;
		this.contactId = contactId;
	}

	/**
	 * Returns the message that was added.
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * Returns the ID of the group to which the message belongs.
	 */
	public GroupId getGroupId() {
		return message.getGroupId();
	}

	/**
	 * Returns the ID of the contact from which the message was received, or
	 * null if the message was locally generated.
	 */
	@Nullable
	public ContactId getContactId() {
		return contactId;
	}
}
