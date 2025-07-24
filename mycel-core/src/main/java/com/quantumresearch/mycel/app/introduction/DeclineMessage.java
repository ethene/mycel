package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class DeclineMessage extends AbstractIntroductionMessage {

	private final SessionId sessionId;

	protected DeclineMessage(MessageId messageId, GroupId groupId,
			long timestamp, @Nullable MessageId previousMessageId,
			SessionId sessionId, long autoDeleteTimer) {
		super(messageId, groupId, timestamp, previousMessageId,
				autoDeleteTimer);
		this.sessionId = sessionId;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

}
