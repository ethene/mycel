package com.quantumresearch.mycel.app.api.forum;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.crypto.CryptoExecutor;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.Nullable;

import static com.quantumresearch.mycel.app.api.forum.ForumManager.CLIENT_ID;

@NotNullByDefault
public interface ForumPostFactory {

	String SIGNING_LABEL_POST = CLIENT_ID.getString() + "/POST";

	@CryptoExecutor
	ForumPost createPost(GroupId groupId, long timestamp,
			@Nullable MessageId parent, LocalAuthor author, String text)
			throws FormatException, GeneralSecurityException;

}
