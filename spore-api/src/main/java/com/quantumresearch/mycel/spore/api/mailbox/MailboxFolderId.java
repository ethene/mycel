package com.quantumresearch.mycel.spore.api.mailbox;

import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
public class MailboxFolderId extends MailboxId {
	public MailboxFolderId(byte[] id) {
		super(id);
	}

	/**
	 * Creates a {@link MailboxFolderId} from the given string.
	 *
	 * @throws IllegalArgumentException if token is not valid.
	 */
	public static MailboxFolderId fromString(@Nullable String token)
			throws InvalidMailboxIdException {
		return new MailboxFolderId(bytesFromString(token));
	}
}
