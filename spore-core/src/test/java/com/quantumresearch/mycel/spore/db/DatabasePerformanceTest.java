package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.identity.Identity;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.sync.validation.MessageState;
import com.quantumresearch.mycel.spore.test.BrambleTestCase;
import com.quantumresearch.mycel.spore.test.UTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import static java.util.logging.Level.OFF;
import static com.quantumresearch.mycel.spore.api.record.Record.RECORD_HEADER_BYTES;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_IDS;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_LENGTH;
import static com.quantumresearch.mycel.spore.api.sync.validation.MessageState.DELIVERED;
import static com.quantumresearch.mycel.spore.test.TestUtils.deleteTestDirectory;
import static com.quantumresearch.mycel.spore.test.TestUtils.getAuthor;
import static com.quantumresearch.mycel.spore.test.TestUtils.getGroup;
import static com.quantumresearch.mycel.spore.test.TestUtils.getIdentity;
import static com.quantumresearch.mycel.spore.test.TestUtils.getMessage;
import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomBytes;
import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.spore.test.TestUtils.getTestDirectory;
import static com.quantumresearch.mycel.spore.test.UTest.Result.INCONCLUSIVE;
import static com.quantumresearch.mycel.spore.test.UTest.Z_CRITICAL_0_1;
import static com.quantumresearch.mycel.spore.util.StringUtils.getRandomString;
import static org.junit.Assert.assertTrue;

public abstract class DatabasePerformanceTest extends BrambleTestCase {

	/**
	 * How many contacts to simulate.
	 */
	private static final int CONTACTS = 20;

	/**
	 * How many clients to simulate. Briar has nine: transport properties,
	 * introductions, messaging, forums, forum sharing, blogs,
	 * blog sharing, private groups, and private group sharing.
	 */
	private static final int CLIENTS = 10;
	private static final int CLIENT_ID_LENGTH = 50;

	/**
	 * How many groups to simulate for each contact. Briar has seven:
	 * transport properties, introductions, messaging, forum sharing, blog
	 * sharing, private group sharing, and the contact's blog.
	 */
	private static final int GROUPS_PER_CONTACT = 10;

	/**
	 * How many local groups to simulate. Briar has three: transport
	 * properties, introductions and RSS feeds.
	 */
	private static final int LOCAL_GROUPS = 5;

	private static final int MESSAGES_PER_GROUP = 20;
	private static final int METADATA_KEYS_PER_GROUP = 5;
	private static final int METADATA_KEYS_PER_MESSAGE = 5;
	private static final int METADATA_KEY_LENGTH = 10;
	private static final int METADATA_VALUE_LENGTH = 100;
	private static final int OFFERED_MESSAGES_PER_CONTACT = 100;

	/**
	 * How many benchmark iterations to run in each block.
	 */
	private static final int ITERATIONS_PER_BLOCK = 10;

	/**
	 * How many blocks must be similar before we conclude a steady state has
	 * been reached.
	 */
	private static final int STEADY_STATE_BLOCKS = 5;

	// All our transports use a maximum latency of 30 seconds
	private static final int MAX_LATENCY = 30 * 1000;

	private static final int BATCH_CAPACITY =
			(RECORD_HEADER_BYTES + MAX_MESSAGE_LENGTH) * 2;

	protected final File testDir = getTestDirectory();
	private final File resultsFile = new File(getTestName() + ".tsv");
	protected final Random random = new Random();

	private LocalAuthor localAuthor;
	private List<ClientId> clientIds;
	private List<Contact> contacts;
	private List<Group> groups;
	private List<Message> messages;
	private Map<GroupId, List<Metadata>> messageMeta;
	private Map<ContactId, List<Group>> contactGroups;
	private Map<GroupId, List<MessageId>> groupMessages;

	protected abstract String getTestName();

	protected abstract void benchmark(String name,
			BenchmarkTask<Database<Connection>> task) throws Exception;

	DatabasePerformanceTest() {
		// Disable logging
		Logger.getLogger("").setLevel(OFF);
	}

	@Before
	public void setUp() {
		assertTrue(testDir.mkdirs());
	}

	@After
	public void tearDown() {
		deleteTestDirectory(testDir);
	}

	@Test
	public void testContainsContactByAuthorId() throws Exception {
		String name = "containsContact(T, AuthorId, AuthorId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			AuthorId remote = pickRandom(contacts).getAuthor().getId();
			db.containsContact(txn, remote, localAuthor.getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testContainsContactByContactId() throws Exception {
		String name = "containsContact(T, ContactId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.containsContact(txn, pickRandom(contacts).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testContainsGroup() throws Exception {
		String name = "containsGroup(T, GroupId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.containsGroup(txn, pickRandom(groups).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testContainsIdentity() throws Exception {
		String name = "containsIdentity(T, AuthorId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.containsIdentity(txn, localAuthor.getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testContainsMessage() throws Exception {
		String name = "containsMessage(T, MessageId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.containsMessage(txn, pickRandom(messages).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testContainsVisibleMessage() throws Exception {
		String name = "containsVisibleMessage(T, ContactId, MessageId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.containsVisibleMessage(txn, pickRandom(contacts).getId(),
					pickRandom(messages).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testCountOfferedMessages() throws Exception {
		String name = "countOfferedMessages(T, ContactId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.countOfferedMessages(txn, pickRandom(contacts).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetContact() throws Exception {
		String name = "getContact(T, ContactId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getContact(txn, pickRandom(contacts).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetContacts() throws Exception {
		String name = "getContacts(T)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getContacts(txn);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetContactsByRemoteAuthorId() throws Exception {
		String name = "getContactsByAuthorId(T, AuthorId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			AuthorId remote = pickRandom(contacts).getAuthor().getId();
			db.getContactsByAuthorId(txn, remote);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetContactsByLocalAuthorId() throws Exception {
		String name = "getContacts(T, AuthorId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getContacts(txn, localAuthor.getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetGroup() throws Exception {
		String name = "getGroup(T, GroupId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getGroup(txn, pickRandom(groups).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetGroupMetadata() throws Exception {
		String name = "getGroupMetadata(T, GroupId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getGroupMetadata(txn, pickRandom(groups).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetGroups() throws Exception {
		String name = "getGroups(T, ClientId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getGroups(txn, pickRandom(clientIds), 123);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetGroupVisibilityWithContactId() throws Exception {
		String name = "getGroupVisibility(T, ContactId, GroupId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			ContactId c = pickRandom(contacts).getId();
			db.getGroupVisibility(txn, c,
					pickRandom(contactGroups.get(c)).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetGroupVisibility() throws Exception {
		String name = "getGroupVisibility(T, GroupId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getGroupVisibility(txn, pickRandom(groups).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetIdentity() throws Exception {
		String name = "getIdentity(T, AuthorId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getIdentity(txn, localAuthor.getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetIdentities() throws Exception {
		String name = "getIdentities(T)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getIdentities(txn);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageDependencies() throws Exception {
		String name = "getMessageDependencies(T, MessageId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessageDependencies(txn, pickRandom(messages).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageDependents() throws Exception {
		String name = "getMessageDependents(T, MessageId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessageDependents(txn, pickRandom(messages).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageIds() throws Exception {
		String name = "getMessageIds(T, GroupId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessageIds(txn, pickRandom(groups).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageIdsWithMatchingQuery() throws Exception {
		String name = "getMessageIds(T, GroupId, Metadata) [match]";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			GroupId g = pickRandom(groups).getId();
			db.getMessageIds(txn, g, pickRandom(messageMeta.get(g)));
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageIdsWithNonMatchingQuery() throws Exception {
		String name = "getMessageIds(T, GroupId, Metadata) [no match]";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			Metadata query = getMetadata(METADATA_KEYS_PER_MESSAGE);
			db.getMessageIds(txn, pickRandom(groups).getId(), query);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageMetadataByGroupId() throws Exception {
		String name = "getMessageMetadata(T, GroupId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessageMetadata(txn, pickRandom(groups).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageMetadataByMessageId() throws Exception {
		String name = "getMessageMetadata(T, MessageId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessageMetadata(txn, pickRandom(messages).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageMetadataForValidator() throws Exception {
		String name = "getMessageMetadataForValidator(T, MessageId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessageMetadataForValidator(txn,
					pickRandom(messages).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageState() throws Exception {
		String name = "getMessageState(T, MessageId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessageState(txn, pickRandom(messages).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageStatusByGroupId() throws Exception {
		String name = "getMessageStatus(T, ContactId, GroupId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			ContactId c = pickRandom(contacts).getId();
			GroupId g = pickRandom(contactGroups.get(c)).getId();
			db.getMessageStatus(txn, c, g);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessageStatusByMessageId() throws Exception {
		String name = "getMessageStatus(T, ContactId, MessageId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			ContactId c = pickRandom(contacts).getId();
			GroupId g = pickRandom(contactGroups.get(c)).getId();
			db.getMessageStatus(txn, c, pickRandom(groupMessages.get(g)));
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessagesToAck() throws Exception {
		String name = "getMessagesToAck(T, ContactId, int)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessagesToAck(txn, pickRandom(contacts).getId(),
					MAX_MESSAGE_IDS);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessagesToOffer() throws Exception {
		String name = "getMessagesToOffer(T, ContactId, int)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessagesToOffer(txn, pickRandom(contacts).getId(),
					MAX_MESSAGE_IDS, MAX_LATENCY);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessagesToRequest() throws Exception {
		String name = "getMessagesToRequest(T, ContactId, int)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessagesToRequest(txn, pickRandom(contacts).getId(),
					MAX_MESSAGE_IDS);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessagesToSend() throws Exception {
		String name = "getMessagesToSend(T, ContactId, int)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessagesToSend(txn, pickRandom(contacts).getId(),
					BATCH_CAPACITY, MAX_LATENCY);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessagesToShare() throws Exception {
		String name = "getMessagesToShare(T)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessagesToShare(txn);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessagesToValidate() throws Exception {
		String name = "getMessagesToValidate(T)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessagesToValidate(txn);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetPendingMessages() throws Exception {
		String name = "getPendingMessages(T)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getPendingMessages(txn);
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetMessage() throws Exception {
		String name = "getMessage(T, MessageId)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getMessage(txn, pickRandom(messages).getId());
			db.commitTransaction(txn);
		});
	}

	@Test
	public void testGetRequestedMessagesToSend() throws Exception {
		String name = "getRequestedMessagesToSend(T, ContactId, int)";
		benchmark(name, db -> {
			Connection txn = db.startTransaction();
			db.getRequestedMessagesToSend(txn, pickRandom(contacts).getId(),
					BATCH_CAPACITY, MAX_LATENCY);
			db.commitTransaction(txn);
		});
	}

	private <T> T pickRandom(List<T> list) {
		return list.get(random.nextInt(list.size()));
	}

	void populateDatabase(Database<Connection> db) throws DbException {
		Identity identity = getIdentity();
		localAuthor = identity.getLocalAuthor();
		clientIds = new ArrayList<>();
		contacts = new ArrayList<>();
		groups = new ArrayList<>();
		messages = new ArrayList<>();
		messageMeta = new HashMap<>();
		contactGroups = new HashMap<>();
		groupMessages = new HashMap<>();

		for (int i = 0; i < CLIENTS; i++) clientIds.add(getClientId());

		Connection txn = db.startTransaction();
		db.addIdentity(txn, identity);
		for (int i = 0; i < CONTACTS; i++) {
			ContactId c = db.addContact(txn, getAuthor(), localAuthor.getId(),
					null, random.nextBoolean());
			contacts.add(db.getContact(txn, c));
			contactGroups.put(c, new ArrayList<>());
			for (int j = 0; j < GROUPS_PER_CONTACT; j++) {
				Group g = getGroup(clientIds.get(j % CLIENTS), 123);
				groups.add(g);
				messageMeta.put(g.getId(), new ArrayList<>());
				contactGroups.get(c).add(g);
				groupMessages.put(g.getId(), new ArrayList<>());
				db.addGroup(txn, g);
				db.addGroupVisibility(txn, c, g.getId(), true);
				Metadata gm = getMetadata(METADATA_KEYS_PER_GROUP);
				db.mergeGroupMetadata(txn, g.getId(), gm);
				for (int k = 0; k < MESSAGES_PER_GROUP; k++) {
					Message m = getMessage(g.getId());
					messages.add(m);
					MessageState state =
							MessageState.fromValue(random.nextInt(4));
					boolean shared = random.nextBoolean();
					boolean temporary = random.nextBoolean();
					ContactId sender = random.nextBoolean() ? c : null;
					db.addMessage(txn, m, state, shared, temporary, sender);
					if (random.nextBoolean())
						db.raiseRequestedFlag(txn, c, m.getId());
					Metadata mm = getMetadata(METADATA_KEYS_PER_MESSAGE);
					messageMeta.get(g.getId()).add(mm);
					db.mergeMessageMetadata(txn, m.getId(), mm);
					if (k > 0) {
						MessageId dependency =
								pickRandom(groupMessages.get(g.getId()));
						db.addMessageDependency(txn, m, dependency, state);
					}
					groupMessages.get(g.getId()).add(m.getId());
				}
			}
			for (int j = 0; j < OFFERED_MESSAGES_PER_CONTACT; j++) {
				db.addOfferedMessage(txn, c, new MessageId(getRandomId()));
			}
		}
		for (int i = 0; i < LOCAL_GROUPS; i++) {
			Group g = getGroup(clientIds.get(i % CLIENTS), 123);
			groups.add(g);
			messageMeta.put(g.getId(), new ArrayList<>());
			groupMessages.put(g.getId(), new ArrayList<>());
			db.addGroup(txn, g);
			Metadata gm = getMetadata(METADATA_KEYS_PER_GROUP);
			db.mergeGroupMetadata(txn, g.getId(), gm);
			for (int j = 0; j < MESSAGES_PER_GROUP; j++) {
				Message m = getMessage(g.getId());
				messages.add(m);
				boolean temporary = random.nextBoolean();
				db.addMessage(txn, m, DELIVERED, false, temporary, null);
				Metadata mm = getMetadata(METADATA_KEYS_PER_MESSAGE);
				messageMeta.get(g.getId()).add(mm);
				db.mergeMessageMetadata(txn, m.getId(), mm);
				if (j > 0) {
					MessageId dependency =
							pickRandom(groupMessages.get(g.getId()));
					db.addMessageDependency(txn, m, dependency, DELIVERED);
				}
				groupMessages.get(g.getId()).add(m.getId());
			}
		}
		db.commitTransaction(txn);
	}

	private ClientId getClientId() {
		return new ClientId(getRandomString(CLIENT_ID_LENGTH));
	}

	private Metadata getMetadata(int keys) {
		Metadata meta = new Metadata();
		for (int i = 0; i < keys; i++) {
			String key = getRandomString(METADATA_KEY_LENGTH);
			byte[] value = getRandomBytes(METADATA_VALUE_LENGTH);
			meta.put(key, value);
		}
		return meta;
	}

	long measureOne(Database<Connection> db,
			BenchmarkTask<Database<Connection>> task) throws Exception {
		long start = System.nanoTime();
		task.run(db);
		return System.nanoTime() - start;
	}

	private List<Double> measureBlock(Database<Connection> db,
			BenchmarkTask<Database<Connection>> task) throws Exception {
		List<Double> durations = new ArrayList<>(ITERATIONS_PER_BLOCK);
		for (int i = 0; i < ITERATIONS_PER_BLOCK; i++)
			durations.add((double) measureOne(db, task));
		return durations;
	}

	SteadyStateResult measureSteadyState(Database<Connection> db,
			BenchmarkTask<Database<Connection>> task) throws Exception {
		List<Double> durations = measureBlock(db, task);
		int blocks = 1, steadyBlocks = 1;
		while (steadyBlocks < STEADY_STATE_BLOCKS) {
			List<Double> prev = durations;
			durations = measureBlock(db, task);
			// Compare to the previous block with a large P value, which
			// decreases our chance of getting an inconclusive result, making
			// this a conservative test for steady state
			if (UTest.test(prev, durations, Z_CRITICAL_0_1) == INCONCLUSIVE)
				steadyBlocks++;
			else steadyBlocks = 1;
			blocks++;
		}
		return new SteadyStateResult(blocks, durations);
	}

	void writeResult(String result) throws IOException {
		System.out.println(result);
		PrintWriter out =
				new PrintWriter(new FileOutputStream(resultsFile, true), true);
		out.println(new Date() + "\t" + result);
		out.close();
	}

	static class SteadyStateResult {

		final int blocks;
		final List<Double> durations;

		SteadyStateResult(int blocks, List<Double> durations) {
			this.blocks = blocks;
			this.durations = durations;
		}
	}
}
