package com.quantumresearch.mycel.app.test;

import com.quantumresearch.mycel.spore.api.FeatureFlags;
import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.identity.AuthorFactory;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.plugin.BluetoothConstants;
import com.quantumresearch.mycel.spore.api.plugin.LanTcpConstants;
import com.quantumresearch.mycel.spore.api.plugin.TorConstants;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.GroupFactory;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.app.api.avatar.AvatarManager;
import com.quantumresearch.mycel.app.api.avatar.AvatarMessageEncoder;
import com.quantumresearch.mycel.app.api.blog.Blog;
import com.quantumresearch.mycel.app.api.blog.BlogManager;
import com.quantumresearch.mycel.app.api.blog.BlogPost;
import com.quantumresearch.mycel.app.api.blog.BlogPostFactory;
import com.quantumresearch.mycel.app.api.forum.Forum;
import com.quantumresearch.mycel.app.api.forum.ForumManager;
import com.quantumresearch.mycel.app.api.forum.ForumPost;
import com.quantumresearch.mycel.app.api.messaging.MessagingManager;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessage;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessageFactory;
import com.quantumresearch.mycel.app.api.privategroup.GroupMessage;
import com.quantumresearch.mycel.app.api.privategroup.GroupMessageFactory;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroup;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupFactory;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationFactory;
import com.quantumresearch.mycel.app.api.test.TestAvatarCreator;
import com.quantumresearch.mycel.app.api.test.TestDataCreator;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import static java.util.Collections.emptyList;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.plugin.BluetoothConstants.UUID_BYTES;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.SHARED;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;
import static com.quantumresearch.mycel.spore.util.StringUtils.getRandomString;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static com.quantumresearch.mycel.app.test.TestData.AUTHOR_NAMES;
import static com.quantumresearch.mycel.app.test.TestData.GROUP_NAMES;

@NotNullByDefault
public class TestDataCreatorImpl implements TestDataCreator {

	private final Logger LOG =
			getLogger(TestDataCreatorImpl.class.getName());

	private final AuthorFactory authorFactory;
	private final Clock clock;
	private final GroupFactory groupFactory;
	private final PrivateMessageFactory privateMessageFactory;
	private final BlogPostFactory blogPostFactory;

	private final DatabaseComponent db;
	private final IdentityManager identityManager;
	private final CryptoComponent crypto;
	private final ContactManager contactManager;
	private final TransportPropertyManager transportPropertyManager;
	private final MessagingManager messagingManager;
	private final BlogManager blogManager;
	private final ForumManager forumManager;
	private final PrivateGroupManager privateGroupManager;
	private final PrivateGroupFactory privateGroupFactory;
	private final GroupMessageFactory groupMessageFactory;
	private final GroupInvitationFactory groupInvitationFactory;
	private final TestAvatarCreator testAvatarCreator;
	private final AvatarMessageEncoder avatarMessageEncoder;

	private final FeatureFlags featureFlags;

	@IoExecutor
	private final Executor ioExecutor;

	private final Random random = new Random();
	private final Map<Contact, LocalAuthor> localAuthors = new HashMap<>();

	@Inject
	TestDataCreatorImpl(AuthorFactory authorFactory, Clock clock,
			GroupFactory groupFactory,
			PrivateMessageFactory privateMessageFactory,
			BlogPostFactory blogPostFactory, DatabaseComponent db,
			IdentityManager identityManager,
			CryptoComponent crypto,
			ContactManager contactManager,
			TransportPropertyManager transportPropertyManager,
			MessagingManager messagingManager, BlogManager blogManager,
			ForumManager forumManager,
			PrivateGroupManager privateGroupManager,
			PrivateGroupFactory privateGroupFactory,
			GroupMessageFactory groupMessageFactory,
			GroupInvitationFactory groupInvitationFactory,
			TestAvatarCreator testAvatarCreator,
			AvatarMessageEncoder avatarMessageEncoder,
			FeatureFlags featureFlags,
			@IoExecutor Executor ioExecutor) {
		this.authorFactory = authorFactory;
		this.clock = clock;
		this.groupFactory = groupFactory;
		this.privateMessageFactory = privateMessageFactory;
		this.blogPostFactory = blogPostFactory;
		this.db = db;
		this.identityManager = identityManager;
		this.crypto = crypto;
		this.contactManager = contactManager;
		this.transportPropertyManager = transportPropertyManager;
		this.messagingManager = messagingManager;
		this.blogManager = blogManager;
		this.forumManager = forumManager;
		this.privateGroupManager = privateGroupManager;
		this.privateGroupFactory = privateGroupFactory;
		this.groupMessageFactory = groupMessageFactory;
		this.groupInvitationFactory = groupInvitationFactory;
		this.testAvatarCreator = testAvatarCreator;
		this.avatarMessageEncoder = avatarMessageEncoder;
		this.featureFlags = featureFlags;
		this.ioExecutor = ioExecutor;
	}

	@Override
	public void createTestData(int numContacts, int numPrivateMsgs,
			int avatarPercent, int numBlogPosts, int numForums,
			int numForumPosts, int numPrivateGroups,
			int numPrivateGroupMessages) {
		if (numContacts == 0) throw new IllegalArgumentException();
		if (avatarPercent < 0 || avatarPercent > 100)
			throw new IllegalArgumentException();
		ioExecutor.execute(() -> {
			try {
				createTestDataOnIoExecutor(numContacts, numPrivateMsgs,
						avatarPercent, numBlogPosts, numForums, numForumPosts,
						numPrivateGroups, numPrivateGroupMessages);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	@IoExecutor
	private void createTestDataOnIoExecutor(int numContacts, int numPrivateMsgs,
			int avatarPercent, int numBlogPosts, int numForums,
			int numForumPosts, int numPrivateGroups,
			int numPrivateGroupMessages) throws DbException {
		List<Contact> contacts = createContacts(numContacts, avatarPercent);
		createPrivateMessages(contacts, numPrivateMsgs);
		createBlogPosts(contacts, numBlogPosts);
		List<Forum> forums = createForums(contacts, numForums);
		for (Forum forum : forums) {
			createRandomForumPosts(forum, contacts, numForumPosts);
		}
		List<PrivateGroup> groups =
				createPrivateGroups(contacts, numPrivateGroups);
		for (PrivateGroup group : groups) {
			createRandomPrivateGroupMessages(group, contacts,
					numPrivateGroupMessages);
		}
	}

	private List<Contact> createContacts(int numContacts, int avatarPercent)
			throws DbException {
		List<Contact> contacts = new ArrayList<>(numContacts);
		LocalAuthor localAuthor = identityManager.getLocalAuthor();
		for (int i = 0; i < numContacts; i++) {
			LocalAuthor remote = getRandomAuthor();
			Contact contact = addContact(localAuthor.getId(), remote,
					random.nextBoolean(), avatarPercent);
			contacts.add(contact);
		}
		return contacts;
	}

	private Contact addContact(AuthorId localAuthorId, LocalAuthor remote,
			boolean alias, int avatarPercent) throws DbException {
		// prepare to add contact
		SecretKey secretKey = getSecretKey();
		long timestamp = clock.currentTimeMillis();
		boolean verified = random.nextBoolean();

		// prepare transport properties
		Map<TransportId, TransportProperties> props =
				getRandomTransportProperties();

		Contact contact = db.transactionWithResult(false, txn -> {
			ContactId contactId = contactManager.addContact(txn, remote,
					localAuthorId, secretKey, timestamp, true, verified, true);
			if (alias) {
				contactManager.setContactAlias(txn, contactId,
						getRandomAuthorName());
			}
			transportPropertyManager.addRemoteProperties(txn, contactId, props);
			return db.getContact(txn, contactId);
		});
		if (random.nextInt(100) + 1 <= avatarPercent) addAvatar(contact);

		if (LOG.isLoggable(INFO)) {
			LOG.info("Added contact " + remote.getName() +
					" with transport properties: " + props);
		}
		localAuthors.put(contact, remote);
		return contact;
	}

	@Override
	public Contact addContact(String name, boolean alias, boolean avatar)
			throws DbException {
		LocalAuthor localAuthor = identityManager.getLocalAuthor();
		LocalAuthor remote = authorFactory.createLocalAuthor(name);
		int avatarPercent = avatar ? 100 : 0;
		return addContact(localAuthor.getId(), remote, alias, avatarPercent);
	}

	private String getRandomAuthorName() {
		int i = random.nextInt(AUTHOR_NAMES.length);
		return AUTHOR_NAMES[i];
	}

	private LocalAuthor getRandomAuthor() {
		return authorFactory.createLocalAuthor(getRandomAuthorName());
	}

	private SecretKey getSecretKey() {
		byte[] b = new byte[SecretKey.LENGTH];
		random.nextBytes(b);
		return new SecretKey(b);
	}

	private Map<TransportId, TransportProperties> getRandomTransportProperties() {
		Map<TransportId, TransportProperties> props = new HashMap<>();
		// Bluetooth
		TransportProperties bt = new TransportProperties();
		String btAddress = getRandomBluetoothAddress();
		String uuid = getRandomUUID();
		bt.put(BluetoothConstants.PROP_ADDRESS, btAddress);
		bt.put(BluetoothConstants.PROP_UUID, uuid);
		props.put(BluetoothConstants.ID, bt);

		// LAN
		TransportProperties lan = new TransportProperties();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			if (sb.length() > 0) sb.append(',');
			sb.append(getRandomLanAddress());
		}
		lan.put(LanTcpConstants.PROP_IP_PORTS, sb.toString());
		String port = String.valueOf(getRandomPortNumber());
		lan.put(LanTcpConstants.PROP_PORT, port);
		props.put(LanTcpConstants.ID, lan);

		// Tor
		TransportProperties tor = new TransportProperties();
		String torAddress = getRandomTorAddress();
		tor.put(TorConstants.PROP_ONION_V3, torAddress);
		props.put(TorConstants.ID, tor);

		return props;
	}

	private String getRandomBluetoothAddress() {
		byte[] mac = new byte[6];
		random.nextBytes(mac);

		StringBuilder sb = new StringBuilder(18);
		for (byte b : mac) {
			if (sb.length() > 0) sb.append(":");
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	private String getRandomUUID() {
		byte[] uuid = new byte[UUID_BYTES];
		random.nextBytes(uuid);
		return UUID.nameUUIDFromBytes(uuid).toString();
	}

	private String getRandomLanAddress() {
		StringBuilder sb = new StringBuilder();
		// address
		if (random.nextInt(5) == 0) {
			sb.append("10.");
			sb.append(random.nextInt(2)).append('.');
		} else {
			sb.append("192.168.");
		}
		sb.append(random.nextInt(2)).append('.');
		sb.append(random.nextInt(255));
		// port
		sb.append(':').append(getRandomPortNumber());
		return sb.toString();
	}

	private int getRandomPortNumber() {
		return 32768 + random.nextInt(32768);
	}

	private String getRandomTorAddress() {
		byte[] pubkeyBytes =
				crypto.generateSignatureKeyPair().getPublic().getEncoded();
		return crypto.encodeOnion(pubkeyBytes);
	}

	private void addAvatar(Contact c) throws DbException {
		AuthorId authorId = c.getAuthor().getId();
		GroupId groupId = groupFactory.createGroup(AvatarManager.CLIENT_ID,
				AvatarManager.MAJOR_VERSION, authorId.getBytes()).getId();
		InputStream is;
		try {
			is = testAvatarCreator.getAvatarInputStream();
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			return;
		}
		if (is == null) return;
		Message m;
		try {
			m = avatarMessageEncoder.encodeUpdateMessage(groupId, 0,
					"image/jpeg", is).getFirst();
		} catch (IOException e) {
			throw new DbException(e);
		}
		db.transaction(false, txn -> {
			// TODO: Do this properly via clients without breaking encapsulation
			db.setGroupVisibility(txn, c.getId(), groupId, SHARED);
			db.receiveMessage(txn, c.getId(), m);
		});
	}

	// TODO: Do this properly via clients without breaking encapsulation
	private void shareGroup(ContactId contactId, GroupId groupId)
			throws DbException {
		db.transaction(false, txn ->
				db.setGroupVisibility(txn, contactId, groupId, SHARED));
	}

	private void createPrivateMessages(List<Contact> contacts,
			int numPrivateMsgs) throws DbException {
		for (Contact contact : contacts) {
			Group group = messagingManager.getContactGroup(contact);
			shareGroup(contact.getId(), group.getId());
			for (int i = 0; i < numPrivateMsgs; i++) {
				createRandomPrivateMessage(contact.getId(), group.getId(), i);
			}
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numPrivateMsgs +
					" private messages per contact.");
		}
	}

	private void createRandomPrivateMessage(ContactId contactId,
			GroupId groupId, int num) throws DbException {
		long timestamp = clock.currentTimeMillis() - (long) num * 60 * 1000;
		String text = getRandomText();
		boolean local = random.nextBoolean();
		boolean autoDelete = random.nextBoolean();
		createPrivateMessage(contactId, groupId, text, timestamp, local,
				autoDelete);
	}

	private void createPrivateMessage(ContactId contactId, GroupId groupId,
			String text, long timestamp, boolean local, boolean autoDelete)
			throws DbException {
		long timer = autoDelete ?
				MIN_AUTO_DELETE_TIMER_MS : NO_AUTO_DELETE_TIMER;
		try {
			PrivateMessage m = privateMessageFactory.createPrivateMessage(
					groupId, timestamp, text, emptyList(), timer);
			if (local) {
				messagingManager.addLocalMessage(m);
			} else {
				db.transaction(false, txn ->
						db.receiveMessage(txn, contactId, m.getMessage()));
			}
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	private void createBlogPosts(List<Contact> contacts, int numBlogPosts)
			throws DbException {
		if (!featureFlags.shouldEnableBlogsInCore()) return;
		LocalAuthor localAuthor = identityManager.getLocalAuthor();
		Blog ours = blogManager.getPersonalBlog(localAuthor);
		for (Contact contact : contacts) {
			Blog theirs = blogManager.getPersonalBlog(contact.getAuthor());
			shareGroup(contact.getId(), ours.getId());
			shareGroup(contact.getId(), theirs.getId());
		}
		for (int i = 0; i < numBlogPosts; i++) {
			Contact contact = contacts.get(random.nextInt(contacts.size()));
			LocalAuthor author = localAuthors.get(contact);
			addBlogPost(contact.getId(), author, i);
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numBlogPosts + " blog posts.");
		}
	}

	private void addBlogPost(ContactId contactId, LocalAuthor author, int num)
			throws DbException {
		Blog blog = blogManager.getPersonalBlog(author);
		long timestamp = clock.currentTimeMillis() - (long) num * 60 * 1000;
		String text = getRandomText();
		try {
			BlogPost blogPost = blogPostFactory.createBlogPost(blog.getId(),
					timestamp, null, author, text);
			db.transaction(false, txn ->
					db.receiveMessage(txn, contactId, blogPost.getMessage()));
		} catch (FormatException | GeneralSecurityException e) {
			throw new AssertionError(e);
		}
	}

	private List<Forum> createForums(List<Contact> contacts, int numForums)
			throws DbException {
		if (!featureFlags.shouldEnableForumsInCore()) return emptyList();
		List<Forum> forums = new ArrayList<>(numForums);
		for (int i = 0; i < numForums; i++) {
			// create forum
			String name = GROUP_NAMES[random.nextInt(GROUP_NAMES.length)];
			Forum forum = forumManager.addForum(name);

			// share with all contacts
			for (Contact contact : contacts) {
				shareGroup(contact.getId(), forum.getId());
			}
			forums.add(forum);
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numForums + " forums.");
		}
		return forums;
	}

	private void createRandomForumPosts(Forum forum, List<Contact> contacts,
			int numForumPosts) throws DbException {
		List<ForumPost> posts = new ArrayList<>();
		for (int i = 0; i < numForumPosts; i++) {
			Contact contact = contacts.get(random.nextInt(contacts.size()));
			LocalAuthor author = localAuthors.get(contact);
			long timestamp = clock.currentTimeMillis() - (long) i * 60 * 1000;
			String text = getRandomText();
			MessageId parent = null;
			if (random.nextBoolean() && posts.size() > 0) {
				ForumPost parentPost = posts.get(random.nextInt(posts.size()));
				parent = parentPost.getMessage().getId();
			}
			ForumPost post = forumManager.createLocalPost(forum.getId(), text,
					timestamp, parent, author);
			posts.add(post);
			db.transaction(false, txn ->
					db.receiveMessage(txn, contact.getId(), post.getMessage()));
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numForumPosts + " forum posts.");
		}
	}

	private List<PrivateGroup> createPrivateGroups(List<Contact> contacts,
			int numPrivateGroups) throws DbException {
		if (!featureFlags.shouldEnablePrivateGroupsInCore()) return emptyList();
		List<PrivateGroup> groups = new ArrayList<>(numPrivateGroups);
		for (int i = 0; i < numPrivateGroups; i++) {
			// create private group
			String name = GROUP_NAMES[random.nextInt(GROUP_NAMES.length)];
			LocalAuthor creator = identityManager.getLocalAuthor();
			PrivateGroup group =
					privateGroupFactory.createPrivateGroup(name, creator);
			GroupMessage joinMsg = groupMessageFactory.createJoinMessage(
					group.getId(),
					clock.currentTimeMillis() - (long) (100 - i) * 60 * 1000,
					creator
			);
			privateGroupManager.addPrivateGroup(group, joinMsg, true);
			groups.add(group);
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numPrivateGroups + " private groups.");
		}
		return groups;
	}

	private void createRandomPrivateGroupMessages(PrivateGroup group,
			List<Contact> contacts, int amount) throws DbException {
		List<GroupMessage> messages = new ArrayList<>();
		PrivateKey creatorPrivateKey =
				identityManager.getLocalAuthor().getPrivateKey();
		int numMembers = random.nextInt(contacts.size());
		if (numMembers == 0) numMembers++;
		Map<Contact, MessageId> membersLastMessage = new HashMap<>();
		List<Contact> members = new ArrayList<>(numMembers);
		for (int i = 0; i < numMembers; i++) {
			Contact contact = contacts.get(i);
			members.add(contact);
		}
		for (int i = 0; i < amount; i++) {
			Contact contact = members.get(random.nextInt(numMembers));
			LocalAuthor author = localAuthors.get(contact);
			long timestamp =
					clock.currentTimeMillis() -
							(long) (amount - i) * 60 * 1000;

			GroupMessage msg;
			if (!membersLastMessage.containsKey(contact)) {
				// join message as first message of member
				shareGroup(contact.getId(), group.getId());
				long inviteTimestamp = timestamp - 1;
				byte[] creatorSignature =
						groupInvitationFactory.signInvitation(contact,
								group.getId(), inviteTimestamp,
								creatorPrivateKey);
				msg = groupMessageFactory.createJoinMessage(group.getId(),
								timestamp, author, inviteTimestamp,
								creatorSignature);
			} else {
				// random text after first message
				String text = getRandomText();
				MessageId parent = null;
				if (random.nextBoolean() && messages.size() > 0) {
					GroupMessage parentMessage =
							messages.get(random.nextInt(messages.size()));
					parent = parentMessage.getMessage().getId();
				}
				MessageId lastMsg = membersLastMessage.get(contact);
				msg = groupMessageFactory.createGroupMessage(
						group.getId(), timestamp, parent, author, text,
						lastMsg);
				messages.add(msg);
			}
			membersLastMessage.put(contact, msg.getMessage().getId());
			db.transaction(false, txn ->
					db.receiveMessage(txn, contact.getId(), msg.getMessage()));
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + amount + " private group messages.");
		}
	}

	private String getRandomText() {
		int minLength = 3 + random.nextInt(500);
		int maxWordLength = 15;
		StringBuilder sb = new StringBuilder();
		while (sb.length() < minLength) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(getRandomString(random.nextInt(maxWordLength) + 1));
		}
		if (random.nextBoolean()) {
			sb.append(" \uD83D\uDC96 \uD83E\uDD84 \uD83C\uDF08");
		}
		return sb.toString();
	}

}
