package com.quantumresearch.mycel.app.api.blog;

import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import com.quantumresearch.mycel.app.api.conversation.ConversationMessageVisitor;
import com.quantumresearch.mycel.app.api.sharing.InvitationRequest;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
public class BlogInvitationRequest extends InvitationRequest<Blog> {

	public BlogInvitationRequest(MessageId id, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, Blog blog, @Nullable String text,
			boolean available, boolean canBeOpened, long autoDeleteTimer) {
		super(id, groupId, time, local, read, sent, seen, sessionId, blog,
				text, available, canBeOpened, autoDeleteTimer);
	}

	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitBlogInvitationRequest(this);
	}
}
