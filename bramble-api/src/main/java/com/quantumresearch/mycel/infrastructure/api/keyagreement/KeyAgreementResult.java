package com.quantumresearch.mycel.infrastructure.api.keyagreement;

import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;

public class KeyAgreementResult {

	private final SecretKey masterKey;
	private final DuplexTransportConnection connection;
	private final TransportId transportId;
	private final boolean alice;

	public KeyAgreementResult(SecretKey masterKey,
			DuplexTransportConnection connection, TransportId transportId,
			boolean alice) {
		this.masterKey = masterKey;
		this.connection = connection;
		this.transportId = transportId;
		this.alice = alice;
	}

	public SecretKey getMasterKey() {
		return masterKey;
	}

	public DuplexTransportConnection getConnection() {
		return connection;
	}

	public TransportId getTransportId() {
		return transportId;
	}

	public boolean wasAlice() {
		return alice;
	}
}
