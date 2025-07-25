package com.quantumresearch.mycel.app.api.privategroup;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.MessageTracker.GroupCount;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.List;

@NotNullByDefault
public interface PrivateGroupManager {

	/**
	 * The unique ID of the private group client.
	 */
	ClientId CLIENT_ID = new ClientId("com.quantumresearch.mycel.app.privategroup");

	/**
	 * The current major version of the private group client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the private group client.
	 */
	int MINOR_VERSION = 0;

	/**
	 * Adds a new private group and joins it.
	 *
	 * @param group The private group to add
	 * @param joinMsg The new member's join message
	 * @param creator True if the group is added by its creator
	 */
	void addPrivateGroup(PrivateGroup group, GroupMessage joinMsg,
			boolean creator) throws DbException;

	/**
	 * Adds a new private group and joins it.
	 *
	 * @param group The private group to add
	 * @param joinMsg The new member's join message
	 * @param creator True if the group is added by its creator
	 */
	void addPrivateGroup(Transaction txn, PrivateGroup group,
			GroupMessage joinMsg, boolean creator) throws DbException;

	/**
	 * Removes a dissolved private group.
	 */
	void removePrivateGroup(Transaction txn, GroupId g) throws DbException;

	/**
	 * Removes a dissolved private group.
	 */
	void removePrivateGroup(GroupId g) throws DbException;

	/**
	 * Returns the ID of the user's previous message sent to the group
	 */
	MessageId getPreviousMsgId(Transaction txn, GroupId g) throws DbException;

	/**
	 * Returns the ID of the user's previous message sent to the group
	 */
	MessageId getPreviousMsgId(GroupId g) throws DbException;

	/**
	 * Marks the given private group as dissolved.
	 */
	void markGroupDissolved(Transaction txn, GroupId g) throws DbException;

	/**
	 * Returns true if the given private group has been dissolved.
	 */
	boolean isDissolved(Transaction txn, GroupId g) throws DbException;

	/**
	 * Returns true if the given private group has been dissolved.
	 */
	boolean isDissolved(GroupId g) throws DbException;

	/**
	 * Stores and sends a local private group message.
	 */
	GroupMessageHeader addLocalMessage(GroupMessage p) throws DbException;

	/**
	 * Stores and sends a local private group message.
	 */
	GroupMessageHeader addLocalMessage(Transaction txn, GroupMessage p)
			throws DbException;

	/**
	 * Returns the private group with the given ID.
	 */
	PrivateGroup getPrivateGroup(GroupId g) throws DbException;

	/**
	 * Returns the private group with the given ID.
	 */
	PrivateGroup getPrivateGroup(Transaction txn, GroupId g) throws DbException;

	/**
	 * Returns all private groups the user is a member of.
	 */
	Collection<PrivateGroup> getPrivateGroups() throws DbException;

	/**
	 * Returns all private groups the user is a member of.
	 */
	Collection<PrivateGroup> getPrivateGroups(Transaction txn)
			throws DbException;

	/**
	 * Returns true if the given private group was created by us.
	 */
	boolean isOurPrivateGroup(Transaction txn, PrivateGroup g)
			throws DbException;

	/**
	 * Returns the text of the private group message with the given ID.
	 */
	String getMessageText(MessageId m) throws DbException;

	/**
	 * Returns the text of the private group message with the given ID.
	 */
	String getMessageText(Transaction txn, MessageId m) throws DbException;

	/**
	 * Returns the headers of all messages in the given private group.
	 */
	Collection<GroupMessageHeader> getHeaders(GroupId g) throws DbException;

	/**
	 * Returns the headers of all messages in the given private group.
	 */
	List<GroupMessageHeader> getHeaders(Transaction txn, GroupId g)
			throws DbException;

	/**
	 * Returns all members of the given private group.
	 */
	Collection<GroupMember> getMembers(GroupId g) throws DbException;

	/**
	 * Returns all members of the given private group.
	 */
	Collection<GroupMember> getMembers(Transaction txn, GroupId g)
			throws DbException;

	/**
	 * Returns true if the given author is a member of the given private group.
	 */
	boolean isMember(Transaction txn, GroupId g, Author a) throws DbException;

	/**
	 * Returns the group count for the given private group.
	 */
	GroupCount getGroupCount(Transaction txn, GroupId g) throws DbException;

	/**
	 * Returns the group count for the given private group.
	 */
	GroupCount getGroupCount(GroupId g) throws DbException;

	/**
	 * Marks a message as read or unread and updates the group count.
	 */
	void setReadFlag(Transaction txn, GroupId g, MessageId m, boolean read)
			throws DbException;

	/**
	 * Marks a message as read or unread and updates the group count.
	 */
	void setReadFlag(GroupId g, MessageId m, boolean read) throws DbException;

	/**
	 * Called when a contact relationship has been revealed between the user
	 * and the given author in the given private group.
	 *
	 * @param byContact True if the contact revealed the relationship first,
	 * otherwise false.
	 */
	void relationshipRevealed(Transaction txn, GroupId g, AuthorId a,
			boolean byContact) throws FormatException, DbException;

	/**
	 * Registers a hook to be called when members are added or private groups
	 * are removed.
	 */
	void registerPrivateGroupHook(PrivateGroupHook hook);

	@NotNullByDefault
	interface PrivateGroupHook {

		/**
		 * Called when a member is being added to a private group.
		 *
		 * @param txn A read-write transaction
		 * @param g The ID of the private group
		 * @param a The member that is being added
		 */
		void addingMember(Transaction txn, GroupId g, Author a)
				throws DbException;

		/**
		 * Called when a private group is being removed.
		 *
		 * @param txn A read-write transaction
		 * @param g The ID of the private group that is being removed
		 */
		void removingGroup(Transaction txn, GroupId g) throws DbException;

	}

}
