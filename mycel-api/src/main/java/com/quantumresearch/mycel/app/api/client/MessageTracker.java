package com.quantumresearch.mycel.app.api.client;

import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
public interface MessageTracker {

	/**
	 * Initializes the group count with zero messages,
	 * but uses the current time as latest message time for sorting.
	 */
	void initializeGroupCount(Transaction txn, GroupId g) throws DbException;

	/**
	 * Gets the number of visible and unread messages in the group
	 * as well as the timestamp of the latest message
	 **/
	GroupCount getGroupCount(GroupId g) throws DbException;

	/**
	 * Gets the number of visible and unread messages in the group
	 * as well as the timestamp of the latest message
	 **/
	GroupCount getGroupCount(Transaction txn, GroupId g) throws DbException;

	/**
	 * Updates the group count for the given incoming message.
	 * <p>
	 * For messages that are part of a conversation (private chat),
	 * use the corresponding function inside
	 * {@link ConversationManager} instead.
	 */
	void trackIncomingMessage(Transaction txn, Message m) throws DbException;

	/**
	 * Updates the group count for the given outgoing message.
	 * <p>
	 * For messages that are part of a conversation (private chat),
	 * use the corresponding function inside
	 * {@link ConversationManager} instead.
	 */
	void trackOutgoingMessage(Transaction txn, Message m) throws DbException;

	/**
	 * Updates the group count for the given message.
	 * <p>
	 * For messages that are part of a conversation (private chat),
	 * use the corresponding function inside
	 * {@link ConversationManager} instead.
	 */
	void trackMessage(Transaction txn, GroupId g, long timestamp, boolean read)
			throws DbException;

	/**
	 * Loads the stored message id for the respective group id or returns null
	 * if none is available.
	 */
	@Nullable
	MessageId loadStoredMessageId(GroupId g) throws DbException;

	/**
	 * Stores the message id for the respective group id. Exactly one message id
	 * can be stored for any group id at any time, older values are overwritten.
	 */
	void storeMessageId(GroupId g, MessageId m) throws DbException;

	/**
	 * Marks a message as read or unread and updates the group count.
	 *
	 * @return True if the message was previously marked as read
	 */
	boolean setReadFlag(Transaction txn, GroupId g, MessageId m, boolean read)
			throws DbException;

	/**
	 * Resets the {@link GroupCount} to the given msgCount and unreadCount.
	 * The latestMsgTime will be set to the current time.
	 * <p>
	 * Such reset is needed when recalculating the counts
	 * after deleting messages from a group.
	 */
	void resetGroupCount(Transaction txn, GroupId g, int msgCount,
			int unreadCount) throws DbException;

	class GroupCount {

		private final int msgCount, unreadCount;
		private final long latestMsgTime;

		public GroupCount(int msgCount, int unreadCount, long latestMsgTime) {
			this.msgCount = msgCount;
			this.unreadCount = unreadCount;
			this.latestMsgTime = latestMsgTime;
		}

		public int getMsgCount() {
			return msgCount;
		}

		public int getUnreadCount() {
			return unreadCount;
		}

		public long getLatestMsgTime() {
			return latestMsgTime;
		}
	}

}
