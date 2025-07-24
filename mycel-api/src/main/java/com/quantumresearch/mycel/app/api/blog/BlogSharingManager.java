package com.quantumresearch.mycel.app.api.blog;

import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.app.api.sharing.SharingManager;

public interface BlogSharingManager extends SharingManager<Blog> {

	/**
	 * The unique ID of the blog sharing client.
	 */
	ClientId CLIENT_ID = new ClientId("com.quantumresearch.mycel.app.blog.sharing");

	/**
	 * The current major version of the blog sharing client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the blog sharing client.
	 */
	int MINOR_VERSION = 1;
}
