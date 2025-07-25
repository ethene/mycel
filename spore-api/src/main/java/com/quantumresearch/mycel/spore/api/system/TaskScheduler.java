package com.quantumresearch.mycel.spore.api.system;

import com.quantumresearch.mycel.spore.api.Cancellable;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * A service that can be used to schedule the execution of tasks.
 */
@NotNullByDefault
public interface TaskScheduler {

	/**
	 * Submits the given task to the given executor after the given delay.
	 * <p>
	 * If the platform supports wake locks, a wake lock will be held while
	 * submitting and running the task.
	 *
	 * @return A {@link Cancellable} for cancelling the task.
	 */
	Cancellable schedule(Runnable task, Executor executor, long delay,
			TimeUnit unit);

	/**
	 * Submits the given task to the given executor after the given delay,
	 * and then repeatedly with the given interval between executions
	 * (measured from the end of one execution to the beginning of the next).
	 * <p>
	 * If the platform supports wake locks, a wake lock will be held while
	 * submitting and running the task.
	 *
	 * @return A {@link Cancellable} for cancelling all future executions of
	 * the task.
	 */
	Cancellable scheduleWithFixedDelay(Runnable task, Executor executor,
			long delay, long interval, TimeUnit unit);

}
