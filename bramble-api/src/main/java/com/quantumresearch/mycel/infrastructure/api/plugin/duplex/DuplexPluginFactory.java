package com.quantumresearch.mycel.infrastructure.api.plugin.duplex;

import com.quantumresearch.mycel.infrastructure.api.plugin.PluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

/**
 * Factory for creating a plugin for a duplex transport.
 */
@NotNullByDefault
public interface DuplexPluginFactory extends PluginFactory<DuplexPlugin> {
}
