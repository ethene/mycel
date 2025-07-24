package com.quantumresearch.mycel.spore.api.keyagreement.event;

import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.keyagreement.KeyAgreementResult;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a BQP protocol completes.
 */
@Immutable
@NotNullByDefault
public class KeyAgreementFinishedEvent extends Event {

	private final KeyAgreementResult result;

	public KeyAgreementFinishedEvent(KeyAgreementResult result) {
		this.result = result;
	}

	public KeyAgreementResult getResult() {
		return result;
	}
}
