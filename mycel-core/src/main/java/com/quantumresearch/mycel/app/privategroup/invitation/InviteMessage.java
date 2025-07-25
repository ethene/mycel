package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class InviteMessage extends DeletableGroupInvitationMessage {

	private final String groupName;
	private final Author creator;
	private final byte[] salt, signature;
	@Nullable
	private final String text;

	InviteMessage(MessageId id, GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, String groupName, Author creator, byte[] salt,
			@Nullable String text, byte[] signature, long autoDeleteTimer) {
		super(id, contactGroupId, privateGroupId, timestamp, autoDeleteTimer);
		this.groupName = groupName;
		this.creator = creator;
		this.salt = salt;
		this.text = text;
		this.signature = signature;
	}

	String getGroupName() {
		return groupName;
	}

	Author getCreator() {
		return creator;
	}

	byte[] getSalt() {
		return salt;
	}

	@Nullable
	String getText() {
		return text;
	}

	byte[] getSignature() {
		return signature;
	}
}
