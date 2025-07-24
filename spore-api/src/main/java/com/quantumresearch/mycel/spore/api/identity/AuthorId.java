package com.quantumresearch.mycel.spore.api.identity;

import com.quantumresearch.mycel.spore.api.UniqueId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies an
 * {@link Author}.
 */
@ThreadSafe
@NotNullByDefault
public class AuthorId extends UniqueId {

	/**
	 * Label for hashing authors to calculate their identities.
	 */
	public static final String LABEL = "com.quantumresearch.mycel.spore/AUTHOR_ID";

	public AuthorId(byte[] id) {
		super(id);
	}
}
