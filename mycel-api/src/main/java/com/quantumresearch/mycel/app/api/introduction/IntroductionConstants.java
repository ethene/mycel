package com.quantumresearch.mycel.app.api.introduction;

import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;

public interface IntroductionConstants {

	/**
	 * The maximum length of the introducer's optional message to the
	 * introducees in UTF-8 bytes.
	 */
	int MAX_INTRODUCTION_TEXT_LENGTH = MAX_MESSAGE_BODY_LENGTH - 1024;

	String LABEL_SESSION_ID = "com.quantumresearch.mycel.app.introduction/SESSION_ID";

	String LABEL_MASTER_KEY = "com.quantumresearch.mycel.app.introduction/MASTER_KEY";

	String LABEL_ALICE_MAC_KEY =
			"com.quantumresearch.mycel.app.introduction/ALICE_MAC_KEY";

	String LABEL_BOB_MAC_KEY =
			"com.quantumresearch.mycel.app.introduction/BOB_MAC_KEY";

	String LABEL_AUTH_MAC = "com.quantumresearch.mycel.app.introduction/AUTH_MAC";

	String LABEL_AUTH_SIGN = "com.quantumresearch.mycel.app.introduction/AUTH_SIGN";

	String LABEL_AUTH_NONCE = "com.quantumresearch.mycel.app.introduction/AUTH_NONCE";

	String LABEL_ACTIVATE_MAC =
			"com.quantumresearch.mycel.app.introduction/ACTIVATE_MAC";

}
