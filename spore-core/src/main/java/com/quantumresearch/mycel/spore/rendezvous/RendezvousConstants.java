package com.quantumresearch.mycel.spore.rendezvous;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;

interface RendezvousConstants {

	/**
	 * The current version of the rendezvous protocol.
	 */
	byte PROTOCOL_VERSION = 0;

	/**
	 * How long to try to rendezvous with a pending contact before giving up.
	 */
	long RENDEZVOUS_TIMEOUT_MS = DAYS.toMillis(2);

	/**
	 * How often to try to rendezvous with pending contacts.
	 */
	long POLLING_INTERVAL_MS = MINUTES.toMillis(1);

	/**
	 * Label for deriving the rendezvous key from the static master key.
	 */
	String RENDEZVOUS_KEY_LABEL =
			"com.quantumresearch.mycel.spore.rendezvous/RENDEZVOUS_KEY";

	/**
	 * Label for deriving key material from the rendezvous key.
	 */
	String KEY_MATERIAL_LABEL =
			"com.quantumresearch.mycel.spore.rendezvous/KEY_MATERIAL";
}
