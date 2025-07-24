package com.quantumresearch.mycel.app.api.forum;

import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.PostHeader;
import com.quantumresearch.mycel.app.api.identity.AuthorInfo;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ForumPostHeader extends PostHeader {

	public ForumPostHeader(MessageId id, @Nullable MessageId parentId,
			long timestamp, Author author, AuthorInfo authorInfo,
			boolean read) {
		super(id, parentId, timestamp, author, authorInfo, read);
	}

}
