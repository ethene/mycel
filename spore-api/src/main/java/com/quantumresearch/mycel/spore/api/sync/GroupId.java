package com.quantumresearch.mycel.spore.api.sync;

import com.quantumresearch.mycel.spore.api.UniqueId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies a {@link Group}.
 */
@ThreadSafe
@NotNullByDefault
public class GroupId extends UniqueId {

	/**
	 * Label for hashing groups to calculate their identifiers.
	 */
	public static final String LABEL = "com.quantumresearch.mycel.spore/GROUP_ID";

	public GroupId(byte[] id) {
		super(id);
	}
}
