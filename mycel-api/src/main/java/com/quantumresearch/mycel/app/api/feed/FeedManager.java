package com.quantumresearch.mycel.app.api.feed;

import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@NotNullByDefault
public interface FeedManager {

	/**
	 * The unique ID of the RSS feed client.
	 */
	ClientId CLIENT_ID = new ClientId("com.quantumresearch.mycel.app.feed");

	/**
	 * The current major version of the RSS feed client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * Adds an RSS feed as a new dedicated blog, or updates the existing blog
	 * if a blog for the feed already exists.
	 */
	Feed addFeed(String url) throws DbException, IOException;

	/**
	 * Adds an RSS feed as a new dedicated blog, or updates the existing blog
	 * if a blog for the feed already exists.
	 */
	Feed addFeed(InputStream in) throws DbException, IOException;

	/**
	 * Removes an RSS feed.
	 */
	void removeFeed(Feed feed) throws DbException;

	/**
	 * Returns a list of all added RSS feeds
	 */
	List<Feed> getFeeds() throws DbException;

	/**
	 * Returns a list of all added RSS feeds
	 */
	List<Feed> getFeeds(Transaction txn) throws DbException;
}
