package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
abstract class DeletableGroupInvitationMessage extends GroupInvitationMessage {

	private final long autoDeleteTimer;

	DeletableGroupInvitationMessage(MessageId id, GroupId contactGroupId,
			GroupId privateGroupId, long timestamp, long autoDeleteTimer) {
		super(id, contactGroupId, privateGroupId, timestamp);
		this.autoDeleteTimer = autoDeleteTimer;
	}

	public long getAutoDeleteTimer() {
		return autoDeleteTimer;
	}
}
