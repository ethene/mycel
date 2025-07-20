package com.quantumresearch.mycel.infrastructure.api.data;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.db.Metadata;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MetadataParser {

	BdfDictionary parse(Metadata m) throws FormatException;
}
