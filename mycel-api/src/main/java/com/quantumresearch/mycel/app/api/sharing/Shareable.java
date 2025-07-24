package com.quantumresearch.mycel.app.api.sharing;

import com.quantumresearch.mycel.spore.api.Nameable;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Shareable extends Nameable {

	GroupId getId();

}
