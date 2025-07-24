package com.quantumresearch.mycel.app.introduction;

import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.identity.Author;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionEncoder {

	BdfDictionary getIntroduceeSessionsByIntroducerQuery(Author introducer);

	BdfDictionary getIntroducerSessionsQuery();

	BdfDictionary encodeIntroducerSession(IntroducerSession s);

	BdfDictionary encodeIntroduceeSession(IntroduceeSession s);

}
