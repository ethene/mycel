package com.quantumresearch.mycel.spore.api.identity.event;

import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when an identity is removed.
 */
@Immutable
@NotNullByDefault
public class IdentityRemovedEvent extends Event {

	private final AuthorId authorId;

	public IdentityRemovedEvent(AuthorId authorId) {
		this.authorId = authorId;
	}

	public AuthorId getAuthorId() {
		return authorId;
	}
}
