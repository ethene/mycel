package com.quantumresearch.mycel.app.test;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.event.EventListener;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.atomic.AtomicReference;

import static com.quantumresearch.mycel.app.test.BriarIntegrationTest.waitForEvents;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NotNullByDefault
public class TestEventListener<T extends Event> implements EventListener {

	@FunctionalInterface
	public interface EventRunnable {
		void run() throws Exception;
	}

	public static <T extends Event> T assertEvent(
			BriarIntegrationTestComponent c, Class<T> clazz, EventRunnable r)
			throws Exception {
		TestEventListener<T> listener = new TestEventListener<>(clazz);
		c.getEventBus().addListener(listener);
		try {
			r.run();
			waitForEvents(c);
			return listener.assertAndGetEvent();
		} finally {
			c.getEventBus().removeListener(listener);
		}
	}

	private TestEventListener(Class<T> clazz) {
		this.clazz = clazz;
	}

	private final Class<T> clazz;

	private final AtomicReference<T> event = new AtomicReference<>();

	@Override
	public void eventOccurred(Event e) {
		if (e.getClass().equals(clazz)) {
			//noinspection unchecked
			assertNull("Event already received", event.getAndSet((T) e));
		}
	}

	private T assertAndGetEvent() {
		T t = event.get();
		assertNotNull("No event received", t);
		return t;
	}

}
