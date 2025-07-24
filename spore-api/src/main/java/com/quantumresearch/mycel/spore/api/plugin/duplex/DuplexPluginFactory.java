package com.quantumresearch.mycel.spore.api.plugin.duplex;

import com.quantumresearch.mycel.spore.api.plugin.PluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

/**
 * Factory for creating a plugin for a duplex transport.
 */
@NotNullByDefault
public interface DuplexPluginFactory extends PluginFactory<DuplexPlugin> {
}
