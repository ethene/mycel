package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionEncoder {

	BdfDictionary encodeSession(Session s);

}
