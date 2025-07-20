package com.quantumresearch.mycel.infrastructure.api.plugin.simplex;

import com.quantumresearch.mycel.infrastructure.api.plugin.PluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

/**
 * Factory for creating a plugin for a simplex transport.
 */
@NotNullByDefault
public interface SimplexPluginFactory extends PluginFactory<SimplexPlugin> {
}
