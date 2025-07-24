package com.quantumresearch.mycel.spore.api.data;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MetadataParser {

	BdfDictionary parse(Metadata m) throws FormatException;
}
