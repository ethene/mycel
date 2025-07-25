package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.mailbox.MailboxApi.TolerableFailureException;

/**
 * An interface for calling an API endpoint with the option to retry the call.
 */
interface ApiCall {

	/**
	 * This method makes a synchronous call to an API endpoint and returns
	 * true if the call should be retried, in which case the method may be
	 * called again on the same {@link ApiCall} instance after a delay.
	 *
	 * @return True if the API call needs to be retried, or false if the API
	 * call succeeded or {@link TolerableFailureException failed tolerably}.
	 */
	@IoExecutor
	boolean callApi();
}
