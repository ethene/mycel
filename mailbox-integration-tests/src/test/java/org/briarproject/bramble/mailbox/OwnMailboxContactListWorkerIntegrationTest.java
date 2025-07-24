package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorFactory;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.quantumresearch.mycel.spore.mailbox.MailboxIntegrationTestUtils.retryUntilSuccessOrTimeout;
import static com.quantumresearch.mycel.spore.test.TestUtils.getSecretKey;
import static org.junit.Assert.assertEquals;

public class OwnMailboxContactListWorkerIntegrationTest
		extends AbstractMailboxIntegrationTest {

	private MailboxProperties ownerProperties;

	private LocalAuthor localAuthor1;

	private final SecretKey rootKey = getSecretKey();
	private final long timestamp = System.currentTimeMillis();

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		localAuthor1 = c1.getIdentityManager().getLocalAuthor();
		ownerProperties = pair(c1, mailbox);
	}

	@Test
	public void testUploadContacts() throws Exception {
		// Check the initial conditions
		ContactManager contactManager = c1.getContactManager();
		assertEquals(0, contactManager.getPendingContacts().size());
		assertEquals(0, contactManager.getContacts().size());

		int numContactsToAdd = 5;
		List<ContactId> expectedContacts = createContacts(c1, numContactsToAdd);

		// Check for number of contacts on mailbox via API every 100ms
		retryUntilSuccessOrTimeout(1000, 100, () -> {
			Collection<ContactId> contacts = api.getContacts(ownerProperties);
			if (contacts.size() == numContactsToAdd) {
				assertEquals(expectedContacts, contacts);
				return true;
			}
			return false;
		});
	}

	private List<ContactId> createContacts(
			MailboxIntegrationTestComponent component, int numContacts)
			throws DbException {
		List<ContactId> contactIds = new ArrayList<>();
		ContactManager contactManager = component.getContactManager();
		AuthorFactory authorFactory = component.getAuthorFactory();
		for (int i = 0; i < numContacts; i++) {
			Author remote = authorFactory.createLocalAuthor("Bob " + i);
			ContactId c = contactManager.addContact(remote,
					localAuthor1.getId(), rootKey, timestamp, true, true, true);
			contactIds.add(c);
		}
		return contactIds;
	}
}
