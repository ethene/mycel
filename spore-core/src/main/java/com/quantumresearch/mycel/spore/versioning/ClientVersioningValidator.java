package com.quantumresearch.mycel.spore.versioning;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.BdfMessageContext;
import com.quantumresearch.mycel.spore.api.client.BdfMessageValidator;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.api.sync.ClientId.MAX_CLIENT_ID_LENGTH;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkLength;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkSize;
import static com.quantumresearch.mycel.spore.versioning.ClientVersioningConstants.MSG_KEY_LOCAL;
import static com.quantumresearch.mycel.spore.versioning.ClientVersioningConstants.MSG_KEY_UPDATE_VERSION;

@Immutable
@NotNullByDefault
class ClientVersioningValidator extends BdfMessageValidator {

	ClientVersioningValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		super(clientHelper, metadataEncoder, clock);
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws FormatException {
		// Client states, update version
		checkSize(body, 2);
		// Client states
		BdfList states = body.getList(0);
		int size = states.size();
		for (int i = 0; i < size; i++) {
			BdfList clientState = states.getList(i);
			// Client ID, major version, minor version, active
			checkSize(clientState, 4);
			String clientId = clientState.getString(0);
			checkLength(clientId, 1, MAX_CLIENT_ID_LENGTH);
			int majorVersion = clientState.getInt(1);
			if (majorVersion < 0) throw new FormatException();
			int minorVersion = clientState.getInt(2);
			if (minorVersion < 0) throw new FormatException();
			clientState.getBoolean(3);
		}
		// Update version
		long updateVersion = body.getLong(1);
		if (updateVersion < 0) throw new FormatException();
		// Return the metadata
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_UPDATE_VERSION, updateVersion);
		meta.put(MSG_KEY_LOCAL, false);
		return new BdfMessageContext(meta);
	}
}
