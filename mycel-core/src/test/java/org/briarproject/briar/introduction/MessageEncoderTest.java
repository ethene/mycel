package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import org.jmock.Expectations;
import org.junit.Test;

import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
import static com.quantumresearch.mycel.spore.test.TestUtils.getAuthor;
import static com.quantumresearch.mycel.spore.test.TestUtils.getMessage;
import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.spore.util.StringUtils.getRandomString;
import static com.quantumresearch.mycel.app.api.introduction.IntroductionConstants.MAX_INTRODUCTION_TEXT_LENGTH;
import static com.quantumresearch.mycel.app.introduction.MessageType.REQUEST;

public class MessageEncoderTest extends BrambleMockTestCase {

	private final ClientHelper clientHelper = context.mock(ClientHelper.class);
	private final MessageFactory messageFactory =
			context.mock(MessageFactory.class);
	private final MessageEncoder messageEncoder =
			new MessageEncoderImpl(clientHelper, messageFactory);

	private final GroupId groupId = new GroupId(getRandomId());
	private final Message message =
			getMessage(groupId, MAX_MESSAGE_BODY_LENGTH);
	private final long timestamp = message.getTimestamp();
	private final byte[] body = message.getBody();
	private final Author author = getAuthor();
	private final BdfList authorList = new BdfList();
	private final String text = getRandomString(MAX_INTRODUCTION_TEXT_LENGTH);

	@Test
	public void testEncodeRequestMessage() throws FormatException {
		context.checking(new Expectations() {{
			oneOf(clientHelper).toList(author);
			will(returnValue(authorList));
		}});
		expectCreateMessage(
				BdfList.of(REQUEST.getValue(), null, authorList, text));

		messageEncoder.encodeRequestMessage(groupId, timestamp, null,
				author, text);
	}

	private void expectCreateMessage(BdfList bodyList) throws FormatException {
		context.checking(new Expectations() {{
			oneOf(clientHelper).toByteArray(bodyList);
			will(returnValue(body));
			oneOf(messageFactory).createMessage(groupId, timestamp, body);
			will(returnValue(message));
		}});
	}

}
