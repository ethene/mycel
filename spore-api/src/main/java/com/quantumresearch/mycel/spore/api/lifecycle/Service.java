package com.quantumresearch.mycel.spore.api.lifecycle;

import com.quantumresearch.mycel.spore.api.system.Wakeful;

public interface Service {

	/**
	 * Starts the service. This method must not be called concurrently with
	 * {@link #stopService()}.
	 */
	@Wakeful
	void startService() throws ServiceException;

	/**
	 * Stops the service. This method must not be called concurrently with
	 * {@link #startService()}.
	 */
	@Wakeful
	void stopService() throws ServiceException;
}
