package com.quantumresearch.mycel.app.api.privategroup;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.app.api.identity.AuthorInfo;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class GroupMember {

	private final Author author;
	private final AuthorInfo authorInfo;
	private final boolean isCreator;
	@Nullable
	private final ContactId contactId;
	private final Visibility visibility;

	public GroupMember(Author author, AuthorInfo authorInfo, boolean isCreator,
			@Nullable ContactId contactId, Visibility visibility) {
		this.author = author;
		this.authorInfo = authorInfo;
		this.isCreator = isCreator;
		this.contactId = contactId;
		this.visibility = visibility;
	}

	public Author getAuthor() {
		return author;
	}

	public AuthorInfo getAuthorInfo() {
		return authorInfo;
	}

	public boolean isCreator() {
		return isCreator;
	}

	/**
	 * Returns the ContactId of a visible contact
	 * or null if the contact is not visible or the member is no contact.
	 */
	@Nullable
	public ContactId getContactId() {
		return contactId;
	}

	public Visibility getVisibility() {
		return visibility;
	}

}
