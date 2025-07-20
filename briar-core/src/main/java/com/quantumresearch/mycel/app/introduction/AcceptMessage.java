package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.infrastructure.api.crypto.PublicKey;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportProperties;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class AcceptMessage extends AbstractIntroductionMessage {

	private final SessionId sessionId;
	private final PublicKey ephemeralPublicKey;
	private final long acceptTimestamp;
	private final Map<TransportId, TransportProperties> transportProperties;

	protected AcceptMessage(MessageId messageId, GroupId groupId,
			long timestamp, @Nullable MessageId previousMessageId,
			SessionId sessionId, PublicKey ephemeralPublicKey,
			long acceptTimestamp,
			Map<TransportId, TransportProperties> transportProperties,
			long autoDeleteTimer) {
		super(messageId, groupId, timestamp, previousMessageId,
				autoDeleteTimer);
		this.sessionId = sessionId;
		this.ephemeralPublicKey = ephemeralPublicKey;
		this.acceptTimestamp = acceptTimestamp;
		this.transportProperties = transportProperties;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

	public PublicKey getEphemeralPublicKey() {
		return ephemeralPublicKey;
	}

	public long getAcceptTimestamp() {
		return acceptTimestamp;
	}

	public Map<TransportId, TransportProperties> getTransportProperties() {
		return transportProperties;
	}

}
