package com.quantumresearch.mycel.app.client;

import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.data.BdfDictionary;
import com.quantumresearch.mycel.infrastructure.api.data.BdfEntry;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.test.BrambleMockTestCase;
import com.quantumresearch.mycel.infrastructure.test.TestUtils;
import com.quantumresearch.mycel.app.api.client.MessageTracker;
import org.jmock.Expectations;
import org.junit.Test;

import static com.quantumresearch.mycel.app.client.MessageTrackerConstants.GROUP_KEY_LATEST_MSG;
import static com.quantumresearch.mycel.app.client.MessageTrackerConstants.GROUP_KEY_MSG_COUNT;
import static com.quantumresearch.mycel.app.client.MessageTrackerConstants.GROUP_KEY_STORED_MESSAGE_ID;
import static com.quantumresearch.mycel.app.client.MessageTrackerConstants.GROUP_KEY_UNREAD_COUNT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MessageTrackerTest extends BrambleMockTestCase {

	protected final GroupId groupId = new GroupId(TestUtils.getRandomId());
	protected final ClientHelper clientHelper =
			context.mock(ClientHelper.class);
	private final DatabaseComponent db = context.mock(DatabaseComponent.class);
	private final Clock clock = context.mock(Clock.class);
	private final MessageId messageId = new MessageId(TestUtils.getRandomId());
	private final MessageTracker messageTracker =
			new MessageTrackerImpl(db, clientHelper, clock);
	private final BdfDictionary dictionary = BdfDictionary.of(
			new BdfEntry(GROUP_KEY_STORED_MESSAGE_ID, messageId)
	);

	@Test
	public void testInitializeGroupCount() throws Exception {
		Transaction txn = new Transaction(null, false);
		long now = 42L;
		BdfDictionary dictionary = BdfDictionary.of(
				new BdfEntry(GROUP_KEY_MSG_COUNT, 0),
				new BdfEntry(GROUP_KEY_UNREAD_COUNT, 0),
				new BdfEntry(GROUP_KEY_LATEST_MSG, now)
		);
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(clientHelper).mergeGroupMetadata(txn, groupId, dictionary);
		}});
		messageTracker.initializeGroupCount(txn, groupId);
	}

	@Test
	public void testMessageStore() throws Exception {
		context.checking(new Expectations() {{
			oneOf(clientHelper).mergeGroupMetadata(groupId, dictionary);
		}});
		messageTracker.storeMessageId(groupId, messageId);
	}

	@Test
	public void testMessageLoad() throws Exception {
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(groupId);
			will(returnValue(dictionary));
		}});
		MessageId loadedId = messageTracker.loadStoredMessageId(groupId);
		assertNotNull(loadedId);
		assertEquals(messageId, loadedId);
	}

}
