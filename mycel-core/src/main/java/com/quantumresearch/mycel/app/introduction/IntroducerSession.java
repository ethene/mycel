package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import com.quantumresearch.mycel.app.api.introduction.Role;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.app.api.introduction.Role.INTRODUCER;

@Immutable
@NotNullByDefault
class IntroducerSession extends Session<IntroducerState> {

	private final Introducee introduceeA, introduceeB;

	IntroducerSession(SessionId sessionId, IntroducerState state,
			long requestTimestamp, Introducee introduceeA,
			Introducee introduceeB) {
		super(sessionId, state, requestTimestamp);
		this.introduceeA = introduceeA;
		this.introduceeB = introduceeB;
	}

	IntroducerSession(SessionId sessionId, GroupId groupIdA, Author authorA,
			GroupId groupIdB, Author authorB) {
		this(sessionId, IntroducerState.START, -1,
				new Introducee(sessionId, groupIdA, authorA),
				new Introducee(sessionId, groupIdB, authorB));
	}

	@Override
	Role getRole() {
		return INTRODUCER;
	}

	Introducee getIntroduceeA() {
		return introduceeA;
	}

	Introducee getIntroduceeB() {
		return introduceeB;
	}

	@Immutable
	@NotNullByDefault
	static class Introducee implements PeerSession {
		final SessionId sessionId;
		final GroupId groupId;
		final Author author;
		final long localTimestamp;
		@Nullable
		final MessageId lastLocalMessageId, lastRemoteMessageId;

		Introducee(SessionId sessionId, GroupId groupId, Author author,
				long localTimestamp,
				@Nullable MessageId lastLocalMessageId,
				@Nullable MessageId lastRemoteMessageId) {
			this.sessionId = sessionId;
			this.groupId = groupId;
			this.localTimestamp = localTimestamp;
			this.author = author;
			this.lastLocalMessageId = lastLocalMessageId;
			this.lastRemoteMessageId = lastRemoteMessageId;
		}

		Introducee(Introducee i, Message sent) {
			this(i.sessionId, i.groupId, i.author, sent.getTimestamp(),
					sent.getId(), i.lastRemoteMessageId);
		}

		Introducee(Introducee i, MessageId remoteMessageId) {
			this(i.sessionId, i.groupId, i.author, i.localTimestamp,
					i.lastLocalMessageId, remoteMessageId);
		}

		Introducee(SessionId sessionId, GroupId groupId,
				Author author) {
			this(sessionId, groupId, author, -1, null, null);
		}

		@Override
		public SessionId getSessionId() {
			return sessionId;
		}

		@Override
		public GroupId getContactGroupId() {
			return groupId;
		}

		@Override
		public long getLocalTimestamp() {
			return localTimestamp;
		}

		@Nullable
		@Override
		public MessageId getLastLocalMessageId() {
			return lastLocalMessageId;
		}

		@Nullable
		@Override
		public MessageId getLastRemoteMessageId() {
			return lastRemoteMessageId;
		}

	}

}
