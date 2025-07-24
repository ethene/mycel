package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class LeaveMessage extends DeletableGroupInvitationMessage {

	@Nullable
	private final MessageId previousMessageId;

	LeaveMessage(MessageId id, GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, @Nullable MessageId previousMessageId,
			long autoDeleteTimer) {
		super(id, contactGroupId, privateGroupId, timestamp, autoDeleteTimer);
		this.previousMessageId = previousMessageId;
	}

	@Nullable
	MessageId getPreviousMessageId() {
		return previousMessageId;
	}
}
