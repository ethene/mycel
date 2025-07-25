package com.quantumresearch.mycel.spore.api.plugin.file;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
public interface RemovableDriveManager {

	/**
	 * Returns the currently running reader task, or null if no reader task
	 * is running.
	 */
	@Nullable
	RemovableDriveTask getCurrentReaderTask();

	/**
	 * Returns the currently running writer task,  or null if no writer task
	 * is running.
	 */
	@Nullable
	RemovableDriveTask getCurrentWriterTask();

	/**
	 * Starts and returns a reader task, reading from a stream described by
	 * the given transport properties. If a reader task is already running,
	 * it will be returned and the argument will be ignored.
	 */
	RemovableDriveTask startReaderTask(TransportProperties p);

	/**
	 * Starts and returns a writer task for the given contact, writing to
	 * a stream described by the given transport properties. If a writer task
	 * is already running, it will be returned and the arguments will be
	 * ignored.
	 */
	RemovableDriveTask startWriterTask(ContactId c, TransportProperties p);

	/**
	 * Returns true if the given contact has indicated support for the
	 * removable drive transport.
	 */
	boolean isTransportSupportedByContact(ContactId c) throws DbException;

	/**
	 * Returns true if there is anything to send to the given contact.
	 */
	boolean isWriterTaskNeeded(ContactId c) throws DbException;
}
