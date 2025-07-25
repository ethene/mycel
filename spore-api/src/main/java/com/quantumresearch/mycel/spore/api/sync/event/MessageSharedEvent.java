package com.quantumresearch.mycel.spore.api.sync.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.sync.Group.Visibility;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Map;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a message is shared.
 */
@Immutable
@NotNullByDefault
public class MessageSharedEvent extends Event {

	private final MessageId messageId;
	private final GroupId groupId;
	private final Map<ContactId, Boolean> groupVisibility;

	public MessageSharedEvent(MessageId message, GroupId groupId,
			Map<ContactId, Boolean> groupVisibility) {
		this.messageId = message;
		this.groupId = groupId;
		this.groupVisibility = groupVisibility;
	}

	public MessageId getMessageId() {
		return messageId;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	/**
	 * Returns the IDs of all contacts for which the visibility of the
	 * message's group is either {@link Visibility#SHARED shared} or
	 * {@link Visibility#VISIBLE visible}. The value in the map is true if the
	 * group is {@link Visibility#SHARED shared} or false if the group is
	 * {@link Visibility#VISIBLE visible}.
	 */
	public Map<ContactId, Boolean> getGroupVisibility() {
		return groupVisibility;
	}
}
