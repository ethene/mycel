package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MESSAGE_HEADER_LENGTH;

@NotNullByDefault
public class TestMessageFactory implements MessageFactory {

	@Override
	public Message createMessage(GroupId g, long timestamp, byte[] body) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Message createMessage(byte[] raw) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getRawMessage(Message m) {
		byte[] body = m.getBody();
		byte[] raw = new byte[MESSAGE_HEADER_LENGTH + body.length];
		System.arraycopy(body, 0, raw, MESSAGE_HEADER_LENGTH, body.length);
		return raw;
	}
}
