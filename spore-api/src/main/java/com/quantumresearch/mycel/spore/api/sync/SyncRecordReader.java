package com.quantumresearch.mycel.spore.api.sync;

import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
public interface SyncRecordReader {

	boolean eof() throws IOException;

	boolean hasAck() throws IOException;

	Ack readAck() throws IOException;

	boolean hasMessage() throws IOException;

	Message readMessage() throws IOException;

	boolean hasOffer() throws IOException;

	Offer readOffer() throws IOException;

	boolean hasRequest() throws IOException;

	Request readRequest() throws IOException;

	boolean hasVersions() throws IOException;

	Versions readVersions() throws IOException;

	boolean hasPriority() throws IOException;

	Priority readPriority() throws IOException;
}
