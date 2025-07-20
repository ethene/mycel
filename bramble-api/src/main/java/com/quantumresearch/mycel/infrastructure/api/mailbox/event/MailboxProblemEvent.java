package com.quantumresearch.mycel.infrastructure.api.mailbox.event;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxSettingsManager;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast by {@link MailboxSettingsManager} when
 * recording a connection failure for own Mailbox
 * that has persistent for long enough for the mailbox owner to become active
 * and fix the problem with the mailbox.
 */
@Immutable
@NotNullByDefault
public class MailboxProblemEvent extends Event {

}
