package com.quantumresearch.mycel.app.privategroup;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.privategroup.GroupMessage;
import com.quantumresearch.mycel.app.api.privategroup.GroupMessageFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.app.api.privategroup.MessageType.JOIN;
import static com.quantumresearch.mycel.app.api.privategroup.MessageType.POST;

@Immutable
@NotNullByDefault
class GroupMessageFactoryImpl implements GroupMessageFactory {

	private final ClientHelper clientHelper;

	@Inject
	GroupMessageFactoryImpl(ClientHelper clientHelper) {
		this.clientHelper = clientHelper;
	}

	@Override
	public GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor creator) {

		return createJoinMessage(groupId, timestamp, creator, null);
	}

	@Override
	public GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor member, long inviteTimestamp, byte[] creatorSignature) {

		BdfList invite = BdfList.of(inviteTimestamp, creatorSignature);
		return createJoinMessage(groupId, timestamp, member, invite);
	}

	private GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor member, @Nullable BdfList invite) {
		try {
			// Generate the signature
			BdfList memberList = clientHelper.toList(member);
			BdfList toSign = BdfList.of(
					groupId,
					timestamp,
					memberList,
					invite
			);
			byte[] memberSignature = clientHelper.sign(SIGNING_LABEL_JOIN,
					toSign, member.getPrivateKey());

			// Compose the message
			BdfList body = BdfList.of(
					JOIN.getInt(),
					memberList,
					invite,
					memberSignature
			);
			Message m = clientHelper.createMessage(groupId, timestamp, body);
			return new GroupMessage(m, null, member);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public GroupMessage createGroupMessage(GroupId groupId, long timestamp,
			@Nullable MessageId parentId, LocalAuthor member, String text,
			MessageId previousMsgId) {
		try {
			// Generate the signature
			BdfList memberList = clientHelper.toList(member);
			BdfList toSign = BdfList.of(
					groupId,
					timestamp,
					memberList,
					parentId,
					previousMsgId,
					text
			);
			byte[] signature = clientHelper.sign(SIGNING_LABEL_POST, toSign,
					member.getPrivateKey());

			// Compose the message
			BdfList body = BdfList.of(
					POST.getInt(),
					memberList,
					parentId,
					previousMsgId,
					text,
					signature
			);
			Message m = clientHelper.createMessage(groupId, timestamp, body);
			return new GroupMessage(m, parentId, member);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

}
