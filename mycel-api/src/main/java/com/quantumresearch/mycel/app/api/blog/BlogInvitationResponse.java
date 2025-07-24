package com.quantumresearch.mycel.app.api.blog;

import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import com.quantumresearch.mycel.app.api.conversation.ConversationMessageVisitor;
import com.quantumresearch.mycel.app.api.sharing.InvitationResponse;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public class BlogInvitationResponse extends InvitationResponse {

	public BlogInvitationResponse(MessageId id, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, boolean accept, GroupId shareableId,
			long autoDeleteTimer, boolean isAutoDecline) {
		super(id, groupId, time, local, read, sent, seen, sessionId,
				accept, shareableId, autoDeleteTimer, isAutoDecline);
	}

	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitBlogInvitationResponse(this);
	}
}
