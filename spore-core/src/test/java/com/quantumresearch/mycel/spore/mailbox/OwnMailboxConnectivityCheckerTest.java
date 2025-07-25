package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.api.Cancellable;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.db.TransactionManager;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxSettingsManager;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxVersion;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.mailbox.ConnectivityChecker.ConnectivityObserver;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import com.quantumresearch.mycel.spore.test.CaptureArgumentAction;
import com.quantumresearch.mycel.spore.test.DbExpectations;
import org.jmock.Expectations;
import org.jmock.lib.action.DoAllAction;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxConstants.CLIENT_SUPPORTS;
import static com.quantumresearch.mycel.spore.test.TestUtils.getMailboxProperties;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OwnMailboxConnectivityCheckerTest extends BrambleMockTestCase {

	private final Clock clock = context.mock(Clock.class);
	private final MailboxApiCaller mailboxApiCaller =
			context.mock(MailboxApiCaller.class);
	private final MailboxApi mailboxApi = context.mock(MailboxApi.class);
	private final TransactionManager db =
			context.mock(TransactionManager.class);
	private final MailboxSettingsManager mailboxSettingsManager =
			context.mock(MailboxSettingsManager.class);
	private final Cancellable task = context.mock(Cancellable.class);
	private final ConnectivityObserver observer =
			context.mock(ConnectivityObserver.class);

	private final MailboxProperties properties =
			getMailboxProperties(true, CLIENT_SUPPORTS);
	private final long now = System.currentTimeMillis();
	private final List<MailboxVersion> serverSupports =
			singletonList(new MailboxVersion(123, 456));

	@Test
	public void testObserverIsCalledWhenCheckSucceeds() throws Exception {
		OwnMailboxConnectivityChecker checker = createChecker();
		AtomicReference<ApiCall> apiCall = new AtomicReference<>(null);
		Transaction txn = new Transaction(null, false);

		// When checkConnectivity() is called a check should be started
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(mailboxApiCaller).retryWithBackoff(with(any(ApiCall.class)));
			will(new DoAllAction(
					new CaptureArgumentAction<>(apiCall, ApiCall.class, 0),
					returnValue(task)
			));
		}});

		checker.checkConnectivity(properties, observer);

		// When the check succeeds, the success should be recorded in the DB
		// and the observer should be called
		context.checking(new DbExpectations() {{
			oneOf(mailboxApi).getServerSupports(properties);
			will(returnValue(serverSupports));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(db).transaction(with(false), withDbRunnable(txn));
			oneOf(mailboxSettingsManager).recordSuccessfulConnection(txn, now,
					serverSupports);
			oneOf(observer).onConnectivityCheckSucceeded();
		}});

		// The call should not be retried
		assertFalse(apiCall.get().callApi());
	}

	@Test
	public void testObserverIsNotCalledWhenCheckFails() throws Exception {
		OwnMailboxConnectivityChecker checker = createChecker();
		AtomicReference<ApiCall> apiCall = new AtomicReference<>(null);
		Transaction txn = new Transaction(null, false);

		// When checkConnectivity() is called a check should be started
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(mailboxApiCaller).retryWithBackoff(with(any(ApiCall.class)));
			will(new DoAllAction(
					new CaptureArgumentAction<>(apiCall, ApiCall.class, 0),
					returnValue(task)
			));
		}});

		checker.checkConnectivity(properties, observer);

		// When the check fails, the failure should be recorded in the DB and
		// the observer should not be called
		context.checking(new DbExpectations() {{
			oneOf(mailboxApi).getServerSupports(properties);
			will(throwException(new IOException()));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(db).transaction(with(false), withDbRunnable(txn));
			oneOf(mailboxSettingsManager)
					.recordFailedConnectionAttempt(txn, now);
		}});

		// The call should be retried
		assertTrue(apiCall.get().callApi());
	}

	private OwnMailboxConnectivityChecker createChecker() {
		return new OwnMailboxConnectivityChecker(clock, mailboxApiCaller,
				mailboxApi, db, mailboxSettingsManager);
	}
}
