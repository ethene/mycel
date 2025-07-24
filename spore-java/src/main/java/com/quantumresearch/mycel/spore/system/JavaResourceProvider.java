package com.quantumresearch.mycel.spore.system;

import com.quantumresearch.mycel.spore.api.system.ResourceProvider;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;

import javax.inject.Inject;

import static org.briarproject.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
class JavaResourceProvider implements ResourceProvider {

	@Inject
	JavaResourceProvider() {
	}

	@Override
	public InputStream getResourceInputStream(String name, String extension) {
		ClassLoader cl = getClass().getClassLoader();
		return requireNonNull(cl.getResourceAsStream(name + extension));
	}
}
