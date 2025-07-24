package com.quantumresearch.mycel.spore.mailbox;


import com.quantumresearch.mycel.spore.api.mailbox.MailboxPairingTask;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface MailboxPairingTaskFactory {

	MailboxPairingTask createPairingTask(String qrCodePayload);

}
