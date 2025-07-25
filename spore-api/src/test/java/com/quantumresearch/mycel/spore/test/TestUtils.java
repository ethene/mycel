package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.UniqueId;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.PendingContact;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.crypto.AgreementPrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.AgreementPublicKey;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.crypto.SignaturePrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.SignaturePublicKey;
import com.quantumresearch.mycel.spore.api.db.CommitAction;
import com.quantumresearch.mycel.spore.api.db.EventAction;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.identity.Identity;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxAuthToken;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxFolderId;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdate;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateWithMailbox;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxVersion;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.util.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.crypto.Cipher;

import static java.util.Arrays.asList;
import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.MAX_AGREEMENT_PUBLIC_KEY_BYTES;
import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.MAX_SIGNATURE_PUBLIC_KEY_BYTES;
import static com.quantumresearch.mycel.spore.api.identity.Author.FORMAT_VERSION;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static com.quantumresearch.mycel.spore.api.plugin.TransportId.MAX_TRANSPORT_ID_LENGTH;
import static com.quantumresearch.mycel.spore.api.properties.TransportPropertyConstants.MAX_PROPERTY_LENGTH;
import static com.quantumresearch.mycel.spore.api.sync.ClientId.MAX_CLIENT_ID_LENGTH;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_GROUP_DESCRIPTOR_LENGTH;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
import static com.quantumresearch.mycel.spore.util.IoUtils.copyAndClose;
import static com.quantumresearch.mycel.spore.util.StringUtils.getRandomString;

public class TestUtils {

	private static final AtomicInteger nextTestDir =
			new AtomicInteger((int) (Math.random() * 1000 * 1000));
	private static final Random random = new Random();
	private static final long timestamp = System.currentTimeMillis();
	private static final AtomicInteger nextContactId = new AtomicInteger(1);

	public static File getTestDirectory() {
		int name = nextTestDir.getAndIncrement();
		return new File("test.tmp/" + name);
	}

	public static void deleteTestDirectory(File testDir) {
		IoUtils.deleteFileOrDir(testDir);
		testDir.getParentFile().delete(); // Delete if empty
	}

	public static byte[] getRandomBytes(int length) {
		byte[] b = new byte[length];
		random.nextBytes(b);
		return b;
	}

	public static byte[] getRandomId() {
		return getRandomBytes(UniqueId.LENGTH);
	}

	public static ClientId getClientId() {
		return new ClientId(getRandomString(MAX_CLIENT_ID_LENGTH));
	}

	public static TransportId getTransportId() {
		return new TransportId(getRandomString(MAX_TRANSPORT_ID_LENGTH));
	}

	public static TransportProperties getTransportProperties(int number) {
		TransportProperties tp = new TransportProperties();
		for (int i = 0; i < number; i++) {
			tp.put(getRandomString(1 + random.nextInt(MAX_PROPERTY_LENGTH)),
					getRandomString(1 + random.nextInt(MAX_PROPERTY_LENGTH))
			);
		}
		return tp;
	}

	public static Map<TransportId, TransportProperties> getTransportPropertiesMap(
			int number) {
		Map<TransportId, TransportProperties> map = new HashMap<>();
		for (int i = 0; i < number; i++) {
			map.put(getTransportId(), getTransportProperties(number));
		}
		return map;
	}

	public static SecretKey getSecretKey() {
		return new SecretKey(getRandomBytes(SecretKey.LENGTH));
	}

	public static PublicKey getSignaturePublicKey() {
		byte[] key = getRandomBytes(MAX_SIGNATURE_PUBLIC_KEY_BYTES);
		return new SignaturePublicKey(key);
	}

	public static PrivateKey getSignaturePrivateKey() {
		return new SignaturePrivateKey(getRandomBytes(123));
	}

	public static PublicKey getAgreementPublicKey() {
		byte[] key = getRandomBytes(MAX_AGREEMENT_PUBLIC_KEY_BYTES);
		return new AgreementPublicKey(key);
	}

	public static PrivateKey getAgreementPrivateKey() {
		return new AgreementPrivateKey(getRandomBytes(123));
	}

	public static Identity getIdentity() {
		LocalAuthor localAuthor = getLocalAuthor();
		PublicKey handshakePub = getAgreementPublicKey();
		PrivateKey handshakePriv = getAgreementPrivateKey();
		return new Identity(localAuthor, handshakePub, handshakePriv,
				timestamp);
	}

	public static LocalAuthor getLocalAuthor() {
		AuthorId id = new AuthorId(getRandomId());
		int nameLength = 1 + random.nextInt(MAX_AUTHOR_NAME_LENGTH);
		String name = getRandomString(nameLength);
		PublicKey publicKey = getSignaturePublicKey();
		PrivateKey privateKey = getSignaturePrivateKey();
		return new LocalAuthor(id, FORMAT_VERSION, name, publicKey, privateKey);
	}

	public static Author getAuthor() {
		AuthorId id = new AuthorId(getRandomId());
		int nameLength = 1 + random.nextInt(MAX_AUTHOR_NAME_LENGTH);
		String name = getRandomString(nameLength);
		PublicKey publicKey = getSignaturePublicKey();
		return new Author(id, FORMAT_VERSION, name, publicKey);
	}

	public static Group getGroup(ClientId clientId, int majorVersion) {
		int descriptorLength = 1 + random.nextInt(MAX_GROUP_DESCRIPTOR_LENGTH);
		return getGroup(clientId, majorVersion, descriptorLength);
	}

	public static Group getGroup(ClientId clientId, int majorVersion,
			int descriptorLength) {
		GroupId groupId = new GroupId(getRandomId());
		byte[] descriptor = getRandomBytes(descriptorLength);
		return new Group(groupId, clientId, majorVersion, descriptor);
	}

	public static Message getMessage(GroupId groupId) {
		int bodyLength = 1 + random.nextInt(MAX_MESSAGE_BODY_LENGTH);
		return getMessage(groupId, bodyLength, timestamp);
	}

	public static Message getMessage(GroupId groupId, int bodyLength) {
		return getMessage(groupId, bodyLength, timestamp);
	}

	public static Message getMessage(GroupId groupId, int bodyLength,
			long timestamp) {
		MessageId id = new MessageId(getRandomId());
		byte[] body = getRandomBytes(bodyLength);
		return new Message(id, groupId, timestamp, body);
	}

	public static PendingContact getPendingContact() {
		return getPendingContact(1 + random.nextInt(MAX_AUTHOR_NAME_LENGTH));
	}

	public static PendingContact getPendingContact(int nameLength) {
		PendingContactId id = new PendingContactId(getRandomId());
		PublicKey publicKey = getAgreementPublicKey();
		String alias = getRandomString(nameLength);
		return new PendingContact(id, publicKey, alias, timestamp);
	}

	public static ContactId getContactId() {
		return new ContactId(nextContactId.getAndIncrement());
	}

	public static Contact getContact() {
		return getContact(getAuthor(), new AuthorId(getRandomId()),
				random.nextBoolean());
	}

	public static Contact getContact(Author remote, AuthorId local,
			boolean verified) {
		return getContact(getContactId(), remote, local, verified);
	}

	public static Contact getContact(ContactId c, Author remote, AuthorId local,
			boolean verified) {
		return new Contact(c, remote, local,
				getRandomString(MAX_AUTHOR_NAME_LENGTH),
				getAgreementPublicKey(), verified);
	}

	public static MailboxProperties getMailboxProperties(boolean owner,
			List<MailboxVersion> serverSupports) {
		String onion = getRandomString(56);
		MailboxAuthToken authToken = new MailboxAuthToken(getRandomId());
		if (owner) {
			return new MailboxProperties(onion, authToken, serverSupports);
		}
		MailboxFolderId inboxId = new MailboxFolderId(getRandomId());
		MailboxFolderId outboxId = new MailboxFolderId(getRandomId());
		return new MailboxProperties(onion, authToken, serverSupports,
				inboxId, outboxId);
	}

	public static void writeBytes(File file, byte[] bytes)
			throws IOException {
		FileOutputStream outputStream = new FileOutputStream(file);
		//noinspection TryFinallyCanBeTryWithResources
		try {
			outputStream.write(bytes);
		} finally {
			outputStream.close();
		}
	}

	public static byte[] readBytes(File file) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		FileInputStream inputStream = new FileInputStream(file);
		copyAndClose(inputStream, outputStream);
		return outputStream.toByteArray();
	}

	public static double getMedian(Collection<? extends Number> samples) {
		int size = samples.size();
		if (size == 0) throw new IllegalArgumentException();
		List<Double> sorted = new ArrayList<>(size);
		for (Number n : samples) sorted.add(n.doubleValue());
		Collections.sort(sorted);
		if (size % 2 == 1) return sorted.get(size / 2);
		double low = sorted.get(size / 2 - 1), high = sorted.get(size / 2);
		return (low + high) / 2;
	}

	public static double getMean(Collection<? extends Number> samples) {
		if (samples.isEmpty()) throw new IllegalArgumentException();
		double sum = 0;
		for (Number n : samples) sum += n.doubleValue();
		return sum / samples.size();
	}

	public static double getVariance(Collection<? extends Number> samples) {
		if (samples.size() < 2) throw new IllegalArgumentException();
		double mean = getMean(samples);
		double sumSquareDiff = 0;
		for (Number n : samples) {
			double diff = n.doubleValue() - mean;
			sumSquareDiff += diff * diff;
		}
		return sumSquareDiff / (samples.size() - 1);
	}

	public static double getStandardDeviation(
			Collection<? extends Number> samples) {
		return Math.sqrt(getVariance(samples));
	}

	public static boolean isOptionalTestEnabled(Class<?> testClass) {
		String optionalTests = System.getenv("OPTIONAL_TESTS");
		return optionalTests != null &&
				asList(optionalTests.split(",")).contains(testClass.getName());
	}

	public static boolean mailboxUpdateEqual(@Nullable MailboxUpdate a,
			@Nullable MailboxUpdate b) {
		if (a == null || b == null) {
			return a == b;
		}
		if (!a.hasMailbox() && !b.hasMailbox()) {
			return a.getClientSupports().equals(b.getClientSupports());
		} else if (a.hasMailbox() && b.hasMailbox()) {
			MailboxUpdateWithMailbox am = (MailboxUpdateWithMailbox) a;
			MailboxUpdateWithMailbox bm = (MailboxUpdateWithMailbox) b;
			return am.getClientSupports().equals(bm.getClientSupports()) &&
					mailboxPropertiesEqual(am.getMailboxProperties(),
							bm.getMailboxProperties());
		}
		return false;
	}

	public static boolean mailboxPropertiesEqual(@Nullable MailboxProperties a,
			@Nullable MailboxProperties b) {
		if (a == null || b == null) {
			return a == b;
		}
		return a.getOnion().equals(b.getOnion()) &&
				a.getAuthToken().equals(b.getAuthToken()) &&
				a.isOwner() == b.isOwner() &&
				a.getServerSupports().equals(b.getServerSupports());
	}

	public static boolean hasEvent(Transaction txn,
			Class<? extends Event> eventClass) {
		for (CommitAction action : txn.getActions()) {
			if (action instanceof EventAction) {
				Event event = ((EventAction) action).getEvent();
				if (eventClass.isInstance(event)) return true;
			}
		}
		return false;
	}

	public static <E extends Event> E getEvent(Transaction txn,
			Class<E> eventClass) {
		for (CommitAction action : txn.getActions()) {
			if (action instanceof EventAction) {
				Event event = ((EventAction) action).getEvent();
				if (eventClass.isInstance(event)) return eventClass.cast(event);
			}
		}
		throw new AssertionError();
	}

	public static boolean isCryptoStrengthUnlimited() {
		try {
			return Cipher.getMaxAllowedKeyLength("AES/CBC/PKCS5Padding")
					== Integer.MAX_VALUE;
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError();
		}
	}
}
