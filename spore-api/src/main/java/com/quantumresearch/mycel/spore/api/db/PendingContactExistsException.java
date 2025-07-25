package com.quantumresearch.mycel.spore.api.db;

import com.quantumresearch.mycel.spore.api.contact.PendingContact;

/**
 * Thrown when a duplicate pending contact is added to the database. This
 * exception may occur due to concurrent updates and does not indicate a
 * database error.
 */
public class PendingContactExistsException extends DbException {

	private final PendingContact pendingContact;

	public PendingContactExistsException(PendingContact pendingContact) {
		this.pendingContact = pendingContact;
	}

	public PendingContact getPendingContact() {
		return pendingContact;
	}
}
