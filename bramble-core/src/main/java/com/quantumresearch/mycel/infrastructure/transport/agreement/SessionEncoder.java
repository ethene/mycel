package com.quantumresearch.mycel.infrastructure.transport.agreement;

import com.quantumresearch.mycel.infrastructure.api.data.BdfDictionary;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionEncoder {

	BdfDictionary encodeSession(Session s, TransportId transportId);

	BdfDictionary getSessionQuery(TransportId transportId);
}
