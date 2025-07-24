package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.app.api.autodelete.AutoDeleteManager;
import com.quantumresearch.mycel.app.api.client.ProtocolStateException;
import com.quantumresearch.mycel.app.api.client.SessionId;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager;
import com.quantumresearch.mycel.app.api.privategroup.GroupMessageFactory;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroup;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupFactory;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager;
import com.quantumresearch.mycel.app.api.privategroup.event.GroupInvitationRequestReceivedEvent;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationRequest;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.INVISIBLE;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.SHARED;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.VISIBLE;
import static com.quantumresearch.mycel.app.privategroup.invitation.InviteeState.ACCEPTED;
import static com.quantumresearch.mycel.app.privategroup.invitation.InviteeState.DISSOLVED;
import static com.quantumresearch.mycel.app.privategroup.invitation.InviteeState.ERROR;
import static com.quantumresearch.mycel.app.privategroup.invitation.InviteeState.INVITED;
import static com.quantumresearch.mycel.app.privategroup.invitation.InviteeState.JOINED;
import static com.quantumresearch.mycel.app.privategroup.invitation.InviteeState.LEFT;
import static com.quantumresearch.mycel.app.privategroup.invitation.InviteeState.START;

@Immutable
@NotNullByDefault
class InviteeProtocolEngine extends AbstractProtocolEngine<InviteeSession> {

	InviteeProtocolEngine(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			PrivateGroupManager privateGroupManager,
			PrivateGroupFactory privateGroupFactory,
			GroupMessageFactory groupMessageFactory,
			IdentityManager identityManager,
			MessageParser messageParser,
			MessageEncoder messageEncoder,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager,
			Clock clock) {
		super(db, clientHelper, clientVersioningManager, privateGroupManager,
				privateGroupFactory, groupMessageFactory, identityManager,
				messageParser, messageEncoder,
				autoDeleteManager, conversationManager, clock);
	}

	@Override
	public InviteeSession onInviteAction(Transaction txn, InviteeSession s,
			@Nullable String text, long timestamp, byte[] signature,
			long autoDeleteTimer) {
		throw new UnsupportedOperationException(); // Invalid in this role
	}

	@Override
	public InviteeSession onJoinAction(Transaction txn, InviteeSession s)
			throws DbException {
		switch (s.getState()) {
			case START:
			case ACCEPTED:
			case JOINED:
			case LEFT:
			case DISSOLVED:
			case ERROR:
				throw new ProtocolStateException(); // Invalid in these states
			case INVITED:
				return onLocalAccept(txn, s);
			default:
				throw new AssertionError();
		}
	}

	@Override
	public InviteeSession onLeaveAction(Transaction txn, InviteeSession s,
			boolean isAutoDecline) throws DbException {
		switch (s.getState()) {
			case START:
			case LEFT:
			case DISSOLVED:
			case ERROR:
				return s; // Ignored in these states
			case INVITED:
				return onLocalDecline(txn, s, isAutoDecline);
			case ACCEPTED:
			case JOINED:
				return onLocalLeave(txn, s);
			default:
				throw new AssertionError();
		}
	}

	@Override
	public InviteeSession onMemberAddedAction(Transaction txn,
			InviteeSession s) {
		return s; // Ignored in this role
	}

	@Override
	public InviteeSession onInviteMessage(Transaction txn, InviteeSession s,
			InviteMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
				return onRemoteInvite(txn, s, m);
			case INVITED:
			case ACCEPTED:
			case JOINED:
			case LEFT:
			case DISSOLVED:
				return abort(txn, s); // Invalid in these states
			case ERROR:
				return s; // Ignored in this state
			default:
				throw new AssertionError();
		}
	}

	@Override
	public InviteeSession onJoinMessage(Transaction txn, InviteeSession s,
			JoinMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
			case INVITED:
			case JOINED:
			case LEFT:
			case DISSOLVED:
				return abort(txn, s); // Invalid in these states
			case ACCEPTED:
				return onRemoteJoin(txn, s, m);
			case ERROR:
				return s; // Ignored in this state
			default:
				throw new AssertionError();
		}
	}

	@Override
	public InviteeSession onLeaveMessage(Transaction txn, InviteeSession s,
			LeaveMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
			case DISSOLVED:
				return abort(txn, s); // Invalid in these states
			case INVITED:
			case LEFT:
				return onRemoteLeaveWhenNotSubscribed(txn, s, m);
			case ACCEPTED:
			case JOINED:
				return onRemoteLeaveWhenSubscribed(txn, s, m);
			case ERROR:
				return s; // Ignored in this state
			default:
				throw new AssertionError();
		}
	}

	@Override
	public InviteeSession onAbortMessage(Transaction txn, InviteeSession s,
			AbortMessage m) throws DbException, FormatException {
		return abort(txn, s);
	}

	private InviteeSession onLocalAccept(Transaction txn, InviteeSession s)
			throws DbException {
		// Mark the invite message unavailable to answer
		MessageId inviteId = s.getLastRemoteMessageId();
		if (inviteId == null) throw new IllegalStateException();
		markMessageAvailableToAnswer(txn, inviteId, false);
		// Record the response
		markInviteAccepted(txn, inviteId);
		// Send a JOIN message
		Message sent = sendJoinMessage(txn, s, true);
		// Track the message
		conversationManager.trackOutgoingMessage(txn, sent);
		try {
			// Subscribe to the private group
			subscribeToPrivateGroup(txn, inviteId);
			// Make the private group visible to the contact
			setPrivateGroupVisibility(txn, s, VISIBLE);
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group metadata
		}
		// Move to the ACCEPTED state
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), ACCEPTED);
	}

	private InviteeSession onLocalDecline(Transaction txn, InviteeSession s,
			boolean isAutoDecline) throws DbException {
		// Mark the invite message unavailable to answer
		MessageId inviteId = s.getLastRemoteMessageId();
		if (inviteId == null) throw new IllegalStateException();
		markMessageAvailableToAnswer(txn, inviteId, false);
		// Send a LEAVE message
		Message sent = sendLeaveMessage(txn, s, true, isAutoDecline);
		// Track the message
		conversationManager.trackOutgoingMessage(txn, sent);
		// Move to the START state
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), START);
	}

	private InviteeSession onLocalLeave(Transaction txn, InviteeSession s)
			throws DbException {
		// Send a LEAVE message
		Message sent = sendLeaveMessage(txn, s);
		try {
			// Make the private group invisible to the contact
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group metadata
		}
		// Move to the LEFT state
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), LEFT);
	}

	private InviteeSession onRemoteInvite(Transaction txn, InviteeSession s,
			InviteMessage m) throws DbException, FormatException {
		// The timestamp must be higher than the last invite message, if any
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		// Check that the contact is the creator
		ContactId contactId =
				clientHelper.getContactId(txn, s.getContactGroupId());
		Author contact = db.getContact(txn, contactId).getAuthor();
		if (!contact.getId().equals(m.getCreator().getId()))
			return abort(txn, s);
		// Mark the invite message visible in the UI and available to answer
		markMessageVisibleInUi(txn, m.getId());
		markMessageAvailableToAnswer(txn, m.getId(), true);
		// Track the message
		conversationManager.trackMessage(txn, m.getContactGroupId(),
				m.getTimestamp(), false);
		// Receive the auto-delete timer
		receiveAutoDeleteTimer(txn, m);
		// Broadcast an event
		PrivateGroup privateGroup = privateGroupFactory.createPrivateGroup(
				m.getGroupName(), m.getCreator(), m.getSalt());
		txn.attach(new GroupInvitationRequestReceivedEvent(
				createInvitationRequest(m, privateGroup), contactId));
		// Move to the INVITED state
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				m.getTimestamp(), INVITED);
	}

	private InviteeSession onRemoteJoin(Transaction txn, InviteeSession s,
			JoinMessage m) throws DbException, FormatException {
		// The timestamp must be higher than the last invite message, if any
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		// The dependency, if any, must be the last remote message
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		try {
			// Share the private group with the contact
			setPrivateGroupVisibility(txn, s, SHARED);
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group metadata
		}
		// Move to the JOINED state
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp(), JOINED);
	}

	private InviteeSession onRemoteLeaveWhenNotSubscribed(Transaction txn,
			InviteeSession s, LeaveMessage m)
			throws DbException, FormatException {
		// The timestamp must be higher than the last invite message, if any
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		// The dependency, if any, must be the last remote message
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		// Mark any invite messages in the session unavailable to answer
		markInvitesUnavailableToAnswer(txn, s);
		// Move to the DISSOLVED state
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp(), DISSOLVED);
	}

	private InviteeSession onRemoteLeaveWhenSubscribed(Transaction txn,
			InviteeSession s, LeaveMessage m)
			throws DbException, FormatException {
		// The timestamp must be higher than the last invite message, if any
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		// The dependency, if any, must be the last remote message
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		try {
			// Make the private group invisible to the contact
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group metadata
		}
		// Mark the group dissolved
		privateGroupManager.markGroupDissolved(txn, s.getPrivateGroupId());
		// Move to the DISSOLVED state
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp(), DISSOLVED);
	}

	private InviteeSession abort(Transaction txn, InviteeSession s)
			throws DbException, FormatException {
		// If the session has already been aborted, do nothing
		if (s.getState() == ERROR) return s;
		// Mark any invite messages in the session unavailable to answer
		markInvitesUnavailableToAnswer(txn, s);
		// If we subscribe, make the private group invisible to the contact
		if (isSubscribedPrivateGroup(txn, s.getPrivateGroupId()))
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		// Send an ABORT message
		Message sent = sendAbortMessage(txn, s);
		// Move to the ERROR state
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), ERROR);
	}

	private GroupInvitationRequest createInvitationRequest(InviteMessage m,
			PrivateGroup pg) {
		SessionId sessionId = new SessionId(m.getPrivateGroupId().getBytes());
		return new GroupInvitationRequest(m.getId(), m.getContactGroupId(),
				m.getTimestamp(), false, false, false, false, sessionId, pg,
				m.getText(), true, false, m.getAutoDeleteTimer());
	}

}
