package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.Group.Visibility;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.app.api.autodelete.AutoDeleteManager;
import com.quantumresearch.mycel.app.api.client.ProtocolStateException;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager;
import com.quantumresearch.mycel.app.api.sharing.Shareable;
import com.quantumresearch.mycel.app.api.sharing.event.ContactLeftShareableEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static java.lang.Math.max;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.INVISIBLE;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.SHARED;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.VISIBLE;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static com.quantumresearch.mycel.app.sharing.MessageType.ABORT;
import static com.quantumresearch.mycel.app.sharing.MessageType.ACCEPT;
import static com.quantumresearch.mycel.app.sharing.MessageType.DECLINE;
import static com.quantumresearch.mycel.app.sharing.MessageType.INVITE;
import static com.quantumresearch.mycel.app.sharing.MessageType.LEAVE;
import static com.quantumresearch.mycel.app.sharing.State.LOCAL_INVITED;
import static com.quantumresearch.mycel.app.sharing.State.LOCAL_LEFT;
import static com.quantumresearch.mycel.app.sharing.State.REMOTE_HANGING;
import static com.quantumresearch.mycel.app.sharing.State.REMOTE_INVITED;
import static com.quantumresearch.mycel.app.sharing.State.SHARING;
import static com.quantumresearch.mycel.app.sharing.State.START;

@Immutable
@NotNullByDefault
abstract class ProtocolEngineImpl<S extends Shareable>
		implements ProtocolEngine<S> {

	protected final DatabaseComponent db;
	protected final ClientHelper clientHelper;
	protected final MessageParser<S> messageParser;

	private final ClientVersioningManager clientVersioningManager;
	private final MessageEncoder messageEncoder;
	private final AutoDeleteManager autoDeleteManager;
	private final ConversationManager conversationManager;
	private final Clock clock;
	private final ClientId sharingClientId, shareableClientId;
	private final int sharingClientMajorVersion, shareableClientMajorVersion;

	ProtocolEngineImpl(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MessageEncoder messageEncoder,
			MessageParser<S> messageParser,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager,
			Clock clock,
			ClientId sharingClientId,
			int sharingClientMajorVersion,
			ClientId shareableClientId,
			int shareableClientMajorVersion) {
		this.db = db;
		this.clientHelper = clientHelper;
		this.clientVersioningManager = clientVersioningManager;
		this.messageEncoder = messageEncoder;
		this.messageParser = messageParser;
		this.autoDeleteManager = autoDeleteManager;
		this.conversationManager = conversationManager;
		this.clock = clock;
		this.sharingClientId = sharingClientId;
		this.sharingClientMajorVersion = sharingClientMajorVersion;
		this.shareableClientId = shareableClientId;
		this.shareableClientMajorVersion = shareableClientMajorVersion;
	}

	@Override
	public Session onInviteAction(Transaction txn, Session s,
			@Nullable String text) throws DbException {
		switch (s.getState()) {
			case START:
				return onLocalInvite(txn, s, text);
			case LOCAL_INVITED:
			case REMOTE_INVITED:
			case SHARING:
			case LOCAL_LEFT:
			case REMOTE_HANGING:
				throw new ProtocolStateException(); // Invalid in these states
			default:
				throw new AssertionError();
		}
	}

	private Session onLocalInvite(Transaction txn, Session s,
			@Nullable String text) throws DbException {
		// Send an INVITE message
		Message sent = sendInviteMessage(txn, s, text);
		// Track the message
		conversationManager.trackOutgoingMessage(txn, sent);
		// Make the shareable visible to the contact
		try {
			setShareableVisibility(txn, s, VISIBLE);
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group metadata
		}
		// Move to the REMOTE_INVITED state
		return new Session(REMOTE_INVITED, s.getContactGroupId(),
				s.getShareableId(), sent.getId(), s.getLastRemoteMessageId(),
				sent.getTimestamp(), s.getInviteTimestamp());
	}

	private Message sendInviteMessage(Transaction txn, Session s,
			@Nullable String text) throws DbException {
		Group g = db.getGroup(txn, s.getShareableId());
		BdfList descriptor;
		try {
			descriptor = clientHelper.toList(g.getDescriptor());
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group descriptor
		}
		Message m;
		long localTimestamp = getTimestampForVisibleMessage(txn, s);
		ContactId c = clientHelper.getContactId(txn, s.getContactGroupId());
		if (contactSupportsAutoDeletion(txn, c)) {
			long timer = autoDeleteManager.getAutoDeleteTimer(txn, c,
					localTimestamp);
			m = messageEncoder.encodeInviteMessage(s.getContactGroupId(),
					localTimestamp, s.getLastLocalMessageId(), descriptor,
					text, timer);
			sendMessage(txn, m, INVITE, s.getShareableId(), true, timer);
			// Set the auto-delete timer duration on the message
			if (timer != NO_AUTO_DELETE_TIMER) {
				db.setCleanupTimerDuration(txn, m.getId(), timer);
			}
		} else {
			m = messageEncoder.encodeInviteMessage(s.getContactGroupId(),
					localTimestamp, s.getLastLocalMessageId(), descriptor,
					text);
			sendMessage(txn, m, INVITE, s.getShareableId(), true,
					NO_AUTO_DELETE_TIMER);
		}
		return m;
	}

	@Override
	public Session onAcceptAction(Transaction txn, Session s)
			throws DbException {
		switch (s.getState()) {
			case LOCAL_INVITED:
				return onLocalAccept(txn, s);
			case START:
			case REMOTE_INVITED:
			case SHARING:
			case LOCAL_LEFT:
			case REMOTE_HANGING:
				throw new ProtocolStateException(); // Invalid in these states
			default:
				throw new AssertionError();
		}
	}

	private Session onLocalAccept(Transaction txn, Session s)
			throws DbException {
		// Mark the invite message unavailable to answer
		MessageId inviteId = s.getLastRemoteMessageId();
		if (inviteId == null) throw new IllegalStateException();
		markMessageAvailableToAnswer(txn, inviteId, false);
		// Mark the invite message as accepted
		markInvitationAccepted(txn, inviteId);
		// Send a ACCEPT message
		Message sent = sendAcceptMessage(txn, s);
		// Track the message
		conversationManager.trackOutgoingMessage(txn, sent);
		try {
			// Add and subscribe to the shareable
			addShareable(txn, inviteId);
			// Share the shareable with the contact
			setShareableVisibility(txn, s, SHARED);
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group metadata
		}
		// Move to the SHARING state
		return new Session(SHARING, s.getContactGroupId(), s.getShareableId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp());
	}

	protected abstract void addShareable(Transaction txn, MessageId inviteId)
			throws DbException, FormatException;

	private Message sendAcceptMessage(Transaction txn, Session s)
			throws DbException {
		Message m;
		long localTimestamp = getTimestampForVisibleMessage(txn, s);
		ContactId c = clientHelper.getContactId(txn, s.getContactGroupId());
		if (contactSupportsAutoDeletion(txn, c)) {
			long timer = autoDeleteManager.getAutoDeleteTimer(txn, c,
					localTimestamp);
			m = messageEncoder.encodeAcceptMessage(s.getContactGroupId(),
					s.getShareableId(), localTimestamp,
					s.getLastLocalMessageId(), timer);
			sendMessage(txn, m, ACCEPT, s.getShareableId(), true, timer);
			// Set the auto-delete timer duration on the message
			if (timer != NO_AUTO_DELETE_TIMER) {
				db.setCleanupTimerDuration(txn, m.getId(), timer);
			}
		} else {
			m = messageEncoder.encodeAcceptMessage(s.getContactGroupId(),
					s.getShareableId(), localTimestamp,
					s.getLastLocalMessageId());
			sendMessage(txn, m, ACCEPT, s.getShareableId(), true,
					NO_AUTO_DELETE_TIMER);
		}
		return m;
	}

	@Override
	public Session onDeclineAction(Transaction txn, Session s,
			boolean isAutoDecline) throws DbException {
		switch (s.getState()) {
			case LOCAL_INVITED:
				return onLocalDecline(txn, s, isAutoDecline);
			case START:
			case REMOTE_INVITED:
			case SHARING:
			case LOCAL_LEFT:
			case REMOTE_HANGING:
				throw new ProtocolStateException(); // Invalid in these states
			default:
				throw new AssertionError();
		}
	}

	private Session onLocalDecline(Transaction txn, Session s,
			boolean isAutoDecline) throws DbException {
		// Mark the invite message unavailable to answer
		MessageId inviteId = s.getLastRemoteMessageId();
		if (inviteId == null) throw new IllegalStateException();
		markMessageAvailableToAnswer(txn, inviteId, false);
		// Send a DECLINE message
		Message sent = sendDeclineMessage(txn, s, isAutoDecline);
		// Track the message
		conversationManager.trackOutgoingMessage(txn, sent);
		// Move to the START state
		return new Session(START, s.getContactGroupId(), s.getShareableId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp());
	}

	private Message sendDeclineMessage(Transaction txn, Session s,
			boolean isAutoDecline) throws DbException {
		Message m;
		long localTimestamp = getTimestampForVisibleMessage(txn, s);
		ContactId c = clientHelper.getContactId(txn, s.getContactGroupId());
		if (contactSupportsAutoDeletion(txn, c)) {
			long timer = autoDeleteManager.getAutoDeleteTimer(txn, c,
					localTimestamp);
			m = messageEncoder.encodeDeclineMessage(s.getContactGroupId(),
					s.getShareableId(), localTimestamp,
					s.getLastLocalMessageId(), timer);
			sendMessage(txn, m, DECLINE, s.getShareableId(), true, timer,
					isAutoDecline);
			// Set the auto-delete timer duration on the local message
			if (timer != NO_AUTO_DELETE_TIMER) {
				db.setCleanupTimerDuration(txn, m.getId(), timer);
			}
			if (isAutoDecline) {
				// Broadcast an event, so the auto-decline becomes visible
				Event e = getAutoDeclineInvitationResponseReceivedEvent(
						s, m, c, timer);
				txn.attach(e);
			}
		} else {
			m = messageEncoder.encodeDeclineMessage(s.getContactGroupId(),
					s.getShareableId(), localTimestamp,
					s.getLastLocalMessageId());
			sendMessage(txn, m, DECLINE, s.getShareableId(), true,
					NO_AUTO_DELETE_TIMER);
		}
		return m;
	}

	abstract Event getAutoDeclineInvitationResponseReceivedEvent(Session s,
			Message m, ContactId contactId, long timer);

	@Override
	public Session onLeaveAction(Transaction txn, Session s)
			throws DbException {
		switch (s.getState()) {
			case REMOTE_INVITED:
				return onLocalLeave(txn, s, REMOTE_HANGING);
			case SHARING:
				return onLocalLeave(txn, s, LOCAL_LEFT);
			case START:
			case LOCAL_INVITED:
			case LOCAL_LEFT:
			case REMOTE_HANGING:
				return s; // Ignored in this state
			default:
				throw new AssertionError();
		}
	}

	private Session onLocalLeave(Transaction txn, Session s, State nextState)
			throws DbException {
		try {
			// Stop sharing the shareable (not actually needed in REMOTE_LEFT)
			setShareableVisibility(txn, s, INVISIBLE);
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group metadata
		}
		// Send a LEAVE message
		Message sent = sendLeaveMessage(txn, s);
		// Move to the next state
		return new Session(nextState, s.getContactGroupId(), s.getShareableId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp());
	}

	private Message sendLeaveMessage(Transaction txn, Session session)
			throws DbException {
		long localTimestamp = getTimestampForInvisibleMessage(session);
		Message m = messageEncoder.encodeLeaveMessage(
				session.getContactGroupId(), session.getShareableId(),
				localTimestamp, session.getLastLocalMessageId());
		sendMessage(txn, m, LEAVE, session.getShareableId(), false,
				NO_AUTO_DELETE_TIMER);
		return m;
	}

	@Override
	public Session onInviteMessage(Transaction txn, Session s,
			InviteMessage<S> m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
			case LOCAL_LEFT:
				return onRemoteInvite(txn, s, m, true, LOCAL_INVITED);
			case REMOTE_INVITED:
				return onRemoteInviteWhenInvited(txn, s, m);
			case REMOTE_HANGING:
				return onRemoteInvite(txn, s, m, false, LOCAL_LEFT);
			case LOCAL_INVITED:
			case SHARING:
				return abortWithMessage(txn, s); // Invalid in these states
			default:
				throw new AssertionError();
		}
	}

	private Session onRemoteInvite(Transaction txn, Session s,
			InviteMessage<S> m, boolean available, State nextState)
			throws DbException, FormatException {
		// The timestamp must be higher than the last invite message, if any
		if (m.getTimestamp() <= s.getInviteTimestamp())
			return abortWithMessage(txn, s);
		// The dependency, if any, must be the last remote message
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abortWithMessage(txn, s);
		// Mark the invite message visible in the UI and (un)available to answer
		markMessageVisibleInUi(txn, m.getId());
		markMessageAvailableToAnswer(txn, m.getId(), available);
		// Track the message
		conversationManager.trackMessage(txn, m.getContactGroupId(),
				m.getTimestamp(), false);
		// Receive the auto-delete timer
		receiveAutoDeleteTimer(txn, m);
		// Broadcast an event
		ContactId contactId =
				clientHelper.getContactId(txn, s.getContactGroupId());
		txn.attach(getInvitationRequestReceivedEvent(m, contactId, available,
				false));
		// Move to the next state
		return new Session(nextState, s.getContactGroupId(), s.getShareableId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				m.getTimestamp());
	}

	private Session onRemoteInviteWhenInvited(Transaction txn, Session s,
			InviteMessage<S> m) throws DbException, FormatException {
		// The timestamp must be higher than the last invite message, if any
		if (m.getTimestamp() <= s.getInviteTimestamp())
			return abortWithMessage(txn, s);
		// The dependency, if any, must be the last remote message
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abortWithMessage(txn, s);
		// Mark the invite message visible in the UI and unavailable to answer
		markMessageVisibleInUi(txn, m.getId());
		markMessageAvailableToAnswer(txn, m.getId(), false);
		// Track the message
		conversationManager.trackMessage(txn, m.getContactGroupId(),
				m.getTimestamp(), false);
		// Receive the auto-delete timer
		receiveAutoDeleteTimer(txn, m);
		// Share the shareable with the contact
		setShareableVisibility(txn, s, SHARED);
		// Broadcast an event
		ContactId contactId =
				clientHelper.getContactId(txn, s.getContactGroupId());
		txn.attach(getInvitationRequestReceivedEvent(m, contactId, false,
				true));
		// Move to the next state
		return new Session(SHARING, s.getContactGroupId(), s.getShareableId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				m.getTimestamp());
	}

	abstract Event getInvitationRequestReceivedEvent(InviteMessage<S> m,
			ContactId contactId, boolean available, boolean canBeOpened);

	@Override
	public Session onAcceptMessage(Transaction txn, Session s,
			AcceptMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case REMOTE_INVITED:
				return onRemoteAcceptWhenInvited(txn, s, m);
			case REMOTE_HANGING:
				return onRemoteAccept(txn, s, m, LOCAL_LEFT);
			case START:
			case LOCAL_INVITED:
			case SHARING:
			case LOCAL_LEFT:
				return abortWithMessage(txn, s); // Invalid in these states
			default:
				throw new AssertionError();
		}
	}

	private Session onRemoteAccept(Transaction txn, Session s, AcceptMessage m,
			State nextState) throws DbException, FormatException {
		// The timestamp must be higher than the last invite message
		if (m.getTimestamp() <= s.getInviteTimestamp())
			return abortWithMessage(txn, s);
		// The dependency, if any, must be the last remote message
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abortWithMessage(txn, s);
		// Mark the response visible in the UI
		markMessageVisibleInUi(txn, m.getId());
		// Track the message
		conversationManager.trackMessage(txn, m.getContactGroupId(),
				m.getTimestamp(), false);
		// Receive the auto-delete timer
		receiveAutoDeleteTimer(txn, m);
		// Broadcast an event
		ContactId contactId =
				clientHelper.getContactId(txn, m.getContactGroupId());
		txn.attach(getInvitationResponseReceivedEvent(m, contactId));
		// Move to the next state
		return new Session(nextState, s.getContactGroupId(), s.getShareableId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp());
	}

	private Session onRemoteAcceptWhenInvited(Transaction txn, Session s,
			AcceptMessage m) throws DbException, FormatException {
		// Perform normal remote accept validation and operation
		Session session = onRemoteAccept(txn, s, m, SHARING);
		// Share the shareable with the contact, if session was not reset
		if (session.getState() != START)
			setShareableVisibility(txn, s, SHARED);
		return session;
	}

	abstract Event getInvitationResponseReceivedEvent(AcceptMessage m,
			ContactId contactId);

	@Override
	public Session onDeclineMessage(Transaction txn, Session s,
			DeclineMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case REMOTE_INVITED:
			case REMOTE_HANGING:
				return onRemoteDecline(txn, s, m);
			case START:
			case LOCAL_INVITED:
			case SHARING:
			case LOCAL_LEFT:
				return abortWithMessage(txn, s); // Invalid in these states
			default:
				throw new AssertionError();
		}
	}

	private Session onRemoteDecline(Transaction txn, Session s,
			DeclineMessage m) throws DbException, FormatException {
		// The timestamp must be higher than the last invite message
		if (m.getTimestamp() <= s.getInviteTimestamp())
			return abortWithMessage(txn, s);
		// The dependency, if any, must be the last remote message
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abortWithMessage(txn, s);
		// Mark the response visible in the UI
		markMessageVisibleInUi(txn, m.getId());
		// Track the message
		conversationManager.trackMessage(txn, m.getContactGroupId(),
				m.getTimestamp(), false);
		// Receive the auto-delete timer
		receiveAutoDeleteTimer(txn, m);
		// Make the shareable invisible (not actually needed in REMOTE_HANGING)
		try {
			setShareableVisibility(txn, s, INVISIBLE);
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group metadata
		}
		// Broadcast an event
		ContactId contactId =
				clientHelper.getContactId(txn, m.getContactGroupId());
		txn.attach(getInvitationResponseReceivedEvent(m, contactId));
		// Move to the next state
		return new Session(START, s.getContactGroupId(), s.getShareableId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp());
	}

	abstract Event getInvitationResponseReceivedEvent(DeclineMessage m,
			ContactId contactId);

	@Override
	public Session onLeaveMessage(Transaction txn, Session s,
			LeaveMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case LOCAL_INVITED:
				return onRemoteLeaveWhenInvited(txn, s, m);
			case LOCAL_LEFT:
				return onRemoteLeaveWhenLocalLeft(txn, s, m);
			case SHARING:
				return onRemoteLeaveWhenSharing(txn, s, m);
			case START:
			case REMOTE_INVITED:
			case REMOTE_HANGING:
				return abortWithMessage(txn, s); // Invalid in these states
			default:
				throw new AssertionError();
		}
	}

	private Session onRemoteLeaveWhenInvited(Transaction txn, Session s,
			LeaveMessage m) throws DbException, FormatException {
		// The dependency, if any, must be the last remote message
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abortWithMessage(txn, s);
		// Mark any invite messages in the session unavailable to answer
		markInvitesUnavailableToAnswer(txn, s);
		// Move to the next state
		return new Session(START, s.getContactGroupId(), s.getShareableId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp());
	}

	private Session onRemoteLeaveWhenLocalLeft(Transaction txn, Session s,
			LeaveMessage m) throws DbException, FormatException {
		// The dependency, if any, must be the last remote message
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abortWithMessage(txn, s);
		// Move to the next state
		return new Session(START, s.getContactGroupId(), s.getShareableId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp());
	}

	private Session onRemoteLeaveWhenSharing(Transaction txn, Session s,
			LeaveMessage m) throws DbException, FormatException {
		// The dependency, if any, must be the last remote message
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abortWithMessage(txn, s);
		// Broadcast event informing that contact left
		ContactId contactId =
				clientHelper.getContactId(txn, s.getContactGroupId());
		ContactLeftShareableEvent e = new ContactLeftShareableEvent(
				s.getShareableId(), contactId);
		txn.attach(e);
		// Stop sharing the shareable with the contact
		setShareableVisibility(txn, s, INVISIBLE);
		// Send a LEAVE message, so the other party doesn't hang in LOCAL_LEFT
		Message sent = sendLeaveMessage(txn, s);
		// Move to the next state
		return new Session(START, s.getContactGroupId(), s.getShareableId(),
				sent.getId(), m.getId(), sent.getTimestamp(),
				s.getInviteTimestamp());
	}

	@Override
	public Session onAbortMessage(Transaction txn, Session s, AbortMessage m)
			throws DbException, FormatException {
		abort(txn, s);
		return new Session(START, s.getContactGroupId(), s.getShareableId(),
				null, m.getId(), 0, 0);
	}

	private void abort(Transaction txn, Session s)
			throws DbException, FormatException {
		// Mark any invite messages in the session unavailable to answer
		markInvitesUnavailableToAnswer(txn, s);
		// If we subscribe, make the shareable invisible to the contact
		if (isSubscribed(txn, s.getShareableId()))
			setShareableVisibility(txn, s, INVISIBLE);
	}

	private Session abortWithMessage(Transaction txn, Session s)
			throws DbException, FormatException {
		abort(txn, s);
		// Send an ABORT message
		Message sent = sendAbortMessage(txn, s);
		// Reset the session back to initial state
		return new Session(START, s.getContactGroupId(), s.getShareableId(),
				sent.getId(), null, 0, 0);
	}

	private void markInvitesUnavailableToAnswer(Transaction txn, Session s)
			throws DbException, FormatException {
		GroupId shareableId = s.getShareableId();
		BdfDictionary query =
				messageParser.getInvitesAvailableToAnswerQuery(shareableId);
		Map<MessageId, BdfDictionary> results =
				clientHelper.getMessageMetadataAsDictionary(txn,
						s.getContactGroupId(), query);
		for (MessageId m : results.keySet())
			markMessageAvailableToAnswer(txn, m, false);
	}

	private boolean isSubscribed(Transaction txn, GroupId g)
			throws DbException {
		if (!db.containsGroup(txn, g)) return false;
		Group group = db.getGroup(txn, g);
		return group.getClientId().equals(shareableClientId);
	}

	private Message sendAbortMessage(Transaction txn, Session session)
			throws DbException {
		long localTimestamp = getTimestampForInvisibleMessage(session);
		Message m = messageEncoder.encodeAbortMessage(
				session.getContactGroupId(), session.getShareableId(),
				localTimestamp, session.getLastLocalMessageId());
		sendMessage(txn, m, ABORT, session.getShareableId(), false,
				NO_AUTO_DELETE_TIMER);
		return m;
	}

	private void sendMessage(Transaction txn, Message m, MessageType type,
			GroupId shareableId, boolean visibleInConversation,
			long autoDeleteTimer) throws DbException {
		sendMessage(txn, m, type, shareableId, visibleInConversation,
				autoDeleteTimer, false);
	}

	private void sendMessage(Transaction txn, Message m, MessageType type,
			GroupId shareableId, boolean visibleInConversation,
			long autoDeleteTimer, boolean isAutoDecline) throws DbException {
		BdfDictionary meta = messageEncoder.encodeMetadata(type, shareableId,
				m.getTimestamp(), true, true, visibleInConversation, false,
				false, autoDeleteTimer, isAutoDecline);
		try {
			clientHelper.addLocalMessage(txn, m, meta, true, false);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	private void markMessageAvailableToAnswer(Transaction txn, MessageId m,
			boolean available) throws DbException {
		BdfDictionary meta = new BdfDictionary();
		messageEncoder.setAvailableToAnswer(meta, available);
		try {
			clientHelper.mergeMessageMetadata(txn, m, meta);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	private void markMessageVisibleInUi(Transaction txn, MessageId m)
			throws DbException {
		BdfDictionary meta = new BdfDictionary();
		messageEncoder.setVisibleInUi(meta, true);
		try {
			clientHelper.mergeMessageMetadata(txn, m, meta);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	private void markInvitationAccepted(Transaction txn, MessageId m)
			throws DbException {
		BdfDictionary meta = new BdfDictionary();
		messageEncoder.setInvitationAccepted(meta, true);
		try {
			clientHelper.mergeMessageMetadata(txn, m, meta);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	private void setShareableVisibility(Transaction txn, Session session,
			Visibility preferred) throws DbException, FormatException {
		// Apply min of preferred visibility and client's visibility
		ContactId contactId =
				clientHelper.getContactId(txn, session.getContactGroupId());
		Visibility client = clientVersioningManager.getClientVisibility(txn,
				contactId, shareableClientId, shareableClientMajorVersion);
		Visibility min = Visibility.min(preferred, client);
		db.setGroupVisibility(txn, contactId, session.getShareableId(), min);
	}

	private boolean isInvalidDependency(Session session,
			@Nullable MessageId dependency) {
		MessageId expected = session.getLastRemoteMessageId();
		if (dependency == null) return expected != null;
		return !dependency.equals(expected);
	}

	/**
	 * Returns a timestamp for a visible outgoing message. The timestamp is
	 * later than the timestamp of any message sent or received so far in the
	 * conversation, and later than the {@link #getSessionTimestamp(Session)
	 * session timestamp}.
	 */
	private long getTimestampForVisibleMessage(Transaction txn, Session s)
			throws DbException {
		ContactId c = clientHelper.getContactId(txn, s.getContactGroupId());
		long conversationTimestamp =
				conversationManager.getTimestampForOutgoingMessage(txn, c);
		return max(conversationTimestamp, getSessionTimestamp(s) + 1);
	}

	/**
	 * Returns a timestamp for an invisible outgoing message. The timestamp is
	 * later than the {@link #getSessionTimestamp(Session) session timestamp}.
	 */
	private long getTimestampForInvisibleMessage(Session s) {
		return max(clock.currentTimeMillis(), getSessionTimestamp(s) + 1);
	}

	/**
	 * Returns the latest timestamp of any message sent so far in the session,
	 * and any invite message sent or received so far in the session.
	 */
	private long getSessionTimestamp(Session s) {
		return max(s.getLocalTimestamp(), s.getInviteTimestamp());
	}

	private void receiveAutoDeleteTimer(Transaction txn,
			DeletableSharingMessage m) throws DbException {
		ContactId c = clientHelper.getContactId(txn, m.getContactGroupId());
		autoDeleteManager.receiveAutoDeleteTimer(txn, c, m.getAutoDeleteTimer(),
				m.getTimestamp());
	}

	private boolean contactSupportsAutoDeletion(Transaction txn, ContactId c)
			throws DbException {
		int minorVersion = clientVersioningManager.getClientMinorVersion(txn, c,
				sharingClientId, sharingClientMajorVersion);
		// Auto-delete was added in client version 0.1
		return minorVersion >= 1;
	}
}
