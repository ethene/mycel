package com.quantumresearch.mycel.infrastructure.mailbox;

import com.quantumresearch.mycel.infrastructure.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.event.EventExecutor;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxPairingTask;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxSettingsManager;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxUpdateManager;
import com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class MailboxPairingTaskFactoryImpl implements MailboxPairingTaskFactory {

	private final Executor eventExecutor;
	private final DatabaseComponent db;
	private final CryptoComponent crypto;
	private final Clock clock;
	private final MailboxApi api;
	private final MailboxSettingsManager mailboxSettingsManager;
	private final MailboxUpdateManager mailboxUpdateManager;
	private final QrCodeClassifier qrCodeClassifier;

	@Inject
	MailboxPairingTaskFactoryImpl(
			@EventExecutor Executor eventExecutor,
			DatabaseComponent db,
			CryptoComponent crypto,
			Clock clock,
			MailboxApi api,
			MailboxSettingsManager mailboxSettingsManager,
			MailboxUpdateManager mailboxUpdateManager,
			QrCodeClassifier qrCodeClassifier) {
		this.eventExecutor = eventExecutor;
		this.db = db;
		this.crypto = crypto;
		this.clock = clock;
		this.api = api;
		this.mailboxSettingsManager = mailboxSettingsManager;
		this.mailboxUpdateManager = mailboxUpdateManager;
		this.qrCodeClassifier = qrCodeClassifier;
	}

	@Override
	public MailboxPairingTask createPairingTask(String qrCodePayload) {
		return new MailboxPairingTaskImpl(qrCodePayload, eventExecutor, db,
				crypto, clock, api, mailboxSettingsManager,
				mailboxUpdateManager, qrCodeClassifier);
	}
}
