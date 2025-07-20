package com.quantumresearch.mycel.app.avatar;

import com.quantumresearch.mycel.infrastructure.api.data.BdfDictionary;
import com.quantumresearch.mycel.infrastructure.api.data.BdfEntry;
import com.quantumresearch.mycel.infrastructure.api.data.BdfList;
import com.quantumresearch.mycel.infrastructure.api.data.BdfReader;
import com.quantumresearch.mycel.infrastructure.api.data.BdfReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.data.MetadataEncoder;
import com.quantumresearch.mycel.infrastructure.api.db.Metadata;
import com.quantumresearch.mycel.infrastructure.api.sync.Group;
import com.quantumresearch.mycel.infrastructure.api.sync.InvalidMessageException;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageContext;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.test.BrambleMockTestCase;
import org.jmock.Expectations;
import org.junit.Test;

import java.io.InputStream;

import static com.quantumresearch.mycel.infrastructure.api.transport.TransportConstants.MAX_CLOCK_DIFFERENCE;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getClientId;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getGroup;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getMessage;
import static com.quantumresearch.mycel.infrastructure.util.StringUtils.getRandomString;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MAX_CONTENT_TYPE_BYTES;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;
import static com.quantumresearch.mycel.app.avatar.AvatarConstants.MSG_KEY_VERSION;
import static com.quantumresearch.mycel.app.avatar.AvatarConstants.MSG_TYPE_UPDATE;
import static org.junit.Assert.assertEquals;

public class AvatarValidatorTest extends BrambleMockTestCase {

	private final BdfReaderFactory bdfReaderFactory =
			context.mock(BdfReaderFactory.class);
	private final MetadataEncoder metadataEncoder =
			context.mock(MetadataEncoder.class);
	private final Clock clock = context.mock(Clock.class);
	private final BdfReader reader = context.mock(BdfReader.class);

	private final Group group = getGroup(getClientId(), 123);
	private final Message message = getMessage(group.getId());
	private final long now = message.getTimestamp() + 1000;
	private final String contentType = getRandomString(MAX_CONTENT_TYPE_BYTES);
	private final long version = System.currentTimeMillis();
	private final BdfDictionary meta = BdfDictionary.of(
			new BdfEntry(MSG_KEY_VERSION, version),
			new BdfEntry(MSG_KEY_CONTENT_TYPE, contentType),
			// Descriptor length is zero as the test doesn't read from the
			// counting input stream
			new BdfEntry(MSG_KEY_DESCRIPTOR_LENGTH, 0L)
	);

	private final AvatarValidator validator =
			new AvatarValidator(bdfReaderFactory, metadataEncoder, clock);

	@Test(expected = InvalidMessageException.class)
	public void testRejectsFarFutureTimestamp() throws Exception {
		expectCheckTimestamp(message.getTimestamp() - MAX_CLOCK_DIFFERENCE - 1);

		validator.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsEmptyBody() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(new BdfList());

		validator.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsTooShortBody() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version));

		validator.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsUnknownMessageType() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE + 1, version, contentType));

		validator.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsNonLongVersion() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, "foo", contentType));

		validator.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsNonStringContentType() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version, 1337));

		validator.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsEmptyContentType() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version, ""));

		validator.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsTooLongContentType() throws Exception {
		String contentType = getRandomString(MAX_CONTENT_TYPE_BYTES + 1);

		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version, contentType));

		validator.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsTooLongBody() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version, contentType, 1));

		validator.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsNegativeVersion() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, -1, contentType));

		validator.validateMessage(message, group);
	}

	@Test
	public void testAcceptsUpdateMessage() throws Exception {
		testAcceptsUpdateMessage(
				BdfList.of(MSG_TYPE_UPDATE, version, contentType), meta);
	}

	@Test
	public void testAcceptsZeroVersion() throws Exception {
		BdfList body = BdfList.of(MSG_TYPE_UPDATE, 0L, contentType);
		BdfDictionary meta = BdfDictionary.of(
				new BdfEntry(MSG_KEY_VERSION, 0L),
				new BdfEntry(MSG_KEY_CONTENT_TYPE, contentType),
				new BdfEntry(MSG_KEY_DESCRIPTOR_LENGTH, 0L)
		);
		testAcceptsUpdateMessage(body, meta);
	}

	private void testAcceptsUpdateMessage(BdfList body, BdfDictionary meta)
			throws Exception {
		expectCheckTimestamp(now);
		expectParseList(body);
		expectEncodeMetadata(meta);

		MessageContext result = validator.validateMessage(message, group);
		assertEquals(0, result.getDependencies().size());
	}

	private void expectCheckTimestamp(long now) {
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
		}});
	}

	private void expectParseList(BdfList body) throws Exception {
		context.checking(new Expectations() {{
			oneOf(bdfReaderFactory).createReader(with(any(InputStream.class)));
			will(returnValue(reader));
			oneOf(reader).readList();
			will(returnValue(body));
		}});
	}

	private void expectEncodeMetadata(BdfDictionary meta) throws Exception {
		context.checking(new Expectations() {{
			oneOf(metadataEncoder).encode(meta);
			will(returnValue(new Metadata()));
		}});
	}
}
