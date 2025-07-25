package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.api.Cancellable;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxFileId;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxFolderId;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.spore.mailbox.MailboxApi.MailboxFile;
import com.quantumresearch.mycel.spore.mailbox.MailboxApi.TolerableFailureException;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import com.quantumresearch.mycel.spore.test.CaptureArgumentAction;
import org.jmock.Expectations;
import org.jmock.lib.action.DoAllAction;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static com.quantumresearch.mycel.spore.test.TestUtils.deleteTestDirectory;
import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.spore.test.TestUtils.getTestDirectory;

abstract class MailboxDownloadWorkerTest<W extends MailboxDownloadWorker>
		extends BrambleMockTestCase {

	final ConnectivityChecker connectivityChecker =
			context.mock(ConnectivityChecker.class);
	final TorReachabilityMonitor torReachabilityMonitor =
			context.mock(TorReachabilityMonitor.class);
	final MailboxApiCaller mailboxApiCaller =
			context.mock(MailboxApiCaller.class);
	final MailboxApi mailboxApi = context.mock(MailboxApi.class);
	final MailboxFileManager mailboxFileManager =
			context.mock(MailboxFileManager.class);
	private final Cancellable apiCall = context.mock(Cancellable.class);

	private final long now = System.currentTimeMillis();
	final MailboxFile file1 =
			new MailboxFile(new MailboxFileId(getRandomId()), now - 1);
	final MailboxFile file2 =
			new MailboxFile(new MailboxFileId(getRandomId()), now);
	final List<MailboxFile> files = asList(file1, file2);

	private File testDir, tempFile;
	MailboxProperties mailboxProperties;
	W worker;

	@Before
	public void setUp() {
		testDir = getTestDirectory();
		tempFile = new File(testDir, "temp");
	}

	@After
	public void tearDown() {
		deleteTestDirectory(testDir);
	}


	void expectStartConnectivityCheck() {
		context.checking(new Expectations() {{
			oneOf(connectivityChecker).checkConnectivity(
					with(mailboxProperties), with(worker));
		}});
	}

	void expectStartTask(AtomicReference<ApiCall> task) {
		context.checking(new Expectations() {{
			oneOf(mailboxApiCaller).retryWithBackoff(with(any(ApiCall.class)));
			will(new DoAllAction(
					new CaptureArgumentAction<>(task, ApiCall.class, 0),
					returnValue(apiCall)
			));
		}});
	}

	void expectCheckForFoldersWithAvailableFiles(
			List<MailboxFolderId> folderIds) throws Exception {
		context.checking(new Expectations() {{
			oneOf(mailboxApi).getFolders(mailboxProperties);
			will(returnValue(folderIds));
		}});
	}

	void expectCheckForFiles(MailboxFolderId folderId,
			List<MailboxFile> files) throws Exception {
		context.checking(new Expectations() {{
			oneOf(mailboxApi).getFiles(mailboxProperties, folderId);
			will(returnValue(files));
		}});
	}

	void expectDownloadFile(MailboxFolderId folderId,
			MailboxFile file)
			throws Exception {
		context.checking(new Expectations() {{
			oneOf(mailboxFileManager).createTempFileForDownload();
			will(returnValue(tempFile));
			oneOf(mailboxApi).getFile(mailboxProperties, folderId, file.name,
					tempFile);
			oneOf(mailboxFileManager).handleDownloadedFile(tempFile);
		}});
	}

	void expectDeleteFile(MailboxFolderId folderId, MailboxFile file,
			boolean tolerableFailure) throws Exception {
		context.checking(new Expectations() {{
			oneOf(mailboxApi).deleteFile(mailboxProperties, folderId,
					file.name);
			if (tolerableFailure) {
				will(throwException(new TolerableFailureException()));
			}
		}});
	}

	void expectAddReachabilityObserver() {
		context.checking(new Expectations() {{
			oneOf(torReachabilityMonitor).addOneShotObserver(worker);
		}});
	}

	void expectRemoveObservers() {
		context.checking(new Expectations() {{
			oneOf(connectivityChecker).removeObserver(worker);
			oneOf(torReachabilityMonitor).removeObserver(worker);
		}});
	}
}
