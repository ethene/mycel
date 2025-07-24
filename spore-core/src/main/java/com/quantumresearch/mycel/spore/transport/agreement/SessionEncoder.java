package com.quantumresearch.mycel.spore.transport.agreement;

import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionEncoder {

	BdfDictionary encodeSession(Session s, TransportId transportId);

	BdfDictionary getSessionQuery(TransportId transportId);
}
