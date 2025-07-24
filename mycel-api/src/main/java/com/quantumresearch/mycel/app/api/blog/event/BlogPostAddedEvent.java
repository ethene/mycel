package com.quantumresearch.mycel.app.api.blog.event;

import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.app.api.blog.BlogPostHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a blog post is added to the database.
 */
@Immutable
@NotNullByDefault
public class BlogPostAddedEvent extends Event {

	private final GroupId groupId;
	private final BlogPostHeader header;
	private final boolean local;

	public BlogPostAddedEvent(GroupId groupId, BlogPostHeader header,
			boolean local) {

		this.groupId = groupId;
		this.header = header;
		this.local = local;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public BlogPostHeader getHeader() {
		return header;
	}

	public boolean isLocal() {
		return local;
	}
}
