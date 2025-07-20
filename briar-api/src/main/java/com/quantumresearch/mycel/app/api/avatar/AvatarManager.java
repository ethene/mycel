package com.quantumresearch.mycel.app.api.avatar;

import com.quantumresearch.mycel.infrastructure.api.contact.Contact;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.infrastructure.api.sync.ClientId;
import com.quantumresearch.mycel.app.api.attachment.AttachmentHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

@NotNullByDefault
public interface AvatarManager {

	/**
	 * The unique ID of the avatar client.
	 */
	ClientId CLIENT_ID = new ClientId("org.briarproject.briar.avatar");

	/**
	 * The current major version of the avatar client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the avatar client.
	 */
	int MINOR_VERSION = 0;

	/**
	 * Store a new profile image represented by the given InputStream
	 * and share it with all contacts.
	 */
	AttachmentHeader addAvatar(String contentType, InputStream in)
			throws DbException, IOException;

	/**
	 * Returns the current known profile image header for the given contact
	 * or null if none is known.
	 */
	@Nullable
	AttachmentHeader getAvatarHeader(Transaction txn, Contact c)
			throws DbException;

	/**
	 * Returns our current profile image header or null if none has been added.
	 */
	@Nullable
	AttachmentHeader getMyAvatarHeader(Transaction txn) throws DbException;
}
