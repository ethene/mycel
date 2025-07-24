package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.system.SecureRandomProvider;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.Provider;

@NotNullByDefault
public class TestSecureRandomProvider implements SecureRandomProvider {

	@Override
	public Provider getProvider() {
		// Use the default provider
		return null;
	}
}
