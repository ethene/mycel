package com.quantumresearch.mycel.spore.api.mailbox;

import com.quantumresearch.mycel.spore.api.Consumer;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MailboxPairingTask extends Runnable {

	/**
	 * Adds an observer to the task. The observer will be notified on the
	 * event thread of the current state of the task and any subsequent state
	 * changes.
	 */
	void addObserver(Consumer<MailboxPairingState> observer);

	/**
	 * Removes an observer from the task.
	 */
	void removeObserver(Consumer<MailboxPairingState> observer);

}
