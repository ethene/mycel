package com.quantumresearch.mycel.infrastructure.api.mailbox.event;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxSettingsManager;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxStatus;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast by {@link MailboxSettingsManager} when
 * recording the connection status of own Mailbox.
 */
@Immutable
@NotNullByDefault
public class OwnMailboxConnectionStatusEvent extends Event {

	private final MailboxStatus status;

	public OwnMailboxConnectionStatusEvent(MailboxStatus status) {
		this.status = status;
	}

	public MailboxStatus getStatus() {
		return status;
	}
}
