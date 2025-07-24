package com.quantumresearch.mycel.spore.transport.agreement;

import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.transport.KeySetId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class Session {

	private final State state;
	@Nullable
	private final MessageId lastLocalMessageId;
	@Nullable
	private final KeyPair localKeyPair;
	@Nullable
	private final Long localTimestamp;
	@Nullable
	private final KeySetId keySetId;

	Session(State state, @Nullable MessageId lastLocalMessageId,
			@Nullable KeyPair localKeyPair, @Nullable Long localTimestamp,
			@Nullable KeySetId keySetId) {
		this.state = state;
		this.lastLocalMessageId = lastLocalMessageId;
		this.localKeyPair = localKeyPair;
		this.localTimestamp = localTimestamp;
		this.keySetId = keySetId;
	}

	State getState() {
		return state;
	}

	@Nullable
	MessageId getLastLocalMessageId() {
		return lastLocalMessageId;
	}

	@Nullable
	KeyPair getLocalKeyPair() {
		return localKeyPair;
	}

	@Nullable
	Long getLocalTimestamp() {
		return localTimestamp;
	}

	@Nullable
	KeySetId getKeySetId() {
		return keySetId;
	}
}
