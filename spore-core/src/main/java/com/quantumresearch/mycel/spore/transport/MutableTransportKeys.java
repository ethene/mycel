package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.transport.TransportKeys;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
class MutableTransportKeys {

	private final TransportId transportId;
	private final MutableIncomingKeys inPrev, inCurr, inNext;
	private final MutableOutgoingKeys outCurr;
	@Nullable
	private final SecretKey rootKey;
	private final boolean alice;

	MutableTransportKeys(TransportKeys k) {
		transportId = k.getTransportId();
		inPrev = new MutableIncomingKeys(k.getPreviousIncomingKeys());
		inCurr = new MutableIncomingKeys(k.getCurrentIncomingKeys());
		inNext = new MutableIncomingKeys(k.getNextIncomingKeys());
		outCurr = new MutableOutgoingKeys(k.getCurrentOutgoingKeys());
		if (k.isHandshakeMode()) {
			rootKey = k.getRootKey();
			alice = k.isAlice();
		} else {
			rootKey = null;
			alice = false;
		}
	}

	TransportKeys snapshot() {
		if (rootKey == null) {
			return new TransportKeys(transportId, inPrev.snapshot(),
					inCurr.snapshot(), inNext.snapshot(), outCurr.snapshot());
		} else {
			return new TransportKeys(transportId, inPrev.snapshot(),
					inCurr.snapshot(), inNext.snapshot(), outCurr.snapshot(),
					rootKey, alice);
		}
	}

	TransportId getTransportId() {
		return transportId;
	}

	MutableIncomingKeys getPreviousIncomingKeys() {
		return inPrev;
	}

	MutableIncomingKeys getCurrentIncomingKeys() {
		return inCurr;
	}

	MutableIncomingKeys getNextIncomingKeys() {
		return inNext;
	}

	MutableOutgoingKeys getCurrentOutgoingKeys() {
		return outCurr;
	}

	boolean isHandshakeMode() {
		return rootKey != null;
	}

	SecretKey getRootKey() {
		if (rootKey == null) throw new UnsupportedOperationException();
		return rootKey;
	}

	boolean isAlice() {
		if (rootKey == null) throw new UnsupportedOperationException();
		return alice;
	}
}
