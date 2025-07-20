package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.client.ContactGroupFactory;
import com.quantumresearch.mycel.infrastructure.api.contact.Contact;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.data.BdfDictionary;
import com.quantumresearch.mycel.infrastructure.api.data.MetadataParser;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.db.Metadata;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.infrastructure.api.identity.Author;
import com.quantumresearch.mycel.infrastructure.api.identity.IdentityManager;
import com.quantumresearch.mycel.infrastructure.api.identity.LocalAuthor;
import com.quantumresearch.mycel.infrastructure.api.sync.Group;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.infrastructure.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.infrastructure.test.BrambleMockTestCase;
import com.quantumresearch.mycel.app.api.blog.Blog;
import com.quantumresearch.mycel.app.api.blog.BlogInvitationResponse;
import com.quantumresearch.mycel.app.api.blog.BlogManager;
import com.quantumresearch.mycel.app.api.client.MessageTracker;
import com.quantumresearch.mycel.app.api.client.SessionId;
import org.jmock.Expectations;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.quantumresearch.mycel.infrastructure.api.sync.Group.Visibility.SHARED;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getAuthor;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getContact;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getGroup;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getLocalAuthor;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getMessage;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.app.api.blog.BlogSharingManager.CLIENT_ID;
import static com.quantumresearch.mycel.app.api.blog.BlogSharingManager.MAJOR_VERSION;

public class BlogSharingManagerImplTest extends BrambleMockTestCase {

	private final BlogSharingManagerImpl blogSharingManager;
	private final DatabaseComponent db = context.mock(DatabaseComponent.class);
	private final IdentityManager identityManager =
			context.mock(IdentityManager.class);
	private final ClientHelper clientHelper = context.mock(ClientHelper.class);
	private final ClientVersioningManager clientVersioningManager =
			context.mock(ClientVersioningManager.class);
	private final SessionEncoder sessionEncoder =
			context.mock(SessionEncoder.class);
	private final SessionParser sessionParser =
			context.mock(SessionParser.class);
	private final ContactGroupFactory contactGroupFactory =
			context.mock(ContactGroupFactory.class);
	private final BlogManager blogManager = context.mock(BlogManager.class);

	private final LocalAuthor localAuthor = getLocalAuthor();
	private final Author author = getAuthor();
	private final Contact contact =
			getContact(author, localAuthor.getId(), true);
	private final ContactId contactId = contact.getId();
	private final Collection<Contact> contacts =
			Collections.singletonList(contact);
	private final Group localGroup = getGroup(CLIENT_ID, MAJOR_VERSION);
	private final Group contactGroup = getGroup(CLIENT_ID, MAJOR_VERSION);
	private final Group blogGroup =
			getGroup(BlogManager.CLIENT_ID, BlogManager.MAJOR_VERSION);
	private final Blog blog = new Blog(blogGroup, author, false);
	private final Group localBlogGroup =
			getGroup(BlogManager.CLIENT_ID, BlogManager.MAJOR_VERSION);
	private final Blog localBlog = new Blog(localBlogGroup, localAuthor, false);
	@SuppressWarnings("unchecked")
	private final ProtocolEngine<Blog> engine =
			context.mock(ProtocolEngine.class);

	@SuppressWarnings("unchecked")
	public BlogSharingManagerImplTest() {
		MetadataParser metadataParser = context.mock(MetadataParser.class);
		MessageParser<Blog> messageParser = context.mock(MessageParser.class);
		MessageTracker messageTracker = context.mock(MessageTracker.class);
		InvitationFactory<Blog, BlogInvitationResponse> invitationFactory =
				context.mock(InvitationFactory.class);
		blogSharingManager = new BlogSharingManagerImpl(db, clientHelper,
				clientVersioningManager, metadataParser, messageParser,
				sessionEncoder, sessionParser, messageTracker,
				contactGroupFactory, engine, invitationFactory, identityManager,
				blogManager);
	}

	@Test
	public void testOpenDatabaseHookFirstTimeWithExistingContact()
			throws Exception {
		Transaction txn = new Transaction(null, false);

		context.checking(new Expectations() {{
			// The local group doesn't exist - we need to set things up
			oneOf(contactGroupFactory).createLocalGroup(CLIENT_ID,
					MAJOR_VERSION);
			will(returnValue(localGroup));
			oneOf(db).containsGroup(txn, localGroup.getId());
			will(returnValue(false));
			oneOf(db).addGroup(txn, localGroup);
			// Get contacts
			oneOf(db).getContacts(txn);
			will(returnValue(contacts));
		}});
		// Set things up for the contact
		expectAddingContact(txn);

		blogSharingManager.onDatabaseOpened(txn);
	}

	private void expectAddingContact(Transaction txn) throws Exception {
		Map<MessageId, BdfDictionary> sessions = Collections.emptyMap();

		context.checking(new Expectations() {{
			// Create the contact group and share it with the contact
			oneOf(contactGroupFactory).createContactGroup(CLIENT_ID,
					MAJOR_VERSION, contact);
			will(returnValue(contactGroup));
			oneOf(db).addGroup(txn, contactGroup);
			oneOf(clientVersioningManager).getClientVisibility(txn, contactId,
					CLIENT_ID, MAJOR_VERSION);
			will(returnValue(SHARED));
			oneOf(db).setGroupVisibility(txn, contactId, contactGroup.getId(),
					SHARED);
			// Attach the contact ID to the group
			oneOf(clientHelper)
					.setContactId(txn, contactGroup.getId(), contactId);
			// Get our blog and the contact's blog
			oneOf(identityManager).getLocalAuthor(txn);
			will(returnValue(localAuthor));
			oneOf(blogManager).getPersonalBlog(localAuthor);
			will(returnValue(localBlog));
			oneOf(blogManager).getPersonalBlog(author);
			will(returnValue(blog));
		}});
		// Pre-share our blog with the contact and vice versa
		expectPreShareShareable(txn, contact, localBlog, sessions);
		expectPreShareShareable(txn, contact, blog, sessions);
	}

	@Test
	public void testOpenDatabaseHookSubsequentTime() throws Exception {
		Transaction txn = new Transaction(null, false);

		context.checking(new Expectations() {{
			// The local group exists - everything has been set up
			oneOf(contactGroupFactory).createLocalGroup(CLIENT_ID,
					MAJOR_VERSION);
			will(returnValue(localGroup));
			oneOf(db).containsGroup(txn, localGroup.getId());
			will(returnValue(true));
		}});

		blogSharingManager.onDatabaseOpened(txn);
	}

	@Test
	public void testAddingContact() throws Exception {
		Transaction txn = new Transaction(null, false);

		expectAddingContact(txn);

		blogSharingManager.addingContact(txn, contact);
	}

	@Test
	public void testRemovingBlogFreshState() throws Exception {
		Map<MessageId, BdfDictionary> sessions = new HashMap<>(0);
		testRemovingBlog(sessions);
	}

	@Test
	public void testRemovingBlogExistingState() throws Exception {
		Map<MessageId, BdfDictionary> sessions = new HashMap<>(1);
		sessions.put(new MessageId(getRandomId()), new BdfDictionary());
		testRemovingBlog(sessions);
	}

	@Test(expected = DbException.class)
	public void testRemovingBlogMultipleSessions() throws Exception {
		Map<MessageId, BdfDictionary> sessions = new HashMap<>(2);
		sessions.put(new MessageId(getRandomId()), new BdfDictionary());
		sessions.put(new MessageId(getRandomId()), new BdfDictionary());
		testRemovingBlog(sessions);
	}

	private void expectPreShareShareable(Transaction txn, Contact contact,
			Blog blog, Map<MessageId, BdfDictionary> sessions)
			throws Exception {
		Group contactGroup = getGroup(CLIENT_ID, MAJOR_VERSION);
		BdfDictionary sessionDict = new BdfDictionary();
		Message message = getMessage(contactGroup.getId());
		context.checking(new Expectations() {{
			oneOf(contactGroupFactory).createContactGroup(CLIENT_ID,
					MAJOR_VERSION, contact);
			will(returnValue(contactGroup));
			oneOf(sessionParser)
					.getSessionQuery(new SessionId(blog.getId().getBytes()));
			will(returnValue(sessionDict));
			oneOf(clientHelper).getMessageMetadataAsDictionary(txn,
					contactGroup.getId(), sessionDict);
			will(returnValue(sessions));
			if (sessions.size() == 0) {
				oneOf(db).addGroup(txn, blog.getGroup());
				oneOf(clientVersioningManager).getClientVisibility(txn,
						contactId, BlogManager.CLIENT_ID,
						BlogManager.MAJOR_VERSION);
				will(returnValue(SHARED));
				oneOf(db).setGroupVisibility(txn, contact.getId(),
						blog.getGroup().getId(), SHARED);
				oneOf(clientHelper)
						.createMessageForStoringMetadata(contactGroup.getId());
				will(returnValue(message));
				oneOf(db).addLocalMessage(txn, message, new Metadata(), false,
						false);
				oneOf(sessionEncoder).encodeSession(with(any(Session.class)));
				will(returnValue(sessionDict));
				oneOf(clientHelper).mergeMessageMetadata(txn, message.getId(),
						sessionDict);
			}
		}});
	}

	private void testRemovingBlog(Map<MessageId, BdfDictionary> sessions)
			throws Exception {
		Transaction txn = new Transaction(null, false);
		BdfDictionary sessionDict = new BdfDictionary();
		Session session = new Session(contactGroup.getId(), blog.getId());

		context.checking(new Expectations() {{
			oneOf(db).getContacts(txn);
			will(returnValue(contacts));
			oneOf(contactGroupFactory).createContactGroup(CLIENT_ID,
					MAJOR_VERSION, contact);
			will(returnValue(contactGroup));
			oneOf(sessionParser)
					.getSessionQuery(new SessionId(blog.getId().getBytes()));
			will(returnValue(sessionDict));
			oneOf(clientHelper).getMessageMetadataAsDictionary(txn,
					contactGroup.getId(), sessionDict);
			will(returnValue(sessions));
			if (sessions.size() == 1) {
				oneOf(sessionParser)
						.parseSession(contactGroup.getId(), sessionDict);
				will(returnValue(session));
				oneOf(engine).onLeaveAction(txn, session);
				will(returnValue(session));
				oneOf(sessionEncoder).encodeSession(session);
				will(returnValue(sessionDict));
				oneOf(clientHelper).mergeMessageMetadata(txn,
						sessions.keySet().iterator().next(), sessionDict);
			}
		}});
		blogSharingManager.removingBlog(txn, blog);
	}

}
