package com.quantumresearch.mycel.infrastructure.api.identity.event;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.identity.AuthorId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when an identity is added.
 */
@Immutable
@NotNullByDefault
public class IdentityAddedEvent extends Event {

	private final AuthorId authorId;

	public IdentityAddedEvent(AuthorId authorId) {
		this.authorId = authorId;
	}

	public AuthorId getAuthorId() {
		return authorId;
	}
}
