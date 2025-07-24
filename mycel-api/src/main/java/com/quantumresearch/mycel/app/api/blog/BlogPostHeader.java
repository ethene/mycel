package com.quantumresearch.mycel.app.api.blog;

import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.PostHeader;
import com.quantumresearch.mycel.app.api.identity.AuthorInfo;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class BlogPostHeader extends PostHeader {

	private final MessageType type;
	private final GroupId groupId;
	private final long timeReceived;
	private final boolean rssFeed;

	public BlogPostHeader(MessageType type, GroupId groupId, MessageId id,
			@Nullable MessageId parentId, long timestamp, long timeReceived,
			Author author, AuthorInfo authorInfo, boolean rssFeed, boolean read) {
		super(id, parentId, timestamp, author, authorInfo, read);
		this.type = type;
		this.groupId = groupId;
		this.timeReceived = timeReceived;
		this.rssFeed = rssFeed;
	}

	public BlogPostHeader(MessageType type, GroupId groupId, MessageId id,
			long timestamp, long timeReceived, Author author,
			AuthorInfo authorInfo, boolean rssFeed, boolean read) {
		this(type, groupId, id, null, timestamp, timeReceived, author,
				authorInfo, rssFeed, read);
	}

	public MessageType getType() {
		return type;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public long getTimeReceived() {
		return timeReceived;
	}

	public boolean isRssFeed() {
		return rssFeed;
	}

}
