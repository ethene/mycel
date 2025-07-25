package com.quantumresearch.mycel.spore.api.mailbox;

import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
public class MailboxFileId extends MailboxId {
	public MailboxFileId(byte[] id) {
		super(id);
	}

	/**
	 * Creates a {@link MailboxFileId} from the given string.
	 *
	 * @throws IllegalArgumentException if token is not valid.
	 */
	public static MailboxFileId fromString(@Nullable String token)
			throws InvalidMailboxIdException {
		return new MailboxFileId(bytesFromString(token));
	}
}
