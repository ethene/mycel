package com.quantumresearch.mycel.spore.api.rendezvous;

import com.quantumresearch.mycel.spore.api.contact.PendingContactId;

/**
 * Interface for the poller that makes rendezvous connections to pending
 * contacts.
 */
public interface RendezvousPoller {

	long getLastPollTime(PendingContactId p);
}
