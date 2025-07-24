package com.quantumresearch.mycel.app.api.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.crypto.CryptoExecutor;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import org.briarproject.nullsafety.NotNullByDefault;

import static com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationManager.CLIENT_ID;

@NotNullByDefault
public interface GroupInvitationFactory {

	String SIGNING_LABEL_INVITE = CLIENT_ID.getString() + "/INVITE";

	/**
	 * Returns a signature to include when inviting a member to join a private
	 * group. If the member accepts the invitation, the signature will be
	 * included in the member's join message.
	 */
	@CryptoExecutor
	byte[] signInvitation(Contact c, GroupId privateGroupId, long timestamp,
			PrivateKey privateKey);

	/**
	 * Returns a token to be signed by the creator when inviting a member to
	 * join a private group. If the member accepts the invitation, the
	 * signature will be included in the member's join message.
	 */
	BdfList createInviteToken(AuthorId creatorId, AuthorId memberId,
			GroupId privateGroupId, long timestamp);

}
