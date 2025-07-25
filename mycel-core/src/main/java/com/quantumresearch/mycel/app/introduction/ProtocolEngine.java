package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
interface ProtocolEngine<S extends Session<?>> {

	S onRequestAction(Transaction txn, S session, @Nullable String text)
			throws DbException;

	S onAcceptAction(Transaction txn, S session) throws DbException;

	/**
	 * Declines an introduction.
	 *
	 * @param isAutoDecline true if automatically declined due to deletion
	 * and false if initiated by the user.
	 */
	S onDeclineAction(Transaction txn, S session, boolean isAutoDecline)
			throws DbException;

	S onRequestMessage(Transaction txn, S session, RequestMessage m)
			throws DbException, FormatException;

	S onAcceptMessage(Transaction txn, S session, AcceptMessage m)
			throws DbException, FormatException;

	S onDeclineMessage(Transaction txn, S session, DeclineMessage m)
			throws DbException, FormatException;

	S onAuthMessage(Transaction txn, S session, AuthMessage m)
			throws DbException, FormatException;

	S onActivateMessage(Transaction txn, S session, ActivateMessage m)
			throws DbException, FormatException;

	S onAbortMessage(Transaction txn, S session, AbortMessage m)
			throws DbException, FormatException;

}
