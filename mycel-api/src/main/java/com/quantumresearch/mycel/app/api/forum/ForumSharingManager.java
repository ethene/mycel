package com.quantumresearch.mycel.app.api.forum;

import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.app.api.sharing.SharingManager;

public interface ForumSharingManager extends SharingManager<Forum> {

	/**
	 * The unique ID of the forum sharing client.
	 */
	ClientId CLIENT_ID = new ClientId("com.quantumresearch.mycel.app.forum.sharing");

	/**
	 * The current major version of the forum sharing client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the forum sharing client.
	 */
	int MINOR_VERSION = 1;
}
