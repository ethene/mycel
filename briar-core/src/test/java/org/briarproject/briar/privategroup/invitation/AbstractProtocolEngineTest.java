package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.contact.Contact;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.data.BdfDictionary;
import com.quantumresearch.mycel.infrastructure.api.data.BdfEntry;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.infrastructure.api.identity.Author;
import com.quantumresearch.mycel.infrastructure.api.identity.IdentityManager;
import com.quantumresearch.mycel.infrastructure.api.sync.Group;
import com.quantumresearch.mycel.infrastructure.api.sync.Group.Visibility;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.infrastructure.test.BrambleMockTestCase;
import com.quantumresearch.mycel.app.api.autodelete.AutoDeleteManager;
import com.quantumresearch.mycel.app.api.client.MessageTracker;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager;
import com.quantumresearch.mycel.app.api.privategroup.GroupMessageFactory;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroup;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupFactory;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationManager;
import org.jmock.Expectations;

import static com.quantumresearch.mycel.infrastructure.api.identity.AuthorConstants.MAX_SIGNATURE_LENGTH;
import static com.quantumresearch.mycel.infrastructure.api.sync.Group.Visibility.SHARED;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getContact;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getGroup;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getMessage;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getRandomBytes;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.infrastructure.util.StringUtils.getRandomString;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static com.quantumresearch.mycel.app.api.privategroup.PrivateGroupConstants.GROUP_SALT_LENGTH;
import static com.quantumresearch.mycel.app.api.privategroup.PrivateGroupConstants.MAX_GROUP_INVITATION_TEXT_LENGTH;
import static com.quantumresearch.mycel.app.api.privategroup.PrivateGroupConstants.MAX_GROUP_NAME_LENGTH;
import static com.quantumresearch.mycel.app.privategroup.invitation.MessageType.ABORT;
import static com.quantumresearch.mycel.app.privategroup.invitation.MessageType.INVITE;
import static com.quantumresearch.mycel.app.privategroup.invitation.MessageType.JOIN;
import static com.quantumresearch.mycel.app.privategroup.invitation.MessageType.LEAVE;
import static org.junit.Assert.assertEquals;

abstract class AbstractProtocolEngineTest extends BrambleMockTestCase {

	final DatabaseComponent db = context.mock(DatabaseComponent.class);
	final ClientHelper clientHelper = context.mock(ClientHelper.class);
	final ClientVersioningManager clientVersioningManager =
			context.mock(ClientVersioningManager.class);
	final PrivateGroupFactory privateGroupFactory =
			context.mock(PrivateGroupFactory.class);
	final PrivateGroupManager privateGroupManager =
			context.mock(PrivateGroupManager.class);
	final MessageParser messageParser = context.mock(MessageParser.class);
	final GroupMessageFactory groupMessageFactory =
			context.mock(GroupMessageFactory.class);
	final IdentityManager identityManager = context.mock(IdentityManager.class);
	final MessageEncoder messageEncoder = context.mock(MessageEncoder.class);
	final AutoDeleteManager autoDeleteManager =
			context.mock(AutoDeleteManager.class);
	final ConversationManager conversationManager =
			context.mock(ConversationManager.class);
	final Clock clock = context.mock(Clock.class);

	final Transaction txn = new Transaction(null, false);
	final Contact contact = getContact();
	final ContactId contactId = contact.getId();
	final Author author = contact.getAuthor();
	final GroupId contactGroupId = new GroupId(getRandomId());
	final Group privateGroupGroup = getGroup(PrivateGroupManager.CLIENT_ID,
			PrivateGroupManager.MAJOR_VERSION);
	final GroupId privateGroupId = privateGroupGroup.getId();
	final PrivateGroup privateGroup = new PrivateGroup(privateGroupGroup,
			getRandomString(MAX_GROUP_NAME_LENGTH), author,
			getRandomBytes(GROUP_SALT_LENGTH));
	final byte[] signature = getRandomBytes(MAX_SIGNATURE_LENGTH);
	final MessageId lastLocalMessageId = new MessageId(getRandomId());
	final MessageId lastRemoteMessageId = new MessageId(getRandomId());
	final Message message = getMessage(contactGroupId);
	final MessageId messageId = message.getId();
	final long messageTimestamp = message.getTimestamp();
	final long inviteTimestamp = messageTimestamp - 1;
	final long localTimestamp = inviteTimestamp - 1;

	final InviteMessage inviteMessage =
			new InviteMessage(new MessageId(getRandomId()), contactGroupId,
					privateGroupId, 0L, privateGroup.getName(),
					privateGroup.getCreator(), privateGroup.getSalt(),
					getRandomString(MAX_GROUP_INVITATION_TEXT_LENGTH),
					signature, NO_AUTO_DELETE_TIMER);
	final JoinMessage joinMessage =
			new JoinMessage(new MessageId(getRandomId()), contactGroupId,
					privateGroupId, 0L, lastRemoteMessageId,
					NO_AUTO_DELETE_TIMER);
	final LeaveMessage leaveMessage =
			new LeaveMessage(new MessageId(getRandomId()), contactGroupId,
					privateGroupId, 0L, lastRemoteMessageId,
					NO_AUTO_DELETE_TIMER);
	final AbortMessage abortMessage =
			new AbortMessage(messageId, contactGroupId, privateGroupId,
					inviteTimestamp + 1);

	void assertSessionConstantsUnchanged(Session<?> s1, Session<?> s2) {
		assertEquals(s1.getRole(), s2.getRole());
		assertEquals(s1.getContactGroupId(), s2.getContactGroupId());
		assertEquals(s1.getPrivateGroupId(), s2.getPrivateGroupId());
	}

	void assertSessionRecordedSentMessage(Session<?> s) {
		assertEquals(messageId, s.getLastLocalMessageId());
		assertEquals(lastRemoteMessageId, s.getLastRemoteMessageId());
		assertEquals(messageTimestamp, s.getLocalTimestamp());
		assertEquals(inviteTimestamp, s.getInviteTimestamp());
	}

	void expectGetTimestampForInvisibleMessage(long time) {
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(time));
		}});
	}

	void expectGetTimestampForVisibleMessage(long time) throws Exception {
		context.checking(new Expectations() {{
			oneOf(clientHelper).getContactId(txn, contactGroupId);
			will(returnValue(contactId));
			oneOf(conversationManager)
					.getTimestampForOutgoingMessage(txn, contactId);
			will(returnValue(time));
		}});
	}

	void expectSendInviteMessage(String text) throws Exception {
		expectGetTimestampForVisibleMessage(messageTimestamp);
		expectCheckWhetherContactSupportsAutoDeletion();
		context.checking(new Expectations() {{
			oneOf(messageEncoder).encodeInviteMessage(contactGroupId,
					privateGroupId, inviteTimestamp, privateGroup.getName(),
					author, privateGroup.getSalt(), text, signature,
					NO_AUTO_DELETE_TIMER);
			will(returnValue(message));
		}});
		expectSendMessage(INVITE, true);
	}

	void expectSendJoinMessage(JoinMessage m, boolean visible)
			throws Exception {
		if (visible) expectGetTimestampForVisibleMessage(messageTimestamp);
		else expectGetTimestampForInvisibleMessage(messageTimestamp);
		expectCheckWhetherContactSupportsAutoDeletion();
		if (visible) expectGetAutoDeleteTimer(messageTimestamp);
		context.checking(new Expectations() {{
			oneOf(messageEncoder).encodeJoinMessage(m.getContactGroupId(),
					m.getPrivateGroupId(), m.getTimestamp(),
					lastLocalMessageId, NO_AUTO_DELETE_TIMER);
			will(returnValue(message));
		}});
		expectSendMessage(JOIN, visible);
	}

	void expectSendLeaveMessage(boolean visible) throws Exception {
		if (visible) expectGetTimestampForVisibleMessage(messageTimestamp);
		else expectGetTimestampForInvisibleMessage(messageTimestamp);
		expectCheckWhetherContactSupportsAutoDeletion();
		if (visible) expectGetAutoDeleteTimer(messageTimestamp);
		context.checking(new Expectations() {{
			oneOf(messageEncoder).encodeLeaveMessage(contactGroupId,
					privateGroupId, messageTimestamp, lastLocalMessageId,
					NO_AUTO_DELETE_TIMER);
			will(returnValue(message));
		}});
		expectSendMessage(LEAVE, visible);
	}

	void expectSendAbortMessage() throws Exception {
		expectGetTimestampForInvisibleMessage(messageTimestamp);
		context.checking(new Expectations() {{
			oneOf(messageEncoder)
					.encodeAbortMessage(contactGroupId, privateGroupId,
							messageTimestamp);
			will(returnValue(message));
		}});
		expectSendMessage(ABORT, false);
	}

	private void expectSendMessage(MessageType type, boolean visible)
			throws Exception {
		BdfDictionary meta = BdfDictionary.of(new BdfEntry("me", "ta"));
		context.checking(new Expectations() {{
			oneOf(messageEncoder).encodeMetadata(type, privateGroupId,
					message.getTimestamp(), true, true, visible, false, false,
					NO_AUTO_DELETE_TIMER, false);
			will(returnValue(meta));
			oneOf(clientHelper).addLocalMessage(txn, message, meta, true,
					false);
		}});
	}

	void expectSetPrivateGroupVisibility(Visibility v) throws Exception {
		expectGetContactId();
		context.checking(new Expectations() {{
			oneOf(clientVersioningManager).getClientVisibility(txn, contactId,
					PrivateGroupManager.CLIENT_ID,
					PrivateGroupManager.MAJOR_VERSION);
			will(returnValue(SHARED));
			oneOf(db).setGroupVisibility(txn, contactId, privateGroupId, v);
		}});
	}

	void expectGetContactId() throws Exception {
		context.checking(new Expectations() {{
			oneOf(clientHelper).getContactId(txn, contactGroupId);
			will(returnValue(contactId));
		}});
	}

	void expectIsSubscribedPrivateGroup() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).containsGroup(txn, privateGroupId);
			will(returnValue(true));
			oneOf(db).getGroup(txn, privateGroupId);
			will(returnValue(privateGroupGroup));
		}});
	}

	void expectIsNotSubscribedPrivateGroup() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).containsGroup(txn, privateGroupId);
			will(returnValue(false));
		}});
	}

	void expectMarkMessageVisibleInUi(MessageId m) throws Exception {
		BdfDictionary d = new BdfDictionary();
		context.checking(new Expectations() {{
			oneOf(messageEncoder).setVisibleInUi(d, true);
			oneOf(clientHelper).mergeMessageMetadata(txn, m, d);
		}});
	}

	void expectCheckWhetherContactSupportsAutoDeletion() throws Exception {
		context.checking(new Expectations() {{
			oneOf(clientHelper).getContactId(txn, contactGroupId);
			will(returnValue(contactId));
			oneOf(clientVersioningManager).getClientMinorVersion(txn, contactId,
					GroupInvitationManager.CLIENT_ID,
					GroupInvitationManager.MAJOR_VERSION);
			will(returnValue(GroupInvitationManager.MINOR_VERSION));
		}});
	}

	void expectGetAutoDeleteTimer(long timestamp) throws Exception {
		context.checking(new Expectations() {{
			oneOf(autoDeleteManager).getAutoDeleteTimer(txn, contactId,
					timestamp);
			will(returnValue(NO_AUTO_DELETE_TIMER));
		}});
	}

	void expectTrackUnreadMessage(long timestamp) throws Exception {
		context.checking(new Expectations() {{
			oneOf(conversationManager).trackMessage(txn, contactGroupId, timestamp,
					false);
		}});
	}

	void expectReceiveAutoDeleteTimer(DeletableGroupInvitationMessage m)
			throws Exception {
		context.checking(new Expectations() {{
			oneOf(clientHelper).getContactId(txn, contactGroupId);
			will(returnValue(contactId));
			oneOf(autoDeleteManager).receiveAutoDeleteTimer(txn, contactId,
					m.getAutoDeleteTimer(), m.getTimestamp());
		}});
	}
}
