package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.app.api.sharing.Shareable;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
interface ProtocolEngine<S extends Shareable> {

	Session onInviteAction(Transaction txn, Session session,
			@Nullable String text) throws DbException;

	Session onAcceptAction(Transaction txn, Session session) throws DbException;

	Session onDeclineAction(Transaction txn, Session session,
			boolean isAutoDecline) throws DbException;

	Session onLeaveAction(Transaction txn, Session session) throws DbException;

	Session onInviteMessage(Transaction txn, Session session,
			InviteMessage<S> m) throws DbException, FormatException;

	Session onAcceptMessage(Transaction txn, Session session, AcceptMessage m)
			throws DbException, FormatException;

	Session onDeclineMessage(Transaction txn, Session session, DeclineMessage m)
			throws DbException, FormatException;

	Session onLeaveMessage(Transaction txn, Session session, LeaveMessage m)
			throws DbException, FormatException;

	Session onAbortMessage(Transaction txn, Session session, AbortMessage m)
			throws DbException, FormatException;

}
