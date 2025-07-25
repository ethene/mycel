package com.quantumresearch.mycel.spore.plugin.file;

import android.app.Application;
import android.net.Uri;

import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveConstants.PROP_URI;
import static com.quantumresearch.mycel.spore.util.StringUtils.isNullOrEmpty;

@Immutable
@NotNullByDefault
class AndroidRemovableDrivePlugin extends RemovableDrivePlugin {

	private final Application app;

	AndroidRemovableDrivePlugin(Application app, PluginCallback callback,
			long maxLatency) {
		super(callback, maxLatency);
		this.app = app;
	}

	@Override
	InputStream openInputStream(TransportProperties p) throws IOException {
		String uri = p.get(PROP_URI);
		if (isNullOrEmpty(uri)) throw new IllegalArgumentException();
		try {
			return app.getContentResolver().openInputStream(Uri.parse(uri));
		} catch (SecurityException e) {
			throw new IOException(e);
		}
	}

	@Override
	OutputStream openOutputStream(TransportProperties p) throws IOException {
		String uri = p.get(PROP_URI);
		if (isNullOrEmpty(uri)) throw new IllegalArgumentException();
		try {
			return app.getContentResolver()
					.openOutputStream(Uri.parse(uri), "wt");
		} catch (SecurityException e) {
			throw new IOException(e);
		}
	}
}
