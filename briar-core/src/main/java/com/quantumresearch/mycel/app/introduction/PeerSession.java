package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
interface PeerSession {

	SessionId getSessionId();

	GroupId getContactGroupId();

	long getLocalTimestamp();

	@Nullable
	MessageId getLastLocalMessageId();

	@Nullable
	MessageId getLastRemoteMessageId();

}
