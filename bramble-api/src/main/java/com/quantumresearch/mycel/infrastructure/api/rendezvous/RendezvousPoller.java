package com.quantumresearch.mycel.infrastructure.api.rendezvous;

import com.quantumresearch.mycel.infrastructure.api.contact.PendingContactId;

/**
 * Interface for the poller that makes rendezvous connections to pending
 * contacts.
 */
public interface RendezvousPoller {

	long getLastPollTime(PendingContactId p);
}
