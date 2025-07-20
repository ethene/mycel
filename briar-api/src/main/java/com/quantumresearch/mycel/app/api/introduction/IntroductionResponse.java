package com.quantumresearch.mycel.app.api.introduction;

import com.quantumresearch.mycel.infrastructure.api.identity.Author;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import com.quantumresearch.mycel.app.api.conversation.ConversationMessageVisitor;
import com.quantumresearch.mycel.app.api.conversation.ConversationResponse;
import com.quantumresearch.mycel.app.api.identity.AuthorInfo;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.app.api.introduction.Role.INTRODUCER;

@Immutable
@NotNullByDefault
public class IntroductionResponse extends ConversationResponse {

	private final Author introducedAuthor;
	private final AuthorInfo introducedAuthorInfo;
	private final Role ourRole;
	private final boolean canSucceed;

	public IntroductionResponse(MessageId messageId, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, boolean accepted, Author author,
			AuthorInfo introducedAuthorInfo, Role role, boolean canSucceed,
			long autoDeleteTimer, boolean isAutoDecline) {
		super(messageId, groupId, time, local, read, sent, seen, sessionId,
				accepted, autoDeleteTimer, isAutoDecline);
		this.introducedAuthor = author;
		this.introducedAuthorInfo = introducedAuthorInfo;
		this.ourRole = role;
		this.canSucceed = canSucceed;
	}

	public Author getIntroducedAuthor() {
		return introducedAuthor;
	}

	public AuthorInfo getIntroducedAuthorInfo() {
		return introducedAuthorInfo;
	}

	public boolean canSucceed() {
		return canSucceed;
	}

	public boolean isIntroducer() {
		return ourRole == INTRODUCER;
	}

	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitIntroductionResponse(this);
	}

}
