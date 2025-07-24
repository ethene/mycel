package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.system.Clock;
import org.jmock.Expectations;

import static com.quantumresearch.mycel.spore.test.TestUtils.getAuthor;
import static com.quantumresearch.mycel.spore.test.TestUtils.getClientId;
import static com.quantumresearch.mycel.spore.test.TestUtils.getGroup;
import static com.quantumresearch.mycel.spore.test.TestUtils.getMessage;

public abstract class ValidatorTestCase extends BrambleMockTestCase {

	protected final ClientHelper clientHelper =
			context.mock(ClientHelper.class);
	protected final MetadataEncoder metadataEncoder =
			context.mock(MetadataEncoder.class);
	protected final Clock clock = context.mock(Clock.class);

	protected final Group group = getGroup(getClientId(), 123);
	protected final GroupId groupId = group.getId();
	protected final byte[] descriptor = group.getDescriptor();
	protected final Message message = getMessage(groupId);
	protected final MessageId messageId = message.getId();
	protected final long timestamp = message.getTimestamp();
	protected final Author author = getAuthor();
	protected final BdfList authorList = BdfList.of(
			author.getFormatVersion(),
			author.getName(),
			author.getPublicKey()
	);

	protected void expectParseAuthor(BdfList authorList, Author author)
			throws Exception {
		context.checking(new Expectations() {{
			oneOf(clientHelper).parseAndValidateAuthor(authorList);
			will(returnValue(author));
		}});
	}

}