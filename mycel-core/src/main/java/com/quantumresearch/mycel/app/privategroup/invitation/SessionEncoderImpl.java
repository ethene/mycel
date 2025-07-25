package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.data.BdfDictionary.NULL_VALUE;
import static com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationConstants.SESSION_KEY_INVITE_TIMESTAMP;
import static com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationConstants.SESSION_KEY_IS_SESSION;
import static com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationConstants.SESSION_KEY_LAST_LOCAL_MESSAGE_ID;
import static com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationConstants.SESSION_KEY_LAST_REMOTE_MESSAGE_ID;
import static com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationConstants.SESSION_KEY_LOCAL_TIMESTAMP;
import static com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationConstants.SESSION_KEY_PRIVATE_GROUP_ID;
import static com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationConstants.SESSION_KEY_ROLE;
import static com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationConstants.SESSION_KEY_SESSION_ID;
import static com.quantumresearch.mycel.app.privategroup.invitation.GroupInvitationConstants.SESSION_KEY_STATE;

@Immutable
@NotNullByDefault
class SessionEncoderImpl implements SessionEncoder {

	@Inject
	SessionEncoderImpl() {
	}

	@Override
	public BdfDictionary encodeSession(Session s) {
		BdfDictionary d = new BdfDictionary();
		d.put(SESSION_KEY_IS_SESSION, true);
		d.put(SESSION_KEY_SESSION_ID, s.getPrivateGroupId());
		d.put(SESSION_KEY_PRIVATE_GROUP_ID, s.getPrivateGroupId());
		MessageId lastLocalMessageId = s.getLastLocalMessageId();
		if (lastLocalMessageId == null)
			d.put(SESSION_KEY_LAST_LOCAL_MESSAGE_ID, NULL_VALUE);
		else d.put(SESSION_KEY_LAST_LOCAL_MESSAGE_ID, lastLocalMessageId);
		MessageId lastRemoteMessageId = s.getLastRemoteMessageId();
		if (lastRemoteMessageId == null)
			d.put(SESSION_KEY_LAST_REMOTE_MESSAGE_ID, NULL_VALUE);
		else d.put(SESSION_KEY_LAST_REMOTE_MESSAGE_ID, lastRemoteMessageId);
		d.put(SESSION_KEY_LOCAL_TIMESTAMP, s.getLocalTimestamp());
		d.put(SESSION_KEY_INVITE_TIMESTAMP, s.getInviteTimestamp());
		d.put(SESSION_KEY_ROLE, s.getRole().getValue());
		d.put(SESSION_KEY_STATE, s.getState().getValue());
		return d;
	}
}
