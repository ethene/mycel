package com.quantumresearch.mycel.spore.plugin.file;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveTask;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface RemovableDriveTaskFactory {

	RemovableDriveTask createReader(RemovableDriveTaskRegistry registry,
			TransportProperties p);

	RemovableDriveTask createWriter(RemovableDriveTaskRegistry registry,
			ContactId c, TransportProperties p);
}
