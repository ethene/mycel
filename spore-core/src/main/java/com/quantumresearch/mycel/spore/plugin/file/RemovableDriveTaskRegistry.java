package com.quantumresearch.mycel.spore.plugin.file;

import com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveTask;
import org.briarproject.nullsafety.NotNullByDefault;

@Deprecated // We can simply remove tasks when they finish
@NotNullByDefault
interface RemovableDriveTaskRegistry {

	void removeReader(RemovableDriveTask task);

	void removeWriter(RemovableDriveTask task);
}
