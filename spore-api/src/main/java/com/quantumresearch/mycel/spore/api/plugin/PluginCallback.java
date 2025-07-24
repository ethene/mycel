package com.quantumresearch.mycel.spore.api.plugin;

import com.quantumresearch.mycel.spore.api.plugin.Plugin.State;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportActiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportInactiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportStateEvent;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.settings.Settings;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

/**
 * An interface through which a transport plugin interacts with the rest of
 * the application.
 */
@NotNullByDefault
public interface PluginCallback extends ConnectionHandler {

	/**
	 * Returns the plugin's settings
	 */
	Settings getSettings();

	/**
	 * Returns the plugin's local transport properties.
	 */
	TransportProperties getLocalProperties();

	/**
	 * Returns the plugin's remote transport properties.
	 */
	Collection<TransportProperties> getRemoteProperties();

	/**
	 * Merges the given settings with the plugin's settings
	 */
	void mergeSettings(Settings s);

	/**
	 * Merges the given properties with the plugin's local transport properties.
	 */
	void mergeLocalProperties(TransportProperties p);

	/**
	 * Informs the callback of the plugin's current state.
	 * <p>
	 * If the current state is different from the previous state, the callback
	 * will broadcast a {@link TransportStateEvent}. If the current state is
	 * {@link State#ACTIVE} and the previous state was not
	 * {@link State#ACTIVE}, the callback will broadcast a
	 * {@link TransportActiveEvent}. If the current state is not
	 * {@link State#ACTIVE} and the previous state was {@link State#ACTIVE},
	 * the callback will broadcast a {@link TransportInactiveEvent}.
	 * <p>
	 * This method can safely be called while holding a lock.
	 */
	void pluginStateChanged(State state);
}
