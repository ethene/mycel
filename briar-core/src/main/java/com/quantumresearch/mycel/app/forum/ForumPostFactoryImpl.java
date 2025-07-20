package com.quantumresearch.mycel.app.forum;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.data.BdfList;
import com.quantumresearch.mycel.infrastructure.api.identity.LocalAuthor;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.forum.ForumPost;
import com.quantumresearch.mycel.app.api.forum.ForumPostFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.util.StringUtils.utf8IsTooLong;
import static com.quantumresearch.mycel.app.api.forum.ForumConstants.MAX_FORUM_POST_TEXT_LENGTH;

@Immutable
@NotNullByDefault
class ForumPostFactoryImpl implements ForumPostFactory {

	private final ClientHelper clientHelper;

	@Inject
	ForumPostFactoryImpl(ClientHelper clientHelper) {
		this.clientHelper = clientHelper;
	}

	@Override
	public ForumPost createPost(GroupId groupId, long timestamp,
			@Nullable MessageId parent, LocalAuthor author, String text)
			throws FormatException, GeneralSecurityException {
		// Validate the arguments
		if (utf8IsTooLong(text, MAX_FORUM_POST_TEXT_LENGTH))
			throw new IllegalArgumentException();
		// Serialise the data to be signed
		BdfList authorList = clientHelper.toList(author);
		BdfList signed = BdfList.of(groupId, timestamp, parent, authorList,
				text);
		// Sign the data
		byte[] sig = clientHelper.sign(SIGNING_LABEL_POST, signed,
				author.getPrivateKey());
		// Serialise the signed message
		BdfList message = BdfList.of(parent, authorList, text, sig);
		Message m = clientHelper.createMessage(groupId, timestamp, message);
		return new ForumPost(m, parent, author);
	}

}
