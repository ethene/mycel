package com.quantumresearch.mycel.app.api.messaging;

import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;

public interface MessagingConstants {

	/**
	 * The maximum length of a private message's text in UTF-8 bytes.
	 */
	int MAX_PRIVATE_MESSAGE_TEXT_LENGTH = MAX_MESSAGE_BODY_LENGTH - 2048;

	/**
	 * The maximum number of attachments per private message.
	 */
	int MAX_ATTACHMENTS_PER_MESSAGE = 10;

}
