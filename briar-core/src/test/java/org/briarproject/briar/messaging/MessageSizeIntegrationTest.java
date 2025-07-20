package com.quantumresearch.mycel.app.messaging;

import com.quantumresearch.mycel.infrastructure.api.UniqueId;
import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.infrastructure.api.data.BdfList;
import com.quantumresearch.mycel.infrastructure.api.identity.AuthorFactory;
import com.quantumresearch.mycel.infrastructure.api.identity.LocalAuthor;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.infrastructure.test.BrambleTestCase;
import com.quantumresearch.mycel.app.api.attachment.AttachmentHeader;
import com.quantumresearch.mycel.app.api.forum.ForumPost;
import com.quantumresearch.mycel.app.api.forum.ForumPostFactory;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessage;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessageFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static com.quantumresearch.mycel.infrastructure.api.identity.AuthorConstants.MAX_PUBLIC_KEY_LENGTH;
import static com.quantumresearch.mycel.infrastructure.api.record.Record.MAX_RECORD_PAYLOAD_BYTES;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getRandomBytes;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.infrastructure.util.IoUtils.copyAndClose;
import static com.quantumresearch.mycel.infrastructure.util.StringUtils.getRandomString;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MAX_CONTENT_TYPE_BYTES;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MAX_IMAGE_SIZE;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.MAX_AUTO_DELETE_TIMER_MS;
import static com.quantumresearch.mycel.app.api.forum.ForumConstants.MAX_FORUM_POST_TEXT_LENGTH;
import static com.quantumresearch.mycel.app.api.messaging.MessagingConstants.MAX_ATTACHMENTS_PER_MESSAGE;
import static com.quantumresearch.mycel.app.api.messaging.MessagingConstants.MAX_PRIVATE_MESSAGE_TEXT_LENGTH;
import static com.quantumresearch.mycel.app.messaging.MessageTypes.ATTACHMENT;
import static org.junit.Assert.assertTrue;

public class MessageSizeIntegrationTest extends BrambleTestCase {

	@Inject
	CryptoComponent crypto;
	@Inject
	AuthorFactory authorFactory;
	@Inject
	PrivateMessageFactory privateMessageFactory;
	@Inject
	ForumPostFactory forumPostFactory;
	@Inject
	ClientHelper clientHelper;
	@Inject
	MessageFactory messageFactory;

	public MessageSizeIntegrationTest() {
		MessageSizeIntegrationTestComponent component =
				DaggerMessageSizeIntegrationTestComponent.builder().build();
		MessageSizeIntegrationTestComponent.Helper
				.injectEagerSingletons(component);
		component.inject(this);
	}

	@Test
	public void testLegacyPrivateMessageFitsIntoRecord() throws Exception {
		// Create a maximum-length private message
		GroupId groupId = new GroupId(getRandomId());
		long timestamp = Long.MAX_VALUE;
		String text = getRandomString(MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
		PrivateMessage message = privateMessageFactory
				.createLegacyPrivateMessage(groupId, timestamp, text);
		// Check the size of the serialised message
		int length = message.getMessage().getRawLength();
		assertTrue(length > UniqueId.LENGTH + 8
				+ MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
		assertTrue(length <= MAX_RECORD_PAYLOAD_BYTES);
	}

	@Test
	public void testPrivateMessageFitsIntoRecord() throws Exception {
		// Create a maximum-length private message
		GroupId groupId = new GroupId(getRandomId());
		long timestamp = Long.MAX_VALUE;
		String text = getRandomString(MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
		// Create the maximum number of maximum-length attachment headers
		List<AttachmentHeader> headers = new ArrayList<>();
		for (int i = 0; i < MAX_ATTACHMENTS_PER_MESSAGE; i++) {
			headers.add(new AttachmentHeader(groupId,
					new MessageId(getRandomId()),
					getRandomString(MAX_CONTENT_TYPE_BYTES)));
		}
		PrivateMessage message = privateMessageFactory.createPrivateMessage(
				groupId, timestamp, text, headers, MAX_AUTO_DELETE_TIMER_MS);
		// Check the size of the serialised message
		int length = message.getMessage().getRawLength();
		assertTrue(length > UniqueId.LENGTH + 8
				+ MAX_PRIVATE_MESSAGE_TEXT_LENGTH + MAX_ATTACHMENTS_PER_MESSAGE
				* (UniqueId.LENGTH + MAX_CONTENT_TYPE_BYTES) + 4);
		assertTrue(length <= MAX_RECORD_PAYLOAD_BYTES);
	}

	@Test
	public void testAttachmentFitsIntoRecord() throws Exception {
		// Create a maximum-length attachment
		String contentType = getRandomString(MAX_CONTENT_TYPE_BYTES);
		byte[] data = getRandomBytes(MAX_IMAGE_SIZE);

		ByteArrayInputStream dataIn = new ByteArrayInputStream(data);
		ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
		byte[] descriptor =
				clientHelper.toByteArray(BdfList.of(ATTACHMENT, contentType));
		bodyOut.write(descriptor);
		copyAndClose(dataIn, bodyOut);
		byte[] body = bodyOut.toByteArray();

		GroupId groupId = new GroupId(getRandomId());
		long timestamp = Long.MAX_VALUE;
		Message message =
				messageFactory.createMessage(groupId, timestamp, body);

		// Check the size of the serialised message
		int length = message.getRawLength();
		assertTrue(length > UniqueId.LENGTH + 8
				+ 1 + MAX_CONTENT_TYPE_BYTES + MAX_IMAGE_SIZE);
		assertTrue(length <= MAX_RECORD_PAYLOAD_BYTES);
	}

	@Test
	public void testForumPostFitsIntoRecord() throws Exception {
		// Create a maximum-length author
		String authorName = getRandomString(MAX_AUTHOR_NAME_LENGTH);
		LocalAuthor author = authorFactory.createLocalAuthor(authorName);
		// Create a maximum-length forum post
		GroupId groupId = new GroupId(getRandomId());
		long timestamp = Long.MAX_VALUE;
		MessageId parent = new MessageId(getRandomId());
		String text = getRandomString(MAX_FORUM_POST_TEXT_LENGTH);
		ForumPost post = forumPostFactory.createPost(groupId,
				timestamp, parent, author, text);
		// Check the size of the serialised message
		int length = post.getMessage().getRawLength();
		assertTrue(length > UniqueId.LENGTH + 8 + UniqueId.LENGTH + 4
				+ MAX_AUTHOR_NAME_LENGTH + MAX_PUBLIC_KEY_LENGTH
				+ MAX_FORUM_POST_TEXT_LENGTH);
		assertTrue(length <= MAX_RECORD_PAYLOAD_BYTES);
	}
}
