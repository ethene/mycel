package com.quantumresearch.mycel.infrastructure.client;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.client.BdfMessageContext;
import com.quantumresearch.mycel.infrastructure.api.client.BdfMessageValidator;
import com.quantumresearch.mycel.infrastructure.api.data.BdfDictionary;
import com.quantumresearch.mycel.infrastructure.api.data.BdfList;
import com.quantumresearch.mycel.infrastructure.api.db.Metadata;
import com.quantumresearch.mycel.infrastructure.api.sync.Group;
import com.quantumresearch.mycel.infrastructure.api.sync.InvalidMessageException;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageContext;
import com.quantumresearch.mycel.infrastructure.test.ValidatorTestCase;
import org.briarproject.nullsafety.NotNullByDefault;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.Test;

import static com.quantumresearch.mycel.infrastructure.api.transport.TransportConstants.MAX_CLOCK_DIFFERENCE;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class BdfMessageValidatorTest extends ValidatorTestCase {

	@NotNullByDefault
	private final BdfMessageValidator failIfSubclassIsCalled =
			new BdfMessageValidator(clientHelper, metadataEncoder, clock) {
				@Override
				protected BdfMessageContext validateMessage(Message m, Group g,
						BdfList body) {
					throw new AssertionError();
				}
			};

	private final BdfList body = BdfList.of(123, 456);
	private final BdfDictionary dictionary = new BdfDictionary();
	private final Metadata meta = new Metadata();

	public BdfMessageValidatorTest() {
		context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsFarFutureTimestamp() throws Exception {
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(timestamp - MAX_CLOCK_DIFFERENCE - 1));
		}});

		failIfSubclassIsCalled.validateMessage(message, group);
	}

	@Test
	public void testAcceptsMaxTimestamp() throws Exception {
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(timestamp - MAX_CLOCK_DIFFERENCE));
			oneOf(clientHelper).toList(message, true);
			will(returnValue(body));
			oneOf(metadataEncoder).encode(dictionary);
			will(returnValue(meta));
		}});

		@NotNullByDefault
		BdfMessageValidator v = new BdfMessageValidator(clientHelper,
				metadataEncoder, clock) {
			@Override
			protected BdfMessageContext validateMessage(Message m, Group g,
					BdfList b) {
				assertSame(message, m);
				assertSame(group, g);
				assertSame(body, b);
				return new BdfMessageContext(dictionary);
			}
		};
		MessageContext messageContext = v.validateMessage(message, group);
		assertEquals(0, messageContext.getDependencies().size());
		assertSame(meta, messageContext.getMetadata());
	}

	@Test
	public void testAcceptsMinLengthMessage() throws Exception {
		Message shortMessage = getMessage(groupId, 1);

		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(timestamp));
			oneOf(clientHelper).toList(shortMessage, true);
			will(returnValue(body));
			oneOf(metadataEncoder).encode(dictionary);
			will(returnValue(meta));
		}});

		@NotNullByDefault
		BdfMessageValidator v = new BdfMessageValidator(clientHelper,
				metadataEncoder, clock) {
			@Override
			protected BdfMessageContext validateMessage(Message m, Group g,
					BdfList b) {
				assertSame(shortMessage, m);
				assertSame(group, g);
				assertSame(body, b);
				return new BdfMessageContext(dictionary);
			}
		};
		MessageContext messageContext = v.validateMessage(shortMessage, group);
		assertEquals(0, messageContext.getDependencies().size());
		assertSame(meta, messageContext.getMetadata());
	}

	@Test(expected = InvalidMessageException.class)
	public void testRejectsInvalidBdfList() throws Exception {
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(timestamp));
			oneOf(clientHelper).toList(message, true);
			will(throwException(new FormatException()));
		}});

		failIfSubclassIsCalled.validateMessage(message, group);
	}

	@Test(expected = InvalidMessageException.class)
	public void testRethrowsFormatExceptionFromSubclass() throws Exception {
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(timestamp));
			oneOf(clientHelper).toList(message, true);
			will(returnValue(body));
		}});

		@NotNullByDefault
		BdfMessageValidator v = new BdfMessageValidator(clientHelper,
				metadataEncoder, clock) {
			@Override
			protected BdfMessageContext validateMessage(Message m, Group g,
					BdfList b) throws FormatException {
				throw new FormatException();
			}
		};
		v.validateMessage(message, group);
	}
}
