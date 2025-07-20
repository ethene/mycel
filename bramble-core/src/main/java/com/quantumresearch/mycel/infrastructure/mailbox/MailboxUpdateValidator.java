package com.quantumresearch.mycel.infrastructure.mailbox;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.client.BdfMessageContext;
import com.quantumresearch.mycel.infrastructure.api.client.BdfMessageValidator;
import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.data.BdfDictionary;
import com.quantumresearch.mycel.infrastructure.api.data.BdfList;
import com.quantumresearch.mycel.infrastructure.api.data.MetadataEncoder;
import com.quantumresearch.mycel.infrastructure.api.sync.Group;
import com.quantumresearch.mycel.infrastructure.api.sync.InvalidMessageException;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxUpdateManager.MSG_KEY_LOCAL;
import static com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxUpdateManager.MSG_KEY_VERSION;
import static com.quantumresearch.mycel.infrastructure.util.ValidationUtils.checkSize;

@Immutable
@NotNullByDefault
class MailboxUpdateValidator extends BdfMessageValidator {

	MailboxUpdateValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		super(clientHelper, metadataEncoder, clock);
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws InvalidMessageException, FormatException {
		// Version, Properties, clientSupports, serverSupports
		checkSize(body, 4);
		// Version
		long version = body.getLong(0);
		if (version < 0) throw new FormatException();
		// clientSupports
		BdfList clientSupports = body.getList(1);
		// serverSupports
		BdfList serverSupports = body.getList(2);
		// Properties
		BdfDictionary dictionary = body.getDictionary(3);
		clientHelper.parseAndValidateMailboxUpdate(clientSupports,
				serverSupports, dictionary);
		// Return the metadata
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_VERSION, version);
		meta.put(MSG_KEY_LOCAL, false);
		return new BdfMessageContext(meta);
	}

}
