package com.quantumresearch.mycel.spore.api.client;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.InvalidMessageException;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageContext;
import com.quantumresearch.mycel.spore.api.sync.validation.MessageValidator;
import com.quantumresearch.mycel.spore.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.transport.TransportConstants.MAX_CLOCK_DIFFERENCE;

@Immutable
@NotNullByDefault
public abstract class BdfMessageValidator implements MessageValidator {

	protected static final Logger LOG =
			getLogger(BdfMessageValidator.class.getName());

	protected final ClientHelper clientHelper;
	protected final MetadataEncoder metadataEncoder;
	protected final Clock clock;
	protected final boolean canonical;

	/**
	 * Transitional alternative to
	 * {@link #BdfMessageValidator(ClientHelper, MetadataEncoder, Clock)} that
	 * accepts messages in non-canonical form, for backward compatibility.
	 */
	@Deprecated
	protected BdfMessageValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock, boolean canonical) {
		this.clientHelper = clientHelper;
		this.metadataEncoder = metadataEncoder;
		this.clock = clock;
		this.canonical = canonical;
	}

	protected BdfMessageValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		this(clientHelper, metadataEncoder, clock, true);
	}

	protected abstract BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws InvalidMessageException, FormatException;

	@Override
	public MessageContext validateMessage(Message m, Group g)
			throws InvalidMessageException {
		// Reject the message if it's too far in the future
		long now = clock.currentTimeMillis();
		if (m.getTimestamp() - now > MAX_CLOCK_DIFFERENCE) {
			throw new InvalidMessageException(
					"Timestamp is too far in the future");
		}
		try {
			BdfList bodyList = clientHelper.toList(m, canonical);
			BdfMessageContext result = validateMessage(m, g, bodyList);
			Metadata meta = metadataEncoder.encode(result.getDictionary());
			return new MessageContext(meta, result.getDependencies());
		} catch (FormatException e) {
			throw new InvalidMessageException(e);
		}
	}
}
