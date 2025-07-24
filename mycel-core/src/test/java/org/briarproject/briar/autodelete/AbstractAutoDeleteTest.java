package com.quantumresearch.mycel.app.autodelete;

import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.system.TimeTravelModule;
import com.quantumresearch.mycel.spore.test.TestDatabaseConfigModule;
import com.quantumresearch.mycel.app.api.autodelete.AutoDeleteManager;
import com.quantumresearch.mycel.app.api.client.MessageTracker.GroupCount;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager.ConversationClient;
import com.quantumresearch.mycel.app.api.conversation.ConversationMessageHeader;
import com.quantumresearch.mycel.app.test.BriarIntegrationTest;
import com.quantumresearch.mycel.app.test.MycelIntegrationTestComponent;
import com.quantumresearch.mycel.app.test.DaggerMycelIntegrationTestComponent;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.sort;
import static com.quantumresearch.mycel.spore.api.cleanup.CleanupManager.BATCH_DELAY_MS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class AbstractAutoDeleteTest extends
		BriarIntegrationTest<MycelIntegrationTestComponent> {

	protected final long startTime = System.currentTimeMillis();

	protected abstract ConversationClient getConversationClient(
			MycelIntegrationTestComponent component);

	@Override
	protected void createComponents() {
		MycelIntegrationTestComponent component =
				DaggerMycelIntegrationTestComponent.builder().build();
		MycelIntegrationTestComponent.Helper.injectEagerSingletons(component);
		component.inject(this);

		c0 = DaggerMycelIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t0Dir))
				.timeTravelModule(new TimeTravelModule(true))
				.build();
		MycelIntegrationTestComponent.Helper.injectEagerSingletons(c0);

		c1 = DaggerMycelIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t1Dir))
				.timeTravelModule(new TimeTravelModule(true))
				.build();
		MycelIntegrationTestComponent.Helper.injectEagerSingletons(c1);

		c2 = DaggerMycelIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t2Dir))
				.timeTravelModule(new TimeTravelModule(true))
				.build();
		MycelIntegrationTestComponent.Helper.injectEagerSingletons(c2);

		// Use different times to avoid creating identical messages that are
		// treated as redundant copies of the same message (#1907)
		try {
			c0.getTimeTravel().setCurrentTimeMillis(startTime);
			c1.getTimeTravel().setCurrentTimeMillis(startTime + 1);
			c2.getTimeTravel().setCurrentTimeMillis(startTime + 2);
		} catch (InterruptedException e) {
			fail();
		}
	}

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		// Run the initial cleanup task that was scheduled at startup
		c0.getTimeTravel().addCurrentTimeMillis(BATCH_DELAY_MS);
		c1.getTimeTravel().addCurrentTimeMillis(BATCH_DELAY_MS);
		c2.getTimeTravel().addCurrentTimeMillis(BATCH_DELAY_MS);
	}

	protected List<ConversationMessageHeader> getMessageHeaders(
			MycelIntegrationTestComponent component, ContactId contactId)
			throws Exception {
		DatabaseComponent db = component.getDatabaseComponent();
		ConversationClient conversationClient =
				getConversationClient(component);
		return sortHeaders(db.transactionWithResult(true, txn ->
				conversationClient.getMessageHeaders(txn, contactId)));
	}

	@SuppressWarnings({"UseCompareMethod", "Java8ListSort"}) // Animal Sniffer
	protected List<ConversationMessageHeader> sortHeaders(
			Collection<ConversationMessageHeader> in) {
		List<ConversationMessageHeader> out = new ArrayList<>(in);
		sort(out, (a, b) ->
				Long.valueOf(a.getTimestamp()).compareTo(b.getTimestamp()));
		return out;
	}

	@FunctionalInterface
	protected interface HeaderConsumer {
		void accept(ConversationMessageHeader header) throws DbException;
	}

	protected void forEachHeader(MycelIntegrationTestComponent component,
			ContactId contactId, int size, HeaderConsumer consumer)
			throws Exception {
		List<ConversationMessageHeader> headers =
				getMessageHeaders(component, contactId);
		assertEquals(size, headers.size());
		for (ConversationMessageHeader h : headers) consumer.accept(h);
	}

	protected long getAutoDeleteTimer(MycelIntegrationTestComponent component,
			ContactId contactId) throws DbException {
		DatabaseComponent db = component.getDatabaseComponent();
		AutoDeleteManager autoDeleteManager = component.getAutoDeleteManager();
		return db.transactionWithResult(false,
				txn -> autoDeleteManager.getAutoDeleteTimer(txn, contactId));
	}

	protected void markMessageRead(MycelIntegrationTestComponent component,
			Contact contact, MessageId messageId) throws Exception {
		ConversationManager conversationManager =
				component.getConversationManager();
		ConversationClient conversationClient =
				getConversationClient(component);
		GroupId groupId = conversationClient.getContactGroup(contact).getId();
		conversationManager.setReadFlag(groupId, messageId, true);
		waitForEvents(component);
	}

	protected void assertGroupCount(MycelIntegrationTestComponent component,
			ContactId contactId, int messageCount, int unreadCount)
			throws DbException {
		DatabaseComponent db = component.getDatabaseComponent();
		ConversationClient conversationClient =
				getConversationClient(component);

		GroupCount gc = db.transactionWithResult(true, txn ->
				conversationClient.getGroupCount(txn, contactId));
		assertEquals("messageCount", messageCount, gc.getMsgCount());
		assertEquals("unreadCount", unreadCount, gc.getUnreadCount());
	}
}
