package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.api.mailbox.MailboxFolderId;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.spore.mailbox.MailboxApi.ApiException;
import com.quantumresearch.mycel.spore.mailbox.MailboxApi.MailboxFile;
import com.quantumresearch.mycel.spore.mailbox.MailboxApi.TolerableFailureException;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.annotation.concurrent.ThreadSafe;

import static java.util.Collections.emptyList;
import static org.briarproject.nullsafety.NullSafety.requireNonNull;

@ThreadSafe
@NotNullByDefault
class ContactMailboxDownloadWorker extends MailboxDownloadWorker {

	ContactMailboxDownloadWorker(
			ConnectivityChecker connectivityChecker,
			TorReachabilityMonitor torReachabilityMonitor,
			MailboxApiCaller mailboxApiCaller,
			MailboxApi mailboxApi,
			MailboxFileManager mailboxFileManager,
			MailboxProperties mailboxProperties) {
		super(connectivityChecker, torReachabilityMonitor, mailboxApiCaller,
				mailboxApi, mailboxFileManager, mailboxProperties);
		if (mailboxProperties.isOwner()) throw new IllegalArgumentException();
	}

	@Override
	protected ApiCall createApiCallForDownloadCycle() {
		return new SimpleApiCall(this::apiCallListInbox);
	}

	private void apiCallListInbox() throws IOException, ApiException {
		synchronized (lock) {
			if (state == State.DESTROYED) return;
		}
		LOG.info("Listing inbox");
		MailboxFolderId folderId =
				requireNonNull(mailboxProperties.getInboxId());
		List<MailboxFile> files;
		try {
			files = mailboxApi.getFiles(mailboxProperties, folderId);
		} catch (TolerableFailureException e) {
			LOG.warning("Inbox folder does not exist");
			files = emptyList();
		}
		if (files.isEmpty()) {
			onDownloadCycleFinished();
		} else {
			Queue<FolderFile> queue = new LinkedList<>();
			for (MailboxFile file : files) {
				queue.add(new FolderFile(folderId, file.name));
			}
			downloadNextFile(queue);
		}
	}
}
