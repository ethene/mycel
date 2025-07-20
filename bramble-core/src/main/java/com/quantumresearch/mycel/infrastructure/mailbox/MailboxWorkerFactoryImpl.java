package com.quantumresearch.mycel.infrastructure.mailbox;

import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionRegistry;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxFolderId;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxUpdateManager;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.api.system.TaskScheduler;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class MailboxWorkerFactoryImpl implements MailboxWorkerFactory {

	private final Executor ioExecutor;
	private final DatabaseComponent db;
	private final Clock clock;
	private final TaskScheduler taskScheduler;
	private final EventBus eventBus;
	private final ConnectionRegistry connectionRegistry;
	private final MailboxApiCaller mailboxApiCaller;
	private final MailboxApi mailboxApi;
	private final MailboxFileManager mailboxFileManager;
	private final MailboxUpdateManager mailboxUpdateManager;

	@Inject
	MailboxWorkerFactoryImpl(@IoExecutor Executor ioExecutor,
			DatabaseComponent db,
			Clock clock,
			TaskScheduler taskScheduler,
			EventBus eventBus,
			ConnectionRegistry connectionRegistry,
			MailboxApiCaller mailboxApiCaller,
			MailboxApi mailboxApi,
			MailboxFileManager mailboxFileManager,
			MailboxUpdateManager mailboxUpdateManager) {
		this.ioExecutor = ioExecutor;
		this.db = db;
		this.clock = clock;
		this.taskScheduler = taskScheduler;
		this.eventBus = eventBus;
		this.connectionRegistry = connectionRegistry;
		this.mailboxApiCaller = mailboxApiCaller;
		this.mailboxApi = mailboxApi;
		this.mailboxFileManager = mailboxFileManager;
		this.mailboxUpdateManager = mailboxUpdateManager;
	}

	@Override
	public MailboxWorker createUploadWorker(
			ConnectivityChecker connectivityChecker,
			MailboxProperties properties, MailboxFolderId folderId,
			ContactId contactId) {
		MailboxUploadWorker worker = new MailboxUploadWorker(ioExecutor, db,
				clock, taskScheduler, eventBus, connectionRegistry,
				connectivityChecker, mailboxApiCaller, mailboxApi,
				mailboxFileManager, properties, folderId, contactId);
		eventBus.addListener(worker);
		return worker;
	}

	@Override
	public MailboxWorker createDownloadWorkerForContactMailbox(
			ConnectivityChecker connectivityChecker,
			TorReachabilityMonitor reachabilityMonitor,
			MailboxProperties properties) {
		return new ContactMailboxDownloadWorker(connectivityChecker,
				reachabilityMonitor, mailboxApiCaller, mailboxApi,
				mailboxFileManager, properties);
	}

	@Override
	public MailboxWorker createDownloadWorkerForOwnMailbox(
			ConnectivityChecker connectivityChecker,
			TorReachabilityMonitor reachabilityMonitor,
			MailboxProperties properties) {
		return new OwnMailboxDownloadWorker(connectivityChecker,
				reachabilityMonitor, mailboxApiCaller, mailboxApi,
				mailboxFileManager, properties);
	}

	@Override
	public MailboxWorker createContactListWorkerForOwnMailbox(
			ConnectivityChecker connectivityChecker,
			MailboxProperties properties) {
		OwnMailboxContactListWorker worker = new OwnMailboxContactListWorker(
				ioExecutor, db, eventBus, connectivityChecker, mailboxApiCaller,
				mailboxApi, mailboxUpdateManager, properties);
		eventBus.addListener(worker);
		return worker;
	}
}
