package com.quantumresearch.mycel.infrastructure.mailbox;

import com.quantumresearch.mycel.infrastructure.api.Pair;
import com.quantumresearch.mycel.infrastructure.api.contact.Contact;
import com.quantumresearch.mycel.infrastructure.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxAuthToken;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingState;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingState.ConnectionError;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingState.InvalidQrCode;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingState.MailboxAlreadyPaired;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingState.Paired;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingState.Pairing;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingState.QrCodeReceived;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingState.UnexpectedError;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingTask;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxSettingsManager;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxUpdate;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxUpdateManager;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxVersion;
import com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier;
import com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier.QrCodeType;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.mailbox.MailboxApi.ApiException;
import com.quantumresearch.mycel.infrastructure.mailbox.MailboxApi.MailboxAlreadyPairedException;
import com.quantumresearch.mycel.infrastructure.test.BrambleMockTestCase;
import com.quantumresearch.mycel.infrastructure.test.DbExpectations;
import com.quantumresearch.mycel.infrastructure.test.ImmediateExecutor;
import com.quantumresearch.mycel.infrastructure.test.PredicateMatcher;
import org.jmock.Expectations;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singletonList;
import static com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxConstants.QR_FORMAT_VERSION;
import static com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier.QrCodeType.BQP;
import static com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier.QrCodeType.MAILBOX;
import static com.quantumresearch.mycel.infrastructure.mailbox.MailboxTestUtils.getQrCodePayload;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getContact;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getRandomBytes;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.infrastructure.util.StringUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MailboxPairingTaskImplTest extends BrambleMockTestCase {

	private final Executor executor = new ImmediateExecutor();
	private final DatabaseComponent db =
			context.mock(DatabaseComponent.class);
	private final CryptoComponent crypto = context.mock(CryptoComponent.class);
	private final Clock clock = context.mock(Clock.class);
	private final MailboxApi api = context.mock(MailboxApi.class);
	private final MailboxSettingsManager mailboxSettingsManager =
			context.mock(MailboxSettingsManager.class);
	private final MailboxUpdateManager mailboxUpdateManager =
			context.mock(MailboxUpdateManager.class);
	private final QrCodeClassifier qrCodeClassifier =
			context.mock(QrCodeClassifier.class);

	private final String onion = getRandomString(56);
	private final byte[] onionBytes = getRandomBytes(32);
	private final MailboxAuthToken setupToken =
			new MailboxAuthToken(getRandomId());
	private final MailboxAuthToken ownerToken =
			new MailboxAuthToken(getRandomId());
	private final String validPayload =
			getQrCodePayload(onionBytes, setupToken.getBytes());
	private final long time = System.currentTimeMillis();
	private final MailboxProperties setupProperties = new MailboxProperties(
			onion, setupToken, new ArrayList<>());
	private final MailboxProperties ownerProperties = new MailboxProperties(
			onion, ownerToken, new ArrayList<>());

	@Test
	public void testInitialQrCodeReceivedState() {
		MailboxPairingTask task = createPairingTask(getRandomString(42));
		task.addObserver(state ->
				assertTrue(state instanceof QrCodeReceived));
	}

	@Test
	public void testInvalidQrCodeType() {
		String payload = getRandomString(65);
		MailboxPairingTask task = createPairingTask(payload);

		expectClassifyQrCode(payload, BQP, QR_FORMAT_VERSION);

		task.run();
		task.addObserver(state ->
				assertTrue(state instanceof InvalidQrCode));
	}

	@Test
	public void testInvalidQrCodeVersion() {
		String payload = getRandomString(65);
		MailboxPairingTask task = createPairingTask(payload);

		expectClassifyQrCode(payload, MAILBOX, QR_FORMAT_VERSION + 1);

		task.run();
		task.addObserver(state ->
				assertTrue(state instanceof InvalidQrCode));
	}

	@Test
	public void testInvalidQrCodeLength() {
		String payload = getRandomString(42);
		MailboxPairingTask task = createPairingTask(payload);

		expectClassifyQrCode(payload, MAILBOX, QR_FORMAT_VERSION);

		task.run();
		task.addObserver(state ->
				assertTrue(state instanceof InvalidQrCode));
	}

	@Test
	public void testSuccessfulPairing() throws Exception {
		expectClassifyQrCode(validPayload, MAILBOX, QR_FORMAT_VERSION);
		context.checking(new Expectations() {{
			oneOf(crypto).encodeOnion(onionBytes);
			will(returnValue(onion));
			oneOf(api).setup(with(matches(setupProperties)));
			will(returnValue(ownerProperties));
			oneOf(clock).currentTimeMillis();
			will(returnValue(time));
		}});
		Contact contact1 = getContact();
		Transaction txn = new Transaction(null, false);
		MailboxUpdate updateNoMailbox = new MailboxUpdate(
				singletonList(new MailboxVersion(47, 11)));
		context.checking(new DbExpectations() {{
			oneOf(db).transaction(with(false), withDbRunnable(txn));
			oneOf(mailboxSettingsManager).setOwnMailboxProperties(
					with(txn), with(matches(ownerProperties)));
			oneOf(mailboxSettingsManager).recordSuccessfulConnection(txn, time,
					ownerProperties.getServerSupports());
			oneOf(db).getContacts(txn);
			will(returnValue(singletonList(contact1)));
			oneOf(mailboxUpdateManager).getRemoteUpdate(txn,
					contact1.getId());
			will(returnValue(updateNoMailbox));
			oneOf(db).resetUnackedMessagesToSend(txn, contact1.getId());
		}});

		AtomicInteger i = new AtomicInteger(0);
		MailboxPairingTask task = createPairingTask(validPayload);
		task.addObserver(state -> {
			if (i.get() == 0) {
				assertEquals(QrCodeReceived.class, state.getClass());
			} else if (i.get() == 1) {
				assertEquals(Pairing.class, state.getClass());
			} else if (i.get() == 2) {
				assertEquals(Paired.class, state.getClass());
			} else fail("Unexpected change of state " + state.getClass());
			i.getAndIncrement();
		});
		task.run();
	}

	@Test
	public void testAlreadyPaired() throws Exception {
		testApiException(new MailboxAlreadyPairedException(),
				MailboxAlreadyPaired.class);
	}

	@Test
	public void testMailboxApiException() throws Exception {
		testApiException(new ApiException(), UnexpectedError.class);
	}

	@Test
	public void testApiIOException() throws Exception {
		testApiException(new IOException(), ConnectionError.class);
	}

	private void testApiException(Exception e,
			Class<? extends MailboxPairingState> s) throws Exception {
		expectClassifyQrCode(validPayload, MAILBOX, QR_FORMAT_VERSION);
		context.checking(new Expectations() {{
			oneOf(crypto).encodeOnion(onionBytes);
			will(returnValue(onion));
			oneOf(api).setup(with(matches(setupProperties)));
			will(throwException(e));
		}});

		MailboxPairingTask task = createPairingTask(validPayload);
		task.run();
		task.addObserver(state -> assertEquals(state.getClass(), s));
	}

	@Test
	public void testDbException() throws Exception {
		expectClassifyQrCode(validPayload, MAILBOX, QR_FORMAT_VERSION);
		context.checking(new Expectations() {{
			oneOf(crypto).encodeOnion(onionBytes);
			will(returnValue(onion));
			oneOf(api).setup(with(matches(setupProperties)));
			will(returnValue(ownerProperties));
			oneOf(clock).currentTimeMillis();
			will(returnValue(time));
		}});
		Transaction txn = new Transaction(null, false);
		context.checking(new DbExpectations() {{
			oneOf(db).transaction(with(false), withDbRunnable(txn));
			oneOf(mailboxSettingsManager).setOwnMailboxProperties(
					with(txn), with(matches(ownerProperties)));
			will(throwException(new DbException()));
		}});

		MailboxPairingTask task = createPairingTask(validPayload);
		task.run();
		task.addObserver(state ->
				assertEquals(state.getClass(), UnexpectedError.class));
	}

	private PredicateMatcher<MailboxProperties> matches(MailboxProperties p2) {
		return new PredicateMatcher<>(MailboxProperties.class, p1 ->
				p1.getAuthToken().equals(p2.getAuthToken()) &&
						p1.getOnion().equals(p2.getOnion()) &&
						p1.isOwner() == p2.isOwner() &&
						p1.getServerSupports().equals(p2.getServerSupports()));
	}

	private MailboxPairingTask createPairingTask(String qrCodePayload) {
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(time));
		}});

		return new MailboxPairingTaskImpl(qrCodePayload, executor, db,
				crypto, clock, api, mailboxSettingsManager,
				mailboxUpdateManager, qrCodeClassifier);
	}

	private void expectClassifyQrCode(String payload, QrCodeType qrCodeType,
			int formatVersion) {
		context.checking(new Expectations() {{
			oneOf(qrCodeClassifier).classifyQrCode(payload);
			will(returnValue(new Pair<>(qrCodeType, formatVersion)));
		}});
	}
}
