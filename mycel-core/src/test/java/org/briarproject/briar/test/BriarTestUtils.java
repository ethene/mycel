package com.quantumresearch.mycel.app.test;

import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorFactory;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.util.Base32;
import com.quantumresearch.mycel.app.api.client.MessageTracker;
import com.quantumresearch.mycel.app.api.client.MessageTracker.GroupCount;

import java.util.Locale;

import static java.lang.System.arraycopy;
import static com.quantumresearch.mycel.spore.api.contact.HandshakeLinkConstants.FORMAT_VERSION;
import static com.quantumresearch.mycel.spore.api.contact.HandshakeLinkConstants.RAW_LINK_BYTES;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static com.quantumresearch.mycel.spore.util.StringUtils.getRandomString;
import static org.junit.Assert.assertEquals;

public class BriarTestUtils {

	public static void assertGroupCount(MessageTracker tracker, GroupId g,
			long msgCount, long unreadCount, long latestMsgTime)
			throws DbException {
		GroupCount groupCount = tracker.getGroupCount(g);
		assertEquals(msgCount, groupCount.getMsgCount());
		assertEquals(unreadCount, groupCount.getUnreadCount());
		assertEquals(latestMsgTime, groupCount.getLatestMsgTime());
	}

	public static void assertGroupCount(MessageTracker tracker, GroupId g,
			long msgCount, long unreadCount) throws DbException {
		GroupCount c1 = tracker.getGroupCount(g);
		assertEquals(msgCount, c1.getMsgCount());
		assertEquals(unreadCount, c1.getUnreadCount());
	}

	public static Author getRealAuthor(AuthorFactory authorFactory) {
		String name = getRandomString(MAX_AUTHOR_NAME_LENGTH);
		return authorFactory.createLocalAuthor(name);
	}

	public static LocalAuthor getRealLocalAuthor(AuthorFactory authorFactory) {
		String name = getRandomString(MAX_AUTHOR_NAME_LENGTH);
		return authorFactory.createLocalAuthor(name);
	}

	public static String getRealHandshakeLink(CryptoComponent cryptoComponent) {
		KeyPair keyPair = cryptoComponent.generateAgreementKeyPair();
		byte[] linkBytes = new byte[RAW_LINK_BYTES];
		byte[] publicKey = keyPair.getPublic().getEncoded();
		linkBytes[0] = FORMAT_VERSION;
		arraycopy(publicKey, 0, linkBytes, 1, RAW_LINK_BYTES - 1);
		return ("mycel://" + Base32.encode(linkBytes)).toLowerCase(Locale.US);
	}

}
