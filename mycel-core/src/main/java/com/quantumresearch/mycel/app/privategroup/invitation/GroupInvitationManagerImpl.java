package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.cleanup.CleanupHook;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.client.ContactGroupFactory;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.ContactManager.ContactHook;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.Group.Visibility;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.sync.MessageStatus;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager.ClientVersioningHook;
import com.quantumresearch.mycel.app.api.autodelete.event.ConversationMessagesDeletedEvent;
import com.quantumresearch.mycel.app.api.client.MessageTracker;
import com.quantumresearch.mycel.app.api.client.ProtocolStateException;
import com.quantumresearch.mycel.app.api.client.SessionId;
import com.quantumresearch.mycel.app.api.conversation.ConversationMessageHeader;
import com.quantumresearch.mycel.app.api.conversation.DeletionResult;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroup;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupFactory;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager.PrivateGroupHook;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationItem;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationManager;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationRequest;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationResponse;
import com.quantumresearch.mycel.app.api.sharing.SharingManager.SharingStatus;
import com.quantumresearch.mycel.app.client.ConversationClientImpl;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.SHARED;
import static com.quantumresearch.mycel.spore.api.sync.validation.IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static com.quantumresearch.mycel.app.privategroup.invitation.CreatorState.DISSOLVED;
import static com.quantumresearch.mycel.app.privategroup.invitation.CreatorState.ERROR;
import static com.quantumresearch.mycel.app.privategroup.invitation.CreatorState.INVITED;
import static com.quantumresearch.mycel.app.privategroup.invitation.CreatorState.JOINED;
import static com.quantumresearch.mycel.app.privategroup.invitation.CreatorState.START;
import static com.quantumresearch.mycel.app.privategroup.invitation.MessageType.ABORT;
import static com.quantumresearch.mycel.app.privategroup.invitation.MessageType.INVITE;
import static com.quantumresearch.mycel.app.privategroup.invitation.MessageType.JOIN;
import static com.quantumresearch.mycel.app.privategroup.invitation.MessageType.LEAVE;
import static com.quantumresearch.mycel.app.privategroup.invitation.Role.CREATOR;
import static com.quantumresearch.mycel.app.privategroup.invitation.Role.INVITEE;
import static com.quantumresearch.mycel.app.privategroup.invitation.Role.PEER;

@Immutable
@NotNullByDefault
class GroupInvitationManagerImpl extends ConversationClientImpl
		implements GroupInvitationManager, OpenDatabaseHook, ContactHook,
		PrivateGroupHook, ClientVersioningHook, CleanupHook {

	private final ClientVersioningManager clientVersioningManager;
	private final ContactGroupFactory contactGroupFactory;
	private final PrivateGroupFactory privateGroupFactory;
	private final PrivateGroupManager privateGroupManager;
	private final MessageParser messageParser;
	private final SessionParser sessionParser;
	private final SessionEncoder sessionEncoder;
	private final ProtocolEngine<CreatorSession> creatorEngine;
	private final ProtocolEngine<InviteeSession> inviteeEngine;
	private final ProtocolEngine<PeerSession> peerEngine;

	@Inject
	GroupInvitationManagerImpl(DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser, MessageTracker messageTracker,
			ContactGroupFactory contactGroupFactory,
			PrivateGroupFactory privateGroupFactory,
			PrivateGroupManager privateGroupManager,
			MessageParser messageParser, SessionParser sessionParser,
			SessionEncoder sessionEncoder,
			ProtocolEngineFactory engineFactory) {
		super(db, clientHelper, metadataParser, messageTracker);
		this.clientVersioningManager = clientVersioningManager;
		this.contactGroupFactory = contactGroupFactory;
		this.privateGroupFactory = privateGroupFactory;
		this.privateGroupManager = privateGroupManager;
		this.messageParser = messageParser;
		this.sessionParser = sessionParser;
		this.sessionEncoder = sessionEncoder;
		creatorEngine = engineFactory.createCreatorEngine();
		inviteeEngine = engineFactory.createInviteeEngine();
		peerEngine = engineFactory.createPeerEngine();
	}

	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		// Create a local group to indicate that we've set this client up
		Group localGroup = contactGroupFactory.createLocalGroup(CLIENT_ID,
				MAJOR_VERSION);
		if (db.containsGroup(txn, localGroup.getId())) return;
		db.addGroup(txn, localGroup);
		// Set things up for any pre-existing contacts
		for (Contact c : db.getContacts(txn)) addingContact(txn, c);
	}

	@Override
	public void addingContact(Transaction txn, Contact c) throws DbException {
		// Create a group to share with the contact
		Group g = getContactGroup(c);
		db.addGroup(txn, g);
		Visibility client = clientVersioningManager.getClientVisibility(txn,
				c.getId(), CLIENT_ID, MAJOR_VERSION);
		db.setGroupVisibility(txn, c.getId(), g.getId(), client);
		// Attach the contact ID to the group
		clientHelper.setContactId(txn, g.getId(), c.getId());
		// If the contact belongs to any private groups, create a peer session
		// or sessions in LEFT state for creator/invitee.
		for (Group group : db.getGroups(txn, PrivateGroupManager.CLIENT_ID,
				PrivateGroupManager.MAJOR_VERSION)) {
			if (privateGroupManager
					.isMember(txn, group.getId(), c.getAuthor())) {
				PrivateGroup pg =
						privateGroupManager.getPrivateGroup(txn, group.getId());
				recreateSession(txn, c, pg, g.getId());
			}
		}
	}

	private void recreateSession(Transaction txn, Contact c,
			PrivateGroup pg, GroupId contactGroupId) throws DbException {
		boolean isOur = privateGroupManager.isOurPrivateGroup(txn, pg);
		boolean isTheirs =
				c.getAuthor().getId().equals(pg.getCreator().getId());
		if (isOur || isTheirs) {
			// we are creator or invitee, create a left session for each role
			MessageId storageId = createStorageId(txn, contactGroupId);
			Session<?> session;
			if (isOur) {
				session = new CreatorSession(contactGroupId, pg.getId(), null,
						null, 0, 0, CreatorState.LEFT);
			} else {
				session = new InviteeSession(contactGroupId, pg.getId(), null,
						null, 0, 0, InviteeState.LEFT);
			}
			try {
				storeSession(txn, storageId, session);
			} catch (FormatException e) {
				throw new DbException(e);
			}
		} else {
			// we are neither creator nor invitee, create peer session
			addingMember(txn, pg.getId(), c);
		}
	}

	@Override
	public void removingContact(Transaction txn, Contact c) throws DbException {
		// mark private groups created by that contact as dissolved
		for (Group g : db.getGroups(txn, PrivateGroupManager.CLIENT_ID,
				PrivateGroupManager.MAJOR_VERSION)) {
			if (privateGroupManager.isMember(txn, g.getId(), c.getAuthor())) {
				PrivateGroup pg =
						privateGroupManager.getPrivateGroup(txn, g.getId());
				// check if contact to be removed is creator of the group
				if (c.getAuthor().getId().equals(pg.getCreator().getId())) {
					privateGroupManager.markGroupDissolved(txn, g.getId());
				}
			}
		}
		// Remove the contact group (all messages will be removed with it)
		db.removeGroup(txn, getContactGroup(c));
	}

	@Override
	public Group getContactGroup(Contact c) {
		return contactGroupFactory.createContactGroup(CLIENT_ID,
				MAJOR_VERSION, c);
	}

	@Override
	protected DeliveryAction incomingMessage(Transaction txn, Message m,
			BdfList body, BdfDictionary bdfMeta)
			throws DbException, FormatException {
		// Parse the metadata
		MessageMetadata meta = messageParser.parseMetadata(bdfMeta);
		// set the clean-up timer that will be started when message gets read
		long timer = meta.getAutoDeleteTimer();
		if (timer != NO_AUTO_DELETE_TIMER) {
			db.setCleanupTimerDuration(txn, m.getId(), timer);
		}
		// Look up the session, if there is one
		SessionId sessionId = getSessionId(meta.getPrivateGroupId());
		StoredSession ss = getSession(txn, m.getGroupId(), sessionId);
		// Handle the message
		Session<?> session;
		MessageId storageId;
		if (ss == null) {
			session = handleFirstMessage(txn, m, body, meta);
			storageId = createStorageId(txn, m.getGroupId());
		} else {
			session = handleMessage(txn, m, body, meta, ss.bdfSession);
			storageId = ss.storageId;
		}
		// Store the updated session
		storeSession(txn, storageId, session);
		return ACCEPT_DO_NOT_SHARE;
	}

	private SessionId getSessionId(GroupId privateGroupId) {
		return new SessionId(privateGroupId.getBytes());
	}

	@Nullable
	private StoredSession getSession(Transaction txn, GroupId contactGroupId,
			SessionId sessionId) throws DbException, FormatException {
		BdfDictionary query = sessionParser.getSessionQuery(sessionId);
		Map<MessageId, BdfDictionary> results = clientHelper
				.getMessageMetadataAsDictionary(txn, contactGroupId, query);
		if (results.size() > 1) throw new DbException();
		if (results.isEmpty()) return null;
		return new StoredSession(results.keySet().iterator().next(),
				results.values().iterator().next());
	}

	private Session<?> handleFirstMessage(Transaction txn, Message m,
			BdfList body, MessageMetadata meta)
			throws DbException, FormatException {
		GroupId privateGroupId = meta.getPrivateGroupId();
		MessageType type = meta.getMessageType();
		if (type == INVITE) {
			InviteeSession session =
					new InviteeSession(m.getGroupId(), privateGroupId);
			return handleMessage(txn, m, body, type, session, inviteeEngine);
		} else if (type == JOIN) {
			PeerSession session =
					new PeerSession(m.getGroupId(), privateGroupId);
			return handleMessage(txn, m, body, type, session, peerEngine);
		} else {
			throw new FormatException(); // Invalid first message
		}
	}

	private Session<?> handleMessage(Transaction txn, Message m, BdfList body,
			MessageMetadata meta, BdfDictionary bdfSession)
			throws DbException, FormatException {
		MessageType type = meta.getMessageType();
		Role role = sessionParser.getRole(bdfSession);
		if (role == CREATOR) {
			CreatorSession session = sessionParser
					.parseCreatorSession(m.getGroupId(), bdfSession);
			return handleMessage(txn, m, body, type, session, creatorEngine);
		} else if (role == INVITEE) {
			InviteeSession session = sessionParser
					.parseInviteeSession(m.getGroupId(), bdfSession);
			return handleMessage(txn, m, body, type, session, inviteeEngine);
		} else if (role == PEER) {
			PeerSession session = sessionParser
					.parsePeerSession(m.getGroupId(), bdfSession);
			return handleMessage(txn, m, body, type, session, peerEngine);
		} else {
			throw new AssertionError();
		}
	}

	private <S extends Session<?>> S handleMessage(Transaction txn, Message m,
			BdfList body, MessageType type, S session, ProtocolEngine<S> engine)
			throws DbException, FormatException {
		if (type == INVITE) {
			InviteMessage invite = messageParser.parseInviteMessage(m, body);
			return engine.onInviteMessage(txn, session, invite);
		} else if (type == JOIN) {
			JoinMessage join = messageParser.parseJoinMessage(m, body);
			return engine.onJoinMessage(txn, session, join);
		} else if (type == LEAVE) {
			LeaveMessage leave = messageParser.parseLeaveMessage(m, body);
			return engine.onLeaveMessage(txn, session, leave);
		} else if (type == ABORT) {
			AbortMessage abort = messageParser.parseAbortMessage(m, body);
			return engine.onAbortMessage(txn, session, abort);
		} else {
			throw new AssertionError();
		}
	}

	private MessageId createStorageId(Transaction txn, GroupId g)
			throws DbException {
		Message m = clientHelper.createMessageForStoringMetadata(g);
		db.addLocalMessage(txn, m, new Metadata(), false, false);
		return m.getId();
	}

	private void storeSession(Transaction txn, MessageId storageId,
			Session<?> session) throws DbException, FormatException {
		BdfDictionary d = sessionEncoder.encodeSession(session);
		clientHelper.mergeMessageMetadata(txn, storageId, d);
	}

	@Override
	public void sendInvitation(GroupId privateGroupId, ContactId c,
			@Nullable String text, long timestamp, byte[] signature,
			long autoDeleteTimer) throws DbException {
		db.transaction(false,
				txn -> sendInvitation(txn, privateGroupId, c, text, timestamp,
						signature, autoDeleteTimer));
	}

	@Override
	public void sendInvitation(Transaction txn, GroupId privateGroupId,
			ContactId c, @Nullable String text, long timestamp,
			byte[] signature, long autoDeleteTimer) throws DbException {
		SessionId sessionId = getSessionId(privateGroupId);
		try {
			// Look up the session, if there is one
			Contact contact = db.getContact(txn, c);
			GroupId contactGroupId = getContactGroup(contact).getId();
			StoredSession ss = getSession(txn, contactGroupId, sessionId);
			// Create or parse the session
			CreatorSession session;
			MessageId storageId;
			if (ss == null) {
				// This is the first invite - create a new session
				session = new CreatorSession(contactGroupId, privateGroupId);
				storageId = createStorageId(txn, contactGroupId);
			} else {
				// An earlier invite was declined, so we already have a session
				session = sessionParser
						.parseCreatorSession(contactGroupId, ss.bdfSession);
				storageId = ss.storageId;
			}
			// Handle the invite action
			session = creatorEngine.onInviteAction(txn, session, text,
					timestamp, signature, autoDeleteTimer);
			// Store the updated session
			storeSession(txn, storageId, session);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public void respondToInvitation(ContactId c, PrivateGroup g, boolean accept)
			throws DbException {
		respondToInvitation(c, getSessionId(g.getId()), accept);
	}

	@Override
	public void respondToInvitation(Transaction txn, ContactId c,
			PrivateGroup g, boolean accept) throws DbException {
		respondToInvitation(txn, c, getSessionId(g.getId()), accept);
	}

	@Override
	public void respondToInvitation(ContactId c, SessionId sessionId,
			boolean accept) throws DbException {
		db.transaction(false,
				txn -> respondToInvitation(txn, c, sessionId, accept, false));
	}

	@Override
	public void respondToInvitation(Transaction txn, ContactId c,
			SessionId sessionId, boolean accept) throws DbException {
		respondToInvitation(txn, c, sessionId, accept, false);
	}

	private void respondToInvitation(Transaction txn, ContactId c,
			SessionId sessionId, boolean accept, boolean isAutoDecline)
			throws DbException {
		try {
			// Look up the session
			Contact contact = db.getContact(txn, c);
			GroupId contactGroupId = getContactGroup(contact).getId();
			StoredSession ss = getSession(txn, contactGroupId, sessionId);
			if (ss == null) throw new IllegalArgumentException();
			// Parse the session
			InviteeSession session = sessionParser
					.parseInviteeSession(contactGroupId, ss.bdfSession);
			// Handle the join or leave action
			if (accept) session = inviteeEngine.onJoinAction(txn, session);
			else session =
					inviteeEngine.onLeaveAction(txn, session, isAutoDecline);
			// Store the updated session
			storeSession(txn, ss.storageId, session);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public void revealRelationship(ContactId c, GroupId g) throws DbException {
		db.transaction(false, txn -> revealRelationship(txn, c, g));
	}

	@Override
	public void revealRelationship(Transaction txn, ContactId c, GroupId g)
			throws DbException {
		try {
			// Look up the session
			Contact contact = db.getContact(txn, c);
			GroupId contactGroupId = getContactGroup(contact).getId();
			StoredSession ss = getSession(txn, contactGroupId, getSessionId(g));
			if (ss == null) throw new IllegalArgumentException();
			// Parse the session
			PeerSession session = sessionParser
					.parsePeerSession(contactGroupId, ss.bdfSession);
			// Handle the join action
			session = peerEngine.onJoinAction(txn, session);
			// Store the updated session
			storeSession(txn, ss.storageId, session);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	private <S extends Session<?>> S handleAction(Transaction txn,
			LocalAction type, S session, ProtocolEngine<S> engine)
			throws DbException {
		if (type == LocalAction.INVITE) {
			throw new IllegalArgumentException();
		} else if (type == LocalAction.JOIN) {
			return engine.onJoinAction(txn, session);
		} else if (type == LocalAction.LEAVE) {
			return engine.onLeaveAction(txn, session, false);
		} else if (type == LocalAction.MEMBER_ADDED) {
			return engine.onMemberAddedAction(txn, session);
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public Collection<ConversationMessageHeader> getMessageHeaders(
			Transaction txn,
			ContactId c) throws DbException {
		try {
			Contact contact = db.getContact(txn, c);
			GroupId contactGroupId = getContactGroup(contact).getId();
			BdfDictionary query = messageParser.getMessagesVisibleInUiQuery();
			Map<MessageId, BdfDictionary> results = clientHelper
					.getMessageMetadataAsDictionary(txn, contactGroupId, query);
			List<ConversationMessageHeader> messages =
					new ArrayList<>(results.size());
			for (Entry<MessageId, BdfDictionary> e : results.entrySet()) {
				MessageId m = e.getKey();
				MessageMetadata meta =
						messageParser.parseMetadata(e.getValue());
				MessageStatus status = db.getMessageStatus(txn, c, m);
				MessageType type = meta.getMessageType();
				if (type == INVITE) {
					messages.add(parseInvitationRequest(txn, contactGroupId, m,
							meta, status));
				} else if (type == JOIN) {
					messages.add(parseInvitationResponse(contactGroupId, m,
							meta, status, true));
				} else if (type == LEAVE) {
					messages.add(parseInvitationResponse(contactGroupId, m,
							meta, status, false));
				}
			}
			return messages;
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	private GroupInvitationRequest parseInvitationRequest(Transaction txn,
			GroupId contactGroupId, MessageId m, MessageMetadata meta,
			MessageStatus status) throws DbException, FormatException {
		SessionId sessionId = getSessionId(meta.getPrivateGroupId());
		// Look up the invite message to get the details of the private group
		InviteMessage invite = messageParser.getInviteMessage(txn, m);
		PrivateGroup pg = privateGroupFactory
				.createPrivateGroup(invite.getGroupName(), invite.getCreator(),
						invite.getSalt());
		// Find out whether the private group can be opened
		boolean canBeOpened = meta.wasAccepted() &&
				db.containsGroup(txn, invite.getPrivateGroupId());
		return new GroupInvitationRequest(m, contactGroupId,
				meta.getTimestamp(), meta.isLocal(), meta.isRead(),
				status.isSent(), status.isSeen(), sessionId, pg,
				invite.getText(), meta.isAvailableToAnswer(), canBeOpened,
				invite.getAutoDeleteTimer());
	}

	private GroupInvitationResponse parseInvitationResponse(
			GroupId contactGroupId, MessageId m, MessageMetadata meta,
			MessageStatus status, boolean accept) {
		SessionId sessionId = getSessionId(meta.getPrivateGroupId());
		return new GroupInvitationResponse(m, contactGroupId,
				meta.getTimestamp(), meta.isLocal(), meta.isRead(),
				status.isSent(), status.isSeen(), sessionId, accept,
				meta.getPrivateGroupId(), meta.getAutoDeleteTimer(),
				meta.isAutoDecline());
	}

	@Override
	public Collection<GroupInvitationItem> getInvitations() throws DbException {
		return db.transactionWithResult(true, this::getInvitations);
	}

	@Override
	public Collection<GroupInvitationItem> getInvitations(Transaction txn)
			throws DbException {
		List<GroupInvitationItem> items = new ArrayList<>();
		BdfDictionary query = messageParser.getInvitesAvailableToAnswerQuery();
		try {
			// Look up the available invite messages for each contact
			for (Contact c : db.getContacts(txn)) {
				GroupId contactGroupId = getContactGroup(c).getId();
				Collection<MessageId> results = clientHelper.getMessageIds(txn,
						contactGroupId, query);
				for (MessageId m : results)
					items.add(parseGroupInvitationItem(txn, c, m));
			}
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return items;
	}

	@Override
	public SharingStatus getSharingStatus(Contact c, GroupId privateGroupId)
			throws DbException {
		return db.transactionWithResult(true,
				txn -> getSharingStatus(txn, c, privateGroupId));
	}

	@Override
	public SharingStatus getSharingStatus(Transaction txn, Contact c,
			GroupId privateGroupId) throws DbException {
		GroupId contactGroupId = getContactGroup(c).getId();
		SessionId sessionId = getSessionId(privateGroupId);
		try {
			Visibility client = clientVersioningManager.getClientVisibility(txn,
					c.getId(), PrivateGroupManager.CLIENT_ID,
					PrivateGroupManager.MAJOR_VERSION);
			StoredSession ss = getSession(txn, contactGroupId, sessionId);
			// The group can't be shared unless the contact supports the client
			if (client != SHARED) return SharingStatus.NOT_SUPPORTED;
			// If there's no session, the contact can be invited
			if (ss == null) return SharingStatus.SHAREABLE;
			// If the session's in the start state, the contact can be invited
			CreatorSession session = sessionParser
					.parseCreatorSession(contactGroupId, ss.bdfSession);
			CreatorState state = session.getState();
			if (state == START) return SharingStatus.SHAREABLE;
			if (state == INVITED) return SharingStatus.INVITE_SENT;
			if (state == JOINED) return SharingStatus.SHARING;
			// Apart from the common case that the contact LEFT the group,
			// the creator can also be a LEFT state, after re-adding a contact
			// and re-creating the session with #recreateSession()
			if (state == CreatorState.LEFT) return SharingStatus.SHARING;
			if (state == DISSOLVED) throw new ProtocolStateException();
			if (state == ERROR) return SharingStatus.ERROR;
			throw new AssertionError("Unhandled state: " + state.name());
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	private GroupInvitationItem parseGroupInvitationItem(Transaction txn,
			Contact c, MessageId m) throws DbException, FormatException {
		InviteMessage invite = messageParser.getInviteMessage(txn, m);
		PrivateGroup privateGroup = privateGroupFactory.createPrivateGroup(
				invite.getGroupName(), invite.getCreator(), invite.getSalt());
		return new GroupInvitationItem(privateGroup, c);
	}

	@Override
	public void addingMember(Transaction txn, GroupId privateGroupId, Author a)
			throws DbException {
		// If the member is a contact, handle the add member action
		for (Contact c : db.getContactsByAuthorId(txn, a.getId()))
			addingMember(txn, privateGroupId, c);
	}

	private void addingMember(Transaction txn, GroupId privateGroupId,
			Contact c) throws DbException {
		try {
			// Look up the session for the contact, if there is one
			GroupId contactGroupId = getContactGroup(c).getId();
			SessionId sessionId = getSessionId(privateGroupId);
			StoredSession ss = getSession(txn, contactGroupId, sessionId);
			// Create or parse the session
			Session<?> session;
			MessageId storageId;
			if (ss == null) {
				// If there's no session the contact must be a peer,
				// otherwise we would have exchanged invitation messages
				PeerSession peerSession =
						new PeerSession(contactGroupId, privateGroupId);
				// Handle the action
				session = peerEngine.onMemberAddedAction(txn, peerSession);
				storageId = createStorageId(txn, contactGroupId);
			} else {
				// Handle the action
				session = handleAction(txn, LocalAction.MEMBER_ADDED,
						contactGroupId, ss.bdfSession);
				storageId = ss.storageId;
			}
			// Store the updated session
			storeSession(txn, storageId, session);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public void removingGroup(Transaction txn, GroupId privateGroupId)
			throws DbException {
		SessionId sessionId = getSessionId(privateGroupId);
		// If we have any sessions in progress, tell the contacts we're leaving
		try {
			for (Contact c : db.getContacts(txn)) {
				// Look up the session for the contact, if there is one
				GroupId contactGroupId = getContactGroup(c).getId();
				StoredSession ss = getSession(txn, contactGroupId, sessionId);
				if (ss == null) continue; // No session for this contact
				// Handle the action
				Session<?> session = handleAction(txn, LocalAction.LEAVE,
						contactGroupId, ss.bdfSession);
				// Store the updated session
				storeSession(txn, ss.storageId, session);
			}
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	private Session<?> handleAction(Transaction txn, LocalAction a,
			GroupId contactGroupId, BdfDictionary bdfSession)
			throws DbException, FormatException {
		Role role = sessionParser.getRole(bdfSession);
		if (role == CREATOR) {
			CreatorSession session = sessionParser
					.parseCreatorSession(contactGroupId, bdfSession);
			return handleAction(txn, a, session, creatorEngine);
		} else if (role == INVITEE) {
			InviteeSession session = sessionParser
					.parseInviteeSession(contactGroupId, bdfSession);
			return handleAction(txn, a, session, inviteeEngine);
		} else if (role == PEER) {
			PeerSession session = sessionParser
					.parsePeerSession(contactGroupId, bdfSession);
			return handleAction(txn, a, session, peerEngine);
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public void onClientVisibilityChanging(Transaction txn, Contact c,
			Visibility v) throws DbException {
		// Apply the client's visibility to the contact group
		Group g = getContactGroup(c);
		db.setGroupVisibility(txn, c.getId(), g.getId(), v);
	}

	ClientVersioningHook getPrivateGroupClientVersioningHook() {
		return this::onPrivateGroupClientVisibilityChanging;
	}

	private void onPrivateGroupClientVisibilityChanging(Transaction txn,
			Contact c, Visibility client) throws DbException {
		try {
			Collection<Group> shareables =
					db.getGroups(txn, PrivateGroupManager.CLIENT_ID,
							PrivateGroupManager.MAJOR_VERSION);
			Map<GroupId, Visibility> m = getPreferredVisibilities(txn, c);
			for (Group g : shareables) {
				Visibility preferred = m.get(g.getId());
				if (preferred == null) continue; // No session for this group
				// Apply min of preferred visibility and client's visibility
				Visibility min = Visibility.min(preferred, client);
				db.setGroupVisibility(txn, c.getId(), g.getId(), min);
			}
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	private Map<GroupId, Visibility> getPreferredVisibilities(Transaction txn,
			Contact c) throws DbException, FormatException {
		GroupId contactGroupId = getContactGroup(c).getId();
		BdfDictionary query = sessionParser.getAllSessionsQuery();
		Map<MessageId, BdfDictionary> results = clientHelper
				.getMessageMetadataAsDictionary(txn, contactGroupId, query);
		Map<GroupId, Visibility> m = new HashMap<>();
		for (BdfDictionary d : results.values()) {
			Session<?> s = sessionParser.parseSession(contactGroupId, d);
			m.put(s.getPrivateGroupId(), s.getState().getVisibility());
		}
		return m;
	}

	@FunctionalInterface
	private interface DeletableSessionRetriever {
		Map<GroupId, DeletableSession> getDeletableSessions(Transaction txn,
				GroupId contactGroup, Map<MessageId, BdfDictionary> metadata)
				throws DbException;
	}

	@FunctionalInterface
	private interface MessageDeletionChecker {
		/**
		 * This is called for all messages belonging to a session.
		 * It returns true if the given {@link MessageId} causes a problem
		 * so that the session can not be deleted.
		 */
		boolean causesProblem(MessageId messageId);
	}

	@Override
	public DeletionResult deleteAllMessages(Transaction txn, ContactId c)
			throws DbException {
		return deleteMessages(txn, c, (txn1, g, metadata) -> {
			// get all sessions and their states
			Map<GroupId, DeletableSession> sessions = new HashMap<>();
			for (BdfDictionary d : metadata.values()) {
				Session<?> session;
				try {
					if (!sessionParser.isSession(d)) continue;
					session = sessionParser.parseSession(g, d);
				} catch (FormatException e) {
					throw new DbException(e);
				}
				sessions.put(session.getPrivateGroupId(),
						new DeletableSession(session.getState()));
			}
			return sessions;
		}, messageId -> false);
	}

	@Override
	public DeletionResult deleteMessages(Transaction txn, ContactId c,
			Set<MessageId> messageIds) throws DbException {
		return deleteMessages(txn, c, (txn1, g, metadata) -> {
			// get only sessions from given messageIds
			Map<GroupId, DeletableSession> sessions = new HashMap<>();
			for (MessageId messageId : messageIds) {
				BdfDictionary d = metadata.get(messageId);
				if (d == null) continue;  // throw new NoSuchMessageException()
				try {
					MessageMetadata messageMetadata =
							messageParser.parseMetadata(d);
					SessionId sessionId =
							getSessionId(messageMetadata.getPrivateGroupId());
					StoredSession ss = getSession(txn1, g, sessionId);
					if (ss == null) throw new DbException();
					Session<?> session = sessionParser
							.parseSession(g, metadata.get(ss.storageId));
					sessions.put(session.getPrivateGroupId(),
							new DeletableSession(session.getState()));
				} catch (FormatException e) {
					throw new DbException(e);
				}
			}
			return sessions;
			// don't delete sessions if a message is not part of messageIds
		}, messageId -> !messageIds.contains(messageId));
	}

	private DeletionResult deleteMessages(Transaction txn, ContactId c,
			DeletableSessionRetriever retriever, MessageDeletionChecker checker)
			throws DbException {
		// get ID of the contact group
		GroupId g = getContactGroup(db.getContact(txn, c)).getId();

		// get metadata for all messages in the group
		// (these are sessions *and* protocol messages)
		Map<MessageId, BdfDictionary> metadata;
		try {
			metadata = clientHelper.getMessageMetadataAsDictionary(txn, g);
		} catch (FormatException e) {
			throw new DbException(e);
		}

		// get sessions and their states from retriever
		Map<GroupId, DeletableSession> sessions =
				retriever.getDeletableSessions(txn, g, metadata);

		// assign protocol messages to their sessions
		for (Entry<MessageId, BdfDictionary> entry : metadata.entrySet()) {
			// parse message metadata and skip messages not visible in UI
			MessageMetadata m;
			try {
				// skip all sessions, we are only interested in messages
				BdfDictionary d = entry.getValue();
				if (sessionParser.isSession(d)) continue;
				m = messageParser.parseMetadata(d);
			} catch (FormatException e) {
				throw new DbException(e);
			}
			if (!m.isVisibleInConversation()) continue;

			// add visible messages to session
			DeletableSession session = sessions.get(m.getPrivateGroupId());
			if (session != null) session.messages.add(entry.getKey());
		}

		// get a set of all messages which were not ACKed by the contact
		Set<MessageId> notAcked = new HashSet<>();
		for (MessageStatus status : db.getMessageStatus(txn, c, g)) {
			if (!status.isSeen()) notAcked.add(status.getMessageId());
		}
		DeletionResult result = deleteCompletedSessions(txn, sessions.values(),
				notAcked, checker);
		recalculateGroupCount(txn, g);
		return result;
	}

	private DeletionResult deleteCompletedSessions(Transaction txn,
			Collection<DeletableSession> sessions, Set<MessageId> notAcked,
			MessageDeletionChecker checker) throws DbException {
		// find completed sessions to delete
		DeletionResult result = new DeletionResult();
		for (DeletableSession session : sessions) {
			if (session.state.isAwaitingResponse()) {
				result.addInvitationSessionInProgress();
				continue;
			}
			// we can only delete sessions
			// where delivery of all messages was confirmed (aka ACKed)
			boolean sessionDeletable = true;
			for (MessageId m : session.messages) {
				if (notAcked.contains(m) || checker.causesProblem(m)) {
					sessionDeletable = false;
					if (notAcked.contains(m))
						result.addInvitationSessionInProgress();
					if (checker.causesProblem(m))
						result.addInvitationNotAllSelected();
				}
			}
			if (sessionDeletable) {
				for (MessageId m : session.messages) {
					db.deleteMessage(txn, m);
					db.deleteMessageMetadata(txn, m);
				}
			}
		}
		return result;
	}

	@Override
	public void deleteMessages(Transaction txn, GroupId g,
			Collection<MessageId> messageIds) throws DbException {
		ContactId c;
		Map<SessionId, DeletableSession> sessions = new HashMap<>();
		try {
			// get the ContactId from the given GroupId
			c = clientHelper.getContactId(txn, g);
			// get sessions for all messages to be deleted
			for (MessageId messageId : messageIds) {
				BdfDictionary d = clientHelper
						.getMessageMetadataAsDictionary(txn, messageId);
				MessageMetadata messageMetadata =
						messageParser.parseMetadata(d);
				if (!messageMetadata.isVisibleInConversation())
					throw new IllegalArgumentException();
				SessionId sessionId =
						getSessionId(messageMetadata.getPrivateGroupId());
				DeletableSession deletableSession = sessions.get(sessionId);
				if (deletableSession == null) {
					StoredSession ss = getSession(txn, g, sessionId);
					if (ss == null) throw new DbException();
					Session<?> session =
							sessionParser.parseSession(g, ss.bdfSession);
					deletableSession = new DeletableSession(session.getState());
					sessions.put(sessionId, deletableSession);
				}
				deletableSession.messages.add(messageId);
			}
		} catch (FormatException e) {
			throw new DbException(e);
		}

		// delete given visible messages in sessions and auto-respond before
		for (Entry<SessionId, DeletableSession> entry : sessions.entrySet()) {
			DeletableSession session = entry.getValue();
			// decline invitee sessions waiting for a response before
			if (session.state instanceof InviteeState &&
					session.state.isAwaitingResponse()) {
				respondToInvitation(txn, c, entry.getKey(), false, true);
			}
			for (MessageId m : session.messages) {
				db.deleteMessage(txn, m);
				db.deleteMessageMetadata(txn, m);
			}
		}
		recalculateGroupCount(txn, g);

		txn.attach(new ConversationMessagesDeletedEvent(c, messageIds));
	}

	@Override
	public Set<MessageId> getMessageIds(Transaction txn, ContactId c)
			throws DbException {
		GroupId g = getContactGroup(db.getContact(txn, c)).getId();
		BdfDictionary query = messageParser.getMessagesVisibleInUiQuery();
		try {
			return new HashSet<>(clientHelper.getMessageIds(txn, g, query));
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	private void recalculateGroupCount(Transaction txn, GroupId g)
			throws DbException {
		BdfDictionary query = messageParser.getMessagesVisibleInUiQuery();
		Map<MessageId, BdfDictionary> results;
		try {
			results =
					clientHelper.getMessageMetadataAsDictionary(txn, g, query);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		int msgCount = 0;
		int unreadCount = 0;
		for (Entry<MessageId, BdfDictionary> entry : results.entrySet()) {
			MessageMetadata meta;
			try {
				meta = messageParser.parseMetadata(entry.getValue());
			} catch (FormatException e) {
				throw new DbException(e);
			}
			msgCount++;
			if (!meta.isRead()) unreadCount++;
		}
		messageTracker.resetGroupCount(txn, g, msgCount, unreadCount);
	}

	private static class StoredSession {

		private final MessageId storageId;
		private final BdfDictionary bdfSession;

		private StoredSession(MessageId storageId, BdfDictionary bdfSession) {
			this.storageId = storageId;
			this.bdfSession = bdfSession;
		}
	}

	private static class DeletableSession {

		private final State state;
		private final List<MessageId> messages = new ArrayList<>();

		private DeletableSession(State state) {
			this.state = state;
		}
	}
}
