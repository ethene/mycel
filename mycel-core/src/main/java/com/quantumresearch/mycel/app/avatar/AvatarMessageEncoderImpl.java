package com.quantumresearch.mycel.app.avatar;

import com.quantumresearch.mycel.spore.api.Pair;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.app.api.attachment.FileTooBigException;
import com.quantumresearch.mycel.app.api.avatar.AvatarMessageEncoder;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
import static com.quantumresearch.mycel.spore.util.IoUtils.copyAndClose;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;
import static com.quantumresearch.mycel.app.avatar.AvatarConstants.MSG_KEY_VERSION;
import static com.quantumresearch.mycel.app.avatar.AvatarConstants.MSG_TYPE_UPDATE;

@Immutable
@NotNullByDefault
class AvatarMessageEncoderImpl implements AvatarMessageEncoder {

	private final ClientHelper clientHelper;
	private final Clock clock;

	@Inject
	AvatarMessageEncoderImpl(ClientHelper clientHelper, Clock clock) {
		this.clientHelper = clientHelper;
		this.clock = clock;
	}

	@Override
	public Pair<Message, BdfDictionary> encodeUpdateMessage(GroupId groupId,
			long version, String contentType, InputStream in)
			throws IOException {
		// 0.0: Message Type, Version, Content-Type
		BdfList list = BdfList.of(MSG_TYPE_UPDATE, version, contentType);
		byte[] descriptor = clientHelper.toByteArray(list);

		// add BdfList and stream content to body
		ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
		bodyOut.write(descriptor);
		copyAndClose(in, bodyOut);
		if (bodyOut.size() > MAX_MESSAGE_BODY_LENGTH)
			throw new FileTooBigException();

		// assemble message
		byte[] body = bodyOut.toByteArray();
		long timestamp = clock.currentTimeMillis();
		Message m = clientHelper.createMessage(groupId, timestamp, body);

		// encode metadata
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_VERSION, version);
		meta.put(MSG_KEY_CONTENT_TYPE, contentType);
		meta.put(MSG_KEY_DESCRIPTOR_LENGTH, descriptor.length);

		return new Pair<>(m, meta);
	}

}
