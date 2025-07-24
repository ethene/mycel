package com.quantumresearch.mycel.app.forum;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.UniqueId;
import com.quantumresearch.mycel.spore.api.client.BdfMessageContext;
import com.quantumresearch.mycel.spore.api.client.BdfMessageValidator;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.InvalidMessageException;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_SIGNATURE_LENGTH;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkLength;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkSize;
import static com.quantumresearch.mycel.app.api.forum.ForumConstants.KEY_AUTHOR;
import static com.quantumresearch.mycel.app.api.forum.ForumConstants.KEY_PARENT;
import static com.quantumresearch.mycel.app.api.forum.ForumConstants.KEY_READ;
import static com.quantumresearch.mycel.app.api.forum.ForumConstants.KEY_TIMESTAMP;
import static com.quantumresearch.mycel.app.api.forum.ForumConstants.MAX_FORUM_POST_TEXT_LENGTH;
import static com.quantumresearch.mycel.app.api.forum.ForumPostFactory.SIGNING_LABEL_POST;

@Immutable
@NotNullByDefault
class ForumPostValidator extends BdfMessageValidator {

	ForumPostValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		super(clientHelper, metadataEncoder, clock);
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws InvalidMessageException, FormatException {
		// Parent ID, author, text, signature
		checkSize(body, 4);

		// Parent ID is optional
		byte[] parent = body.getOptionalRaw(0);
		checkLength(parent, UniqueId.LENGTH);

		// Author
		BdfList authorList = body.getList(1);
		Author author = clientHelper.parseAndValidateAuthor(authorList);

		// Text
		String text = body.getString(2);
		checkLength(text, 0, MAX_FORUM_POST_TEXT_LENGTH);

		// Signature
		byte[] sig = body.getRaw(3);
		checkLength(sig, 1, MAX_SIGNATURE_LENGTH);

		// Verify the signature
		BdfList signed = BdfList.of(g.getId(), m.getTimestamp(), parent,
				authorList, text);
		try {
			clientHelper.verifySignature(sig, SIGNING_LABEL_POST,
					signed, author.getPublicKey());
		} catch (GeneralSecurityException e) {
			throw new InvalidMessageException(e);
		}

		// Return the metadata and dependencies
		BdfDictionary meta = new BdfDictionary();
		Collection<MessageId> dependencies = emptyList();
		meta.put(KEY_TIMESTAMP, m.getTimestamp());
		if (parent != null) {
			meta.put(KEY_PARENT, parent);
			dependencies = singletonList(new MessageId(parent));
		}
		meta.put(KEY_AUTHOR, authorList);
		meta.put(KEY_READ, false);
		return new BdfMessageContext(meta, dependencies);
	}
}
