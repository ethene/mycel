package com.quantumresearch.mycel.spore.api.sync.validation;

import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.InvalidMessageException;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageContext;

public interface MessageValidator {

	/**
	 * Validates the given message and returns its metadata and
	 * dependencies.
	 */
	MessageContext validateMessage(Message m, Group g)
			throws InvalidMessageException;
}
