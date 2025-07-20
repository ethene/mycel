package com.quantumresearch.mycel.infrastructure.mailbox;


import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingTask;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface MailboxPairingTaskFactory {

	MailboxPairingTask createPairingTask(String qrCodePayload);

}
