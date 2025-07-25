package com.quantumresearch.mycel.app.api.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.app.api.client.ProtocolStateException;
import com.quantumresearch.mycel.app.api.client.SessionId;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager.ConversationClient;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroup;
import com.quantumresearch.mycel.app.api.sharing.SharingManager.SharingStatus;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

import javax.annotation.Nullable;

@NotNullByDefault
public interface GroupInvitationManager extends ConversationClient {

	/**
	 * The unique ID of the private group invitation client.
	 */
	ClientId CLIENT_ID =
			new ClientId("com.quantumresearch.mycel.app.privategroup.invitation");

	/**
	 * The current major version of the private group invitation client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the private group invitation client.
	 */
	int MINOR_VERSION = 1;

	/**
	 * Sends an invitation to share the given private group with the given
	 * contact, including an optional message.
	 *
	 * @throws ProtocolStateException if the group is no longer eligible to be
	 * shared with the contact, for example because an invitation is already
	 * pending.
	 */
	void sendInvitation(GroupId g, ContactId c, @Nullable String text,
			long timestamp, byte[] signature, long autoDeleteTimer)
			throws DbException;

	/**
	 * Sends an invitation to share the given private group with the given
	 * contact, including an optional message.
	 *
	 * @throws ProtocolStateException if the group is no longer eligible to be
	 * shared with the contact, for example because an invitation is already
	 * pending.
	 */
	void sendInvitation(Transaction txn, GroupId g, ContactId c,
			@Nullable String text, long timestamp, byte[] signature,
			long autoDeleteTimer) throws DbException;

	/**
	 * Responds to a pending private group invitation from the given contact.
	 *
	 * @throws ProtocolStateException if the invitation is no longer pending,
	 * for example because the group has been dissolved.
	 */
	void respondToInvitation(ContactId c, PrivateGroup g, boolean accept)
			throws DbException;

	/**
	 * Responds to a pending private group invitation from the given contact.
	 *
	 * @throws ProtocolStateException if the invitation is no longer pending,
	 * for example because the group has been dissolved.
	 */
	void respondToInvitation(Transaction txn, ContactId c, PrivateGroup g,
			boolean accept) throws DbException;

	/**
	 * Responds to a pending private group invitation from the given contact.
	 *
	 * @throws ProtocolStateException if the invitation is no longer pending,
	 * for example because the group has been dissolved.
	 */
	void respondToInvitation(ContactId c, SessionId s, boolean accept)
			throws DbException;

	/**
	 * Responds to a pending private group invitation from the given contact.
	 *
	 * @throws ProtocolStateException if the invitation is no longer pending,
	 * for example because the group has been dissolved.
	 */
	void respondToInvitation(Transaction txn, ContactId c, SessionId s,
			boolean accept) throws DbException;

	/**
	 * Makes the user's relationship with the given contact visible to the
	 * given private group.
	 *
	 * @throws ProtocolStateException if the relationship is no longer eligible
	 * to be revealed, for example because the contact has revealed it.
	 */
	void revealRelationship(ContactId c, GroupId g) throws DbException;

	/**
	 * Makes the user's relationship with the given contact visible to the
	 * given private group.
	 *
	 * @throws ProtocolStateException if the relationship is no longer eligible
	 * to be revealed, for example because the contact has revealed it.
	 */
	void revealRelationship(Transaction txn, ContactId c, GroupId g)
			throws DbException;

	/**
	 * Returns all private groups to which the user has been invited.
	 */
	Collection<GroupInvitationItem> getInvitations() throws DbException;

	/**
	 * Returns all private groups to which the user has been invited.
	 */
	Collection<GroupInvitationItem> getInvitations(Transaction txn)
			throws DbException;

	/**
	 * Returns the current {@link SharingStatus} for the given {@link Contact}
	 * and {@link PrivateGroup} identified by the given {@link GroupId}.
	 * This indicates whether the {@link PrivateGroup} can be shared
	 * with the contact.
	 *
	 * @throws ProtocolStateException if {@link PrivateGroup}
	 * was already dissolved.
	 */
	SharingStatus getSharingStatus(Contact c, GroupId g) throws DbException;

	/**
	 * Returns the current {@link SharingStatus} for the given {@link Contact}
	 * and {@link PrivateGroup} identified by the given {@link GroupId}.
	 * This indicates whether the {@link PrivateGroup} can be shared
	 * with the contact.
	 *
	 * @throws ProtocolStateException if {@link PrivateGroup}
	 * was already dissolved.
	 */
	SharingStatus getSharingStatus(Transaction txn, Contact c, GroupId g)
			throws DbException;
}
