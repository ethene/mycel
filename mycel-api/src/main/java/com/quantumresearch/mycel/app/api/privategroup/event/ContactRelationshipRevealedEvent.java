package com.quantumresearch.mycel.app.api.privategroup.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.app.api.privategroup.Visibility;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ContactRelationshipRevealedEvent extends Event {

	private final GroupId groupId;
	private final AuthorId memberId;
	private final ContactId contactId;
	private final Visibility visibility;

	public ContactRelationshipRevealedEvent(GroupId groupId, AuthorId memberId,
			ContactId contactId, Visibility visibility) {
		this.groupId = groupId;
		this.memberId = memberId;
		this.contactId = contactId;
		this.visibility = visibility;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public AuthorId getMemberId() {
		return memberId;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public Visibility getVisibility() {
		return visibility;
	}

}
