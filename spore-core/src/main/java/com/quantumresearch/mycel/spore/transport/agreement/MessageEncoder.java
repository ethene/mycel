package com.quantumresearch.mycel.spore.transport.agreement;

import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface MessageEncoder {

	Message encodeKeyMessage(GroupId contactGroupId,
			TransportId transportId, PublicKey publicKey);

	Message encodeActivateMessage(GroupId contactGroupId,
			TransportId transportId, MessageId previousMessageId);

	BdfDictionary encodeMessageMetadata(TransportId transportId,
			MessageType type, boolean local);
}
