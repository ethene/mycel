package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.data.BdfList;
import com.quantumresearch.mycel.infrastructure.api.data.MetadataEncoder;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.app.api.forum.Forum;
import com.quantumresearch.mycel.app.api.forum.ForumFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.util.ValidationUtils.checkLength;
import static com.quantumresearch.mycel.infrastructure.util.ValidationUtils.checkSize;
import static com.quantumresearch.mycel.app.api.forum.ForumConstants.FORUM_SALT_LENGTH;
import static com.quantumresearch.mycel.app.api.forum.ForumConstants.MAX_FORUM_NAME_LENGTH;

@Immutable
@NotNullByDefault
class ForumSharingValidator extends SharingValidator {

	private final ForumFactory forumFactory;

	@Inject
	ForumSharingValidator(MessageEncoder messageEncoder,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, ForumFactory forumFactory) {
		super(messageEncoder, clientHelper, metadataEncoder, clock);
		this.forumFactory = forumFactory;
	}

	@Override
	protected GroupId validateDescriptor(BdfList descriptor)
			throws FormatException {
		// Name, salt
		checkSize(descriptor, 2);
		String name = descriptor.getString(0);
		checkLength(name, 1, MAX_FORUM_NAME_LENGTH);
		byte[] salt = descriptor.getRaw(1);
		checkLength(salt, FORUM_SALT_LENGTH);
		Forum forum = forumFactory.createForum(name, salt);
		return forum.getId();
	}

}
