package com.quantumresearch.mycel.spore.plugin;

import com.quantumresearch.mycel.spore.api.Cancellable;
import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.connection.ConnectionRegistry;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.event.ContactAddedEvent;
import com.quantumresearch.mycel.spore.api.plugin.ConnectionHandler;
import com.quantumresearch.mycel.spore.api.plugin.Plugin;
import com.quantumresearch.mycel.spore.api.plugin.PluginManager;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.plugin.event.ConnectionClosedEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.ConnectionOpenedEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportActiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportInactiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPlugin;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.system.TaskScheduler;
import com.quantumresearch.mycel.spore.crypto.NeitherSecureNorRandom;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import com.quantumresearch.mycel.spore.test.ImmediateExecutor;
import com.quantumresearch.mycel.spore.test.RunAction;
import org.jmock.Expectations;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.Executor;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static com.quantumresearch.mycel.spore.test.CollectionMatcher.collectionOf;
import static com.quantumresearch.mycel.spore.test.PairMatcher.pairOf;
import static com.quantumresearch.mycel.spore.test.TestUtils.getContactId;
import static com.quantumresearch.mycel.spore.test.TestUtils.getTransportId;

public class PollerImplTest extends BrambleMockTestCase {

	private final TaskScheduler scheduler = context.mock(TaskScheduler.class);
	private final ConnectionManager connectionManager =
			context.mock(ConnectionManager.class);
	private final ConnectionRegistry connectionRegistry =
			context.mock(ConnectionRegistry.class);
	private final PluginManager pluginManager =
			context.mock(PluginManager.class);
	private final TransportPropertyManager transportPropertyManager =
			context.mock(TransportPropertyManager.class);
	private final Clock clock = context.mock(Clock.class);
	private final Cancellable cancellable = context.mock(Cancellable.class);

	private final Executor ioExecutor = new ImmediateExecutor();
	private final TransportId transportId = getTransportId();
	private final ContactId contactId = getContactId();
	private final TransportProperties properties = new TransportProperties();
	private final int pollingInterval = 60 * 1000;
	private final long now = System.currentTimeMillis();

	private final PollerImpl poller;

	public PollerImplTest() {
		// Use a fake SecureRandom that returns all zeroes
		SecureRandom random = new NeitherSecureNorRandom();
		Executor wakefulIoExecutor = new ImmediateExecutor();
		poller = new PollerImpl(ioExecutor, wakefulIoExecutor, scheduler,
				connectionManager, connectionRegistry, pluginManager,
				transportPropertyManager, random, clock);
	}

	@Test
	public void testConnectOnContactAdded() throws Exception {
		// Two simplex plugins: one supports polling, the other doesn't
		SimplexPlugin simplexPlugin = context.mock(SimplexPlugin.class);
		SimplexPlugin simplexPlugin1 =
				context.mock(SimplexPlugin.class, "simplexPlugin1");
		TransportId simplexId1 = getTransportId();
		List<SimplexPlugin> simplexPlugins =
				asList(simplexPlugin, simplexPlugin1);
		TransportConnectionWriter simplexWriter =
				context.mock(TransportConnectionWriter.class);

		// Two duplex plugins: one supports polling, the other doesn't
		DuplexPlugin duplexPlugin = context.mock(DuplexPlugin.class);
		TransportId duplexId = getTransportId();
		DuplexPlugin duplexPlugin1 =
				context.mock(DuplexPlugin.class, "duplexPlugin1");
		List<DuplexPlugin> duplexPlugins =
				asList(duplexPlugin, duplexPlugin1);
		DuplexTransportConnection duplexConnection =
				context.mock(DuplexTransportConnection.class);

		context.checking(new Expectations() {{
			// Get the simplex plugins
			oneOf(pluginManager).getSimplexPlugins();
			will(returnValue(simplexPlugins));
			// The first plugin doesn't support polling
			oneOf(simplexPlugin).shouldPoll();
			will(returnValue(false));
			// The second plugin supports polling
			oneOf(simplexPlugin1).shouldPoll();
			will(returnValue(true));
			// Check whether the contact is already connected
			oneOf(simplexPlugin1).getId();
			will(returnValue(simplexId1));
			oneOf(connectionRegistry).isConnected(contactId, simplexId1);
			will(returnValue(false));
			// Get the transport properties
			oneOf(transportPropertyManager).getRemoteProperties(contactId,
					simplexId1);
			will(returnValue(properties));
			// Connect to the contact
			oneOf(simplexPlugin1).createWriter(properties);
			will(returnValue(simplexWriter));
			// Pass the connection to the connection manager
			oneOf(connectionManager).manageOutgoingConnection(contactId,
					simplexId1, simplexWriter);
			// Get the duplex plugins
			oneOf(pluginManager).getDuplexPlugins();
			will(returnValue(duplexPlugins));
			// The duplex plugin supports polling
			oneOf(duplexPlugin).shouldPoll();
			will(returnValue(true));
			// Check whether the contact is already connected
			oneOf(duplexPlugin).getId();
			will(returnValue(duplexId));
			oneOf(connectionRegistry).isConnected(contactId, duplexId);
			will(returnValue(false));
			// Get the transport properties
			oneOf(transportPropertyManager).getRemoteProperties(contactId,
					duplexId);
			will(returnValue(properties));
			// Connect to the contact
			oneOf(duplexPlugin).createConnection(properties);
			will(returnValue(duplexConnection));
			// Pass the connection to the connection manager
			oneOf(connectionManager).manageOutgoingConnection(contactId,
					duplexId, duplexConnection);
			// The second plugin doesn't support polling
			oneOf(duplexPlugin1).shouldPoll();
			will(returnValue(false));
		}});

		poller.eventOccurred(new ContactAddedEvent(contactId, true));
	}

	@Test
	public void testRescheduleOnOutgoingConnectionClosed() {
		DuplexPlugin plugin = context.mock(DuplexPlugin.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
		}});
		expectReschedule(plugin);

		poller.eventOccurred(new ConnectionClosedEvent(contactId, transportId,
				false, false));
	}

	@Test
	public void testRescheduleAndReconnectOnOutgoingConnectionFailed()
			throws Exception {
		DuplexPlugin plugin = context.mock(DuplexPlugin.class);
		DuplexTransportConnection duplexConnection =
				context.mock(DuplexTransportConnection.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
		}});
		expectReschedule(plugin);
		expectReconnect(plugin, duplexConnection);

		poller.eventOccurred(new ConnectionClosedEvent(contactId, transportId,
				false, true));
	}

	@Test
	public void testRescheduleOnIncomingConnectionClosed() {
		DuplexPlugin plugin = context.mock(DuplexPlugin.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
		}});
		expectReschedule(plugin);

		poller.eventOccurred(new ConnectionClosedEvent(contactId, transportId,
				true, false));
	}

	@Test
	public void testRescheduleOnIncomingConnectionFailed() {
		DuplexPlugin plugin = context.mock(DuplexPlugin.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
		}});
		expectReschedule(plugin);

		poller.eventOccurred(new ConnectionClosedEvent(contactId, transportId,
				true, false));
	}

	@Test
	public void testRescheduleOnConnectionOpened() {
		Plugin plugin = context.mock(Plugin.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Schedule the next poll
			oneOf(plugin).getPollingInterval();
			will(returnValue(pollingInterval));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with((long) pollingInterval),
					with(MILLISECONDS));
			will(returnValue(cancellable));
		}});

		poller.eventOccurred(new ConnectionOpenedEvent(contactId, transportId,
				false));
	}

	@Test
	public void testRescheduleDoesNotReplaceEarlierTask() {
		Plugin plugin = context.mock(Plugin.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
			// First event
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Schedule the next poll
			oneOf(plugin).getPollingInterval();
			will(returnValue(pollingInterval));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with((long) pollingInterval),
					with(MILLISECONDS));
			will(returnValue(cancellable));
			// Second event
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Don't replace the previously scheduled task, due earlier
			oneOf(plugin).getPollingInterval();
			will(returnValue(pollingInterval));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now + 1));
		}});

		poller.eventOccurred(new ConnectionOpenedEvent(contactId, transportId,
				false));
		poller.eventOccurred(new ConnectionOpenedEvent(contactId, transportId,
				false));
	}

	@Test
	public void testRescheduleReplacesLaterTask() {
		Plugin plugin = context.mock(Plugin.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
			// First event
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Schedule the next poll
			oneOf(plugin).getPollingInterval();
			will(returnValue(pollingInterval));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with((long) pollingInterval),
					with(MILLISECONDS));
			will(returnValue(cancellable));
			// Second event
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Replace the previously scheduled task, due later
			oneOf(plugin).getPollingInterval();
			will(returnValue(pollingInterval - 2));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now + 1));
			oneOf(cancellable).cancel();
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with((long) pollingInterval - 2),
					with(MILLISECONDS));
		}});

		poller.eventOccurred(new ConnectionOpenedEvent(contactId, transportId,
				false));
		poller.eventOccurred(new ConnectionOpenedEvent(contactId, transportId,
				false));
	}

	@Test
	public void testPollsOnTransportActivated() throws Exception {
		DuplexPlugin plugin = context.mock(DuplexPlugin.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Schedule a polling task immediately
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(0L), with(MILLISECONDS));
			will(returnValue(cancellable));
			will(new RunAction());
			// Running the polling task schedules the next polling task
			oneOf(plugin).getPollingInterval();
			will(returnValue(pollingInterval));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(0L),
					with(MILLISECONDS));
			will(returnValue(cancellable));
			// Get the transport properties and connected contacts
			oneOf(transportPropertyManager).getRemoteProperties(transportId);
			will(returnValue(singletonMap(contactId, properties)));
			oneOf(connectionRegistry).getConnectedOrBetterContacts(transportId);
			will(returnValue(emptyList()));
			// Poll the plugin
			oneOf(plugin).poll(with(collectionOf(
					pairOf(equal(properties), any(ConnectionHandler.class)))));
		}});

		poller.eventOccurred(new TransportActiveEvent(transportId));
	}

	@Test
	public void testDoesNotPollIfAllContactsAreConnected() throws Exception {
		DuplexPlugin plugin = context.mock(DuplexPlugin.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Schedule a polling task immediately
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(0L), with(MILLISECONDS));
			will(returnValue(cancellable));
			will(new RunAction());
			// Running the polling task schedules the next polling task
			oneOf(plugin).getPollingInterval();
			will(returnValue(pollingInterval));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(0L),
					with(MILLISECONDS));
			will(returnValue(cancellable));
			// Get the transport properties and connected contacts
			oneOf(transportPropertyManager).getRemoteProperties(transportId);
			will(returnValue(singletonMap(contactId, properties)));
			oneOf(connectionRegistry).getConnectedOrBetterContacts(transportId);
			will(returnValue(singletonList(contactId)));
			// All contacts are connected, so don't poll the plugin
		}});

		poller.eventOccurred(new TransportActiveEvent(transportId));
	}

	@Test
	public void testCancelsPollingOnTransportDeactivated() {
		Plugin plugin = context.mock(Plugin.class);

		context.checking(new Expectations() {{
			allowing(plugin).getId();
			will(returnValue(transportId));
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Schedule a polling task immediately
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(0L), with(MILLISECONDS));
			will(returnValue(cancellable));
			// The plugin is deactivated before the task runs - cancel the task
			oneOf(cancellable).cancel();
		}});

		poller.eventOccurred(new TransportActiveEvent(transportId));
		poller.eventOccurred(new TransportInactiveEvent(transportId));
	}

	private void expectReschedule(Plugin plugin) {
		context.checking(new Expectations() {{
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Schedule the next poll
			oneOf(plugin).getPollingInterval();
			will(returnValue(pollingInterval));
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
			oneOf(scheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with((long) pollingInterval),
					with(MILLISECONDS));
			will(returnValue(cancellable));
		}});
	}

	private void expectReconnect(DuplexPlugin plugin,
			DuplexTransportConnection duplexConnection) throws Exception {
		context.checking(new Expectations() {{
			// Get the plugin
			oneOf(pluginManager).getPlugin(transportId);
			will(returnValue(plugin));
			// The plugin supports polling
			oneOf(plugin).shouldPoll();
			will(returnValue(true));
			// Check whether the contact is already connected
			oneOf(connectionRegistry).isConnected(contactId, transportId);
			will(returnValue(false));
			// Get the transport properties
			oneOf(transportPropertyManager).getRemoteProperties(contactId,
					transportId);
			will(returnValue(properties));
			// Connect to the contact
			oneOf(plugin).createConnection(properties);
			will(returnValue(duplexConnection));
			// Pass the connection to the connection manager
			oneOf(connectionManager).manageOutgoingConnection(contactId,
					transportId, duplexConnection);
		}});
	}
}
