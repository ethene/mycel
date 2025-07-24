package com.quantumresearch.mycel.app.api.avatar;

import com.quantumresearch.mycel.spore.api.Pair;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;

import java.io.IOException;
import java.io.InputStream;

public interface AvatarMessageEncoder {
	/**
	 * Returns an update message and its metadata.
	 */
	Pair<Message, BdfDictionary> encodeUpdateMessage(GroupId groupId,
			long version, String contentType, InputStream in)
			throws IOException;
}
