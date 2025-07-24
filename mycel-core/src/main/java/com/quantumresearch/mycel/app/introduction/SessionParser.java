package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.app.api.client.SessionId;
import com.quantumresearch.mycel.app.api.introduction.Role;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionParser {

	BdfDictionary getSessionQuery(SessionId s);

	Role getRole(BdfDictionary d) throws FormatException;

	IntroducerSession parseIntroducerSession(BdfDictionary d)
			throws FormatException;

	IntroduceeSession parseIntroduceeSession(GroupId introducerGroupId,
			BdfDictionary d) throws FormatException;

}
