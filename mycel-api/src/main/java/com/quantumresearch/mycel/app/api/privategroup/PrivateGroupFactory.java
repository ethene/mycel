package com.quantumresearch.mycel.app.api.privategroup;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.Group;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface PrivateGroupFactory {

	/**
	 * Creates a private group with the given name and author.
	 */
	PrivateGroup createPrivateGroup(String name, Author creator);

	/**
	 * Creates a private group with the given name, author and salt.
	 */
	PrivateGroup createPrivateGroup(String name, Author creator, byte[] salt);

	/**
	 * Parses a group and returns the corresponding PrivateGroup.
	 */
	PrivateGroup parsePrivateGroup(Group group) throws FormatException;

}
