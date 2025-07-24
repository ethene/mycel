package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionParser {

	BdfDictionary getSessionQuery(SessionId s);

	BdfDictionary getAllSessionsQuery();

	Role getRole(BdfDictionary d) throws FormatException;

	boolean isSession(BdfDictionary d) throws FormatException;

	Session parseSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;

	CreatorSession parseCreatorSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;

	InviteeSession parseInviteeSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;

	PeerSession parsePeerSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;
}
