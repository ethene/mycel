package com.quantumresearch.mycel.spore.api.sync;

import com.quantumresearch.mycel.spore.api.UniqueId;
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
	public static final String ID_LABEL = "com.quantumresearch.mycel.spore/MESSAGE_ID";

	/**
	 * Label for hashing blocks of messages.
	 */
	public static final String BLOCK_LABEL =
			"com.quantumresearch.mycel.spore/MESSAGE_BLOCK";

	public MessageId(byte[] id) {
		super(id);
	}
}
