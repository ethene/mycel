package com.quantumresearch.mycel.infrastructure.api.data;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.db.Metadata;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MetadataEncoder {

	Metadata encode(BdfDictionary d) throws FormatException;
}
