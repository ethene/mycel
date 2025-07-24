package com.quantumresearch.mycel.spore.transport.agreement;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionParser {

	Session parseSession(BdfDictionary meta) throws FormatException;
}
