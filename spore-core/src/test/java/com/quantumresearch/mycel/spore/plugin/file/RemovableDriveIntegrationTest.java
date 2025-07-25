package com.quantumresearch.mycel.spore.plugin.file;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.event.EventListener;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.Identity;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveTask;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.sync.event.MessageStateChangedEvent;
import com.quantumresearch.mycel.spore.test.BrambleTestCase;
import com.quantumresearch.mycel.spore.test.TestDatabaseConfigModule;
import org.briarproject.nullsafety.NotNullByDefault;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static com.quantumresearch.mycel.spore.api.plugin.file.FileConstants.PROP_PATH;
import static com.quantumresearch.mycel.spore.api.sync.validation.MessageState.DELIVERED;
import static com.quantumresearch.mycel.spore.test.TestUtils.deleteTestDirectory;
import static com.quantumresearch.mycel.spore.test.TestUtils.getSecretKey;
import static com.quantumresearch.mycel.spore.test.TestUtils.getTestDirectory;
import static org.junit.Assert.assertTrue;

public class RemovableDriveIntegrationTest extends BrambleTestCase {

	private static final int TIMEOUT_MS = 5_000;

	private final File testDir = getTestDirectory();
	private final File aliceDir = new File(testDir, "alice");
	private final File bobDir = new File(testDir, "bob");

	private final SecretKey rootKey = getSecretKey();
	private final long timestamp = System.currentTimeMillis();

	private RemovableDriveIntegrationTestComponent alice, bob;

	@Before
	public void setUp() {
		assertTrue(testDir.mkdirs());
		alice = DaggerRemovableDriveIntegrationTestComponent.builder()
				.testDatabaseConfigModule(
						new TestDatabaseConfigModule(aliceDir)).build();
		RemovableDriveIntegrationTestComponent.Helper
				.injectEagerSingletons(alice);
		bob = DaggerRemovableDriveIntegrationTestComponent.builder()
				.testDatabaseConfigModule(
						new TestDatabaseConfigModule(bobDir)).build();
		RemovableDriveIntegrationTestComponent.Helper
				.injectEagerSingletons(bob);
	}

	@Test
	public void testWriteAndRead() throws Exception {
		// Create the identities
		Identity aliceIdentity =
				alice.getIdentityManager().createIdentity("Alice");
		Identity bobIdentity = bob.getIdentityManager().createIdentity("Bob");
		// Set up the devices and get the contact IDs
		ContactId bobId = setUp(alice, aliceIdentity,
				bobIdentity.getLocalAuthor(), true);
		ContactId aliceId = setUp(bob, bobIdentity,
				aliceIdentity.getLocalAuthor(), false);
		// Sync Alice's client versions and transport properties
		read(bob, write(alice, bobId), 2);
		// Sync Bob's client versions and transport properties
		read(alice, write(bob, aliceId), 2);
	}

	private ContactId setUp(RemovableDriveIntegrationTestComponent device,
			Identity local, Author remote, boolean alice) throws Exception {
		// Add an identity for the user
		IdentityManager identityManager = device.getIdentityManager();
		identityManager.registerIdentity(local);
		// Start the lifecycle manager
		LifecycleManager lifecycleManager = device.getLifecycleManager();
		lifecycleManager.startServices(getSecretKey());
		lifecycleManager.waitForStartup();
		// Add the other user as a contact
		ContactManager contactManager = device.getContactManager();
		return contactManager.addContact(remote, local.getId(), rootKey,
				timestamp, alice, true, true);
	}

	@SuppressWarnings("SameParameterValue")
	private void read(RemovableDriveIntegrationTestComponent device,
			File file, int deliveries) throws Exception {
		// Listen for message deliveries
		MessageDeliveryListener listener =
				new MessageDeliveryListener(deliveries);
		device.getEventBus().addListener(listener);
		// Read the incoming stream
		TransportProperties p = new TransportProperties();
		p.put(PROP_PATH, file.getAbsolutePath());
		RemovableDriveTask reader =
				device.getRemovableDriveManager().startReaderTask(p);
		CountDownLatch disposedLatch = new CountDownLatch(1);
		reader.addObserver(state -> {
			if (state.isFinished()) disposedLatch.countDown();
		});
		// Wait for the messages to be delivered
		assertTrue(listener.delivered.await(TIMEOUT_MS, MILLISECONDS));
		// Clean up the listener
		device.getEventBus().removeListener(listener);
		// Wait for the reader to be disposed
		disposedLatch.await(TIMEOUT_MS, MILLISECONDS);
	}

	private File write(RemovableDriveIntegrationTestComponent device,
			ContactId contactId) throws Exception {
		// Write the outgoing stream to a file
		File file = File.createTempFile("sync", ".tmp", testDir);
		TransportProperties p = new TransportProperties();
		p.put(PROP_PATH, file.getAbsolutePath());
		RemovableDriveTask writer = device.getRemovableDriveManager()
				.startWriterTask(contactId, p);
		CountDownLatch disposedLatch = new CountDownLatch(1);
		writer.addObserver(state -> {
			if (state.isFinished()) disposedLatch.countDown();
		});
		// Wait for the writer to be disposed
		disposedLatch.await(TIMEOUT_MS, MILLISECONDS);
		// Return the file containing the stream
		return file;
	}

	private void tearDown(RemovableDriveIntegrationTestComponent device)
			throws Exception {
		// Stop the lifecycle manager
		LifecycleManager lifecycleManager = device.getLifecycleManager();
		lifecycleManager.stopServices();
		lifecycleManager.waitForShutdown();
	}

	@After
	public void tearDown() throws Exception {
		// Tear down the devices
		tearDown(alice);
		tearDown(bob);
		deleteTestDirectory(testDir);
	}

	@NotNullByDefault
	private static class MessageDeliveryListener implements EventListener {

		private final CountDownLatch delivered;

		private MessageDeliveryListener(int deliveries) {
			delivered = new CountDownLatch(deliveries);
		}

		@Override
		public void eventOccurred(Event e) {
			if (e instanceof MessageStateChangedEvent) {
				MessageStateChangedEvent m = (MessageStateChangedEvent) e;
				if (m.getState().equals(DELIVERED)) delivered.countDown();
			}
		}
	}
}
