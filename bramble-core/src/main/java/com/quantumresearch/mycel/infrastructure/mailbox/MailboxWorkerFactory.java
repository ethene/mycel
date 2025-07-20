package com.quantumresearch.mycel.infrastructure.mailbox;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxFolderId;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
interface MailboxWorkerFactory {

	MailboxWorker createUploadWorker(ConnectivityChecker connectivityChecker,
			MailboxProperties properties, MailboxFolderId folderId,
			ContactId contactId);

	MailboxWorker createDownloadWorkerForContactMailbox(
			ConnectivityChecker connectivityChecker,
			TorReachabilityMonitor reachabilityMonitor,
			MailboxProperties properties);

	MailboxWorker createDownloadWorkerForOwnMailbox(
			ConnectivityChecker connectivityChecker,
			TorReachabilityMonitor reachabilityMonitor,
			MailboxProperties properties);

	MailboxWorker createContactListWorkerForOwnMailbox(
			ConnectivityChecker connectivityChecker,
			MailboxProperties properties);
}
