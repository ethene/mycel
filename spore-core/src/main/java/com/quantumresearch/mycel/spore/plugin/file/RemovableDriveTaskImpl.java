package com.quantumresearch.mycel.spore.plugin.file;

import com.quantumresearch.mycel.spore.api.Consumer;
import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.plugin.PluginManager;
import com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveTask;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPlugin;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import static java.lang.Math.min;
import static com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveConstants.ID;
import static org.briarproject.nullsafety.NullSafety.requireNonNull;

@ThreadSafe
@NotNullByDefault
abstract class RemovableDriveTaskImpl implements RemovableDriveTask {

	private final Executor eventExecutor;
	private final PluginManager pluginManager;
	final ConnectionManager connectionManager;
	final EventBus eventBus;
	final RemovableDriveTaskRegistry registry;
	final TransportProperties transportProperties;

	private final Object lock = new Object();
	@GuardedBy("lock")
	private final List<Consumer<State>> observers = new ArrayList<>();
	@GuardedBy("lock")
	private State state = new State(0, 0, false, false);

	RemovableDriveTaskImpl(
			Executor eventExecutor,
			PluginManager pluginManager,
			ConnectionManager connectionManager,
			EventBus eventBus,
			RemovableDriveTaskRegistry registry,
			TransportProperties transportProperties) {
		this.eventExecutor = eventExecutor;
		this.pluginManager = pluginManager;
		this.connectionManager = connectionManager;
		this.eventBus = eventBus;
		this.registry = registry;
		this.transportProperties = transportProperties;
	}

	@Override
	public TransportProperties getTransportProperties() {
		return transportProperties;
	}

	@Override
	public void addObserver(Consumer<State> o) {
		State state;
		synchronized (lock) {
			observers.add(o);
			state = this.state;
			eventExecutor.execute(() -> o.accept(state));
		}
	}

	@Override
	public void removeObserver(Consumer<State> o) {
		synchronized (lock) {
			observers.remove(o);
		}
	}

	SimplexPlugin getPlugin() {
		return (SimplexPlugin) requireNonNull(pluginManager.getPlugin(ID));
	}

	void setTotal(long total) {
		synchronized (lock) {
			state = new State(state.getDone(), total, state.isFinished(),
					state.isSuccess());
			notifyObservers();
		}
	}

	void addDone(long done) {
		synchronized (lock) {
			// Done and total come from different sources; make them consistent
			done = min(state.getDone() + done, state.getTotal());
			state = new State(done, state.getTotal(), state.isFinished(),
					state.isSuccess());
			notifyObservers();
		}
	}

	void setSuccess(boolean success) {
		synchronized (lock) {
			state = new State(state.getDone(), state.getTotal(), true, success);
			notifyObservers();
		}
	}

	@GuardedBy("lock")
	private void notifyObservers() {
		List<Consumer<State>> observers = new ArrayList<>(this.observers);
		State state = this.state;
		eventExecutor.execute(() -> {
			for (Consumer<State> o : observers) o.accept(state);
		});
	}
}
