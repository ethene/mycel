package com.quantumresearch.mycel.spore.transport.agreement;

import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfEntry;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.transport.KeySetId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.data.BdfDictionary.NULL_VALUE;
import static com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_IS_SESSION;
import static com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_TRANSPORT_ID;
import static com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementConstants.SESSION_KEY_KEY_SET_ID;
import static com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementConstants.SESSION_KEY_LAST_LOCAL_MESSAGE_ID;
import static com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementConstants.SESSION_KEY_LOCAL_PRIVATE_KEY;
import static com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementConstants.SESSION_KEY_LOCAL_PUBLIC_KEY;
import static com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementConstants.SESSION_KEY_LOCAL_TIMESTAMP;
import static com.quantumresearch.mycel.spore.transport.agreement.TransportKeyAgreementConstants.SESSION_KEY_STATE;

@Immutable
@NotNullByDefault
class SessionEncoderImpl implements SessionEncoder {

	@Inject
	SessionEncoderImpl() {
	}

	@Override
	public BdfDictionary encodeSession(Session s, TransportId transportId) {
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_IS_SESSION, true);
		meta.put(MSG_KEY_TRANSPORT_ID, transportId.getString());
		meta.put(SESSION_KEY_STATE, s.getState().getValue());
		putNullable(meta, SESSION_KEY_LAST_LOCAL_MESSAGE_ID,
				s.getLastLocalMessageId());
		KeyPair localKeyPair = s.getLocalKeyPair();
		if (localKeyPair == null) {
			meta.put(SESSION_KEY_LOCAL_PUBLIC_KEY, NULL_VALUE);
			meta.put(SESSION_KEY_LOCAL_PRIVATE_KEY, NULL_VALUE);
		} else {
			meta.put(SESSION_KEY_LOCAL_PUBLIC_KEY,
					localKeyPair.getPublic().getEncoded());
			meta.put(SESSION_KEY_LOCAL_PRIVATE_KEY,
					localKeyPair.getPrivate().getEncoded());
		}
		putNullable(meta, SESSION_KEY_LOCAL_TIMESTAMP, s.getLocalTimestamp());
		KeySetId keySetId = s.getKeySetId();
		if (keySetId == null) meta.put(SESSION_KEY_KEY_SET_ID, NULL_VALUE);
		else meta.put(SESSION_KEY_KEY_SET_ID, keySetId.getInt());
		return meta;
	}

	@Override
	public BdfDictionary getSessionQuery(TransportId transportId) {
		return BdfDictionary.of(
				new BdfEntry(MSG_KEY_IS_SESSION, true),
				new BdfEntry(MSG_KEY_TRANSPORT_ID, transportId.getString()));
	}

	private void putNullable(BdfDictionary meta, String key,
			@Nullable Object o) {
		meta.put(key, o == null ? NULL_VALUE : o);
	}
}
