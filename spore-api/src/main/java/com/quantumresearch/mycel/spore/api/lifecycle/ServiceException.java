package com.quantumresearch.mycel.spore.api.lifecycle;

/**
 * An exception that indicates an error starting or stopping a {@link Service}.
 */
public class ServiceException extends Exception {

	public ServiceException() {
		super();
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}
}
