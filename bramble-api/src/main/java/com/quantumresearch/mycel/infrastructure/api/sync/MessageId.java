package com.quantumresearch.mycel.infrastructure.api.sync;

import com.quantumresearch.mycel.infrastructure.api.UniqueId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies a
 * {@link Message}.
 */
@ThreadSafe
@NotNullByDefault
public class MessageId extends UniqueId {

	/**
	 * Label for hashing messages to calculate their identifiers.
	 */
	public static final String ID_LABEL = "org.briarproject.bramble/MESSAGE_ID";

	/**
	 * Label for hashing blocks of messages.
	 */
	public static final String BLOCK_LABEL =
			"org.briarproject.bramble/MESSAGE_BLOCK";

	public MessageId(byte[] id) {
		super(id);
	}
}
