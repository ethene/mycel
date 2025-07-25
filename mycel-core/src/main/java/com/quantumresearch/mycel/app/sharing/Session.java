package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.app.sharing.State.START;

@Immutable
@NotNullByDefault
class Session {

	private final State state;
	private final GroupId contactGroupId, shareableId;
	@Nullable
	private final MessageId lastLocalMessageId, lastRemoteMessageId;
	private final long localTimestamp, inviteTimestamp;

	Session(State state, GroupId contactGroupId, GroupId shareableId,
			@Nullable MessageId lastLocalMessageId,
			@Nullable MessageId lastRemoteMessageId, long localTimestamp,
			long inviteTimestamp) {
		this.state = state;
		this.contactGroupId = contactGroupId;
		this.shareableId = shareableId;
		this.lastLocalMessageId = lastLocalMessageId;
		this.lastRemoteMessageId = lastRemoteMessageId;
		this.localTimestamp = localTimestamp;
		this.inviteTimestamp = inviteTimestamp;
	}

	Session(GroupId contactGroupId, GroupId shareableId) {
		this(START, contactGroupId, shareableId, null, null, 0, 0);
	}

	public State getState() {
		return state;
	}

	GroupId getContactGroupId() {
		return contactGroupId;
	}

	GroupId getShareableId() {
		return shareableId;
	}

	@Nullable
	MessageId getLastLocalMessageId() {
		return lastLocalMessageId;
	}

	@Nullable
	MessageId getLastRemoteMessageId() {
		return lastRemoteMessageId;
	}

	long getLocalTimestamp() {
		return localTimestamp;
	}

	long getInviteTimestamp() {
		return inviteTimestamp;
	}

}
