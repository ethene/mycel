package com.quantumresearch.mycel.infrastructure.identity;

import com.quantumresearch.mycel.infrastructure.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.infrastructure.api.crypto.KeyPair;
import com.quantumresearch.mycel.infrastructure.api.crypto.PrivateKey;
import com.quantumresearch.mycel.infrastructure.api.crypto.PublicKey;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.infrastructure.api.identity.AuthorFactory;
import com.quantumresearch.mycel.infrastructure.api.identity.Identity;
import com.quantumresearch.mycel.infrastructure.api.identity.LocalAuthor;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.test.BrambleMockTestCase;
import com.quantumresearch.mycel.infrastructure.test.DbExpectations;
import org.jmock.Expectations;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getAgreementPrivateKey;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getAgreementPublicKey;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getIdentity;
import static org.junit.Assert.assertEquals;

public class IdentityManagerImplTest extends BrambleMockTestCase {

	private final DatabaseComponent db = context.mock(DatabaseComponent.class);
	private final CryptoComponent crypto = context.mock(CryptoComponent.class);
	private final AuthorFactory authorFactory =
			context.mock(AuthorFactory.class);
	private final Clock clock = context.mock(Clock.class);

	private final Transaction txn = new Transaction(null, false);
	private final Identity identityWithKeys = getIdentity();
	private final LocalAuthor localAuthor = identityWithKeys.getLocalAuthor();
	private final Identity identityWithoutKeys = new Identity(localAuthor,
			null, null, identityWithKeys.getTimeCreated());
	private final PublicKey handshakePublicKey = getAgreementPublicKey();
	private final PrivateKey handshakePrivateKey = getAgreementPrivateKey();
	private final KeyPair handshakeKeyPair =
			new KeyPair(handshakePublicKey, handshakePrivateKey);

	private final IdentityManagerImpl identityManager =
			new IdentityManagerImpl(db, crypto, authorFactory, clock);

	@Test
	public void testOpenDatabaseIdentityRegistered() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).addIdentity(txn, identityWithKeys);
		}});

		identityManager.registerIdentity(identityWithKeys);
		identityManager.onDatabaseOpened(txn);
	}

	@Test
	public void testOpenDatabaseHandshakeKeysGenerated() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).getIdentities(txn);
			will(returnValue(singletonList(identityWithoutKeys)));
			oneOf(crypto).generateAgreementKeyPair();
			will(returnValue(handshakeKeyPair));
			oneOf(db).setHandshakeKeyPair(txn, localAuthor.getId(),
					handshakePublicKey, handshakePrivateKey);
		}});

		identityManager.onDatabaseOpened(txn);
	}

	@Test
	public void testOpenDatabaseNoHandshakeKeysGenerated() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).getIdentities(txn);
			will(returnValue(singletonList(identityWithKeys)));
		}});

		identityManager.onDatabaseOpened(txn);
	}

	@Test
	public void testGetLocalAuthorIdentityRegistered() throws DbException {
		identityManager.registerIdentity(identityWithKeys);
		assertEquals(localAuthor, identityManager.getLocalAuthor());
	}

	@Test
	public void testGetLocalAuthorHandshakeKeysGenerated() throws Exception {
		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(true), withDbCallable(txn));
			oneOf(db).getIdentities(txn);
			will(returnValue(singletonList(identityWithoutKeys)));
			oneOf(crypto).generateAgreementKeyPair();
			will(returnValue(handshakeKeyPair));
		}});

		assertEquals(localAuthor, identityManager.getLocalAuthor());
	}

	@Test
	public void testGetLocalAuthorNoHandshakeKeysGenerated() throws Exception {
		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(true), withDbCallable(txn));
			oneOf(db).getIdentities(txn);
			will(returnValue(singletonList(identityWithKeys)));
		}});

		assertEquals(localAuthor, identityManager.getLocalAuthor());
	}

}
