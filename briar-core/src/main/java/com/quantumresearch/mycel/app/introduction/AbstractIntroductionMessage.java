package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
abstract class AbstractIntroductionMessage {

	private final MessageId messageId;
	private final GroupId groupId;
	private final long timestamp;
	@Nullable
	private final MessageId previousMessageId;
	private final long autoDeleteTimer;

	AbstractIntroductionMessage(MessageId messageId, GroupId groupId,
			long timestamp, @Nullable MessageId previousMessageId,
			long autoDeleteTimer) {
		this.messageId = messageId;
		this.groupId = groupId;
		this.timestamp = timestamp;
		this.previousMessageId = previousMessageId;
		this.autoDeleteTimer = autoDeleteTimer;
	}

	MessageId getMessageId() {
		return messageId;
	}

	GroupId getGroupId() {
		return groupId;
	}

	long getTimestamp() {
		return timestamp;
	}

	@Nullable
	MessageId getPreviousMessageId() {
		return previousMessageId;
	}

	public long getAutoDeleteTimer() {
		return autoDeleteTimer;
	}
}
