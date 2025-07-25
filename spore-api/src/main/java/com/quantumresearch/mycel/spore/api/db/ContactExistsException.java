package com.quantumresearch.mycel.spore.api.db;

import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;

/**
 * Thrown when a duplicate contact is added to the database. This exception may
 * occur due to concurrent updates and does not indicate a database error.
 */
public class ContactExistsException extends DbException {

	private final AuthorId local;
	private final Author remote;

	public ContactExistsException(AuthorId local, Author remote) {
		this.local = local;
		this.remote = remote;
	}

	public AuthorId getLocalAuthorId() {
		return local;
	}

	public Author getRemoteAuthor() {
		return remote;
	}
}
