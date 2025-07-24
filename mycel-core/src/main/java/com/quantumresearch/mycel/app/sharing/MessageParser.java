package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.sharing.Shareable;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface MessageParser<S extends Shareable> {

	BdfDictionary getMessagesVisibleInUiQuery();

	BdfDictionary getInvitesAvailableToAnswerQuery();

	BdfDictionary getInvitesAvailableToAnswerQuery(GroupId shareableId);

	MessageMetadata parseMetadata(BdfDictionary meta) throws FormatException;

	S createShareable(BdfList descriptor) throws FormatException;

	InviteMessage<S> getInviteMessage(Transaction txn, MessageId m)
			throws DbException, FormatException;

	InviteMessage<S> parseInviteMessage(Message m, BdfList body)
			throws FormatException;

	AcceptMessage parseAcceptMessage(Message m, BdfList body)
			throws FormatException;

	DeclineMessage parseDeclineMessage(Message m, BdfList body)
			throws FormatException;

	LeaveMessage parseLeaveMessage(Message m, BdfList body)
			throws FormatException;

	AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException;

}
