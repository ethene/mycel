package com.quantumresearch.mycel.spore.api.sync;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface GroupFactory {

	/**
	 * Creates a group with the given client ID, major version and descriptor.
	 */
	Group createGroup(ClientId c, int majorVersion, byte[] descriptor);
}
