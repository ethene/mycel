package com.quantumresearch.mycel.app.api.blog;

import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.forum.ForumPost;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class BlogPost extends ForumPost {

	public BlogPost(Message message, @Nullable MessageId parent,
			Author author) {
		super(message, parent, author);
	}
}
