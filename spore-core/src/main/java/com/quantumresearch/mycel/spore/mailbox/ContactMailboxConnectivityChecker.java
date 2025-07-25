package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.mailbox.MailboxApi.ApiException;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.logging.Logger;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static java.util.logging.Logger.getLogger;

@ThreadSafe
@NotNullByDefault
class ContactMailboxConnectivityChecker extends ConnectivityCheckerImpl {

	private static final Logger LOG =
			getLogger(ContactMailboxConnectivityChecker.class.getName());

	private final MailboxApi mailboxApi;

	@Inject
	ContactMailboxConnectivityChecker(Clock clock,
			MailboxApiCaller mailboxApiCaller, MailboxApi mailboxApi) {
		super(clock, mailboxApiCaller);
		this.mailboxApi = mailboxApi;
	}

	@Override
	ApiCall createConnectivityCheckTask(MailboxProperties properties) {
		if (properties.isOwner()) throw new IllegalArgumentException();
		return new SimpleApiCall(() -> {
			LOG.info("Checking connectivity of contact's mailbox");
			if (!mailboxApi.checkStatus(properties)) throw new ApiException();
			LOG.info("Contact's mailbox is reachable");
			// Call the observers and cache the result
			onConnectivityCheckSucceeded(clock.currentTimeMillis());
		});
	}

}
