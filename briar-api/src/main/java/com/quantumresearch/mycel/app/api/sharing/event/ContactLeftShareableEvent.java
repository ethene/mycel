package com.quantumresearch.mycel.app.api.sharing.event;


import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ContactLeftShareableEvent extends Event {

	private final GroupId groupId;
	private final ContactId contactId;

	public ContactLeftShareableEvent(GroupId groupId, ContactId contactId) {
		this.groupId = groupId;
		this.contactId = contactId;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public ContactId getContactId() {
		return contactId;
	}

}
