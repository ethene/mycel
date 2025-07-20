package com.quantumresearch.mycel.app.api.introduction.event;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.app.api.client.SessionId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IntroductionAbortedEvent extends Event {

	private final SessionId sessionId;

	public IntroductionAbortedEvent(SessionId sessionId) {
		this.sessionId = sessionId;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

}
