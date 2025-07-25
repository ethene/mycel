package com.quantumresearch.mycel.spore.api.plugin.file;

import com.quantumresearch.mycel.spore.api.Consumer;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface RemovableDriveTask extends Runnable {

	/**
	 * Returns the {@link TransportProperties} that were used for creating
	 * this task.
	 */
	TransportProperties getTransportProperties();

	/**
	 * Adds an observer to the task. The observer will be notified on the
	 * event thread of the current state of the task and any subsequent state
	 * changes.
	 */
	void addObserver(Consumer<State> observer);

	/**
	 * Removes an observer from the task.
	 */
	void removeObserver(Consumer<State> observer);

	class State {

		private final long done, total;
		private final boolean finished, success;

		public State(long done, long total, boolean finished, boolean success) {
			this.done = done;
			this.total = total;
			this.finished = finished;
			this.success = success;
		}

		/**
		 * Returns the total length in bytes of the messages read or written
		 * so far, or zero if the total is unknown.
		 */
		public long getDone() {
			return done;
		}

		/**
		 * Returns the total length in bytes of the messages that will have
		 * been read or written when the task is complete, or zero if the
		 * total is unknown.
		 */
		public long getTotal() {
			return total;
		}

		public boolean isFinished() {
			return finished;
		}

		public boolean isSuccess() {
			return success;
		}
	}
}
