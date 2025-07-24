package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.app.api.autodelete.AutoDeleteManager;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager;
import com.quantumresearch.mycel.app.api.conversation.ConversationRequest;
import com.quantumresearch.mycel.app.api.forum.Forum;
import com.quantumresearch.mycel.app.api.forum.ForumInvitationResponse;
import com.quantumresearch.mycel.app.api.forum.ForumManager;
import com.quantumresearch.mycel.app.api.forum.ForumSharingManager;
import com.quantumresearch.mycel.app.api.forum.event.ForumInvitationRequestReceivedEvent;
import com.quantumresearch.mycel.app.api.forum.event.ForumInvitationResponseReceivedEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class ForumProtocolEngineImpl extends ProtocolEngineImpl<Forum> {

	private final ForumManager forumManager;
	private final InvitationFactory<Forum, ForumInvitationResponse>
			invitationFactory;

	@Inject
	ForumProtocolEngineImpl(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MessageEncoder messageEncoder,
			MessageParser<Forum> messageParser,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager,
			Clock clock,
			ForumManager forumManager,
			InvitationFactory<Forum, ForumInvitationResponse> invitationFactory) {
		super(db, clientHelper, clientVersioningManager, messageEncoder,
				messageParser, autoDeleteManager,
				conversationManager, clock, ForumSharingManager.CLIENT_ID,
				ForumSharingManager.MAJOR_VERSION, ForumManager.CLIENT_ID,
				ForumManager.MAJOR_VERSION);
		this.forumManager = forumManager;
		this.invitationFactory = invitationFactory;
	}

	@Override
	Event getInvitationRequestReceivedEvent(InviteMessage<Forum> m,
			ContactId contactId, boolean available, boolean canBeOpened) {
		ConversationRequest<Forum> request = invitationFactory
				.createInvitationRequest(false, false, true, false, m,
						contactId, available, canBeOpened,
						m.getAutoDeleteTimer());
		return new ForumInvitationRequestReceivedEvent(request, contactId);
	}

	@Override
	Event getInvitationResponseReceivedEvent(AcceptMessage m,
			ContactId contactId) {
		ForumInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), m.getContactGroupId(),
						m.getTimestamp(), false, false, true, false,
						true, m.getShareableId(), m.getAutoDeleteTimer(),
						false);
		return new ForumInvitationResponseReceivedEvent(response, contactId);
	}

	@Override
	Event getInvitationResponseReceivedEvent(DeclineMessage m,
			ContactId contactId) {
		ForumInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), m.getContactGroupId(),
						m.getTimestamp(), false, false, true, false,
						false, m.getShareableId(), m.getAutoDeleteTimer(),
						false);
		return new ForumInvitationResponseReceivedEvent(response, contactId);
	}

	@Override
	Event getAutoDeclineInvitationResponseReceivedEvent(Session s, Message m,
			ContactId contactId, long timer) {
		ForumInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), s.getContactGroupId(),
						m.getTimestamp(), true, false, false, true,
						false, s.getShareableId(), timer, true);
		return new ForumInvitationResponseReceivedEvent(response, contactId);
	}

	@Override
	protected void addShareable(Transaction txn, MessageId inviteId)
			throws DbException, FormatException {
		InviteMessage<Forum> invite =
				messageParser.getInviteMessage(txn, inviteId);
		forumManager.addForum(txn, invite.getShareable());
	}

}
