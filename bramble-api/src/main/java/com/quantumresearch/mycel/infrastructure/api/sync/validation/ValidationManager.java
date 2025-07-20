package com.quantumresearch.mycel.infrastructure.api.sync.validation;

import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.infrastructure.api.sync.ClientId;
import org.briarproject.nullsafety.NotNullByDefault;

/**
 * Responsible for managing message validators and passing them messages to
 * validate.
 */
@NotNullByDefault
public interface ValidationManager {

	/**
	 * Registers the {@link MessageValidator} for the given client. This method
	 * should be called before
	 * {@link LifecycleManager#startServices(SecretKey)}.
	 */
	void registerMessageValidator(ClientId c, int majorVersion,
			MessageValidator v);

	/**
	 * Registers the {@link IncomingMessageHook} for the given client. The hook
	 * will be called once for each incoming message that passes validation.
	 * This method should be called before
	 * {@link LifecycleManager#startServices(SecretKey)}.
	 */
	void registerIncomingMessageHook(ClientId c, int majorVersion,
			IncomingMessageHook hook);
}
