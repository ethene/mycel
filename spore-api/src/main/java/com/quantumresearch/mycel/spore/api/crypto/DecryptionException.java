package com.quantumresearch.mycel.spore.api.crypto;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public class DecryptionException extends Exception {

	private final DecryptionResult result;

	public DecryptionException(DecryptionResult result) {
		this.result = result;
	}

	public DecryptionResult getDecryptionResult() {
		return result;
	}
}
