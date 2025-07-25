package com.quantumresearch.mycel.spore.client;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.UniqueId;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.KeyParser;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfEntry;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.BdfReader;
import com.quantumresearch.mycel.spore.api.data.BdfReaderFactory;
import com.quantumresearch.mycel.spore.api.data.BdfWriter;
import com.quantumresearch.mycel.spore.api.data.BdfWriterFactory;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorFactory;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdate;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateWithMailbox;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxVersion;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import com.quantumresearch.mycel.spore.test.DbExpectations;
import org.jmock.Expectations;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.util.Collections.singletonList;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_PUBLIC_KEY_LENGTH;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_SIGNATURE_LENGTH;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_KEY_AUTHTOKEN;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_KEY_INBOXID;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_KEY_ONION;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_KEY_OUTBOXID;
import static com.quantumresearch.mycel.spore.test.TestUtils.getAuthor;
import static com.quantumresearch.mycel.spore.test.TestUtils.getMailboxProperties;
import static com.quantumresearch.mycel.spore.test.TestUtils.getMessage;
import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomBytes;
import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.spore.test.TestUtils.getSignaturePrivateKey;
import static com.quantumresearch.mycel.spore.test.TestUtils.getSignaturePublicKey;
import static com.quantumresearch.mycel.spore.test.TestUtils.mailboxUpdateEqual;
import static com.quantumresearch.mycel.spore.util.StringUtils.getRandomString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ClientHelperImplTest extends BrambleMockTestCase {

	private final DatabaseComponent db = context.mock(DatabaseComponent.class);
	private final MessageFactory messageFactory =
			context.mock(MessageFactory.class);
	private final BdfReaderFactory bdfReaderFactory =
			context.mock(BdfReaderFactory.class);
	private final BdfWriterFactory bdfWriterFactory =
			context.mock(BdfWriterFactory.class);
	private final MetadataParser metadataParser =
			context.mock(MetadataParser.class);
	private final MetadataEncoder metadataEncoder =
			context.mock(MetadataEncoder.class);
	private final CryptoComponent cryptoComponent =
			context.mock(CryptoComponent.class);
	private final AuthorFactory authorFactory =
			context.mock(AuthorFactory.class);
	private final KeyParser keyParser = context.mock(KeyParser.class);

	private final GroupId groupId = new GroupId(getRandomId());
	private final BdfDictionary dictionary = new BdfDictionary();
	private final Message message = getMessage(groupId);
	private final MessageId messageId = message.getId();
	private final long timestamp = message.getTimestamp();
	private final Metadata metadata = new Metadata();
	private final BdfList list = BdfList.of("Sign this!", getRandomBytes(42));
	private final String label = getRandomString(5);
	private final Author author = getAuthor();

	private final ClientHelper clientHelper = new ClientHelperImpl(db,
			messageFactory, bdfReaderFactory, bdfWriterFactory, metadataParser,
			metadataEncoder, cryptoComponent, authorFactory);

	private final MailboxUpdateWithMailbox validMailboxUpdateWithMailbox;
	private final BdfList emptyClientSupports;
	private final BdfList someClientSupports;
	private final BdfList emptyServerSupports;
	private final BdfList someServerSupports;

	public ClientHelperImplTest() {
		emptyClientSupports = new BdfList();
		someClientSupports = BdfList.of(BdfList.of(1, 0));
		emptyServerSupports = new BdfList();
		someServerSupports = BdfList.of(BdfList.of(1, 0));
		validMailboxUpdateWithMailbox = new MailboxUpdateWithMailbox(
				singletonList(new MailboxVersion(1, 0)),
				getMailboxProperties(false,
						singletonList(new MailboxVersion(1, 0))));
	}

	private BdfDictionary getValidMailboxUpdateWithMailboxDict() {
		BdfDictionary dict = new BdfDictionary();
		MailboxProperties properties =
				validMailboxUpdateWithMailbox.getMailboxProperties();
		dict.put(PROP_KEY_ONION, properties.getOnion());
		dict.put(PROP_KEY_AUTHTOKEN, properties.getAuthToken());
		dict.put(PROP_KEY_INBOXID, properties.getInboxId());
		dict.put(PROP_KEY_OUTBOXID, properties.getOutboxId());
		return dict;
	}

	@Test
	public void testAddLocalMessage() throws Exception {
		boolean shared = new Random().nextBoolean();
		Transaction txn = new Transaction(null, false);

		context.checking(new DbExpectations() {{
			oneOf(db).transaction(with(false), withDbRunnable(txn));
			oneOf(metadataEncoder).encode(dictionary);
			will(returnValue(metadata));
			oneOf(db).addLocalMessage(txn, message, metadata, shared, false);
		}});

		clientHelper.addLocalMessage(message, dictionary, shared);
	}

	@Test
	public void testCreateMessage() throws Exception {
		byte[] bytes = expectToByteArray(list);

		context.checking(new Expectations() {{
			oneOf(messageFactory).createMessage(groupId, timestamp, bytes);
		}});

		clientHelper.createMessage(groupId, timestamp, list);
	}

	@Test
	public void testGetMessageAsList() throws Exception {
		Transaction txn = new Transaction(null, true);

		expectToList(true);
		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(true), withDbCallable(txn));
			oneOf(db).getMessage(txn, messageId);
			will(returnValue(message));
		}});

		clientHelper.getMessageAsList(messageId);
	}

	@Test
	public void testGetGroupMetadataAsDictionary() throws Exception {
		Transaction txn = new Transaction(null, true);

		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(true), withDbCallable(txn));
			oneOf(db).getGroupMetadata(txn, groupId);
			will(returnValue(metadata));
			oneOf(metadataParser).parse(metadata);
			will(returnValue(dictionary));
		}});

		assertEquals(dictionary,
				clientHelper.getGroupMetadataAsDictionary(groupId));
	}

	@Test
	public void testGetMessageMetadataAsDictionary() throws Exception {
		Transaction txn = new Transaction(null, true);

		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(true), withDbCallable(txn));
			oneOf(db).getMessageMetadata(txn, messageId);
			will(returnValue(metadata));
			oneOf(metadataParser).parse(metadata);
			will(returnValue(dictionary));
		}});

		assertEquals(dictionary,
				clientHelper.getMessageMetadataAsDictionary(messageId));
	}

	@Test
	public void testGetMessageMetadataAsDictionaryMap() throws Exception {
		Map<MessageId, BdfDictionary> map = new HashMap<>();
		map.put(messageId, dictionary);
		Transaction txn = new Transaction(null, true);

		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(true), withDbCallable(txn));
			oneOf(db).getMessageMetadata(txn, groupId);
			will(returnValue(Collections.singletonMap(messageId, metadata)));
			oneOf(metadataParser).parse(metadata);
			will(returnValue(dictionary));
		}});

		assertEquals(map, clientHelper.getMessageMetadataAsDictionary(groupId));
	}

	@Test
	public void testGetMessageMetadataAsDictionaryQuery() throws Exception {
		Map<MessageId, BdfDictionary> map = new HashMap<>();
		map.put(messageId, dictionary);
		BdfDictionary query =
				BdfDictionary.of(new BdfEntry("query", "me"));
		Metadata queryMetadata = new Metadata();
		queryMetadata.put("query", getRandomBytes(42));
		Transaction txn = new Transaction(null, true);

		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(true), withDbCallable(txn));
			oneOf(metadataEncoder).encode(query);
			will(returnValue(queryMetadata));
			oneOf(db).getMessageMetadata(txn, groupId, queryMetadata);
			will(returnValue(Collections.singletonMap(messageId, metadata)));
			oneOf(metadataParser).parse(metadata);
			will(returnValue(dictionary));
		}});

		assertEquals(map,
				clientHelper.getMessageMetadataAsDictionary(groupId, query));
	}

	@Test
	public void testMergeGroupMetadata() throws Exception {
		Transaction txn = new Transaction(null, false);

		context.checking(new DbExpectations() {{
			oneOf(db).transaction(with(false), withDbRunnable(txn));
			oneOf(metadataEncoder).encode(dictionary);
			will(returnValue(metadata));
			oneOf(db).mergeGroupMetadata(txn, groupId, metadata);
		}});

		clientHelper.mergeGroupMetadata(groupId, dictionary);
	}

	@Test
	public void testMergeMessageMetadata() throws Exception {
		Transaction txn = new Transaction(null, false);

		context.checking(new DbExpectations() {{
			oneOf(db).transaction(with(false), withDbRunnable(txn));
			oneOf(metadataEncoder).encode(dictionary);
			will(returnValue(metadata));
			oneOf(db).mergeMessageMetadata(txn, messageId, metadata);
		}});

		clientHelper.mergeMessageMetadata(messageId, dictionary);
	}

	@Test
	public void testToByteArray() throws Exception {
		byte[] bytes = expectToByteArray(list);

		assertArrayEquals(bytes, clientHelper.toByteArray(list));
	}

	@Test
	public void testToList() throws Exception {
		expectToList(true);

		assertEquals(list, clientHelper.toList(getRandomBytes(123)));
	}

	@Test
	public void testToListWithNoEof() throws Exception {
		expectToList(false); // no EOF after list

		try {
			clientHelper.toList(getRandomBytes(123));
			fail();
		} catch (FormatException e) {
			// expected
		}
	}

	@Test
	public void testSign() throws Exception {
		PrivateKey privateKey = getSignaturePrivateKey();
		byte[] signature = getRandomBytes(42);

		byte[] bytes = expectToByteArray(list);
		context.checking(new Expectations() {{
			oneOf(cryptoComponent).sign(label, bytes, privateKey);
			will(returnValue(signature));
		}});

		assertArrayEquals(signature,
				clientHelper.sign(label, list, privateKey));
	}

	@Test
	public void testVerifySignature() throws Exception {
		byte[] signature = getRandomBytes(MAX_SIGNATURE_LENGTH);
		byte[] signed = expectToByteArray(list);
		PublicKey publicKey = getSignaturePublicKey();

		context.checking(new Expectations() {{
			oneOf(cryptoComponent).verifySignature(signature, label, signed,
					publicKey);
			will(returnValue(true));
		}});

		clientHelper.verifySignature(signature, label, list, publicKey);
	}

	@Test
	public void testVerifyWrongSignature() throws Exception {
		byte[] signature = getRandomBytes(MAX_SIGNATURE_LENGTH);
		byte[] signed = expectToByteArray(list);
		PublicKey publicKey = getSignaturePublicKey();

		context.checking(new Expectations() {{
			oneOf(cryptoComponent).verifySignature(signature, label, signed,
					publicKey);
			will(returnValue(false));
		}});

		try {
			clientHelper.verifySignature(signature, label, list, publicKey);
			fail();
		} catch (GeneralSecurityException e) {
			// expected
		}
	}

	@Test
	public void testParsesAndEncodesAuthor() throws Exception {
		context.checking(new Expectations() {{
			oneOf(cryptoComponent).getSignatureKeyParser();
			will(returnValue(keyParser));
			oneOf(keyParser).parsePublicKey(author.getPublicKey().getEncoded());
			will(returnValue(author.getPublicKey()));
			oneOf(authorFactory).createAuthor(author.getFormatVersion(),
					author.getName(), author.getPublicKey());
			will(returnValue(author));
		}});

		BdfList authorList = clientHelper.toList(author);
		assertEquals(author, clientHelper.parseAndValidateAuthor(authorList));
	}

	@Test
	public void testAcceptsValidAuthor() throws Exception {
		BdfList authorList = BdfList.of(
				author.getFormatVersion(),
				author.getName(),
				author.getPublicKey().getEncoded()
		);

		context.checking(new Expectations() {{
			oneOf(cryptoComponent).getSignatureKeyParser();
			will(returnValue(keyParser));
			oneOf(keyParser).parsePublicKey(author.getPublicKey().getEncoded());
			will(returnValue(author.getPublicKey()));
			oneOf(authorFactory).createAuthor(author.getFormatVersion(),
					author.getName(), author.getPublicKey());
			will(returnValue(author));
		}});

		assertEquals(author, clientHelper.parseAndValidateAuthor(authorList));
	}

	@Test(expected = FormatException.class)
	public void testRejectsTooShortAuthor() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				author.getName()
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsTooLongAuthor() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				author.getName(),
				author.getPublicKey().getEncoded(),
				"foo"
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithNullFormatVersion() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				null,
				author.getName(),
				author.getPublicKey().getEncoded()
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithNonIntegerFormatVersion()
			throws Exception {
		BdfList invalidAuthor = BdfList.of(
				"foo",
				author.getName(),
				author.getPublicKey().getEncoded()
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithUnknownFormatVersion() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion() + 1,
				author.getName(),
				author.getPublicKey().getEncoded()
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithTooShortName() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				"",
				author.getPublicKey().getEncoded()
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithTooLongName() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				getRandomString(MAX_AUTHOR_NAME_LENGTH + 1),
				author.getPublicKey().getEncoded()
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithNullName() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				null,
				author.getPublicKey().getEncoded()
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithNonStringName() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				getRandomBytes(5),
				author.getPublicKey()
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithTooShortPublicKey() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				author.getName(),
				new byte[0]
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithTooLongPublicKey() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				author.getName(),
				getRandomBytes(MAX_PUBLIC_KEY_LENGTH + 1)
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithNullPublicKey() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				author.getName(),
				null
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithNonRawPublicKey() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				author.getName(),
				"foo"
		);
		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	@Test(expected = FormatException.class)
	public void testRejectsAuthorWithInvalidPublicKey() throws Exception {
		BdfList invalidAuthor = BdfList.of(
				author.getFormatVersion(),
				author.getName(),
				author.getPublicKey().getEncoded()
		);

		context.checking(new Expectations() {{
			oneOf(cryptoComponent).getSignatureKeyParser();
			will(returnValue(keyParser));
			oneOf(keyParser).parsePublicKey(author.getPublicKey().getEncoded());
			will(throwException(new GeneralSecurityException()));
		}});

		clientHelper.parseAndValidateAuthor(invalidAuthor);
	}

	private byte[] expectToByteArray(BdfList list) throws Exception {
		BdfWriter bdfWriter = context.mock(BdfWriter.class);

		context.checking(new Expectations() {{
			oneOf(bdfWriterFactory)
					.createWriter(with(any(ByteArrayOutputStream.class)));
			will(returnValue(bdfWriter));
			oneOf(bdfWriter).writeList(list);
		}});
		return new byte[0];
	}

	private void expectToList(boolean eof) throws Exception {
		BdfReader bdfReader = context.mock(BdfReader.class);

		context.checking(new Expectations() {{
			oneOf(bdfReaderFactory)
					.createReader(with(any(InputStream.class)), with(true));
			will(returnValue(bdfReader));
			oneOf(bdfReader).readList();
			will(returnValue(list));
			oneOf(bdfReader).eof();
			will(returnValue(eof));
		}});
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateWithEmptyClientSupports()
			throws Exception {
		BdfDictionary emptyPropsDict = new BdfDictionary();
		clientHelper.parseAndValidateMailboxUpdate(emptyClientSupports,
				emptyServerSupports, emptyPropsDict
		);
	}

	@Test
	public void testParseMailboxUpdateNoMailbox() throws Exception {
		BdfDictionary emptyPropsDict = new BdfDictionary();
		MailboxUpdate parsedUpdate = clientHelper.parseAndValidateMailboxUpdate(
				someClientSupports, emptyServerSupports, emptyPropsDict);
		assertFalse(parsedUpdate.hasMailbox());
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateNoMailboxWithSomeServerSupports()
			throws Exception {
		BdfDictionary emptyPropsDict = new BdfDictionary();
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				someServerSupports, emptyPropsDict);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateShortSupports() throws Exception {
		clientHelper.parseAndValidateMailboxUpdate(BdfList.of(BdfList.of(1)),
				emptyServerSupports, new BdfDictionary());
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateLongSupports() throws Exception {
		clientHelper.parseAndValidateMailboxUpdate(
				BdfList.of(BdfList.of(1, 0, 0)), emptyServerSupports,
				new BdfDictionary());
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateNonIntSupports() throws Exception {
		clientHelper.parseAndValidateMailboxUpdate(
				BdfList.of(BdfList.of(1, "0")), emptyServerSupports,
				new BdfDictionary()
		);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateNonListSupports() throws Exception {
		clientHelper.parseAndValidateMailboxUpdate(
				BdfList.of("non-list"), emptyServerSupports,
				new BdfDictionary());
	}

	@Test
	public void testParseValidMailboxUpdateWithMailbox() throws Exception {
		MailboxUpdate parsedUpdate = clientHelper.parseAndValidateMailboxUpdate(
				someClientSupports, someServerSupports,
				getValidMailboxUpdateWithMailboxDict());
		assertTrue(
				mailboxUpdateEqual(validMailboxUpdateWithMailbox,
						parsedUpdate));
	}

	@Test(expected = FormatException.class)
	public void rejectsMailboxUpdateWithEmptyServerSupports() throws Exception {
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				emptyServerSupports, getValidMailboxUpdateWithMailboxDict());
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateOnionNotDecodable() throws Exception {
		BdfDictionary propsDict = getValidMailboxUpdateWithMailboxDict();
		String badOnion = "!" + propsDict.getString(PROP_KEY_ONION)
				.substring(1);
		propsDict.put(PROP_KEY_ONION, badOnion);
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				emptyServerSupports, propsDict);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateOnionWrongLength() throws Exception {
		BdfDictionary propsDict = getValidMailboxUpdateWithMailboxDict();
		String tooLongOnion = propsDict.getString(PROP_KEY_ONION) + "!";
		propsDict.put(PROP_KEY_ONION, tooLongOnion);
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				emptyServerSupports, propsDict
		);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateInboxIdWrongLength() throws Exception {
		BdfDictionary propsDict = getValidMailboxUpdateWithMailboxDict();
		propsDict.put(PROP_KEY_INBOXID, getRandomBytes(UniqueId.LENGTH + 1));
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				someServerSupports, propsDict);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateOutboxIdWrongLength() throws Exception {
		BdfDictionary propsDict = getValidMailboxUpdateWithMailboxDict();
		propsDict.put(PROP_KEY_OUTBOXID, getRandomBytes(UniqueId.LENGTH + 1));
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				someServerSupports, propsDict);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateAuthTokenWrongLength()
			throws Exception {
		BdfDictionary propsDict = getValidMailboxUpdateWithMailboxDict();
		propsDict.put(PROP_KEY_AUTHTOKEN, getRandomBytes(UniqueId.LENGTH + 1));
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				someServerSupports, propsDict);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateMissingOnion() throws Exception {
		BdfDictionary propsDict = getValidMailboxUpdateWithMailboxDict();
		propsDict.remove(PROP_KEY_ONION);
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				someServerSupports, propsDict
		);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateMissingAuthToken() throws Exception {
		BdfDictionary propsDict = getValidMailboxUpdateWithMailboxDict();
		propsDict.remove(PROP_KEY_AUTHTOKEN);
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				someServerSupports, propsDict);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateMissingInboxId() throws Exception {
		BdfDictionary propsDict = getValidMailboxUpdateWithMailboxDict();
		propsDict.remove(PROP_KEY_INBOXID);
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				someServerSupports, propsDict);
	}

	@Test(expected = FormatException.class)
	public void testRejectsMailboxUpdateMissingOutboxId() throws Exception {
		BdfDictionary propsDict = getValidMailboxUpdateWithMailboxDict();
		propsDict.remove(PROP_KEY_OUTBOXID);
		clientHelper.parseAndValidateMailboxUpdate(someClientSupports,
				someServerSupports, propsDict);
	}

}
