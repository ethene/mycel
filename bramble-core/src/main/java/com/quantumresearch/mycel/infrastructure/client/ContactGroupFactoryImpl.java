package com.quantumresearch.mycel.infrastructure.client;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.client.ContactGroupFactory;
import com.quantumresearch.mycel.infrastructure.api.contact.Contact;
import com.quantumresearch.mycel.infrastructure.api.data.BdfList;
import com.quantumresearch.mycel.infrastructure.api.identity.AuthorId;
import com.quantumresearch.mycel.infrastructure.api.sync.ClientId;
import com.quantumresearch.mycel.infrastructure.api.sync.Group;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class ContactGroupFactoryImpl implements ContactGroupFactory {

	private static final byte[] LOCAL_GROUP_DESCRIPTOR = new byte[0];

	private final GroupFactory groupFactory;
	private final ClientHelper clientHelper;

	@Inject
	ContactGroupFactoryImpl(GroupFactory groupFactory,
			ClientHelper clientHelper) {
		this.groupFactory = groupFactory;
		this.clientHelper = clientHelper;
	}

	@Override
	public Group createLocalGroup(ClientId clientId, int majorVersion) {
		return groupFactory.createGroup(clientId, majorVersion,
				LOCAL_GROUP_DESCRIPTOR);
	}

	@Override
	public Group createContactGroup(ClientId clientId, int majorVersion,
			Contact contact) {
		AuthorId local = contact.getLocalAuthorId();
		AuthorId remote = contact.getAuthor().getId();
		byte[] descriptor = createGroupDescriptor(local, remote);
		return groupFactory.createGroup(clientId, majorVersion, descriptor);
	}

	@Override
	public Group createContactGroup(ClientId clientId, int majorVersion,
			AuthorId authorId1, AuthorId authorId2) {
		byte[] descriptor = createGroupDescriptor(authorId1, authorId2);
		return groupFactory.createGroup(clientId, majorVersion, descriptor);
	}

	private byte[] createGroupDescriptor(AuthorId local, AuthorId remote) {
		try {
			if (local.compareTo(remote) < 0)
				return clientHelper.toByteArray(BdfList.of(local, remote));
			else return clientHelper.toByteArray(BdfList.of(remote, local));
		} catch (FormatException e) {
			throw new RuntimeException(e);
		}
	}
}
