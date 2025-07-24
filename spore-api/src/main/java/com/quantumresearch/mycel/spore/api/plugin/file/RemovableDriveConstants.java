package com.quantumresearch.mycel.spore.api.plugin.file;

import com.quantumresearch.mycel.spore.api.plugin.TransportId;

public interface RemovableDriveConstants {

	TransportId ID = new TransportId("com.quantumresearch.mycel.spore.drive");

	String PROP_PATH = "path";
	String PROP_URI = "uri";
	String PROP_SUPPORTED = "supported";
}
