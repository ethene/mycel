package com.quantumresearch.mycel.spore;

import org.briarproject.nullsafety.NotNullByDefault;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.concurrent.GuardedBy;

import static java.util.logging.Level.FINE;
import static com.quantumresearch.mycel.spore.util.LogUtils.now;

/**
 * An {@link Executor} that delegates its tasks to another {@link Executor}
 * while limiting the number of tasks that are delegated concurrently. Tasks
 * are delegated in the order they are submitted to this executor.
 */
@NotNullByDefault
public class PoliteExecutor implements Executor {

	private final Object lock = new Object();
	@GuardedBy("lock")
	private final Queue<Runnable> queue = new LinkedList<>();
	private final Executor delegate;
	private final int maxConcurrentTasks;
	private final Logger log;

	@GuardedBy("lock")
	private int concurrentTasks = 0;

	/**
	 * @param tag the tag to be used for logging
	 * @param delegate the executor to which tasks will be delegated
	 * @param maxConcurrentTasks the maximum number of tasks that will be
	 * delegated concurrently. If this is set to 1, tasks submitted to this
	 * executor will run in the order they are submitted and will not run
	 * concurrently
	 */
	public PoliteExecutor(String tag, Executor delegate,
			int maxConcurrentTasks) {
		this.delegate = delegate;
		this.maxConcurrentTasks = maxConcurrentTasks;
		log = Logger.getLogger(tag);
	}

	@Override
	public void execute(Runnable r) {
		long submitted = now();
		Runnable wrapped = () -> {
			if (log.isLoggable(FINE)) {
				long queued = now() - submitted;
				log.fine("Queue time " + queued + " ms");
			}
			try {
				r.run();
			} finally {
				scheduleNext();
			}
		};
		synchronized (lock) {
			if (concurrentTasks < maxConcurrentTasks) {
				concurrentTasks++;
				delegate.execute(wrapped);
			} else {
				queue.add(wrapped);
			}
		}
	}

	private void scheduleNext() {
		synchronized (lock) {
			Runnable next = queue.poll();
			if (next == null) concurrentTasks--;
			else delegate.execute(next);
		}
	}
}
