package com.quantumresearch.mycel.spore.plugin;

import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.plugin.PluginConfig;
import com.quantumresearch.mycel.spore.api.plugin.PluginException;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPluginFactory;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPluginFactory;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.settings.SettingsManager;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import org.jmock.Expectations;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.quantumresearch.mycel.spore.test.TestUtils.getTransportId;

public class PluginManagerImplTest extends BrambleMockTestCase {

	@Test
	public void testStartAndStop() throws Exception {
		Executor ioExecutor = Executors.newSingleThreadExecutor();
		EventBus eventBus = context.mock(EventBus.class);
		PluginConfig pluginConfig = context.mock(PluginConfig.class);
		ConnectionManager connectionManager =
				context.mock(ConnectionManager.class);
		SettingsManager settingsManager =
				context.mock(SettingsManager.class);
		TransportPropertyManager transportPropertyManager =
				context.mock(TransportPropertyManager.class);

		// Two simplex plugin factories: both create plugins, one fails to start
		SimplexPluginFactory simplexFactory =
				context.mock(SimplexPluginFactory.class);
		SimplexPlugin simplexPlugin = context.mock(SimplexPlugin.class);
		TransportId simplexId = getTransportId();
		SimplexPluginFactory simplexFailFactory =
				context.mock(SimplexPluginFactory.class, "simplexFailFactory");
		SimplexPlugin simplexFailPlugin =
				context.mock(SimplexPlugin.class, "simplexFailPlugin");
		TransportId simplexFailId = getTransportId();

		// Two duplex plugin factories: one creates a plugin, the other fails
		DuplexPluginFactory duplexFactory =
				context.mock(DuplexPluginFactory.class);
		DuplexPlugin duplexPlugin = context.mock(DuplexPlugin.class);
		TransportId duplexId = getTransportId();
		DuplexPluginFactory duplexFailFactory =
				context.mock(DuplexPluginFactory.class, "duplexFailFactory");
		TransportId duplexFailId = getTransportId();

		context.checking(new Expectations() {{
			allowing(simplexPlugin).getId();
			will(returnValue(simplexId));
			allowing(simplexFailPlugin).getId();
			will(returnValue(simplexFailId));
			allowing(duplexPlugin).getId();
			will(returnValue(duplexId));
			allowing(pluginConfig).shouldPoll();
			will(returnValue(false));
			// start()
			// First simplex plugin
			oneOf(pluginConfig).getSimplexFactories();
			will(returnValue(Arrays.asList(simplexFactory,
					simplexFailFactory)));
			oneOf(simplexFactory).getId();
			will(returnValue(simplexId));
			oneOf(simplexFactory).createPlugin(with(any(PluginCallback.class)));
			will(returnValue(simplexPlugin)); // Created
			oneOf(simplexPlugin).start();
			// Second simplex plugin
			oneOf(simplexFailFactory).getId();
			will(returnValue(simplexFailId));
			oneOf(simplexFailFactory).createPlugin(with(any(
					PluginCallback.class)));
			will(returnValue(simplexFailPlugin)); // Created
			oneOf(simplexFailPlugin).start();
			will(throwException(new PluginException()));
			// First duplex plugin
			oneOf(pluginConfig).getDuplexFactories();
			will(returnValue(Arrays.asList(duplexFactory, duplexFailFactory)));
			oneOf(duplexFactory).getId();
			will(returnValue(duplexId));
			oneOf(duplexFactory).createPlugin(with(any(PluginCallback.class)));
			will(returnValue(duplexPlugin)); // Created
			oneOf(duplexPlugin).start();
			// Second duplex plugin
			oneOf(duplexFailFactory).getId();
			will(returnValue(duplexFailId));
			oneOf(duplexFailFactory).createPlugin(with(any(
					PluginCallback.class)));
			will(returnValue(null)); // Failed to create a plugin
			// stop()
			// Stop the plugins
			oneOf(simplexPlugin).stop();
			oneOf(simplexFailPlugin).stop();
			oneOf(duplexPlugin).stop();
		}});

		PluginManagerImpl p = new PluginManagerImpl(ioExecutor, ioExecutor,
				eventBus, pluginConfig, connectionManager, settingsManager,
				transportPropertyManager);

		// Two plugins should be started and stopped
		p.startService();
		p.stopService();
	}
}
