package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfEntry;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.app.sharing.SharingConstants.SESSION_KEY_INVITE_TIMESTAMP;
import static com.quantumresearch.mycel.app.sharing.SharingConstants.SESSION_KEY_IS_SESSION;
import static com.quantumresearch.mycel.app.sharing.SharingConstants.SESSION_KEY_LAST_LOCAL_MESSAGE_ID;
import static com.quantumresearch.mycel.app.sharing.SharingConstants.SESSION_KEY_LAST_REMOTE_MESSAGE_ID;
import static com.quantumresearch.mycel.app.sharing.SharingConstants.SESSION_KEY_LOCAL_TIMESTAMP;
import static com.quantumresearch.mycel.app.sharing.SharingConstants.SESSION_KEY_SESSION_ID;
import static com.quantumresearch.mycel.app.sharing.SharingConstants.SESSION_KEY_SHAREABLE_ID;
import static com.quantumresearch.mycel.app.sharing.SharingConstants.SESSION_KEY_STATE;

@Immutable
@NotNullByDefault
class SessionParserImpl implements SessionParser {

	@Inject
	SessionParserImpl() {
	}

	@Override
	public BdfDictionary getSessionQuery(SessionId s) {
		return BdfDictionary.of(new BdfEntry(SESSION_KEY_SESSION_ID, s));
	}

	@Override
	public BdfDictionary getAllSessionsQuery() {
		return BdfDictionary.of(new BdfEntry(SESSION_KEY_IS_SESSION, true));
	}

	@Override
	public boolean isSession(BdfDictionary d) throws FormatException {
		return d.getBoolean(SESSION_KEY_IS_SESSION, false);
	}

	@Override
	public Session parseSession(GroupId contactGroupId,
			BdfDictionary d) throws FormatException {
		return new Session(State.fromValue(getState(d)), contactGroupId,
				getShareableId(d), getLastLocalMessageId(d),
				getLastRemoteMessageId(d), getLocalTimestamp(d),
				getInviteTimestamp(d));
	}

	private int getState(BdfDictionary d) throws FormatException {
		return d.getInt(SESSION_KEY_STATE);
	}

	private GroupId getShareableId(BdfDictionary d) throws FormatException {
		return new GroupId(d.getRaw(SESSION_KEY_SHAREABLE_ID));
	}

	@Nullable
	private MessageId getLastLocalMessageId(BdfDictionary d)
			throws FormatException {
		byte[] b = d.getOptionalRaw(SESSION_KEY_LAST_LOCAL_MESSAGE_ID);
		return b == null ? null : new MessageId(b);
	}

	@Nullable
	private MessageId getLastRemoteMessageId(BdfDictionary d)
			throws FormatException {
		byte[] b = d.getOptionalRaw(SESSION_KEY_LAST_REMOTE_MESSAGE_ID);
		return b == null ? null : new MessageId(b);
	}

	private long getLocalTimestamp(BdfDictionary d) throws FormatException {
		return d.getLong(SESSION_KEY_LOCAL_TIMESTAMP);
	}

	private long getInviteTimestamp(BdfDictionary d) throws FormatException {
		return d.getLong(SESSION_KEY_INVITE_TIMESTAMP);
	}

}
