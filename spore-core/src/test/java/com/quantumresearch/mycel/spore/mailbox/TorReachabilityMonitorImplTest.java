package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.api.Cancellable;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.plugin.Plugin;
import com.quantumresearch.mycel.spore.api.plugin.PluginManager;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportActiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportInactiveEvent;
import com.quantumresearch.mycel.spore.api.system.TaskScheduler;
import com.quantumresearch.mycel.spore.mailbox.TorReachabilityMonitor.TorReachabilityObserver;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import com.quantumresearch.mycel.spore.test.CaptureArgumentAction;
import org.jmock.Expectations;
import org.jmock.lib.action.DoAllAction;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.ACTIVE;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.DISABLED;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.ENABLING;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.ID;
import static com.quantumresearch.mycel.spore.mailbox.TorReachabilityMonitor.REACHABILITY_PERIOD_MS;

public class TorReachabilityMonitorImplTest extends BrambleMockTestCase {

	private final Executor ioExecutor = context.mock(Executor.class);
	private final TaskScheduler taskScheduler =
			context.mock(TaskScheduler.class);
	private final MailboxConfig mailboxConfig = new MailboxConfigImpl();
	private final PluginManager pluginManager =
			context.mock(PluginManager.class);
	private final EventBus eventBus = context.mock(EventBus.class);
	private final Plugin plugin = context.mock(Plugin.class);
	private final Cancellable scheduledTask = context.mock(Cancellable.class);
	private final TorReachabilityObserver observer =
			context.mock(TorReachabilityObserver.class);

	private final TorReachabilityMonitorImpl monitor =
			new TorReachabilityMonitorImpl(ioExecutor, taskScheduler,
					mailboxConfig, pluginManager, eventBus);

	@Test
	public void testSchedulesTaskWhenStartedIfTorIsActive() {
		// Starting the monitor should schedule a task
		context.checking(new Expectations() {{
			oneOf(eventBus).addListener(monitor);
			oneOf(pluginManager).getPlugin(ID);
			will(returnValue(plugin));
			oneOf(plugin).getState();
			will(returnValue(ACTIVE));
			oneOf(taskScheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(REACHABILITY_PERIOD_MS),
					with(MILLISECONDS));
			will(returnValue(scheduledTask));
		}});

		monitor.start();

		// If Tor has only just become active and the TransportActiveEvent
		// arrives after the task has already been scheduled, a second task
		// should not be scheduled
		monitor.eventOccurred(new TransportActiveEvent(ID));

		// Destroying the monitor should cancel the task
		context.checking(new Expectations() {{
			oneOf(eventBus).removeListener(monitor);
			oneOf(scheduledTask).cancel();
		}});

		monitor.destroy();
	}

	@Test
	public void testSchedulesTaskWhenTorBecomesActive() {
		// Starting the monitor should not schedule a task as Tor is inactive
		context.checking(new Expectations() {{
			oneOf(eventBus).addListener(monitor);
			oneOf(pluginManager).getPlugin(ID);
			will(returnValue(plugin));
			oneOf(plugin).getState();
			will(returnValue(ENABLING));
		}});

		monitor.start();

		// When Tor becomes active, a task should be scheduled
		context.checking(new Expectations() {{
			oneOf(taskScheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(REACHABILITY_PERIOD_MS),
					with(MILLISECONDS));
			will(returnValue(scheduledTask));
		}});

		monitor.eventOccurred(new TransportActiveEvent(ID));

		// Destroying the monitor should cancel the task
		context.checking(new Expectations() {{
			oneOf(eventBus).removeListener(monitor);
			oneOf(scheduledTask).cancel();
		}});

		monitor.destroy();
	}

	@Test
	public void testCancelsTaskWhenTorBecomesInactive() {
		// Starting the monitor should schedule a task
		context.checking(new Expectations() {{
			oneOf(eventBus).addListener(monitor);
			oneOf(pluginManager).getPlugin(ID);
			will(returnValue(plugin));
			oneOf(plugin).getState();
			will(returnValue(ACTIVE));
			oneOf(taskScheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(REACHABILITY_PERIOD_MS),
					with(MILLISECONDS));
			will(returnValue(scheduledTask));
		}});

		monitor.start();

		// When Tor becomes inactive, the task should be cancelled
		context.checking(new Expectations() {{
			oneOf(scheduledTask).cancel();
		}});

		monitor.eventOccurred(new TransportInactiveEvent(ID));

		// Destroying the monitor should not affect the task, which has
		// already been cancelled
		context.checking(new Expectations() {{
			oneOf(eventBus).removeListener(monitor);
		}});

		monitor.destroy();
	}

	@Test
	public void testObserverRegisteredBeforeTorBecomesActiveIsCalled() {
		// Starting the monitor should not schedule a task as Tor is inactive
		context.checking(new Expectations() {{
			oneOf(eventBus).addListener(monitor);
			oneOf(pluginManager).getPlugin(ID);
			will(returnValue(plugin));
			oneOf(plugin).getState();
			will(returnValue(DISABLED));
		}});

		monitor.start();

		// Register an observer
		monitor.addOneShotObserver(observer);

		// When Tor becomes active, a task should be scheduled
		AtomicReference<Runnable> runnable = new AtomicReference<>(null);
		context.checking(new Expectations() {{
			oneOf(taskScheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(REACHABILITY_PERIOD_MS),
					with(MILLISECONDS));
			will(new DoAllAction(
					new CaptureArgumentAction<>(runnable, Runnable.class, 0),
					returnValue(scheduledTask)
			));
		}});

		monitor.eventOccurred(new TransportActiveEvent(ID));

		// When the task runs, the observer should be called
		context.checking(new Expectations() {{
			oneOf(observer).onTorReachable();
		}});

		runnable.get().run();
	}

	@Test
	public void testObserverRegisteredBeforeTorBecomesReachableIsCalled() {
		// Starting the monitor should schedule a task
		AtomicReference<Runnable> runnable = new AtomicReference<>(null);
		context.checking(new Expectations() {{
			oneOf(eventBus).addListener(monitor);
			oneOf(pluginManager).getPlugin(ID);
			will(returnValue(plugin));
			oneOf(plugin).getState();
			will(returnValue(ACTIVE));
			oneOf(taskScheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(REACHABILITY_PERIOD_MS),
					with(MILLISECONDS));
			will(new DoAllAction(
					new CaptureArgumentAction<>(runnable, Runnable.class, 0),
					returnValue(scheduledTask)
			));
		}});

		monitor.start();

		// Register an observer
		monitor.addOneShotObserver(observer);

		// When the task runs, the observer should be called
		context.checking(new Expectations() {{
			oneOf(observer).onTorReachable();
		}});

		runnable.get().run();
	}

	@Test
	public void testObserverRegisteredAfterTorBecomesReachableIsCalled() {
		// Starting the monitor should schedule a task
		AtomicReference<Runnable> runnable = new AtomicReference<>(null);
		context.checking(new Expectations() {{
			oneOf(eventBus).addListener(monitor);
			oneOf(pluginManager).getPlugin(ID);
			will(returnValue(plugin));
			oneOf(plugin).getState();
			will(returnValue(ACTIVE));
			oneOf(taskScheduler).schedule(with(any(Runnable.class)),
					with(ioExecutor), with(REACHABILITY_PERIOD_MS),
					with(MILLISECONDS));
			will(new DoAllAction(
					new CaptureArgumentAction<>(runnable, Runnable.class, 0),
					returnValue(scheduledTask)
			));
		}});

		monitor.start();

		// When the task runs, no observers have been registered yet
		runnable.get().run();

		// When an observer is registered, it should be called immediately
		context.checking(new Expectations() {{
			oneOf(observer).onTorReachable();
		}});

		monitor.addOneShotObserver(observer);
	}
}
