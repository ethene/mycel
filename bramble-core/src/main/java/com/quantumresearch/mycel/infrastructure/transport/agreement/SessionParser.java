package com.quantumresearch.mycel.infrastructure.transport.agreement;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.data.BdfDictionary;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionParser {

	Session parseSession(BdfDictionary meta) throws FormatException;
}
