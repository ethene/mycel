package com.quantumresearch.mycel.spore.api.sync;

import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * A record containing a nonce for choosing between redundant sessions.
 */
@Immutable
@NotNullByDefault
public class Priority {

	private final byte[] nonce;

	public Priority(byte[] nonce) {
		this.nonce = nonce;
	}

	public byte[] getNonce() {
		return nonce;
	}
}
