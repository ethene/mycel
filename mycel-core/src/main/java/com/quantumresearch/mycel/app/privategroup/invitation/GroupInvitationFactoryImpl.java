package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.client.ContactGroupFactory;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationManager.CLIENT_ID;
import static com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationManager.MAJOR_VERSION;

@Immutable
@NotNullByDefault
class GroupInvitationFactoryImpl implements GroupInvitationFactory {

	private final ContactGroupFactory contactGroupFactory;
	private final ClientHelper clientHelper;

	@Inject
	GroupInvitationFactoryImpl(ContactGroupFactory contactGroupFactory,
			ClientHelper clientHelper) {
		this.contactGroupFactory = contactGroupFactory;
		this.clientHelper = clientHelper;
	}

	@Override
	public byte[] signInvitation(Contact c, GroupId privateGroupId,
			long timestamp, PrivateKey privateKey) {
		AuthorId creatorId = c.getLocalAuthorId();
		AuthorId memberId = c.getAuthor().getId();
		BdfList token = createInviteToken(creatorId, memberId, privateGroupId,
				timestamp);
		try {
			return clientHelper.sign(SIGNING_LABEL_INVITE, token, privateKey);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public BdfList createInviteToken(AuthorId creatorId, AuthorId memberId,
			GroupId privateGroupId, long timestamp) {
		Group contactGroup = contactGroupFactory.createContactGroup(CLIENT_ID,
				MAJOR_VERSION, creatorId, memberId);
		return BdfList.of(
				timestamp,
				contactGroup.getId(),
				privateGroupId
		);
	}

}
