package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import com.quantumresearch.mycel.app.api.forum.Forum;
import com.quantumresearch.mycel.app.api.forum.ForumInvitationRequest;
import com.quantumresearch.mycel.app.api.forum.ForumInvitationResponse;

import javax.inject.Inject;

public class ForumInvitationFactoryImpl
		implements InvitationFactory<Forum, ForumInvitationResponse> {

	@Inject
	ForumInvitationFactoryImpl() {
	}

	@Override
	public ForumInvitationRequest createInvitationRequest(boolean local,
			boolean sent, boolean seen, boolean read, InviteMessage<Forum> m,
			ContactId c, boolean available, boolean canBeOpened,
			long autoDeleteTimer) {
		SessionId sessionId = new SessionId(m.getShareableId().getBytes());
		return new ForumInvitationRequest(m.getId(), m.getContactGroupId(),
				m.getTimestamp(), local, read, sent, seen, sessionId,
				m.getShareable(), m.getText(), available, canBeOpened,
				autoDeleteTimer);
	}

	@Override
	public ForumInvitationResponse createInvitationResponse(MessageId id,
			GroupId contactGroupId, long time, boolean local, boolean sent,
			boolean seen, boolean read, boolean accept, GroupId shareableId,
			long autoDeleteTimer, boolean isAutoDecline) {
		SessionId sessionId = new SessionId(shareableId.getBytes());
		return new ForumInvitationResponse(id, contactGroupId, time, local,
				read, sent, seen, sessionId, accept, shareableId,
				autoDeleteTimer, isAutoDecline);
	}

}
