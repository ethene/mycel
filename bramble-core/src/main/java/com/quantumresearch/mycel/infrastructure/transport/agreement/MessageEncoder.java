package com.quantumresearch.mycel.infrastructure.transport.agreement;

import com.quantumresearch.mycel.infrastructure.api.crypto.PublicKey;
import com.quantumresearch.mycel.infrastructure.api.data.BdfDictionary;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
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
