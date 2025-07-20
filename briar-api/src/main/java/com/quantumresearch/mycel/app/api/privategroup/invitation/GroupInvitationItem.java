package com.quantumresearch.mycel.app.api.privategroup.invitation;

import com.quantumresearch.mycel.infrastructure.api.contact.Contact;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroup;
import com.quantumresearch.mycel.app.api.sharing.InvitationItem;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class GroupInvitationItem extends InvitationItem<PrivateGroup> {

	private final Contact creator;

	public GroupInvitationItem(PrivateGroup privateGroup, Contact creator) {
		super(privateGroup, false);
		this.creator = creator;
	}

	public Contact getCreator() {
		return creator;
	}

}
