package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.infrastructure.api.crypto.KeyPair;
import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.identity.Author;
import com.quantumresearch.mycel.infrastructure.api.identity.AuthorFactory;
import com.quantumresearch.mycel.infrastructure.api.identity.LocalAuthor;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportProperties;
import com.quantumresearch.mycel.infrastructure.test.BrambleTestCase;
import com.quantumresearch.mycel.app.api.client.SessionId;
import org.junit.Test;

import java.util.Map;

import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getSecretKey;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getTransportPropertiesMap;
import static com.quantumresearch.mycel.app.introduction.IntroduceeSession.Local;
import static com.quantumresearch.mycel.app.introduction.IntroduceeSession.Remote;
import static com.quantumresearch.mycel.app.test.BriarTestUtils.getRealAuthor;
import static com.quantumresearch.mycel.app.test.BriarTestUtils.getRealLocalAuthor;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class IntroductionCryptoIntegrationTest extends BrambleTestCase {

	@Inject
	ClientHelper clientHelper;
	@Inject
	AuthorFactory authorFactory;
	@Inject
	CryptoComponent cryptoComponent;

	private final IntroductionCryptoImpl crypto;

	private final Author introducer;
	private final LocalAuthor alice, bob;
	private final long aliceAcceptTimestamp = 42L;
	private final long bobAcceptTimestamp = 1337L;
	private final SecretKey masterKey = getSecretKey();
	private final KeyPair aliceEphemeral, bobEphemeral;
	private final Map<TransportId, TransportProperties> aliceTransport =
			getTransportPropertiesMap(3);
	private final Map<TransportId, TransportProperties> bobTransport =
			getTransportPropertiesMap(3);

	public IntroductionCryptoIntegrationTest() {
		IntroductionIntegrationTestComponent component =
				DaggerIntroductionIntegrationTestComponent.builder().build();
		IntroductionIntegrationTestComponent.Helper
				.injectEagerSingletons(component);
		component.inject(this);
		crypto = new IntroductionCryptoImpl(cryptoComponent, clientHelper);

		introducer = getRealAuthor(authorFactory);
		LocalAuthor introducee1 = getRealLocalAuthor(authorFactory);
		LocalAuthor introducee2 = getRealLocalAuthor(authorFactory);
		boolean isAlice =
				crypto.isAlice(introducee1.getId(), introducee2.getId());
		alice = isAlice ? introducee1 : introducee2;
		bob = isAlice ? introducee2 : introducee1;
		aliceEphemeral = crypto.generateAgreementKeyPair();
		bobEphemeral = crypto.generateAgreementKeyPair();
	}

	@Test
	public void testGetSessionId() {
		SessionId s1 = crypto.getSessionId(introducer, alice, bob);
		SessionId s2 = crypto.getSessionId(introducer, bob, alice);
		assertEquals(s1, s2);

		SessionId s3 = crypto.getSessionId(alice, bob, introducer);
		assertNotEquals(s1, s3);
	}

	@Test
	public void testIsAlice() {
		assertTrue(crypto.isAlice(alice.getId(), bob.getId()));
		assertFalse(crypto.isAlice(bob.getId(), alice.getId()));
	}

	@Test
	public void testDeriveMasterKey() throws Exception {
		SecretKey aliceMasterKey = crypto.deriveMasterKey(
				aliceEphemeral.getPublic(), aliceEphemeral.getPrivate(),
				bobEphemeral.getPublic(), true);
		SecretKey bobMasterKey = crypto.deriveMasterKey(
				bobEphemeral.getPublic(), bobEphemeral.getPrivate(),
				aliceEphemeral.getPublic(), false);
		assertArrayEquals(aliceMasterKey.getBytes(), bobMasterKey.getBytes());
	}

	@Test
	public void testAliceAuthMac() throws Exception {
		SecretKey aliceMacKey = crypto.deriveMacKey(masterKey, true);
		Local local = new Local(true, null, -1, aliceEphemeral.getPublic(),
				aliceEphemeral.getPrivate(), aliceTransport,
				aliceAcceptTimestamp, aliceMacKey.getBytes());
		Remote remote = new Remote(false, bob, null, bobEphemeral.getPublic(),
				bobTransport, bobAcceptTimestamp, null);
		byte[] aliceMac = crypto.authMac(aliceMacKey, introducer.getId(),
				alice.getId(), local, remote);

		// verify from Bob's perspective
		crypto.verifyAuthMac(aliceMac, aliceMacKey, introducer.getId(),
				bob.getId(), remote, alice.getId(), local);
	}

	@Test
	public void testBobAuthMac() throws Exception {
		SecretKey bobMacKey = crypto.deriveMacKey(masterKey, false);
		Local local = new Local(false, null, -1, bobEphemeral.getPublic(),
				bobEphemeral.getPrivate(), bobTransport,
				bobAcceptTimestamp, bobMacKey.getBytes());
		Remote remote = new Remote(true, alice, null,
				aliceEphemeral.getPublic(), aliceTransport,
				aliceAcceptTimestamp, null);
		byte[] bobMac = crypto.authMac(bobMacKey, introducer.getId(),
				bob.getId(), local, remote);

		// verify from Alice's perspective
		crypto.verifyAuthMac(bobMac, bobMacKey, introducer.getId(),
				alice.getId(), remote, bob.getId(), local);
	}

	@Test
	public void testSign() throws Exception {
		SecretKey macKey = crypto.deriveMacKey(masterKey, true);
		byte[] signature = crypto.sign(macKey, alice.getPrivateKey());
		crypto.verifySignature(macKey, alice.getPublicKey(), signature);
	}

	@Test
	public void testAliceActivateMac() throws Exception {
		SecretKey aliceMacKey = crypto.deriveMacKey(masterKey, true);
		byte[] aliceMac = crypto.activateMac(aliceMacKey);
		crypto.verifyActivateMac(aliceMac, aliceMacKey);
	}

	@Test
	public void testBobActivateMac() throws Exception {
		SecretKey bobMacKey = crypto.deriveMacKey(masterKey, false);
		byte[] bobMac = crypto.activateMac(bobMacKey);
		crypto.verifyActivateMac(bobMac, bobMacKey);
	}
}
