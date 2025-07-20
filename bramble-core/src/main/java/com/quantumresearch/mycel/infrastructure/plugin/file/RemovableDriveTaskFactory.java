package com.quantumresearch.mycel.infrastructure.plugin.file;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.plugin.file.RemovableDriveTask;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface RemovableDriveTaskFactory {

	RemovableDriveTask createReader(RemovableDriveTaskRegistry registry,
			TransportProperties p);

	RemovableDriveTask createWriter(RemovableDriveTaskRegistry registry,
			ContactId c, TransportProperties p);
}
