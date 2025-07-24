package com.quantumresearch.mycel.spore.transport.agreement;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.BdfMessageContext;
import com.quantumresearch.mycel.spore.api.client.BdfMessageValidator;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static java.util.Collections.singletonList;
import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.MAX_AGREEMENT_PUBLIC_KEY_BYTES;
import static com.quantumresearch.mycel.spore.api.plugin.TransportId.MAX_TRANSPORT_ID_LENGTH;
import static com.quantumresearch.mycel.spore.api.system.Clock.MIN_REASONABLE_TIME_MS;
import static com.quantumresearch.mycel.spore.transport.agreement.MessageType.ACTIVATE;
import static com.quantumresearch.mycel.spore.transport.agreement.MessageType.KEY;
import static com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_PUBLIC_KEY;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkLength;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkSize;

@Immutable
@NotNullByDefault
class TransportKeyAgreementValidator extends BdfMessageValidator {

	private final MessageEncoder messageEncoder;

	TransportKeyAgreementValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock,
			MessageEncoder messageEncoder) {
		super(clientHelper, metadataEncoder, clock);
		this.messageEncoder = messageEncoder;
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws FormatException {
		MessageType type = MessageType.fromValue(body.getInt(0));
		if (type == KEY) return validateKeyMessage(m.getTimestamp(), body);
		else if (type == ACTIVATE) return validateActivateMessage(body);
		else throw new AssertionError();
	}

	private BdfMessageContext validateKeyMessage(long timestamp, BdfList body)
			throws FormatException {
		if (timestamp < MIN_REASONABLE_TIME_MS) throw new FormatException();
		// Message type, transport ID, public key
		checkSize(body, 3);
		String transportId = body.getString(1);
		checkLength(transportId, 1, MAX_TRANSPORT_ID_LENGTH);
		byte[] publicKey = body.getRaw(2);
		checkLength(publicKey, 1, MAX_AGREEMENT_PUBLIC_KEY_BYTES);
		BdfDictionary meta = messageEncoder.encodeMessageMetadata(
				new TransportId(transportId), KEY, false);
		meta.put(MSG_KEY_PUBLIC_KEY, publicKey);
		return new BdfMessageContext(meta);
	}

	private BdfMessageContext validateActivateMessage(BdfList body)
			throws FormatException {
		// Message type, transport ID, previous message ID
		checkSize(body, 3);
		String transportId = body.getString(1);
		checkLength(transportId, 1, MAX_TRANSPORT_ID_LENGTH);
		byte[] previousMessageId = body.getRaw(2);
		checkLength(previousMessageId, MessageId.LENGTH);
		BdfDictionary meta = messageEncoder.encodeMessageMetadata(
				new TransportId(transportId), ACTIVATE, false);
		MessageId dependency = new MessageId(previousMessageId);
		return new BdfMessageContext(meta, singletonList(dependency));
	}
}
